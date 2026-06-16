-- ElderMoodAI Backend System - Initial Database Schema
-- Version: 1.0.0
-- Requirements: 2.9, 3.8, 4.6, 5.7, 6.12, 9.9, 12.1

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
    `relationship` VARCHAR(50) NOT NULL COMMENT '子女、配偶、护理员等',
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
    `raw_data_url` VARCHAR(500) COMMENT '加密后的原始数据存储路径',
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
    `action` VARCHAR(100) NOT NULL COMMENT 'LOGIN, LOGOUT, CREATE_ELDER, UPDATE_ELDER, etc.',
    `resource_type` VARCHAR(50) NOT NULL COMMENT 'USER, ELDER, EMOTION_RECORD, etc.',
    `resource_id` BIGINT,
    `ip_address` VARCHAR(50),
    `details` VARCHAR(1000) COMMENT 'JSON格式的详细信息',
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
