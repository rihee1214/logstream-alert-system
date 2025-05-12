package com.rihee.alerting.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SimpleJsonUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  private SimpleJsonUtils() {}

  public static String writeValueAsString(Object data) throws JsonProcessingException {
    return mapper.writeValueAsString(data);
  }
}
