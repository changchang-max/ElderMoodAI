# 数据模型: 居家老人情感分析及可视化系统

**日期**: 2026-05-06
**分支**: `001-elder-mood-ai-system`

## 实体关系概览

```
User ──────────────────── ElderGuardian ──── Elder
 │  (管理员/护理员/家属)    (多对多关联表)      │
 │                                           │
 │                                    EmotionRecord
 │                                           │
 │                                    AlertRecord
 │
AuditLog (记录所有操作)

SystemConfig (全局配置，单例)
```

---

## 1. User（用户）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 主键 |
| phone | VARCHAR(20) | UNIQUE, NULL | 手机号（登录用） |
| email | VARCHAR(100) | UNIQUE, NULL | 邮箱（登录用） |
| password_hash | VARCHAR(255) | NOT NULL | bcrypt 哈希密码 |
| role | ENUM | NOT NULL | 'admin' / 'caregiver' / 'family' |
| name | VARCHAR(50) | NOT NULL | 真实姓名 |
| avatar | VARCHAR(255) | NULL | 头像文件路径 |
| is_active | BOOLEAN | DEFAULT TRUE | 账号是否启用 |
| email_notify | BOOLEAN | DEFAULT FALSE | 是否开启邮件通知 |
| sms_notify | BOOLEAN | DEFAULT FALSE | 是否开启短信通知 |
| site_notify | BOOLEAN | DEFAULT TRUE | 是否开启站内通知 |
| email_smtp_key | VARCHAR(255) | NULL | 邮件授权码（AES-256加密存储） |
| last_login_at | DATETIME | NULL | 最后登录时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**验证规则**:
- phone 和 email 至少有一个不为 NULL。
- role 只能是 'admin' / 'caregiver' / 'family' 之一。
- password_hash 不可直接返回给前端。

---

## 2. Elder（老人）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(50) | NOT NULL | 姓名（AES-256加密存储） |
| age | INT | NOT NULL | 年龄 |
| gender | ENUM | NOT NULL | 'male' / 'female' / 'other' |
| phone | VARCHAR(20) | NULL | 联系电话（AES-256加密存储） |
| address | VARCHAR(255) | NULL | 居住地址（AES-256加密存储） |
| privacy_authorized | BOOLEAN | DEFAULT FALSE | 隐私授权状态 |
| authorized_at | DATETIME | NULL | 授权时间 |
| authorized_by | INT | FK→User.id, NULL | 授权操作人 |
| alert_threshold | JSON | NULL | 个人预警阈值覆盖（NULL时使用全局默认） |
| created_by | INT | FK→User.id | 创建人 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

**验证规则**:
- privacy_authorized = FALSE 时，禁止创建该老人的 EmotionRecord。
- alert_threshold 为 NULL 时，使用 SystemConfig 中的全局阈值。

---

## 3. ElderGuardian（老人-家属关联表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 主键 |
| elder_id | INT | FK→Elder.id, NOT NULL | 老人ID |
| user_id | INT | FK→User.id, NOT NULL | 家属用户ID |
| relation | VARCHAR(50) | NULL | 关系描述（如：子女、配偶） |
| created_at | DATETIME | NOT NULL | 绑定时间 |

**约束**:
- (elder_id, user_id) 联合唯一索引，防止重复绑定。
- user_id 对应的 User.role 应为 'family'（业务层校验）。

---

## 4. EmotionRecord（情感记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 主键 |
| elder_id | INT | FK→Elder.id, NOT NULL | 关联老人 |
| submitted_by | INT | FK→User.id, NOT NULL | 提交用户 |
| emotion_label | VARCHAR(20) | NOT NULL | 情感标签（开心/平静/低落/焦虑/愤怒） |
| confidence | DECIMAL(5,4) | NOT NULL | 综合置信度（0.0000~1.0000） |
| health_score | DECIMAL(5,2) | NOT NULL | 情感健康评分（0~100） |
| text_score | DECIMAL(5,4) | NULL | 文本模态得分 |
| voice_score | DECIMAL(5,4) | NULL | 语音模态得分 |
| image_score | DECIMAL(5,4) | NULL | 图像模态得分 |
| text_content | TEXT | NULL | 文本输入内容（AES-256加密） |
| voice_file | VARCHAR(255) | NULL | 语音文件路径 |
| image_file | VARCHAR(255) | NULL | 图像文件路径 |
| api_raw_result | JSON | NULL | 外部AI API原始返回（加密存储） |
| created_at | DATETIME | NOT NULL | 分析时间 |

**验证规则**:
- text_score / voice_score / image_score 至少有一个不为 NULL（至少提交一种模态）。
- emotion_label 只能是预定义标签之一。
- health_score 计算规则：开心=90~100，平静=70~89，低落=40~69，焦虑=20~39，愤怒=0~19（可配置）。

---

## 5. AlertRecord（预警记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 主键 |
| elder_id | INT | FK→Elder.id, NOT NULL | 关联老人 |
| emotion_record_id | INT | FK→EmotionRecord.id, NOT NULL | 触发预警的情感记录 |
| emotion_label | VARCHAR(20) | NOT NULL | 触发时的情感标签 |
| alert_level | ENUM | NOT NULL | 'low' / 'medium' / 'high' |
| status | ENUM | DEFAULT 'pending' | 'pending' / 'handled' |
| handled_by | INT | FK→User.id, NULL | 处理人 |
| handled_at | DATETIME | NULL | 处理时间 |
| notify_sent | BOOLEAN | DEFAULT FALSE | 是否已发送外部通知 |
| created_at | DATETIME | NOT NULL | 预警触发时间 |

**状态转换**:
- pending → handled（由护理员或家属操作"标记已处理"）

---

## 6. AuditLog（审计日志）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键（大表用BIGINT） |
| user_id | INT | FK→User.id, NULL | 操作人（NULL表示系统自动操作） |
| action | VARCHAR(100) | NOT NULL | 操作类型（如：LOGIN、CREATE_ELDER、DELETE_DATA） |
| target_type | VARCHAR(50) | NULL | 操作对象类型（如：Elder、User、EmotionRecord） |
| target_id | INT | NULL | 操作对象ID |
| detail | JSON | NULL | 变更详情（旧值/新值） |
| ip_address | VARCHAR(45) | NULL | 操作IP地址 |
| created_at | DATETIME | NOT NULL | 操作时间 |

**保留策略**: 审计日志保留不少于90天（章程要求），超期记录自动归档或删除。

---

## 7. SystemConfig（系统配置）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INT | PK, DEFAULT 1 | 单例，始终为1 |
| alert_thresholds | JSON | NOT NULL | 全局预警阈值配置 |
| notify_methods | JSON | NOT NULL | 默认推送方式配置 |
| sms_api_config | JSON | NULL | 短信API配置（加密存储） |
| ai_api_config | JSON | NULL | AI API密钥配置（加密存储） |
| data_retention_days | INT | DEFAULT 365 | 情感数据保留天数 |
| audit_retention_days | INT | DEFAULT 90 | 审计日志保留天数 |
| updated_at | DATETIME | NOT NULL | 最后更新时间 |
| updated_by | INT | FK→User.id | 最后更新人 |

**alert_thresholds 示例**:
```json
{
  "anxiety": { "confidence_min": 0.7, "level": "high" },
  "depression": { "confidence_min": 0.6, "level": "medium" },
  "anger": { "confidence_min": 0.75, "level": "high" }
}
```

---

## 索引设计

| 表 | 索引字段 | 类型 | 用途 |
|----|---------|------|------|
| User | phone | UNIQUE | 手机号登录查询 |
| User | email | UNIQUE | 邮箱登录查询 |
| Elder | privacy_authorized | INDEX | 筛选已授权老人 |
| ElderGuardian | (elder_id, user_id) | UNIQUE | 防重复绑定 |
| ElderGuardian | user_id | INDEX | 查询家属关注的老人列表 |
| EmotionRecord | elder_id | INDEX | 查询老人情感历史 |
| EmotionRecord | created_at | INDEX | 时间范围筛选 |
| EmotionRecord | (elder_id, created_at) | INDEX | 复合查询（可视化） |
| AlertRecord | (elder_id, status) | INDEX | 预警中心筛选 |
| AuditLog | user_id | INDEX | 用户操作查询 |
| AuditLog | created_at | INDEX | 时间范围查询 |
