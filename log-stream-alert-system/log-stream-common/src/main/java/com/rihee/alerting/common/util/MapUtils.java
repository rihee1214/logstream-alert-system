package com.rihee.alerting.common.util;

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
}
