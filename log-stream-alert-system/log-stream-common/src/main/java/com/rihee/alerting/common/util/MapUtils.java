package com.rihee.alerting.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

  public static String toJsonString(Map<String, Object> target) {
    try {
      return OBJECT_MAPPER.writeValueAsString(target);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid input: " + target, e);
    }
  }
}
