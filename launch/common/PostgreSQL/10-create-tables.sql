-- create tables (idempotent)
CREATE TABLE IF NOT EXISTS logs (
    message_id        TEXT PRIMARY KEY,
    logtype           TEXT NOT NULL,
    logged_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    level             TEXT NOT NULL,
    service           TEXT NOT NULL,
    class_name        TEXT,
    message           TEXT,
    host              TEXT,
    container         TEXT,
    stacktrace        TEXT,
    trace_id          TEXT,
    span_id           TEXT,
    parent_span_id    TEXT,
    log_version_major INT  NOT NULL,
    meta              JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS error_logs (
    message_id        TEXT PRIMARY KEY,
    origin_log        JSONB NOT NULL,
    reason            TEXT  NOT NULL,
    occurred_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    stage             TEXT  NOT NULL,
    log_version_major INT   NOT NULL
);

-- 인덱스 (IF NOT EXISTS는 일부 PG 버전에서 인덱스명 중복 방지 용도로 사용)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relname = 'idx_logs_trace_id' AND n.nspname = 'public'
  ) THEN
CREATE INDEX idx_logs_trace_id ON logs (trace_id);
END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relname = 'idx_logs_logged_at' AND n.nspname = 'public'
  ) THEN
CREATE INDEX idx_logs_logged_at ON logs (logged_at DESC);
END IF;
END$$;
