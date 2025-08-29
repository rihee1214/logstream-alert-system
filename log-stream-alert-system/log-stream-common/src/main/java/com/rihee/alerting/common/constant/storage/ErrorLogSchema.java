package com.rihee.alerting.common.constant.storage;

public enum ErrorLogSchema {

  MESSAGE_ID("message_id"),
  ORIGIN_LOG("origin_log"),
  REASON("reason"),
  OCCURRED_AT("occurred_at"),
  STAGE("stage"),
  LOG_VERSION_MAJOR("log_version_major");

  private final String schemaName;

  ErrorLogSchema(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }

}
