package com.rihee.alerting.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@code SimpleJsonUtils}는 Jackson 기반의 JSON 직렬화 유틸리티 클래스입니다.
 *
 * <h2>스레드 안전성</h2>
 *
 * <p>{@code SimpleJsonUtils}는 내부적으로 static {@link ObjectMapper} 인스턴스를 사용하며,
 * {@code ObjectMapper}는 thread-safe하게 설계되어 있으므로 멀티스레드 환경에서도 안전하게 사용할 수 있습니다.</p>
 *
 * @author 리희
 * @since 1.0
 */
public final class SimpleJsonUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  private SimpleJsonUtils() {}

  /**
   * 주어진 객체를 JSON 문자열로 직렬화합니다.
   *
   * <p>이 메서드는 내부적으로 {@link ObjectMapper}를 사용하여 객체를 JSON 형식의 문자열로 변환합니다.
   * 변환 중 문제가 발생할 경우 {@link JsonProcessingException}이 발생합니다.
   *
   * @param data JSON으로 변환할 객체
   * @return 변환된 JSON 문자열
   * @throws JsonProcessingException 직렬화 중 오류가 발생한 경우
   */
  public static String writeValueAsString(Object data) throws JsonProcessingException {
    return mapper.writeValueAsString(data);
  }
}
