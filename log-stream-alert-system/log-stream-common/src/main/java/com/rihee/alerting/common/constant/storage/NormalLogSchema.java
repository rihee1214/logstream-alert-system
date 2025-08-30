package com.rihee.alerting.common.constant.storage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum NormalLogSchema {
  LOG_TYPE("logtype"),
  TIMESTAMP("timestamp"),
  LOG_LEVEL("level"),
  SERVICE("service"),
  CLASS_NAME("class"),
  MESSAGE("message"),
  HOST("host"),
  CONTAINER("container"),
  STACKTRACE("stacktrace"),
  TRACE_ID("traceId"),
  SPAN_ID("spanId"),
  PARENT_SPAN_ID("parentSpanId"),
  SAMPLED("sampled"),
  FLAGS("flags"),
  LOG_VERSION_MAJOR("log_version_major"),
  CALL_TYPE("call_type"),
  CALL_METHOD("call_method"),
  CALL_URI("call_uri"),
  CALL_STATUS_CODE("call_statusCode"),
  CALL_STATUS_MESSAGE("call_statusMessage"),
  CALL_ELAPSED_MS("call_elapsedMs"),
  CALL_REMOTE_TRACE_ID("call_remoteTraceId");

  private final String schemaName;

  NormalLogSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  private static final Set<NormalLogSchema> CALL_SCHEMAS = Collections.unmodifiableSet(
      EnumSet.of(
        CALL_TYPE,
        CALL_METHOD,
        CALL_URI,
        CALL_STATUS_CODE,
        CALL_STATUS_MESSAGE,
        CALL_ELAPSED_MS,
        CALL_REMOTE_TRACE_ID
      )
  );

  public static Set<NormalLogSchema> getCallSchemas() {
    return CALL_SCHEMAS;
  }
}
