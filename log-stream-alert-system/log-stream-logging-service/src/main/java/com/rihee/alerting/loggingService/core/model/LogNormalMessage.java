package com.rihee.alerting.loggingService.core.model;

import static com.rihee.alerting.common.constant.storage.NormalLogSchema.META;

import com.rihee.alerting.common.constant.logging.StructuredLogFields;
import com.rihee.alerting.common.constant.storage.NormalLogSchema;
import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 정상(에러가 아닌) 로그 메시지를 표현하는 구현체입니다.
 *
 * <p>수신된 로그 맵을 {@link StructuredLogFields} 기준으로
 * <em>구조화 영역(structured)</em>과 <em>비구조화 영역(unstructured / meta)</em>으로
 * 분리해 보관합니다. 구조화 영역은 스키마로 정의된 고정 필드들이며,
 * 비구조화 영역은 스키마에 정의되지 않은 여분의 필드(META)입니다.
 *
 * <h2>핵심 특징</h2>
 * <ul>
 *   <li>메시지 키({@link #getMessageKey()})를 통해 로그를 식별</li>
 *   <li>구조화/비구조화 분리 보관 및 JSON 직렬화 지원({@link #toJsonString()})</li>
 *   <li>타임스탬프 정규화: 문자열 입력 시 {@link Timestamp}로 변환하여 저장</li>
 * </ul>
 *
 * <h2>스레드 안전성</h2>
 *
 * <p>내부에 변경 가능한 맵을 보유하므로 <b>스레드 안전하지 않습니다</b>.
 * 단일 스레드 컨텍스트에서 사용하거나 외부 동기화를 보장해야 합니다.</p>
 *
 * @see LogMessage
 * @see StructuredLogFields
 * @see NormalLogSchema
 */
// TODO timestamp String으로 들어왔지만, DB와의 호환을 위해 java.sql.timestamp로 변형해야 할 필요성 있음.
public final class LogNormalMessage extends LogMessage {

  /**
   * 로그 버전의 메이저 값입니다.
   *
   * <p>스키마 호환성 판단에 사용되며, 생성 시
   * {@link NormalLogSchema#LOG_VERSION_MAJOR} 키로 함께 저장됩니다.
   */
  private static final int LOG_VERSION_MAJOR = 1;

  /**
   * 이 로그 메시지의 고유 식별자입니다.
   *
   * <p>외부에서 입력된 값이 빈 값이면
   * {@link #fromOriginMessage(Map, String)} 생성 과정에서 내부적으로
   * {@link LogMessage#generateKey()}로 대체 생성됩니다.
   */
  private final String messageKey;

  /**
   * 구조화/비구조화 로그 데이터를 보관하는 컨테이너입니다.
   *
   * <p>키 라우팅은 {@link StructuredRouter}가 담당합니다.
   * <ul>
   *   <li>{@code NORMAL}: {@link StructuredLogFields}에 속하는 필드</li>
   *   <li>{@code NONE}: 그 외의 모든 메타(비구조화) 필드</li>
   * </ul>
   *
   * <p>초기 맵 인스턴스는 불변 {@link Map#of(Object, Object, Object, Object)}로
   * 생성되지만, 값으로 보관되는 내부 {@link HashMap}은 변경 가능합니다.
   */
  private final Map<StructuredRouter, Map<String, Object>> logMap
                                  = Map.of(
                                      StructuredRouter.NORMAL, new HashMap<>(),
                                      StructuredRouter.NONE, new HashMap<>()
                                    );

  /**
   * 수신된 키를 구조화/비구조화 영역으로 라우팅하기 위한 내부 열거형입니다.
   *
   * <p>{@link StructuredLogFields} 전체 집합을 기준으로 구조화 여부를 판단합니다.
   */
  private enum StructuredRouter {
    /** 구조화(스키마에 정의된) 필드 영역. */
    NORMAL,
    /** 비구조화(메타) 필드 영역. */
    NONE;

    /**
     * 구조화로 간주되는 필드명 집합입니다.
     *
     * <p>{@link StructuredLogFields#getFieldName()}의 전체 값을 미리 계산해
     * 조회 비용을 상수 시간 수준으로 유지합니다.
     */
    private static final Set<String> STRUCTURED_KEYS = EnumSet.allOf(StructuredLogFields.class)
                                                        .stream()
                                                        .map(StructuredLogFields::getFieldName)
                                                        .collect(Collectors.toUnmodifiableSet());

    /**
     * 주어진 키를 구조화/비구조화 영역으로 라우팅합니다.
     *
     * @param key 분류할 필드 키(널 아님 가정)
     * @return 키가 {@link #STRUCTURED_KEYS}에 포함되면 {@link #NORMAL}, 아니면 {@link #NONE}
     */
    public static StructuredRouter routeKey(String key) {
      if (STRUCTURED_KEYS.contains(key)) {
        return NORMAL;
      }  else {
        return NONE;
      }
    }
  }

  /**
   * 수신된 전체 로그 맵을 구조화/비구조화로 분리하여 초기화하는 생성자입니다.
   *
   * <p>동작 순서:
   * <ol>
   *   <li>{@code allLogs}의 엔트리를 순회하며 {@link #put(String, Object)}로 라우팅 저장</li>
   *   <li>{@link NormalLogSchema#LOG_VERSION_MAJOR}를 {@link #LOG_VERSION_MAJOR}로 세팅</li>
   *   <li>
   *     {@link NormalLogSchema#TIMESTAMP} 값 정규화:
   *     <ul>
   *       <li>문자열/텍스트가 존재하면 {@link Timestamp#valueOf(String)}로 변환</li>
   *       <li>없거나 공백이면 현재 시각({@link LocalDateTime#now()})을 사용</li>
   *     </ul>
   *   </li>
   * </ol>
   *
   * @param allLogs 원본 전체 로그 키/값 맵(널 불가)
   * @param messageKey 외부에서 부여한 메시지 키(널 불가, 빈 값일 수 있음)
   * @implNote {@link Timestamp#valueOf(String)}는 {@code yyyy-MM-dd HH:mm:ss[.fffffffff]} 형식만 허용합니다.
   *           이외 형식이 들어오면 런타임 예외가 발생할 수 있으니 상위 계층에서 포맷 보장을 권장합니다.
   */
  private LogNormalMessage(Map<String, Object> allLogs, String messageKey) {
    this.messageKey = messageKey;
    for (Map.Entry<String, Object> entry : allLogs.entrySet()) {
      this.put(entry.getKey(), entry.getValue());
    }
    this.put(NormalLogSchema.LOG_VERSION_MAJOR.getSchemaName(), LOG_VERSION_MAJOR);
    Object rawTimestamp = this.get(NormalLogSchema.TIMESTAMP.getSchemaName());
    if (StringUtils.isNotBlankText(rawTimestamp)) {
      this.put(NormalLogSchema.TIMESTAMP.getSchemaName(),
                Timestamp.valueOf(rawTimestamp.toString()));
    } else {
      this.put(NormalLogSchema.TIMESTAMP.getSchemaName(),
          Timestamp.valueOf(LocalDateTime.now()));
    }
  }

  /**
   * 원본 로그 맵으로부터 {@code LogNormalMessage}를 생성합니다.
   *
   * <p>메시지 키가 비어 있으면 내부에서 {@link LogMessage#generateKey()}로 대체 생성됩니다.
   *
   * @param allLogs 원본 전체 로그 맵
   * @param messageKey 외부에서 제공한 메시지 키(비어 있으면 새로 생성)
   * @return 초기화된 {@link LogNormalMessage} 인스턴스
   */
  public static LogNormalMessage fromOriginMessage(Map<String, Object> allLogs, String messageKey) {
    String tobeMessageKey = messageKey;
    if (StringUtils.isBlank(tobeMessageKey)) {
      tobeMessageKey = generateKey();
    }
    return new LogNormalMessage(allLogs, tobeMessageKey);
  }

  /**
  * 이 메시지가 에러 로그인지 여부를 반환합니다.
  *
  * <p>정상 로그 구현체이므로 항상 {@code false}를 반환합니다.
  *
  * @return 항상 {@code false}
  */
  @Override
  public boolean isError() {
    return false;
  }

  /**
   * 주어진 키에 해당하는 값을 조회합니다.
   *
   * <p>특수 키 처리:
   * <ul>
   *   <li>{@link NormalLogSchema#META} 키로 조회 시, 비구조화 영역 전체를 <b>JSON 문자열</b>로 반환</li>
   *   <li>그 외 키는 구조화/비구조화 영역에서 라우팅된 실제 값을 반환</li>
   * </ul>
   *
   * @param key 조회할 필드 키
   * @return 해당 키에 매핑된 값, 존재하지 않으면 {@code null}.
   *         {@code META}로 조회 시 JSON 문자열 반환.
   */
  @Override
  public Object get(String key) {
    if (META.getSchemaName().equals(key)) {
      return MapUtils.toJsonString(logMap.get(StructuredRouter.NONE));
    } else {
      return logMap.get(StructuredRouter.routeKey(key)).get(key);
    }
  }

  /**
   * 주어진 키/값을 적절한 영역(구조화/비구조화)에 저장합니다.
   *
   * <p>키가 {@link StructuredLogFields}에 정의되어 있으면 구조화 영역,
   * 아니면 비구조화(META) 영역에 저장됩니다.
   *
   * @param key 저장할 필드 키
   * @param value 저장할 값(널 허용)
   */
  @Override
  public void put(String key, Object value) {
    logMap.get(StructuredRouter.routeKey(key)).put(key, value);
  }

  /**
   * 현재 보관 중인 로그를 하나의 JSON 문자열로 직렬화합니다.
   *
   * <p>출력 구조:
   * <pre>
   * {
   *   // 구조화 영역 필드들...
   *   "meta": { ... 비구조화 필드들 ... }
   * }
   * </pre>
   *
   * @return 직렬화된 JSON 문자열
   */
  @Override
  public String toJsonString() {
    Map<String, Object> result = new HashMap<>(logMap.get(StructuredRouter.NORMAL));
    result.put(META.getSchemaName(), logMap.get(StructuredRouter.NONE));
    return MapUtils.toJsonString(result);
  }

  /**
   * 이 로그 메시지의 고유 메시지 키를 반환합니다.
   *
   * @return 메시지 키(비어 있지 않음)
   */
  @Override
  public String getMessageKey() {
    return this.messageKey;
  }

  /**
   * 이 객체의 문자열 표현을 반환합니다.
   *
   * <p>{@link #toJsonString()}의 결과를 그대로 반환합니다.
   *
   * @return JSON 문자열
   */
  @Override
  public String toString() {
    return this.toJsonString();
  }
}
