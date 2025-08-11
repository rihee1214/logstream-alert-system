package com.rihee.alerting.common.identity;

import com.rihee.alerting.common.util.StringUtils;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * {@code LogMessageKeyGenerator}는 로그 메시지를 식별하기 위한
 * 고유한 {@code message key}를 생성하는 유틸리티 클래스입니다.
 *
 * <p>이 키는 Kafka, Fluent Bit, Logging Service 등 여러 로그 파이프라인 구성 요소 간에
 * 동일한 식별 규칙을 유지하도록 설계되었습니다.
 *
 * <h2>키 포맷 규칙</h2>
 * <pre>{@code
 * v1:<service>/<host>/<container>@<uuid>-v4
 * }</pre>
 * <ul>
 *   <li><b>v1</b> — 로그 키 포맷의 스키마 버전</li>
 *   <li><b>service</b> — 로그를 발생시킨 서비스 식별자</li>
 *   <li><b>host</b> — 로그를 발생시킨 서버 또는 노드의 호스트명</li>
 *   <li><b>container</b> — 로그를 발생시킨 컨테이너(Pod, Docker 등) 식별자</li>
 *   <li><b>uuid</b> — UUID v4 기반 고유 식별자</li>
 *   <li><b>v4</b> — UUID 버전 표시</li>
 * </ul>
 *
 * <h2>특이 사항</h2>
 * <ul>
 *   <li>입력 값은 {@code requireNonBlank}를 통해 필수 여부를 검사합니다.</li>
 *   <li>서비스명, 호스트명, 컨테이너명은 모두 소문자로 변환되며,
 *       알파벳 소문자/숫자/._- 이외의 문자는 하이픈({@code -})으로 치환됩니다.</li>
 *   <li>UUID는 항상 {@link UUID#randomUUID()}를 이용해 v4 버전으로 생성됩니다.</li>
 *   <li>생성된 키는 {@link #KEY_PATTERN}을 통해 간단한 정규식 기반 유효성 검사가 가능합니다.</li>
 * </ul>
 *
 * <h2>예시</h2>
 * <pre>{@code
 * String key = LogMessageKeyGenerator.generate("order-service", "host-1", "container-abc");
 * // 결과: v1:order-service/host-1/container-abc@550e8400-e29b-41d4-a716-446655440000:v4
 * }</pre>
 *
 * @author 리희
 * @since 1.0
 */
public final class LogMessageKeyGenerator {

  /**
   * 로그 키 스키마 버전.
   *
   * <p>포맷 규칙 변경 시 이 값을 변경하고 하위 호환 로직을 별도 제공할 수 있습니다.</p>
   */
  private static final String LOG_VERSION = "v1";
  /**
   * UUID 버전 식별자.
   *
   * <p>현재는 v4만 사용하며, 추후 UUID v7 등으로 변경 시 이 값을 수정합니다.</p>
   */
  private static final String UUID_VERSION = "v4";

  /**
   * 안전한 문자 집합을 제외한 모든 문자를 하이픈({@code -})으로 치환하기 위한 정규식 패턴.
   *
   * <p>허용 문자: {@code a-z0-9._-}</p>
   */
  private static final Pattern SAFE = Pattern.compile("[^a-z0-9._-]");
  /**
   * 로그 키 문자열 포맷.
   *
   * <p>형식: {@code v1:service/host/container@uuid:v4}</p>
   */
  private static final String FORMAT = "%s:%s/%s/%s@%s:%s";

  /**
   * 생성된 로그 키의 유효성을 검증할 수 있는 정규식 패턴. (추후 사용을 위함)
   *
   * <p>포맷, 허용 문자, UUID v4 형식, 버전 접두를 포함합니다.</p>
   */
  private static final Pattern KEY_PATTERN =
      Pattern.compile("^v1:[a-z0-9._-]+/[a-z0-9._-]+/[a-z0-9._-]+@[0-9a-f-]{36}-v4$");

  private LogMessageKeyGenerator() {
  }

  /**
   * 로그 메시지 키를 생성합니다.
   *
   * <p>입력된 서비스명, 호스트명, 컨테이너명은 공백이 아닌 값이어야 하며,
   * 모두 소문자로 변환되고, 허용되지 않는 문자는 하이픈({@code -})으로 치환됩니다.</p>
   *
   * @param service   서비스 식별자 (예: {@code order-service})
   * @param host      호스트명 (예: {@code host-1})
   * @param container 컨테이너 식별자 (예: {@code container-abc})
   * @return 생성된 로그 메시지 키
   * @throws IllegalArgumentException 입력값이 null이거나 공백일 경우
   */
  public static String generate(String service, String host, String container) {
    String s = norm(StringUtils.requireNonBlank(service));
    String h = norm(StringUtils.requireNonBlank(host));
    String c = norm(StringUtils.requireNonBlank(container));
    String uuid = UUID.randomUUID().toString(); // v4

    return String.format(FORMAT,
        LOG_VERSION, s, h, c, uuid, UUID_VERSION);
  }

  /**
   * 입력 문자열을 키 생성 규칙에 맞게 변환합니다.
   *
   * <p>규칙:
   * <ul>
   *   <li>앞뒤 공백 제거</li>
   *   <li>소문자 변환</li>
   *   <li>연속된 공백은 하이픈({@code -})으로 치환</li>
   *   <li>허용 문자 집합 외의 문자는 하이픈({@code -})으로 치환</li>
   * </ul>
   * </p>
   *
   * @param v 원본 문자열
   * @return 변환된 문자열
   */
  private static String norm(String v) {
    String s = v.trim().toLowerCase().replaceAll("\\s+", "-");
    return SAFE.matcher(s).replaceAll("-");
  }

}
