# Task 8 - JPA实体类快速参考卡片

## 快速导航

```
ElderMoodAI 后端实体系统
│
├── 核心实体 (Core Entities)
│   ├── User (用户) - 认证与授权基础
│   ├── Elder (老人) - 监护对象
│   └── ElderGuardian (关系) - 监护关系
│
├── 业务数据 (Business Data)
│   ├── EmotionRecord (情感记录) - AI分析结果
│   ├── AlertRecord (预警记录) - 系统预警
│   └── AuditLog (审计日志) - 操作追踪
│
└── 配置数据 (Configuration)
    └── SystemConfig (系统配置) - 全局参数
```

---

## 实体速查表

### User 实体
```java
@Entity @Table(name = "user")
class User {
  Long id;                    // 主键
  String username;            // 用户名 [unique, 3-50]
  String phone;               // 手机号 [unique, optional]
  String email;               // 邮箱 [unique, optional]
  String passwordHash;        // 密码哈希 [BCrypt]
  UserRole role;              // 角色: GUARDIAN|CAREGIVER|ADMIN
  UserStatus status;          // 状态: ACTIVE|INACTIVE|PENDING_APPROVAL
  LocalDateTime createdAt;    // 创建时间 [自动]
  LocalDateTime updatedAt;    // 更新时间 [自动]
}
```
**关键方法:** find/save/update/delete
**外部引用:** ElderGuardian.guardianId, AlertRecord.handledBy, AuditLog.userId

---

### Elder 实体
```java
@Entity @Table(name = "elder")
class Elder {
  Long id;                    // 主键
  String name;                // 姓名 [1-50]
  Gender gender;              // 性别: MALE|FEMALE|OTHER
  LocalDate birthDate;        // 出生日期 [@Past]
  String healthStatus;        // 健康状况 [optional, 0-500]
  Boolean privacyEnabled;     // 隐私保护 [default: true]
  LocalDateTime createdAt;    // 创建时间 [自动]
  LocalDateTime updatedAt;    // 更新时间 [自动]
}
```
**关键方法:** find/save/update/delete
**外部引用:** ElderGuardian.elderId, EmotionRecord.elderId, AlertRecord.elderId

---

### ElderGuardian 实体
```java
@Entity @Table(name = "elder_guardian", 
  uniqueConstraints = @UniqueConstraint(columnNames={"elder_id","guardian_id"}))
class ElderGuardian {
  Long id;                    // 主键
  Long elderId;               // 老人ID [FK → Elder]
  Long guardianId;            // 监护人ID [FK → User]
  String relationship;        // 关系 [1-50] 如: 子女|配偶|护理员
  Boolean authorized;         // 授权状态 [default: false]
  LocalDateTime createdAt;    // 创建时间 [自动]
}
```
**关键方法:** findByElderIdAndGuardianId/findByElderIdAndAuthorized
**约束:** 复合唯一 (elderId, guardianId)

---

### EmotionRecord 实体
```java
@Entity @Table(name = "emotion_record")
class EmotionRecord {
  Long id;                    // 主键
  Long elderId;               // 老人ID [FK → Elder]
  EmotionType emotionType;    // 情感: HAPPY|CALM|SAD|ANXIOUS|ANGRY
  Double confidenceScore;     // 置信度 [@DecimalMin(0.0) @DecimalMax(1.0)]
  EmotionDataSource dataSource; // 来源: VOICE|IMAGE|VIDEO|TEXT|SENSOR
  String rawDataUrl;          // 原始数据 [加密, optional, 0-500]
  LocalDateTime analyzedAt;   // 分析时间
  LocalDateTime createdAt;    // 创建时间 [自动]
}
```
**关键方法:** findByElderIdAndAnalyzedAtBetween/countByEmotionType
**索引:** (elderId, analyzedAt), (emotionType), (createdAt)

---

### AlertRecord 实体
```java
@Entity @Table(name = "alert_record")
class AlertRecord {
  Long id;                    // 主键
  Long elderId;               // 老人ID [FK → Elder]
  AlertType alertType;        // 类型: NEGATIVE_EMOTION|ABNORMAL_BEHAVIOR
  Severity severity;          // 严重度: LOW|MEDIUM|HIGH|CRITICAL
  String message;             // 内容 [1-500]
  AlertStatus status;         // 状态: PENDING|HANDLED|IGNORED [default: PENDING]
  Long handledBy;             // 处理人 [optional, FK → User]
  LocalDateTime handledAt;    // 处理时间 [optional]
  String handleNote;          // 处理备注 [optional, 0-1000]
  LocalDateTime createdAt;    // 创建时间 [自动]
}
```
**关键方法:** findByElderIdAndStatus/findByElderIdAndStatusOrderByCreatedAtDesc
**索引:** (elderId, status, createdAt), (severity)

---

### AuditLog 实体 (不可变)
```java
@Entity @Table(name = "audit_log")
@Immutable  // 仅支持INSERT，不允许UPDATE/DELETE
class AuditLog {
  Long id;                    // 主键
  Long userId;                // 用户ID [FK → User]
  String action;              // 操作: LOGIN|LOGOUT|CREATE_ELDER|UPDATE_ELDER 等 [1-100]
  String resourceType;        // 资源类型: USER|ELDER|EMOTION_RECORD 等 [1-50]
  Long resourceId;            // 资源ID [optional]
  String ipAddress;           // IP地址 [optional, 0-50]
  String details;             // JSON详情 [optional, 0-1000]
  LocalDateTime createdAt;    // 创建时间 [自动，不可更新]
}
```
**关键方法:** findByUserIdAndCreatedAtBetween
**约束:** @Immutable 只读实体
**索引:** (userId, createdAt), (action, createdAt)

---

### SystemConfig 实体
```java
@Entity @Table(name = "system_config",
  uniqueConstraints = @UniqueConstraint(columnNames = "config_key"))
class SystemConfig {
  Long id;                    // 主键
  String configKey;           // 键 [unique, 1-100]
  String configValue;         // 值 [1-500]
  String description;         // 描述 [optional, 0-200]
  LocalDateTime updatedAt;    // 更新时间 [自动]
}
```
**初始配置:**
```
alert.threshold.negative_emotion = 0.7
alert.threshold.critical_emotion = 0.95
notification.email.enabled = false
notification.sms.enabled = false
ai.service.provider = baidu
data.retention.days = 365
```

---

## 枚举速查表

| 枚举类 | 值 | 用途 |
|--------|-----|------|
| **UserRole** | GUARDIAN, CAREGIVER, ADMIN | 用户角色权限 |
| **UserStatus** | ACTIVE, INACTIVE, PENDING_APPROVAL | 用户账户状态 |
| **Gender** | MALE, FEMALE, OTHER | 性别分类 |
| **AlertType** | NEGATIVE_EMOTION, ABNORMAL_BEHAVIOR | 预警分类 |
| **AlertStatus** | PENDING, HANDLED, IGNORED | 预警处理状态 |
| **Severity** | LOW, MEDIUM, HIGH, CRITICAL | 预警严重程度 |
| **EmotionType** | HAPPY, CALM, SAD, ANXIOUS, ANGRY | 情感分类 |
| **EmotionDataSource** | VOICE, IMAGE, VIDEO, TEXT, SENSOR | 数据来源 |

---

## 字段验证规则速查

| 字段 | 验证规则 | 错误消息 |
|------|---------|---------|
| User.username | @NotNull, @Size(3-50) | "用户名不能为空/长度3-50字符" |
| User.phone | @Pattern("^1[3-9]\\d{9}$") | "手机号格式不正确" |
| User.email | @Email | "邮箱格式不正确" |
| Elder.name | @NotNull, @Size(1-50) | "姓名不能为空/长度1-50字符" |
| Elder.birthDate | @NotNull, @Past | "出生日期必须是过去的日期" |
| EmotionRecord.confidenceScore | @DecimalMin(0.0), @DecimalMax(1.0) | "置信度范围0.0-1.0" |
| AlertRecord.message | @NotNull, @Size(1-500) | "预警消息必填/长度1-500字符" |

---

## 外键关系图

```
User ← ┬─ ElderGuardian → Elder
       │                   │
       │                   ├─ EmotionRecord
       │                   └─ AlertRecord ← (handledBy)
       │
       └─ AuditLog

SystemConfig (独立)
```

---

## 常用查询模式

### User 查询
```java
// 通过手机登录
Optional<User> user = userRepo.findByPhone("13812345678");

// 通过邮箱登录
Optional<User> user = userRepo.findByEmail("user@example.com");

// 检查用户名是否存在
boolean exists = userRepo.existsByPhone("13812345678");
```

### Elder 查询
```java
// 查询老人信息
Optional<Elder> elder = elderRepo.findById(1L);

// 查询隐私保护的老人
Optional<Elder> elder = elderRepo.findByIdAndPrivacyEnabled(1L, true);
```

### ElderGuardian 查询
```java
// 查询关系
Optional<ElderGuardian> rel = egRepo.findByElderIdAndGuardianId(1L, 2L);

// 查询已授权的监护人
List<ElderGuardian> guardians = egRepo.findByElderIdAndAuthorized(1L, true);

// 检查是否已授权
boolean authorized = egRepo.existsByElderIdAndGuardianIdAndAuthorized(1L, 2L, true);
```

### EmotionRecord 查询
```java
// 查询时间范围内的情感记录
List<EmotionRecord> records = erRepo.findByElderIdAndAnalyzedAtBetween(
  1L, startTime, endTime
);

// 分页查询
Page<EmotionRecord> page = erRepo.findByElderIdOrderByAnalyzedAtDesc(
  1L, PageRequest.of(0, 20)
);

// 统计特定情感的出现次数
long count = erRepo.countByElderIdAndEmotionTypeAndAnalyzedAtBetween(
  1L, EmotionType.SAD, startTime, endTime
);
```

### AlertRecord 查询
```java
// 查询待处理预警
List<AlertRecord> alerts = arRepo.findByElderIdAndStatus(1L, AlertStatus.PENDING);

// 分页查询已处理预警（按时间倒序）
Page<AlertRecord> page = arRepo.findByElderIdAndStatusOrderByCreatedAtDesc(
  1L, AlertStatus.HANDLED, PageRequest.of(0, 20)
);

// 统计最近24小时的预警数
long count = arRepo.countByElderIdAndStatusAndCreatedAtAfter(
  1L, AlertStatus.PENDING, LocalDateTime.now().minusHours(24)
);
```

### AuditLog 查询
```java
// 查询用户的操作日志（时间范围）
List<AuditLog> logs = alRepo.findByUserIdAndCreatedAtBetween(
  userId, startTime, endTime, PageRequest.of(0, 50)
);

// 查询特定操作的所有日志
List<AuditLog> logs = alRepo.findByActionAndCreatedAtAfter(
  "LOGIN", LocalDateTime.now().minusDays(7)
);
```

### SystemConfig 查询
```java
// 获取配置
Optional<SystemConfig> config = scRepo.findByConfigKey("alert.threshold.negative_emotion");

// 获取配置值
String threshold = config.map(SystemConfig::getConfigValue).orElse("0.7");
```

---

## 创建实体对象示例

### 创建User
```java
User user = User.builder()
  .username("john_doe")
  .phone("13812345678")
  .email("john@example.com")
  .passwordHash(passwordEncoder.encode("password123"))
  .role(UserRole.GUARDIAN)
  .status(UserStatus.ACTIVE)
  .build();
userRepository.save(user);
```

### 创建Elder
```java
Elder elder = Elder.builder()
  .name("张三")
  .gender(Gender.MALE)
  .birthDate(LocalDate.of(1960, 1, 1))
  .healthStatus("高血压，已控制")
  .privacyEnabled(true)
  .build();
elderRepository.save(elder);
```

### 创建ElderGuardian
```java
ElderGuardian relation = ElderGuardian.builder()
  .elderId(elder.getId())
  .guardianId(user.getId())
  .relationship("子女")
  .authorized(false)
  .build();
egRepository.save(relation);
```

### 创建EmotionRecord
```java
EmotionRecord record = EmotionRecord.builder()
  .elderId(elder.getId())
  .emotionType(EmotionType.SAD)
  .confidenceScore(0.85)
  .dataSource(EmotionDataSource.VOICE)
  .rawDataUrl("encrypted://path/to/data")
  .analyzedAt(LocalDateTime.now())
  .build();
erRepository.save(record);
```

### 创建AlertRecord
```java
AlertRecord alert = AlertRecord.builder()
  .elderId(elder.getId())
  .alertType(AlertType.NEGATIVE_EMOTION)
  .severity(Severity.HIGH)
  .message("检测到持续的悲伤情绪")
  .status(AlertStatus.PENDING)
  .build();
arRepository.save(alert);
```

---

## 权限控制矩阵

```
┌─────────────────────────────────────────────────────────────┐
│ 角色权限矩阵                                               │
├──────────────┬──────────────┬──────────────┬────────────────┤
│ 操作         │ ADMIN        │ CAREGIVER    │ GUARDIAN       │
├──────────────┼──────────────┼──────────────┼────────────────┤
│ User CRUD    │ ✅ 全部      │ ✗            │ ✗              │
│ Elder CRUD   │ ✅ 全部      │ ✅ 分配的    │ ✅ 授权的      │
│ Emotion读    │ ✅ 全部      │ ✅ 分配的    │ ✅ 授权的      │
│ Alert 读     │ ✅ 全部      │ ✅ 分配的    │ ✅ 授权的      │
│ Alert 处理   │ ✅ 全部      │ ✅ 全部      │ ✅ 全部        │
│ 审计日志     │ ✅ 全部      │ ✗            │ ✗              │
│ 系统配置     │ ✅ 全部      │ ✗            │ ✗              │
└──────────────┴──────────────┴──────────────┴────────────────┘
```

---

## 生成文档位置

- 📄 `docs/Task8_JPA实体类_验收报告.md` - 完整验收清单
- 📊 `docs/实体关系模型_ERD.md` - ERD关系图和SQL脚本
- 📝 `docs/COMPLETION_SUMMARY_Task8.md` - 完成总结
- 📋 `docs/Task8_实体类快速参考.md` - 本文件（快速参考）

---

## 相关类路径

```
backend/src/main/java/top/publicnote/eldermoodai/backend/
├── entity/
│   ├── User.java
│   ├── Elder.java
│   ├── ElderGuardian.java
│   ├── EmotionRecord.java
│   ├── AlertRecord.java
│   ├── AuditLog.java
│   └── SystemConfig.java
└── enums/
    ├── UserRole.java
    ├── UserStatus.java
    ├── Gender.java
    ├── AlertType.java
    ├── AlertStatus.java
    ├── Severity.java
    ├── EmotionType.java
    └── EmotionDataSource.java
```

---

## 常见问题

**Q: 如何创建新用户？**  
A: 使用 User.builder()，设置必填字段(username, passwordHash, role)，其他字段为可选。

**Q: ElderGuardian 中的 authorized 字段什么时候为 true？**  
A: 初始创建时为 false，需要 Elder 所有者或 Admin 手动授权。

**Q: 为什么 AuditLog 使用 @Immutable？**  
A: 审计日志是追踪记录，不应被修改，以保证审计完整性。

**Q: 如何查询某个老人的所有授权监护人？**  
A: 使用 `egRepository.findByElderIdAndAuthorized(elderId, true)`

**Q: EmotionRecord 的 confidenceScore 范围是多少？**  
A: 0.0 到 1.0，代表 AI 分析的置信度。

**Q: 预警严重程度是如何计算的？**  
A: 根据 confidenceScore：<0.7为LOW, 0.7-0.85为MEDIUM, 0.85-0.95为HIGH, >=0.95为CRITICAL

---

*最后更新: 2024-06-23*  
*Task 8 - JPA实体类完成*
