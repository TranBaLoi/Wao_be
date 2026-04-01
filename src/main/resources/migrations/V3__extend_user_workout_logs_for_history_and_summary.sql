ALTER TABLE user_workout_logs
    ADD COLUMN step_source ENUM ('ESTIMATED', 'GPS', 'HEALTH_CONNECT', 'MANUAL', 'SENSOR') NULL,
    ADD COLUMN created_at DATETIME NULL,
    ADD INDEX idx_user_workout_logs_user_date (user_id, log_date);

UPDATE user_workout_logs
SET step_source = 'MANUAL'
WHERE step_source IS NULL
  AND step_count IS NOT NULL;

UPDATE user_workout_logs
SET created_at = COALESCE(started_at, ended_at, TIMESTAMP(log_date, '00:00:00'), NOW())
WHERE created_at IS NULL;
