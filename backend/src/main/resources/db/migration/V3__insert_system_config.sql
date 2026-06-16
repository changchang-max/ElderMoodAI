-- ElderMoodAI Backend System - Initial System Configuration
-- Version: 1.0.0
-- Requirements: 12.3, 12.4, 12.5, 12.6, 12.7, 12.8

-- Insert default system configurations
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('alert.threshold.negative_emotion', '0.7', '负面情绪预警阈值（0.0-1.0）'),
('alert.threshold.critical_emotion', '0.95', '严重情绪预警阈值（0.0-1.0）'),
('notification.email.enabled', 'false', '是否启用邮件通知（true/false）'),
('notification.sms.enabled', 'false', '是否启用短信通知（true/false）'),
('ai.service.provider', 'baidu', 'AI服务提供商（baidu/aliyun/xunfei）'),
('data.retention.days', '365', '数据保留天数（默认365天）');
