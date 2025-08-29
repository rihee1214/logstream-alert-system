package com.rihee.alerting.loggingService.core.model;

import com.rihee.alerting.common.constant.storage.ErrorLogSchema;
import com.rihee.alerting.common.util.MapUtils;
import com.rihee.alerting.common.util.StringUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code LogErrorMessage}는 유효성 검증 실패 또는 처리 중 오류가 발생한
 * 로그 메시지를 표준화된 에러 로그 형식으로 변환하여 보관하는 구현체이다.
 *
 * <p>에러 로그에는 다음과 같은 필드가 포함된다:
 * <ul>
 *   <li>{@link ErrorLogSchema#MESSAGE_ID} - 원본 로그의 고유 메시지 키</li>
 *   <li>{@link ErrorLogSchema#ORIGIN_LOG} - 원본 로그 데이터(JSON 직렬화 문자열)</li>
 *   <li>{@link ErrorLogSchema#REASON} - 에러 발생 사유</li>
 *   <li>{@link ErrorLogSchema#OCCURRED_AT} - 에러 발생 시간(Instant.now())</li>
 *   <li>{@link ErrorLogSchema#STAGE} - 에러 로그 발생 위치(현재 서비스에서)</li>
 *   <li>{@link ErrorLogSchema#LOG_VERSION_MAJOR} - 로그 스키마의 주요 버전</li>
 * </ul>
 *
 * <p>이 클래스는 불변성을 보장하기 위해 내부적으로 {@link HashMap}의 복사본을 사용하며,
 * 생성은 정적 팩토리 메서드 {@link #fromOriginMessage(String, String, String, String)} 또는
 * {@link #fromNormalMessage(LogMessage, String, String)}를 통해 수행된다.
 */
public class LogErrorMessage implements LogMessage {

  // 에러 로그 스키마의 주요 버전
  private static final int LOG_MAJOR_VERSION = 1;
  // 에러 로그 필드 데이터
  private final Map<String, Object> errorLogs;

  /**
   * 내부 생성자. 주어진 필드 맵을 복사하여 보관한다.
   *
   * @param errorLogs 에러 로그 필드 데이터
   */
  private LogErrorMessage(Map<String, Object> errorLogs) {
    this.errorLogs = new HashMap<>(errorLogs);
  }

  /**
   * 원본 로그 문자열과 메시지 키, 에러 사유를 기반으로 {@code LogErrorMessage}를 생성한다.
   *
   * @param originLog  원본 로그(JSON 직렬화 문자열)
   * @param messageKey 로그의 고유 식별 키
   * @param reason     에러 발생 사유
   * @param stage      에러 발생 위치
   * @return 생성된 {@code LogErrorMessage} 인스턴스
   * @throws IllegalArgumentException {@code messageKey} 또는 {@code reason}이 비어있을 경우
   */
  public static LogErrorMessage fromOriginMessage(String originLog,
                                                          String messageKey,
                                                          String reason,
                                                          String stage) {
    Map<String, Object> errorLogs = buildErrorLogs(originLog, messageKey, reason, stage);
    return new LogErrorMessage(errorLogs);
  }

  /**
   * 정상 로그 메시지 객체와 에러 사유를 기반으로 {@code LogErrorMessage}를 생성한다.
   *
   * <p>{@code message}는 {@link LogMessage#toPersistenceMap()}을 통해 JSON 문자열로 직렬화되며,
   * 해당 데이터와 메시지 키를 에러 로그로 변환한다.
   *
   * @param message 원본 {@link LogMessage} 객체
   * @param reason  에러 발생 사유
   * @param stage      에러 발생 위치
   * @return 생성된 {@code LogErrorMessage} 인스턴스
   * @throws IllegalArgumentException 메시지 키 또는 {@code reason}이 비어있을 경우
   */
  public static LogErrorMessage fromNormalMessage(LogMessage message, String reason,
                                                          String stage) {
    String messageKey = message.getMessageKey();
    String originLog = MapUtils.toJsonString(message.toPersistenceMap());
    Map<String, Object> errorLogs = buildErrorLogs(originLog, messageKey, reason, stage);
    return new LogErrorMessage(errorLogs);
  }

  /**
   * 에러 로그 데이터 맵을 생성한다.
   *
   * @param originLog  원본 로그(JSON 직렬화 문자열)
   * @param messageKey 로그의 고유 식별 키
   * @param reason     에러 발생 사유
   * @param stage      에러 발생 위치
   * @return 에러 로그 필드 데이터
   * @throws IllegalArgumentException {@code messageKey} 또는 {@code reason}이 비어있을 경우
   */
  private static Map<String, Object> buildErrorLogs(String originLog,
                                                            String messageKey,
                                                            String reason,
                                                            String stage) {
    if (StringUtils.isBlank(messageKey)) {
      throw new IllegalArgumentException("messageKey가 제대로 존재하지 않습니다.");
    }

    if (StringUtils.isBlank(reason)) {
      throw new IllegalArgumentException("에러의 이유가 기재되어있지 않습니다.");
    }

    Map<String, Object> errorLogs = new HashMap<>();
    errorLogs.put(ErrorLogSchema.MESSAGE_ID.getSchemaName(), messageKey);
    errorLogs.put(ErrorLogSchema.ORIGIN_LOG.getSchemaName(), originLog);
    errorLogs.put(ErrorLogSchema.REASON.getSchemaName(), reason);
    errorLogs.put(ErrorLogSchema.OCCURRED_AT.getSchemaName(), Instant.now());
    errorLogs.put(ErrorLogSchema.STAGE.getSchemaName(), stage);
    errorLogs.put(ErrorLogSchema.LOG_VERSION_MAJOR.getSchemaName(), LOG_MAJOR_VERSION);
    return errorLogs;
  }

  /**
   * 해당 메시지가 에러 메시지임을 나타낸다.
   *
   * @return 항상 {@code true}
   */
  @Override
  public boolean isError() {
    return true;
  }

  /**
   * 지정된 키에 해당하는 값을 반환한다.
   *
   * @param key 조회할 필드 키
   * @return 필드 값, 없으면 {@code null}
   */
  @Override
  public Object get(String key) {
    return this.errorLogs.get(key);
  }

  /**
   * 지정된 키와 값을 에러 로그에 추가 또는 수정한다.
   *
   * @param key   필드 키
   * @param value 필드 값
   */
  @Override
  public void put(String key, Object value) {
    this.errorLogs.put(key, value);
  }

  /**
   * 영속화 가능한 형태의 로그 데이터를 반환한다.
   *
   * @return 에러 로그 필드 데이터의 복사본
   */
  @Override
  public Map<String, Object> toPersistenceMap() {
    return new HashMap<>(this.errorLogs);
  }

  /**
   * 로그 메시지의 고유 키를 반환한다.
   *
   * @return {@link ErrorLogSchema#MESSAGE_ID} 필드 값
   */
  @Override
  public String getMessageKey() {
    return this.errorLogs.get(ErrorLogSchema.MESSAGE_ID.getSchemaName()).toString();
  }
}
