-- ElderMoodAI Backend System - Database Indexes
-- Version: 1.0.0
-- Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6

-- Note: user.phone, user.email, elder_guardian(elder_id, guardian_id), and
-- system_config.config_key already have UNIQUE constraints defined in V1,
-- which MySQL automatically backs with indexes. No duplicate indexes needed.

-- User Table: Additional query-support indexes
CREATE INDEX `idx_user_status` ON `user`(`status`);

-- Elder Table Indexes
CREATE INDEX `idx_elder_created_at` ON `elder`(`created_at`);

-- Elder-Guardian Relationship Table Indexes
-- (elder_id, guardian_id) unique index already created by V1 UNIQUE constraint
CREATE INDEX `idx_elder_guardian_elder_id` ON `elder_guardian`(`elder_id`);
CREATE INDEX `idx_elder_guardian_guardian_id` ON `elder_guardian`(`guardian_id`);
CREATE INDEX `idx_elder_guardian_authorized` ON `elder_guardian`(`authorized`);

-- Emotion Record Table Indexes
-- Composite index for querying by elder within a time range (Requirements: 14.4)
CREATE INDEX `idx_emotion_record_elder_analyzed` ON `emotion_record`(`elder_id`, `analyzed_at`);
CREATE INDEX `idx_emotion_record_emotion_type` ON `emotion_record`(`emotion_type`);
CREATE INDEX `idx_emotion_record_analyzed_at` ON `emotion_record`(`analyzed_at`);

-- Alert Record Table Indexes
-- Composite index for querying by elder, status, and time (Requirements: 14.5)
CREATE INDEX `idx_alert_record_elder_status_created` ON `alert_record`(`elder_id`, `status`, `created_at`);
CREATE INDEX `idx_alert_record_status` ON `alert_record`(`status`);
CREATE INDEX `idx_alert_record_severity` ON `alert_record`(`severity`);
CREATE INDEX `idx_alert_record_created_at` ON `alert_record`(`created_at`);

-- Audit Log Table Indexes
-- Composite index for querying by user within a time range (Requirements: 14.6)
CREATE INDEX `idx_audit_log_user_created` ON `audit_log`(`user_id`, `created_at`);
CREATE INDEX `idx_audit_log_action` ON `audit_log`(`action`);
CREATE INDEX `idx_audit_log_resource` ON `audit_log`(`resource_type`, `resource_id`);
CREATE INDEX `idx_audit_log_created_at` ON `audit_log`(`created_at`);
