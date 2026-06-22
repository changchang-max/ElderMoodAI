-- ElderMoodAI Backend System - Test Data
-- Version: 1.0.0
-- 插入20条测试数据用于开发和测试

-- ==================== User测试数据 (5条) ====================
-- 密码均为: Password123! (BCrypt加密后)
INSERT INTO `user` (`id`, `username`, `phone`, `email`, `password_hash`, `role`, `status`, `created_at`, `updated_at`) VALUES
(1, 'admin', '13800138000', 'admin@eldermood.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'ADMIN', 'ACTIVE', NOW(), NOW()),
(2, 'guardian_zhang', '13812345678', 'zhang@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'GUARDIAN', 'ACTIVE', NOW(), NOW()),
(3, 'guardian_li', '13823456789', 'li@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'GUARDIAN', 'ACTIVE', NOW(), NOW()),
(4, 'caregiver_wang', '13834567890', 'wang@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'CAREGIVER', 'ACTIVE', NOW(), NOW()),
(5, 'guardian_liu', '13845678901', 'liu@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/Lew.n6J.Xe4vPEq0G', 'GUARDIAN', 'PENDING_APPROVAL', NOW(), NOW());

-- ==================== Elder测试数据 (3条) ====================
INSERT INTO `elder` (`id`, `name`, `gender`, `birth_date`, `health_status`, `privacy_enabled`, `created_at`, `updated_at`) VALUES
(1, '张老先生', 'MALE', '1945-03-15', '患有轻度高血压，日常生活可自理，定期服药', TRUE, NOW(), NOW()),
(2, '李阿姨', 'FEMALE', '1950-08-22', '身体健康，无重大疾病史，生活规律', TRUE, NOW(), NOW()),
(3, '王老太太', 'FEMALE', '1948-12-05', '患有糖尿病和轻度帕金森病，需要日常照护', TRUE, NOW(), NOW());

-- ==================== ElderGuardian关系数据 (5条) ====================
INSERT INTO `elder_guardian` (`id`, `elder_id`, `guardian_id`, `relationship`, `authorized`, `created_at`) VALUES
(1, 1, 2, '儿子', TRUE, NOW()),
(2, 2, 3, '女儿', TRUE, NOW()),
(3, 3, 5, '儿媳', TRUE, NOW()),
(4, 1, 4, '护理员', TRUE, NOW()),
(5, 3, 4, '护理员', TRUE, NOW());

-- ==================== EmotionRecord情感记录数据 (30条) ====================
-- 为张老先生添加记录（10条）
INSERT INTO `emotion_record` (`elder_id`, `emotion_type`, `confidence_score`, `data_source`, `raw_data_url`, `analyzed_at`, `created_at`) VALUES
(1, 'HAPPY', 0.92, 'VOICE', '/encrypted/data/elder1_voice_001.enc', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
(1, 'CALM', 0.88, 'IMAGE', '/encrypted/data/elder1_image_001.enc', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
(1, 'HAPPY', 0.85, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
(1, 'CALM', 0.90, 'VOICE', '/encrypted/data/elder1_voice_002.enc', DATE_SUB(NOW(), INTERVAL 5 HOUR), NOW()),
(1, 'SAD', 0.78, 'IMAGE', '/encrypted/data/elder1_image_002.enc', DATE_SUB(NOW(), INTERVAL 8 HOUR), NOW()),
(1, 'HAPPY', 0.95, 'VOICE', '/encrypted/data/elder1_voice_003.enc', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(1, 'CALM', 0.87, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(1, 'ANXIOUS', 0.72, 'VOICE', '/encrypted/data/elder1_voice_004.enc', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(1, 'HAPPY', 0.89, 'IMAGE', '/encrypted/data/elder1_image_003.enc', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
(1, 'CALM', 0.91, 'VOICE', '/encrypted/data/elder1_voice_005.enc', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());

-- 为李阿姨添加记录（10条）
INSERT INTO `emotion_record` (`elder_id`, `emotion_type`, `confidence_score`, `data_source`, `raw_data_url`, `analyzed_at`, `created_at`) VALUES
(2, 'HAPPY', 0.96, 'IMAGE', '/encrypted/data/elder2_image_001.enc', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
(2, 'HAPPY', 0.93, 'VOICE', '/encrypted/data/elder2_voice_001.enc', DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
(2, 'CALM', 0.89, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW()),
(2, 'HAPPY', 0.91, 'IMAGE', '/encrypted/data/elder2_image_002.enc', DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW()),
(2, 'CALM', 0.87, 'VOICE', '/encrypted/data/elder2_voice_002.enc', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(2, 'HAPPY', 0.94, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(2, 'CALM', 0.90, 'IMAGE', '/encrypted/data/elder2_image_003.enc', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(2, 'HAPPY', 0.88, 'VOICE', '/encrypted/data/elder2_voice_003.enc', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
(2, 'CALM', 0.86, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(2, 'HAPPY', 0.92, 'IMAGE', '/encrypted/data/elder2_image_004.enc', DATE_SUB(NOW(), INTERVAL 6 DAY), NOW());

-- 为王老太太添加记录（10条，包含一些负面情绪）
INSERT INTO `emotion_record` (`elder_id`, `emotion_type`, `confidence_score`, `data_source`, `raw_data_url`, `analyzed_at`, `created_at`) VALUES
(3, 'CALM', 0.85, 'VOICE', '/encrypted/data/elder3_voice_001.enc', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
(3, 'SAD', 0.82, 'IMAGE', '/encrypted/data/elder3_image_001.enc', DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
(3, 'ANXIOUS', 0.88, 'VOICE', '/encrypted/data/elder3_voice_002.enc', DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW()),
(3, 'CALM', 0.79, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR), NOW()),
(3, 'ANGRY', 0.75, 'IMAGE', '/encrypted/data/elder3_image_002.enc', DATE_SUB(NOW(), INTERVAL 12 HOUR), NOW()),
(3, 'SAD', 0.91, 'VOICE', '/encrypted/data/elder3_voice_003.enc', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(3, 'ANXIOUS', 0.86, 'TEXT', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(3, 'CALM', 0.83, 'IMAGE', '/encrypted/data/elder3_image_003.enc', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(3, 'SAD', 0.89, 'VOICE', '/encrypted/data/elder3_voice_004.enc', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
(3, 'HAPPY', 0.76, 'IMAGE', '/encrypted/data/elder3_image_004.enc', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());

-- ==================== AlertRecord预警记录数据 (7条) ====================
-- 张老先生的预警（1条 - 已处理）
INSERT INTO `alert_record` (`elder_id`, `alert_type`, `severity`, `message`, `status`, `handled_by`, `handled_at`, `handle_note`, `created_at`) VALUES
(1, 'NEGATIVE_EMOTION', 'MEDIUM', '检测到悲伤情绪（置信度：0.78），建议关注老人心理状态', 'HANDLED', 2, DATE_SUB(NOW(), INTERVAL 7 HOUR), '已与老人沟通，情绪恢复正常', DATE_SUB(NOW(), INTERVAL 8 HOUR));

-- 王老太太的预警（6条 - 多条待处理和已处理）
INSERT INTO `alert_record` (`elder_id`, `alert_type`, `severity`, `message`, `status`, `handled_by`, `handled_at`, `handle_note`, `created_at`) VALUES
(3, 'NEGATIVE_EMOTION', 'MEDIUM', '检测到悲伤情绪（置信度：0.82），建议关注老人心理状态', 'PENDING', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'NEGATIVE_EMOTION', 'HIGH', '检测到焦虑情绪（置信度：0.88），建议及时干预', 'PENDING', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(3, 'NEGATIVE_EMOTION', 'MEDIUM', '检测到愤怒情绪（置信度：0.75），建议关注老人心理状态', 'HANDLED', 4, DATE_SUB(NOW(), INTERVAL 10 HOUR), '护理员已安抚，老人情绪稳定', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(3, 'NEGATIVE_EMOTION', 'CRITICAL', '检测到强烈悲伤情绪（置信度：0.91），需要立即关注', 'HANDLED', 5, DATE_SUB(NOW(), INTERVAL 20 HOUR), '家属已到场陪伴，老人情绪好转', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'NEGATIVE_EMOTION', 'HIGH', '检测到焦虑情绪（置信度：0.86），建议及时干预', 'HANDLED', 4, DATE_SUB(NOW(), INTERVAL 1 DAY), '已联系医生，调整了药物剂量', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 'NEGATIVE_EMOTION', 'HIGH', '检测到悲伤情绪（置信度：0.89），建议及时干预', 'HANDLED', 5, DATE_SUB(NOW(), INTERVAL 3 DAY), '家属增加了陪伴时间', DATE_SUB(NOW(), INTERVAL 4 DAY));

-- ==================== AuditLog审计日志数据 (15条) ====================
INSERT INTO `audit_log` (`user_id`, `action`, `resource_type`, `resource_id`, `ip_address`, `details`, `created_at`) VALUES
-- 管理员操作
(1, 'LOGIN', 'USER', 1, '192.168.1.100', '{"loginMethod":"email","userAgent":"Mozilla/5.0"}', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'VIEW_DASHBOARD', 'SYSTEM', NULL, '192.168.1.100', '{"viewType":"overview"}', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),

-- 监护人张操作
(2, 'LOGIN', 'USER', 2, '192.168.1.101', '{"loginMethod":"phone","userAgent":"Mozilla/5.0"}', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'VIEW_ELDER_INFO', 'ELDER', 1, '192.168.1.101', '{"elderId":1}', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 'HANDLE_ALERT', 'ALERT', 1, '192.168.1.101', '{"alertId":1,"action":"handled"}', DATE_SUB(NOW(), INTERVAL 7 HOUR)),
(2, 'VIEW_EMOTION_RECORDS', 'EMOTION', NULL, '192.168.1.101', '{"elderId":1,"period":"week"}', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),

-- 监护人李操作
(3, 'LOGIN', 'USER', 3, '192.168.1.102', '{"loginMethod":"email","userAgent":"Mozilla/5.0"}', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 'VIEW_ELDER_INFO', 'ELDER', 2, '192.168.1.102', '{"elderId":2}', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'VIEW_EMOTION_STATISTICS', 'EMOTION', NULL, '192.168.1.102', '{"elderId":2,"statisticsType":"monthly"}', DATE_SUB(NOW(), INTERVAL 1 HOUR)),

-- 护理员王操作
(4, 'LOGIN', 'USER', 4, '192.168.1.103', '{"loginMethod":"phone","userAgent":"Mozilla/5.0"}', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(4, 'HANDLE_ALERT', 'ALERT', 4, '192.168.1.103', '{"alertId":4,"action":"handled"}', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(4, 'HANDLE_ALERT', 'ALERT', 6, '192.168.1.103', '{"alertId":6,"action":"handled"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 'VIEW_ELDER_INFO', 'ELDER', 3, '192.168.1.103', '{"elderId":3}', DATE_SUB(NOW(), INTERVAL 4 HOUR)),

-- 监护人刘操作
(5, 'REGISTER', 'USER', 5, '192.168.1.104', '{"registerMethod":"email"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'HANDLE_ALERT', 'ALERT', 5, '192.168.1.104', '{"alertId":5,"action":"handled"}', DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- 数据统计信息
-- 用户数: 5 (1个管理员, 3个监护人, 1个护理员)
-- 老人数: 3
-- 关系绑定: 5
-- 情感记录: 30 (张老先生10条, 李阿姨10条, 王老太太10条)
-- 预警记录: 7 (1条已处理, 6条包含待处理和已处理)
-- 审计日志: 15
