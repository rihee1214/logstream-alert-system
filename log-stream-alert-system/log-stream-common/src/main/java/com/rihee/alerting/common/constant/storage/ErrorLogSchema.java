package com.rihee.alerting.common.constant.storage;

public enum ErrorLogSchema {

  MESSAGE_ID("message_id"),
  ORIGIN_LOG("origin_log"),
  REASON("reason"),
  OCCURRED_AT("occurred_at"),
  LOG_VERSION("log_version");

  private final String schemaName;

  ErrorLogSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }

}
