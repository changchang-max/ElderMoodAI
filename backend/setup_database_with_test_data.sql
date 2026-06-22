-- ElderMoodAI 数据库完整安装脚本（包含表结构和测试数据）
-- 使用方法: mysql -u root -p < setup_database_with_test_data.sql

-- 删除并重新创建数据库
DROP DATABASE IF EXISTS eldermoodai;
CREATE DATABASE eldermoodai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eldermoodai;

-- ==================== 创建表结构 ====================

-- User Table
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `phone` VARCHAR(20) UNIQUE,
    `email` VARCHAR(100) UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL COMMENT 'GUARDIAN, CAREGIVER, ADMIN',
    `status` VARCHAR(20) NOT NULL COMMENT 'ACTIVE, INACTIVE, PENDING_APPROVAL',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `chk_user_role` CHECK (`role` IN ('GUARDIAN', 'CAREGIVER', 'ADMIN')),
    CONSTRAINT `chk_user_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL')),
    CONSTRAINT `chk_user_contact` CHECK (`phone` IS NOT NULL OR `email` IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Elder Table
CREATE TABLE `elder` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `gender` VARCHAR(10) NOT NULL COMMENT 'MALE, FEMALE, OTHER',
    `birth_date` DATE NOT NULL,
    `health_status` VARCHAR(500),
    `privacy_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `chk_elder_gender` CHECK (`gender` IN ('MALE', 'FEMALE', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人表';

-- Elder-Guardian Relationship Table
CREATE TABLE `elder_guardian` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `elder_id` BIGINT NOT NULL,
    `guardian_id` BIGINT NOT NULL,
    `relationship` VARCHAR(50) NOT NULL,
    `authorized` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_elder_guardian_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_elder_guardian_user` FOREIGN KEY (`guardian_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `uk_elder_guardian` UNIQUE (`elder_id`, `guardian_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人-监护人关系表';

-- Emotion Record Table
CREATE TABLE `emotion_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `elder_id` BIGINT NOT NULL,
    `emotion_type` VARCHAR(20) NOT NULL COMMENT 'HAPPY, CALM, SAD, ANXIOUS, ANGRY',
    `confidence_score` DOUBLE NOT NULL,
    `data_source` VARCHAR(20) NOT NULL COMMENT 'VOICE, IMAGE, VIDEO, TEXT, SENSOR',
    `raw_data_url` VARCHAR(500),
    `analyzed_at` DATETIME NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_emotion_record_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder`(`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_emotion_type` CHECK (`emotion_type` IN ('HAPPY', 'CALM', 'SAD', 'ANXIOUS', 'ANGRY')),
    CONSTRAINT `chk_data_source` CHECK (`data_source` IN ('VOICE', 'IMAGE', 'VIDEO', 'TEXT', 'SENSOR')),
    CONSTRAINT `chk_confidence_score` CHECK (`confidence_score` >= 0.0 AND `confidence_score` <= 1.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情感记录表';

-- Alert Record Table
CREATE TABLE `alert_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `elder_id` BIGINT NOT NULL,
    `alert_type` VARCHAR(50) NOT NULL COMMENT 'NEGATIVE_EMOTION, ABNORMAL_BEHAVIOR',
    `severity` VARCHAR(20) NOT NULL COMMENT 'LOW, MEDIUM, HIGH, CRITICAL',
    `message` VARCHAR(500) NOT NULL,
    `status` VARCHAR(20) NOT NULL COMMENT 'PENDING, HANDLED, IGNORED',
    `handled_by` BIGINT,
    `handled_at` DATETIME,
    `handle_note` VARCHAR(1000),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_alert_record_elder` FOREIGN KEY (`elder_id`) REFERENCES `elder`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_alert_record_handler` FOREIGN KEY (`handled_by`) REFERENCES `user`(`id`) ON DELETE SET NULL,
    CONSTRAINT `chk_alert_type` CHECK (`alert_type` IN ('NEGATIVE_EMOTION', 'ABNORMAL_BEHAVIOR')),
    CONSTRAINT `chk_severity` CHECK (`severity` IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT `chk_alert_status` CHECK (`status` IN ('PENDING', 'HANDLED', 'IGNORED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警记录表';

-- Audit Log Table
CREATE TABLE `audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `action` VARCHAR(100) NOT NULL,
    `resource_type` VARCHAR(50) NOT NULL,
    `resource_id` BIGINT,
    `ip_address` VARCHAR(50),
    `details` VARCHAR(1000),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_audit_log_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- System Config Table
CREATE TABLE `system_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE,
    `config_value` VARCHAR(500) NOT NULL,
    `description` VARCHAR(200),
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ==================== 创建索引 ====================

-- User表索引
CREATE UNIQUE INDEX `idx_user_phone` ON `user`(`phone`);
CREATE UNIQUE INDEX `idx_user_email` ON `user`(`email`);

-- Elder-Guardian表索引
CREATE INDEX `idx_elder_guardian_elder` ON `elder_guardian`(`elder_id`);
CREATE INDEX `idx_elder_guardian_guardian` ON `elder_guardian`(`guardian_id`);

-- Emotion Record表索引
CREATE INDEX `idx_emotion_elder_analyzed` ON `emotion_record`(`elder_id`, `analyzed_at`);

-- Alert Record表索引
CREATE INDEX `idx_alert_elder_status_created` ON `alert_record`(`elder_id`, `status`, `created_at`);

-- Audit Log表索引
CREATE INDEX `idx_audit_user_created` ON `audit_log`(`user_id`, `created_at`);

-- ==================== 插入系统配置 ====================

INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('alert.threshold.negative_emotion', '0.7', 'Negative emotion alert threshold'),
('alert.threshold.critical_emotion', '0.95', 'Critical emotion alert threshold'),
('notification.email.enabled', 'false', 'Email notification enabled'),
('notification.sms.enabled', 'false', 'SMS notification enabled'),
('ai.service.provider', 'baidu', 'AI service provider'),
('data.retention.days', '365', 'Data retention period in days');

-- ==================== 插入测试数据 ====================

-- 用户数据（5条）
INSERT INTO `user` (`id`, `username`, `phone`, `email`, `password_hash`, `role`, `status`) VALUES
(1, 'admin', '13800138000', 'admin@eldermood.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'ADMIN', 'ACTIVE'),
(2, 'guardian_zhang', '13812345678', 'zhang@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'GUARDIAN', 'ACTIVE'),
(3, 'guardian_li', '13823456789', 'li@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'GUARDIAN', 'ACTIVE'),
(4, 'caregiver_wang', '13834567890', 'wang@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'CAREGIVER', 'ACTIVE'),
(5, 'guardian_liu', '13845678901', 'liu@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'GUARDIAN', 'PENDING_APPROVAL');

-- 老人数据（3条）
INSERT INTO `elder` (`id`, `name`, `gender`, `birth_date`, `health_status`, `privacy_enabled`) VALUES
(1, 'Zhang Laoxiansheng', 'MALE', '1945-03-15', 'Mild hypertension, can take care of daily life, regular medication', TRUE),
(2, 'Li Ayi', 'FEMALE', '1950-08-22', 'Healthy, no major medical history, regular lifestyle', TRUE),
(3, 'Wang Laotaitai', 'FEMALE', '1948-12-05', 'Diabetes and mild Parkinson disease, needs daily care', TRUE);

-- 监护关系数据（5条）
INSERT INTO `elder_guardian` (`id`, `elder_id`, `guardian_id`, `relationship`, `authorized`) VALUES
(1, 1, 2, 'Son', TRUE),
(2, 2, 3, 'Daughter', TRUE),
(3, 3, 5, 'Daughter-in-law', TRUE),
(4, 1, 4, 'Caregiver', TRUE),
(5, 3, 4, 'Caregiver', TRUE);

-- 情感记录数据（30条）
INSERT INTO `emotion_record` (`elder_id`, `emotion_type`, `confidence_score`, `data_source`, `raw_data_url`, `analyzed_at`) VALUES
-- Elder 1 records
(1, 'HAPPY', 0.92, 'VOICE', '/encrypted/data/elder1_voice_001.enc', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'CALM', 0.88, 'IMAGE', '/encrypted/data/elder1_image_001.enc', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'HAPPY', 0.85, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 'CALM', 0.90, 'VOICE', '/encrypted/data/elder1_voice_002.enc', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, 'SAD', 0.78, 'IMAGE', '/encrypted/data/elder1_image_002.enc', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(1, 'HAPPY', 0.95, 'VOICE', '/encrypted/data/elder1_voice_003.enc', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'CALM', 0.87, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 'ANXIOUS', 0.72, 'VOICE', '/encrypted/data/elder1_voice_004.enc', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'HAPPY', 0.89, 'IMAGE', '/encrypted/data/elder1_image_003.enc', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 'CALM', 0.91, 'VOICE', '/encrypted/data/elder1_voice_005.enc', DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- Elder 2 records
(2, 'HAPPY', 0.96, 'IMAGE', '/encrypted/data/elder2_image_001.enc', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 'HAPPY', 0.93, 'VOICE', '/encrypted/data/elder2_voice_001.enc', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 'CALM', 0.89, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(2, 'HAPPY', 0.91, 'IMAGE', '/encrypted/data/elder2_image_002.enc', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(2, 'CALM', 0.87, 'VOICE', '/encrypted/data/elder2_voice_002.enc', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'HAPPY', 0.94, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'CALM', 0.90, 'IMAGE', '/encrypted/data/elder2_image_003.enc', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'HAPPY', 0.88, 'VOICE', '/encrypted/data/elder2_voice_003.enc', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'CALM', 0.86, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'HAPPY', 0.92, 'IMAGE', '/encrypted/data/elder2_image_004.enc', DATE_SUB(NOW(), INTERVAL 6 DAY)),
-- Elder 3 records
(3, 'CALM', 0.85, 'VOICE', '/encrypted/data/elder3_voice_001.enc', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(3, 'SAD', 0.82, 'IMAGE', '/encrypted/data/elder3_image_001.enc', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'ANXIOUS', 0.88, 'VOICE', '/encrypted/data/elder3_voice_002.enc', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(3, 'CALM', 0.79, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(3, 'ANGRY', 0.75, 'IMAGE', '/encrypted/data/elder3_image_002.enc', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(3, 'SAD', 0.91, 'VOICE', '/encrypted/data/elder3_voice_003.enc', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'ANXIOUS', 0.86, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'CALM', 0.83, 'IMAGE', '/encrypted/data/elder3_image_003.enc', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 'SAD', 0.89, 'VOICE', '/encrypted/data/elder3_voice_004.enc', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'HAPPY', 0.76, 'IMAGE', '/encrypted/data/elder3_image_004.enc', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 预警记录数据（7条）
INSERT INTO `alert_record` (`elder_id`, `alert_type`, `severity`, `message`, `status`, `handled_by`, `handled_at`, `handle_note`, `created_at`) VALUES
(1, 'NEGATIVE_EMOTION', 'MEDIUM', 'Sad emotion detected (confidence: 0.78), recommend monitoring', 'HANDLED', 2, DATE_SUB(NOW(), INTERVAL 7 HOUR), 'Talked with elder, emotion recovered', DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(3, 'NEGATIVE_EMOTION', 'MEDIUM', 'Sad emotion detected (confidence: 0.82), recommend monitoring', 'PENDING', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'NEGATIVE_EMOTION', 'HIGH', 'Anxious emotion detected (confidence: 0.88), need intervention', 'PENDING', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(3, 'NEGATIVE_EMOTION', 'MEDIUM', 'Angry emotion detected (confidence: 0.75), recommend monitoring', 'HANDLED', 4, DATE_SUB(NOW(), INTERVAL 10 HOUR), 'Caregiver comforted, elder stable', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(3, 'NEGATIVE_EMOTION', 'CRITICAL', 'Strong sad emotion detected (confidence: 0.91), immediate attention needed', 'HANDLED', 5, DATE_SUB(NOW(), INTERVAL 20 HOUR), 'Family arrived, elder improved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'NEGATIVE_EMOTION', 'HIGH', 'Anxious emotion detected (confidence: 0.86), need intervention', 'HANDLED', 4, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Contacted doctor, adjusted medication', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'NEGATIVE_EMOTION', 'HIGH', 'Sad emotion detected (confidence: 0.89), need intervention', 'HANDLED', 5, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Family increased companionship', DATE_SUB(NOW(), INTERVAL 4 DAY));

-- 审计日志数据（15条）
INSERT INTO `audit_log` (`user_id`, `action`, `resource_type`, `resource_id`, `ip_address`, `details`, `created_at`) VALUES
(1, 'LOGIN', 'USER', 1, '192.168.1.100', '{"loginMethod":"email"}', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'VIEW_DASHBOARD', 'SYSTEM', NULL, '192.168.1.100', '{"viewType":"overview"}', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(2, 'LOGIN', 'USER', 2, '192.168.1.101', '{"loginMethod":"phone"}', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'VIEW_ELDER_INFO', 'ELDER', 1, '192.168.1.101', '{"elderId":1}', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 'HANDLE_ALERT', 'ALERT', 1, '192.168.1.101', '{"alertId":1}', DATE_SUB(NOW(), INTERVAL 7 HOUR)),
(2, 'VIEW_EMOTION_RECORDS', 'EMOTION', NULL, '192.168.1.101', '{"elderId":1}', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(3, 'LOGIN', 'USER', 3, '192.168.1.102', '{"loginMethod":"email"}', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 'VIEW_ELDER_INFO', 'ELDER', 2, '192.168.1.102', '{"elderId":2}', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'VIEW_EMOTION_STATISTICS', 'EMOTION', NULL, '192.168.1.102', '{"elderId":2}', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(4, 'LOGIN', 'USER', 4, '192.168.1.103', '{"loginMethod":"phone"}', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(4, 'HANDLE_ALERT', 'ALERT', 4, '192.168.1.103', '{"alertId":4}', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(4, 'HANDLE_ALERT', 'ALERT', 6, '192.168.1.103', '{"alertId":6}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 'VIEW_ELDER_INFO', 'ELDER', 3, '192.168.1.103', '{"elderId":3}', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(5, 'REGISTER', 'USER', 5, '192.168.1.104', '{"registerMethod":"email"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'HANDLE_ALERT', 'ALERT', 5, '192.168.1.104', '{"alertId":5}', DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- 显示统计信息
SELECT 'Database setup completed!' AS Status;
SELECT COUNT(*) AS user_count FROM `user`;
SELECT COUNT(*) AS elder_count FROM `elder`;
SELECT COUNT(*) AS relationship_count FROM `elder_guardian`;
SELECT COUNT(*) AS emotion_count FROM `emotion_record`;
SELECT COUNT(*) AS alert_count FROM `alert_record`;
SELECT COUNT(*) AS audit_count FROM `audit_log`;
SELECT COUNT(*) AS config_count FROM `system_config`;
