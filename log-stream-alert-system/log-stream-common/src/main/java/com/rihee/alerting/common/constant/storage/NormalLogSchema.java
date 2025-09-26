package com.rihee.alerting.common.constant.storage;

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
  LOG_VERSION_MAJOR("log_version_major"),
  META("meta");

  private final String schemaName;

  NormalLogSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }

}
