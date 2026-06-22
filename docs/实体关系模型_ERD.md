# ElderMoodAI 数据模型 - 实体关系图 (ERD)

## 1. 核心实体关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                        ElderMoodAI 数据模型                      │
└─────────────────────────────────────────────────────────────────┘

                          ┌──────────────┐
                          │    User      │
                          ├──────────────┤
                          │ id (PK)      │
                          │ username (U) │◄──────┐
                          │ phone (U)    │       │
                          │ email (U)    │       │
                          │ passwordHash │       │
                          │ role         │       │
                          │ status       │       │
                          │ createdAt    │       │
                          │ updatedAt    │       │
                          └──────────────┘       │
                                  │              │
                    ┌─────────────┼──────────────┤
                    │             │              │
                    │             │         [Guardian]
                    │        [Operator]    (User.role=GUARDIAN)
                    │             │              │
                    │             │              │
        ┌───────────▼────────┐    │     ┌─────────▼──────────┐
        │     Elder          │    │     │  ElderGuardian     │
        ├────────────────────┤    │     ├────────────────────┤
        │ id (PK)            │    │     │ id (PK)            │
        │ name               │◄───┼─────│ elderId (FK)       │
        │ gender             │    │     │ guardianId (FK)────┼──→ User
        │ birthDate          │    │     │ relationship       │
        │ healthStatus       │    │     │ authorized         │
        │ privacyEnabled     │    │     │ createdAt          │
        │ createdAt          │    │     └────────────────────┘
        │ updatedAt          │    │      (UC: elderId + guardianId)
        └────────┬───────────┘    │
                 │                │
                 │ 1              │
            ┌────┼────────────────┘
            │    │
            │    └─ [OperatorId]
            │       (User.id for audit)
            │
   ┌────────▼──────────────┐
   │  EmotionRecord        │
   ├───────────────────────┤
   │ id (PK)               │
   │ elderId (FK)──────────┼──→ Elder
   │ emotionType (enum)    │
   │ confidenceScore       │
   │ dataSource (enum)     │
   │ rawDataUrl (encrypted)│
   │ analyzedAt            │
   │ createdAt             │
   └────────┬──────────────┘
            │
            │ 1..* (多个记录可能触发预警)
            │
   ┌────────▼──────────────┐
   │  AlertRecord          │
   ├───────────────────────┤
   │ id (PK)               │
   │ elderId (FK)──────────┼──→ Elder
   │ alertType (enum)      │
   │ severity (enum)       │
   │ message               │
   │ status (enum)         │
   │ handledBy (FK)────────┼──→ User (可选)
   │ handledAt             │
   │ handleNote            │
   │ createdAt             │
   └───────────────────────┘


┌─────────────────────────────────────────────────────────┐
│              AuditLog (不可变)                           │
├─────────────────────────────────────────────────────────┤
│ id (PK)                                                 │
│ userId (FK)──────────────────────────────────┐          │
│ action                                       │          │
│ resourceType                                 ├────→ User│
│ resourceId                                   │          │
│ ipAddress                                    │          │
│ details (JSON)                               │          │
│ createdAt (immutable)                        │          │
└─────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────┐
│          SystemConfig (全局配置)                         │
├──────────────────────────────────────────────────────────┤
│ id (PK)                                                  │
│ configKey (UC)                                           │
│ configValue                                              │
│ description                                              │
│ updatedAt                                                │
└──────────────────────────────────────────────────────────┘
```

## 2. 用户角色与数据访问权限矩阵

```
┌──────────────┬─────────────────────────────────────────┐
│ 用户角色     │ 数据访问权限                            │
├──────────────┼─────────────────────────────────────────┤
│ ADMIN        │ 全部数据（完全访问）                    │
│ GUARDIAN     │ 授权的Elder及其EmotionRecord、AlertRecord│
│ CAREGIVER    │ 分配的Elder及其EmotionRecord、AlertRecord│
└──────────────┴─────────────────────────────────────────┘

权限树:
  /api/users/
    - POST   → ADMIN only
    - GET    → ADMIN only
    - PUT    → ADMIN or self
    - DELETE → ADMIN only

  /api/elders/
    - POST   → GUARDIAN, CAREGIVER
    - GET    → ADMIN, authorized GUARDIAN/CAREGIVER
    - PUT    → ADMIN, authorized GUARDIAN/CAREGIVER
    - DELETE → ADMIN only

  /api/emotions/
    - POST   → CAREGIVER, ADMIN
    - GET    → ADMIN, authorized GUARDIAN/CAREGIVER
    - DELETE → ADMIN only

  /api/alerts/
    - GET    → ADMIN, authorized GUARDIAN/CAREGIVER
    - PUT    → ADMIN, authorized GUARDIAN/CAREGIVER
    - DELETE → ADMIN only
```

## 3. 实体字段详细映射

### 3.1 User (用户表)

```sql
CREATE TABLE user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  phone VARCHAR(20) UNIQUE,
  email VARCHAR(100) UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'GUARDIAN',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  
  CONSTRAINT check_phone_format CHECK (phone IS NULL OR phone REGEXP '^1[3-9][0-9]{9}$'),
  CONSTRAINT check_email_format CHECK (email IS NULL OR email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$'),
  INDEX idx_phone (phone),
  INDEX idx_email (email),
  INDEX idx_status (status)
);

字段说明:
- id: 主键，自增
- username: 用户名，全局唯一，3-50字符
- phone: 手机号，可选，唯一，11位格式
- email: 邮箱，可选，唯一，RFC格式
- passwordHash: BCrypt哈希密码，255字符
- role: 用户角色枚举值
- status: 用户状态枚举值
- createdAt: 创建时间，自动设置
- updatedAt: 更新时间，自动更新
```

### 3.2 Elder (老人表)

```sql
CREATE TABLE elder (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  birth_date DATE NOT NULL,
  health_status VARCHAR(500),
  privacy_enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  
  CONSTRAINT check_age CHECK (YEAR(CURDATE()) - YEAR(birth_date) BETWEEN 60 AND 120),
  INDEX idx_gender (gender),
  INDEX idx_birth_date (birth_date)
);

字段说明:
- id: 主键，自增
- name: 老人姓名，1-50字符
- gender: 性别枚举（MALE/FEMALE/OTHER）
- birth_date: 出生日期，必须过去日期，年龄60-120岁
- health_status: 健康状况描述，可选，0-500字符
- privacy_enabled: 隐私保护开关，默认开启
- createdAt: 创建时间，自动设置
- updatedAt: 更新时间，自动更新
```

### 3.3 ElderGuardian (老人-监护人关系表)

```sql
CREATE TABLE elder_guardian (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  elder_id BIGINT NOT NULL,
  guardian_id BIGINT NOT NULL,
  relationship VARCHAR(50) NOT NULL,
  authorized BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  
  CONSTRAINT fk_elder_id FOREIGN KEY (elder_id) REFERENCES elder(id) ON DELETE CASCADE,
  CONSTRAINT fk_guardian_id FOREIGN KEY (guardian_id) REFERENCES user(id) ON DELETE CASCADE,
  CONSTRAINT uc_elder_guardian UNIQUE (elder_id, guardian_id),
  INDEX idx_elder_id (elder_id),
  INDEX idx_guardian_id (guardian_id),
  INDEX idx_authorized (authorized)
);

字段说明:
- id: 主键，自增
- elder_id: 老人ID，外键引用Elder
- guardian_id: 监护人ID（User），外键引用User
- relationship: 关系类型，1-50字符（如：子女、配偶、护理员等）
- authorized: 授权状态，默认false
- createdAt: 创建时间，自动设置
- 复合唯一约束：(elder_id, guardian_id) 防止重复绑定
```

### 3.4 EmotionRecord (情感记录表)

```sql
CREATE TABLE emotion_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  elder_id BIGINT NOT NULL,
  emotion_type VARCHAR(20) NOT NULL,
  confidence_score DECIMAL(3,2) NOT NULL,
  data_source VARCHAR(20) NOT NULL,
  raw_data_url VARCHAR(500),
  analyzed_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  
  CONSTRAINT fk_elder_id FOREIGN KEY (elder_id) REFERENCES elder(id) ON DELETE CASCADE,
  CONSTRAINT check_confidence CHECK (confidence_score >= 0.0 AND confidence_score <= 1.0),
  INDEX idx_elder_analyzed (elder_id, analyzed_at),
  INDEX idx_emotion_type (emotion_type),
  INDEX idx_created_at (created_at)
);

字段说明:
- id: 主键，自增
- elder_id: 老人ID，外键引用Elder
- emotion_type: 情感类型（HAPPY/CALM/SAD/ANXIOUS/ANGRY）
- confidence_score: 置信度，0.0-1.0，3位数字2位小数
- data_source: 数据来源（VOICE/IMAGE/VIDEO/TEXT/SENSOR）
- raw_data_url: 原始数据路径，加密存储，可选
- analyzed_at: 分析时间戳
- createdAt: 创建时间，自动设置
```

### 3.5 AlertRecord (预警记录表)

```sql
CREATE TABLE alert_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  elder_id BIGINT NOT NULL,
  alert_type VARCHAR(50) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  message VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  handled_by BIGINT,
  handled_at DATETIME,
  handle_note VARCHAR(1000),
  created_at DATETIME NOT NULL,
  
  CONSTRAINT fk_elder_id FOREIGN KEY (elder_id) REFERENCES elder(id) ON DELETE CASCADE,
  CONSTRAINT fk_handled_by FOREIGN KEY (handled_by) REFERENCES user(id) ON DELETE SET NULL,
  INDEX idx_elder_status_created (elder_id, status, created_at),
  INDEX idx_severity (severity),
  INDEX idx_created_at (created_at)
);

字段说明:
- id: 主键，自增
- elder_id: 老人ID，外键引用Elder
- alert_type: 预警类型（NEGATIVE_EMOTION/ABNORMAL_BEHAVIOR）
- severity: 严重程度（LOW/MEDIUM/HIGH/CRITICAL）
- message: 预警内容，1-500字符
- status: 处理状态（PENDING/HANDLED/IGNORED）
- handled_by: 处理人ID，外键引用User，可选
- handled_at: 处理时间，可选
- handle_note: 处理备注，0-1000字符，可选
- createdAt: 创建时间，自动设置
```

### 3.6 AuditLog (审计日志表，不可变)

```sql
CREATE TABLE audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  action VARCHAR(100) NOT NULL,
  resource_type VARCHAR(50) NOT NULL,
  resource_id BIGINT,
  ip_address VARCHAR(50),
  details VARCHAR(1000),
  created_at DATETIME NOT NULL,
  
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
  INDEX idx_user_created (user_id, created_at),
  INDEX idx_action_created (action, created_at),
  INDEX idx_resource_type (resource_type)
);

字段说明:
- id: 主键，自增
- user_id: 操作用户ID，外键引用User
- action: 操作动作，1-100字符（如LOGIN、CREATE_ELDER、UPDATE_ELDER等）
- resource_type: 资源类型，1-50字符（如USER、ELDER、EMOTION_RECORD等）
- resource_id: 被操作的资源ID，可选
- ip_address: 客户端IP，0-50字符
- details: 操作详情JSON，0-1000字符
- createdAt: 创建时间，自动设置
- 注意：@Immutable，仅支持INSERT，不允许UPDATE/DELETE
```

### 3.7 SystemConfig (系统配置表)

```sql
CREATE TABLE system_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  config_key VARCHAR(100) NOT NULL UNIQUE,
  config_value VARCHAR(500) NOT NULL,
  description VARCHAR(200),
  updated_at DATETIME NOT NULL,
  
  INDEX idx_config_key (config_key)
);

字段说明:
- id: 主键，自增
- configKey: 配置键，全局唯一，1-100字符
- configValue: 配置值，1-500字符
- description: 配置描述，可选，0-200字符
- updatedAt: 最后更新时间，自动更新

预初始化数据:
- alert.threshold.negative_emotion = 0.7
- alert.threshold.critical_emotion = 0.95
- notification.email.enabled = false
- notification.sms.enabled = false
- ai.service.provider = baidu
- data.retention.days = 365
```

## 4. 枚举值参考表

### UserRole (用户角色)
```
GUARDIAN     - 家属/监护人（可查看关联老人的数据）
CAREGIVER    - 护理员（可查看分配的老人数据）
ADMIN        - 系统管理员（完全访问）
```

### UserStatus (用户状态)
```
PENDING_APPROVAL - 待审批（默认注册后状态）
ACTIVE           - 活跃（已审批，可登录）
INACTIVE         - 非活跃/禁用（无法登录）
```

### Gender (性别)
```
MALE   - 男性
FEMALE - 女性
OTHER  - 其他
```

### EmotionType (情感类型)
```
HAPPY   - 开心
CALM    - 平静
SAD     - 悲伤
ANXIOUS - 焦虑
ANGRY   - 愤怒
```

### EmotionDataSource (数据来源)
```
VOICE  - 语音数据
IMAGE  - 图像数据
VIDEO  - 视频数据
TEXT   - 文本数据
SENSOR - 传感器数据
```

### AlertType (预警类型)
```
NEGATIVE_EMOTION   - 负面情绪预警
ABNORMAL_BEHAVIOR  - 异常行为预警
```

### Severity (严重程度)
```
LOW      - 低级 (置信度 < 0.7)
MEDIUM   - 中级 (置信度 0.7 - 0.85)
HIGH     - 高级 (置信度 0.85 - 0.95)
CRITICAL - 紧急 (置信度 >= 0.95)
```

### AlertStatus (预警状态)
```
PENDING - 待处理（默认状态）
HANDLED - 已处理
IGNORED - 已忽略
```

## 5. 数据一致性约束

### 完整性约束
- 外键约束：DELETE CASCADE（删除老人时自动删除相关的关系和记录）
- 唯一性约束：防止数据重复
- 非空约束：必填字段不允许NULL

### 业务规则约束
- 用户状态转移：PENDING_APPROVAL → ACTIVE/INACTIVE
- 预警状态转移：PENDING → HANDLED/IGNORED
- ElderGuardian.authorized：需要Elder所有者或Admin确认
- AuditLog：仅插入，不更新、不删除

### 时间序列约束
- birth_date: 必须早于当前日期
- createdAt: 不可更新
- updatedAt: 自动更新为当前时间
- analyzedAt: 分析时间戳，可以晚于createdAt

## 6. 索引策略

```
高频查询索引:
- user: (phone), (email), (status)
- elder: (gender), (birth_date)
- elder_guardian: (elder_id), (guardian_id), (authorized)
- emotion_record: (elder_id, analyzed_at), (emotion_type), (created_at)
- alert_record: (elder_id, status, created_at), (severity), (created_at)
- audit_log: (user_id, created_at), (action, created_at), (resource_type)
- system_config: (config_key)
```

## 7. 缓存策略

```
缓存层设计:
- Elder信息: Redis key="elder:info:{elderId}", TTL=1小时
- EmotionRecord趋势: Redis key="emotion:trend:{elderId}:{period}", TTL=6小时
- SystemConfig: Redis key="config:{configKey}", TTL=24小时
- 缓存失效: UPDATE/DELETE操作时主动删除相关缓存
```

---

*此文档由Task 8自动生成，反映系统当前的数据模型设计*
