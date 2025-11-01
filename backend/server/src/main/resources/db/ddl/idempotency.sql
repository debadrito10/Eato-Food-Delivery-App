BEGIN;
CREATE TABLE IF NOT EXISTS idempotency_keys (
  id TEXT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  order_id BIGINT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_idk_user_created ON idempotency_keys(user_id, created_at DESC);
COMMIT;
