package com.rihee.alerting.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * {@code MapUtils}는 {@link Properties} 객체를 {@link Map} 형태로 변환하는 유틸리티 기능을 제공합니다.
 *
 * <p>이 클래스는 인스턴스 생성을 허용하지 않으며, 정적 유틸리티 메서드만을 제공합니다.
 *
 * @author 리희
 * @since 1.0
 */
public final class MapUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private MapUtils() {}

  /**
   * {@link Properties} 객체를 {@code Map<String, String>} 형태로 변환합니다.
   *
   * <p>각 key와 value는 {@code String.valueOf(Object)}를 사용하여 문자열로 변환되며,
   * null 값이 존재하는 경우 "null" 문자열로 처리됩니다.
   *
   * @param properties 변환할 {@link Properties} 객체 (null이면 {@link NullPointerException} 발생)
   * @return 변환된 {@code Map<String, String>} 객체
   * @throws NullPointerException 입력된 {@code properties}가 null인 경우
   */
  public static Map<String, String> toMap(Properties properties) {
    return properties.entrySet().stream()
        .collect(Collectors.toMap(
            e -> String.valueOf(e.getKey()),
            e -> String.valueOf(e.getValue())
        ));
  }

  /**
   * JSON 형식의 문자열을 {@link Map} 형태로 변환합니다.
   *
   * <p>주어진 JSON 문자열을 {@code Map<String, Object>} 형태로 역직렬화하여,
   * 구조화된 데이터로 접근할 수 있도록 합니다. 내부적으로 공유된 {@code ObjectMapper}
   * 인스턴스를 사용하며, 입력된 문자열이 올바른 JSON 형식이 아닐 경우
   * {@link IllegalArgumentException}을 발생시킵니다.
   * </p>
   *
   * @param json 변환할 JSON 문자열
   * @return 변환된 {@code Map<String, Object>} 객체
   * @throws IllegalArgumentException JSON 파싱에 실패한 경우 발생
   */
  public static Map<String, Object> fromJson(String json) {
    try {
      return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JSON input: " + json, e);
    }
  }

  /**
   * 주어진 {@link Map} 객체를 JSON 문자열로 직렬화한다.
   *
   * <p>이 메서드는 Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} 를 사용하여
   * 맵의 내용을 JSON 포맷 문자열로 변환한다. 맵의 키는 반드시 {@link String} 이어야 하며,
   * 값은 직렬화 가능한 임의의 타입일 수 있다.</p>
   *
   * <p><strong>주의:</strong> 값이 복잡한 객체일 경우 해당 객체가
   * Jackson 직렬화 규칙에 맞게 처리되어야 하며,
   * 그렇지 않을 경우 예외가 발생할 수 있다.</p>
   *
   * @param target 직렬화할 {@code Map<String, ?>} 객체 (null 불가)
   * @return 주어진 맵을 표현하는 JSON 문자열
   * @throws IllegalArgumentException 맵을 직렬화할 수 없거나 오류가 발생한 경우
   */
  public static String toJsonString(Map<String, ?> target) {
    try {
      return OBJECT_MAPPER.writeValueAsString(target);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid input: " + target, e);
    }
  }

  /**
   * 주어진 {@link InputStream} 을 JSON으로 파싱하여 {@code Map<String, Object>} 형태로 반환한다.
   *
   * <p>이 메서드는 Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} 를 사용하여
   * 입력 스트림을 역직렬화한다. JSON 객체의 각 속성 이름은 {@code String} 키로 매핑되며,
   * 값은 JSON 값의 타입에 따라 {@link Object} 로 표현된다.</p>
   *
   * <p><strong>주의:</strong> 이 메서드는 단순 유틸리티로,
   * JSON 객체 구조가 중첩되거나 배열을 포함하는 경우에도 {@link Object} 로 파싱되어 반환된다.
   * 호출자는 값의 구체 타입을 적절히 캐스팅해야 한다.</p>
   *
   * @param is JSON 데이터를 포함하는 입력 스트림 (null 불가)
   * @return 파싱된 JSON 데이터를 담은 {@code Map<String, Object>} 인스턴스
   * @throws IllegalArgumentException 입력 스트림이 유효한 JSON이 아니거나
   *                                  역직렬화 과정에서 오류가 발생한 경우
   */
  public static Map<String, Object> fromInputStream(InputStream is) {
    try {
      return OBJECT_MAPPER.readValue(is, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid InputStream ", e);
    }
  }
}
