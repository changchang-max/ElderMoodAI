# Design Document: ElderMoodAI Backend System

## Overview

ElderMoodAI（居家老人情感分析及可视化系统）后端系统是一个基于SpringBoot 4.0.6的企业级应用，旨在为居家老人提供多模态情感分析、实时预警、数据可视化和隐私保护功能。系统采用分层架构设计，集成MySQL持久化存储、Redis缓存加速、外部AI服务调用（百度/阿里云/讯飞）、JWT身份认证和AES-256数据加密。核心设计遵循隐私优先、权限分层、测试驱动开发（TDD）和简单性（YAGNI）原则，确保老年用户敏感数据的安全性和系统的可维护性。

系统支持三种用户角色（家属/护理员/管理员），实现基于角色的访问控制（RBAC），确保数据访问的最小权限原则。通过SSE（Server-Sent Events）实现实时预警推送，通过审计日志记录所有敏感操作，满足合规性要求。

## Architecture

### System Architecture Diagram

```mermaid
graph TB
    subgraph "Client Layer"
        WebUI[Web Frontend<br/>Vue3 + Element Plus]
        MobileUI[Mobile App<br/>Optional]
    end
    
    subgraph "API Gateway Layer"
        Gateway[API Gateway<br/>Spring MVC Controllers]
        Auth[Authentication Filter<br/>JWT Validation]
        RBAC[Authorization Filter<br/>Role-Based Access Control]
    end
    
    subgraph "Service Layer"
        UserService[User Service<br/>用户管理]
        ElderService[Elder Service<br/>老人信息管理]
        EmotionService[Emotion Service<br/>情感分析]
        AlertService[Alert Service<br/>预警管理]
        DataService[Data Visualization Service<br/>数据可视化]
        AuditService[Audit Service<br/>审计日志]
    end
    
    subgraph "Integration Layer"
        AIClient[AI Service Client<br/>百度/阿里云/讯飞]
        SseEmitter[SSE Emitter<br/>实时推送]
        EmailClient[Email Client<br/>邮件通知]
    end
    
    subgraph "Data Layer"
        MySQL[(MySQL Database<br/>持久化存储)]
        Redis[(Redis Cache<br/>缓存层)]
    end
    
    subgraph "Security Layer"
        Encryption[AES-256 Encryption<br/>数据加密]
        JWTManager[JWT Manager<br/>Token生成与验证]
    end
    
    WebUI --> Gateway
    MobileUI --> Gateway
    Gateway --> Auth
    Auth --> RBAC
    RBAC --> UserService
    RBAC --> ElderService
    RBAC --> EmotionService
    RBAC --> AlertService
    RBAC --> DataService
    
    UserService --> MySQL
    ElderService --> MySQL
    EmotionService --> MySQL
    AlertService --> MySQL
    DataService --> MySQL
    AuditService --> MySQL
    
    UserService --> Redis
    ElderService --> Redis
    EmotionService --> Redis
    
    EmotionService --> AIClient
    AlertService --> SseEmitter
    AlertService --> EmailClient
    
    UserService --> JWTManager
    EmotionService --> Encryption
    AuditService --> Encryption
```


### Module Division

系统划分为以下核心模块：

1. **认证与授权模块（Authentication & Authorization）**
   - JWT Token生成与验证
   - 用户登录/注册
   - 基于角色的访问控制（RBAC）
   - 权限拦截器

2. **用户管理模块（User Management）**
   - 用户CRUD操作
   - 角色分配与权限管理
   - 账号审核与禁用

3. **老人信息管理模块（Elder Management）**
   - 老人基本信息管理
   - 监护人关系绑定
   - 隐私授权管理

4. **情感分析模块（Emotion Analysis）**
   - 多模态数据采集（语音/图像/视频/文本）
   - 外部AI服务调用
   - 情感分析结果存储与查询

5. **预警管理模块（Alert Management）**
   - 负面情绪自动预警
   - 实时推送（SSE）
   - 预警历史记录

6. **数据可视化模块（Data Visualization）**
   - 情感趋势统计
   - 情感占比分析
   - 健康评分计算

7. **审计日志模块（Audit Logging）**
   - 操作日志记录
   - 敏感数据访问追踪
   - 日志查询与导出

8. **系统配置模块（System Configuration）**
   - 预警阈值配置
   - 推送方式设置
   - 系统参数管理


### Data Flow

#### User Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant UserService
    participant JWTManager
    participant MySQL
    participant Redis
    
    Client->>AuthController: POST /api/auth/login<br/>{phone, password}
    AuthController->>UserService: authenticate(credentials)
    UserService->>MySQL: SELECT * FROM user WHERE phone=?
    MySQL-->>UserService: User entity
    UserService->>UserService: validatePassword(inputPassword, storedHash)
    
    alt Password Valid
        UserService->>JWTManager: generateToken(userId, role)
        JWTManager-->>UserService: JWT Token
        UserService->>Redis: SET token:{userId} = token (TTL: 7 days)
        UserService-->>AuthController: AuthResponse(token, userInfo)
        AuthController-->>Client: 200 OK {token, user}
    else Password Invalid
        UserService-->>AuthController: AuthenticationException
        AuthController-->>Client: 401 Unauthorized
    end
```


#### Emotion Analysis Flow

```mermaid
sequenceDiagram
    participant Client
    participant EmotionController
    participant EmotionService
    participant AIClient
    participant EncryptionService
    participant MySQL
    participant AlertService
    participant SseEmitter
    
    Client->>EmotionController: POST /api/emotion/analyze<br/>{elderId, dataType, rawData}
    EmotionController->>EmotionService: analyzeEmotion(request)
    EmotionService->>AIClient: callAIService(rawData, dataType)
    AIClient-->>EmotionService: AIResponse(emotion, confidence)
    
    EmotionService->>EncryptionService: encrypt(rawData)
    EncryptionService-->>EmotionService: encryptedData
    
    EmotionService->>MySQL: INSERT INTO emotion_record
    MySQL-->>EmotionService: recordId
    
    alt Negative Emotion Detected
        EmotionService->>AlertService: checkAndCreateAlert(elderId, emotion)
        AlertService->>MySQL: INSERT INTO alert_record
        AlertService->>SseEmitter: pushAlert(guardianIds, alertInfo)
        SseEmitter-->>Client: SSE Event: New Alert
    end
    
    EmotionService-->>EmotionController: EmotionAnalysisResult
    EmotionController-->>Client: 200 OK {emotion, confidence, recordId}
```


#### Authorization Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthFilter
    participant JWTManager
    participant Redis
    participant RBACFilter
    participant Controller
    
    Client->>AuthFilter: Request with Authorization Header
    AuthFilter->>JWTManager: validateToken(token)
    
    alt Token Valid
        JWTManager->>Redis: GET token:{userId}
        Redis-->>JWTManager: Cached token
        
        alt Token Matches
            JWTManager-->>AuthFilter: UserContext(userId, role)
            AuthFilter->>RBACFilter: checkPermission(role, resource)
            
            alt Permission Granted
                RBACFilter->>Controller: Forward request
                Controller-->>Client: Response
            else Permission Denied
                RBACFilter-->>Client: 403 Forbidden
            end
        else Token Mismatch
            JWTManager-->>AuthFilter: TokenExpiredException
            AuthFilter-->>Client: 401 Unauthorized
        end
    else Token Invalid
        JWTManager-->>AuthFilter: InvalidTokenException
        AuthFilter-->>Client: 401 Unauthorized
    end
```


## Components and Interfaces

### Component 1: Authentication Service

**Purpose**: 处理用户认证、JWT Token生成与验证、会话管理

**Interface**:
```java
public interface AuthenticationService {
    /**
     * 用户登录认证
     * @param credentials 登录凭证（手机号/邮箱 + 密码）
     * @return 认证响应（包含JWT Token和用户信息）
     * @throws AuthenticationException 认证失败时抛出
     */
    AuthResponse authenticate(LoginCredentials credentials) throws AuthenticationException;
    
    /**
     * 用户注册
     * @param request 注册请求（包含用户基本信息）
     * @return 注册响应（包含用户ID）
     * @throws RegistrationException 注册失败时抛出
     */
    RegisterResponse register(RegisterRequest request) throws RegistrationException;
    
    /**
     * 验证JWT Token
     * @param token JWT Token字符串
     * @return 用户上下文信息
     * @throws InvalidTokenException Token无效时抛出
     */
    UserContext validateToken(String token) throws InvalidTokenException;
    
    /**
     * 用户登出
     * @param userId 用户ID
     */
    void logout(Long userId);
}
```

**Responsibilities**:
- 验证用户登录凭证（密码哈希比对）
- 生成和验证JWT Token
- 管理用户会话（Redis缓存）
- 处理用户注册流程
- 记录认证相关审计日志


### Component 2: Elder Management Service

**Purpose**: 管理老人基本信息、监护人关系、隐私授权

**Interface**:
```java
public interface ElderManagementService {
    /**
     * 创建老人信息
     * @param request 老人信息创建请求
     * @param operatorId 操作人ID
     * @return 老人信息响应
     * @throws PermissionDeniedException 无权限时抛出
     */
    ElderResponse createElder(CreateElderRequest request, Long operatorId) 
        throws PermissionDeniedException;
    
    /**
     * 更新老人信息
     * @param elderId 老人ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @return 更新后的老人信息
     * @throws ElderNotFoundException 老人不存在时抛出
     * @throws PermissionDeniedException 无权限时抛出
     */
    ElderResponse updateElder(Long elderId, UpdateElderRequest request, Long operatorId)
        throws ElderNotFoundException, PermissionDeniedException;
    
    /**
     * 绑定监护人关系
     * @param elderId 老人ID
     * @param guardianId 监护人ID
     * @param relationship 关系类型（子女/配偶/护理员等）
     * @param operatorId 操作人ID
     * @throws ElderNotFoundException 老人不存在时抛出
     * @throws UserNotFoundException 监护人不存在时抛出
     */
    void bindGuardian(Long elderId, Long guardianId, String relationship, Long operatorId)
        throws ElderNotFoundException, UserNotFoundException;
    
    /**
     * 查询老人信息（带权限校验）
     * @param elderId 老人ID
     * @param requesterId 请求人ID
     * @return 老人信息
     * @throws PermissionDeniedException 无权限时抛出
     */
    ElderResponse getElderInfo(Long elderId, Long requesterId) 
        throws PermissionDeniedException;
    
    /**
     * 更新隐私授权状态
     * @param elderId 老人ID
     * @param privacyEnabled 是否启用隐私保护
     * @param operatorId 操作人ID
     */
    void updatePrivacyStatus(Long elderId, boolean privacyEnabled, Long operatorId);
}
```

**Responsibilities**:
- 老人信息的CRUD操作
- 监护人关系管理
- 隐私授权状态控制
- 权限校验（确保只有授权用户可访问老人数据）
- 记录老人信息变更审计日志


### Component 3: Emotion Analysis Service

**Purpose**: 处理多模态情感分析、调用外部AI服务、存储分析结果

**Interface**:
```java
public interface EmotionAnalysisService {
    /**
     * 分析情感（多模态）
     * @param request 情感分析请求（包含老人ID、数据类型、原始数据）
     * @param operatorId 操作人ID
     * @return 情感分析结果
     * @throws AIServiceException AI服务调用失败时抛出
     * @throws PermissionDeniedException 无权限时抛出
     */
    EmotionAnalysisResult analyzeEmotion(EmotionAnalysisRequest request, Long operatorId)
        throws AIServiceException, PermissionDeniedException;
    
    /**
     * 查询情感记录
     * @param elderId 老人ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param requesterId 请求人ID
     * @return 情感记录列表
     * @throws PermissionDeniedException 无权限时抛出
     */
    List<EmotionRecord> queryEmotionRecords(Long elderId, LocalDateTime startTime, 
        LocalDateTime endTime, Long requesterId) throws PermissionDeniedException;
    
    /**
     * 获取情感统计数据
     * @param elderId 老人ID
     * @param period 统计周期（日/周/月）
     * @param requesterId 请求人ID
     * @return 情感统计结果
     */
    EmotionStatistics getEmotionStatistics(Long elderId, StatisticsPeriod period, 
        Long requesterId) throws PermissionDeniedException;
}
```

**Responsibilities**:
- 调用外部AI服务进行情感分析
- 加密存储原始数据（AES-256）
- 存储情感分析结果
- 触发预警检查（负面情绪）
- 提供情感数据查询接口
- 缓存热点数据（Redis）


### Component 4: Alert Management Service

**Purpose**: 管理预警规则、生成预警、实时推送

**Interface**:
```java
public interface AlertManagementService {
    /**
     * 检查并创建预警
     * @param elderId 老人ID
     * @param emotionType 情感类型
     * @param confidenceScore 置信度
     * @return 预警记录（如果触发预警）
     */
    Optional<AlertRecord> checkAndCreateAlert(Long elderId, EmotionType emotionType, 
        Double confidenceScore);
    
    /**
     * 查询预警记录
     * @param elderId 老人ID
     * @param status 预警状态（待处理/已处理/已忽略）
     * @param requesterId 请求人ID
     * @return 预警记录列表
     */
    List<AlertRecord> queryAlerts(Long elderId, AlertStatus status, Long requesterId)
        throws PermissionDeniedException;
    
    /**
     * 处理预警
     * @param alertId 预警ID
     * @param handlerId 处理人ID
     * @param handleNote 处理备注
     */
    void handleAlert(Long alertId, Long handlerId, String handleNote)
        throws AlertNotFoundException, PermissionDeniedException;
    
    /**
     * 订阅实时预警推送（SSE）
     * @param userId 用户ID
     * @return SSE Emitter
     */
    SseEmitter subscribeAlerts(Long userId);
}
```

**Responsibilities**:
- 根据预警规则检查情感数据
- 创建预警记录
- 通过SSE推送实时预警
- 发送邮件/短信通知（可选）
- 管理预警处理状态
- 记录预警处理历史


### Component 5: Data Visualization Service

**Purpose**: 提供数据可视化API、统计分析、趋势计算

**Interface**:
```java
public interface DataVisualizationService {
    /**
     * 获取情感趋势数据
     * @param elderId 老人ID
     * @param period 时间周期（日/周/月）
     * @param requesterId 请求人ID
     * @return 趋势数据（时间序列）
     */
    EmotionTrendData getEmotionTrend(Long elderId, StatisticsPeriod period, 
        Long requesterId) throws PermissionDeniedException;
    
    /**
     * 获取情感占比统计
     * @param elderId 老人ID
     * @param period 时间周期
     * @param requesterId 请求人ID
     * @return 情感占比数据
     */
    EmotionDistributionData getEmotionDistribution(Long elderId, StatisticsPeriod period,
        Long requesterId) throws PermissionDeniedException;
    
    /**
     * 计算健康评分
     * @param elderId 老人ID
     * @param requesterId 请求人ID
     * @return 健康评分（0-100）
     */
    HealthScore calculateHealthScore(Long elderId, Long requesterId)
        throws PermissionDeniedException;
    
    /**
     * 获取情感热力图数据
     * @param elderId 老人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param requesterId 请求人ID
     * @return 热力图数据
     */
    HeatmapData getEmotionHeatmap(Long elderId, LocalDate startDate, LocalDate endDate,
        Long requesterId) throws PermissionDeniedException;
}
```

**Responsibilities**:
- 聚合情感数据生成趋势图
- 计算情感占比统计
- 计算健康评分（基于情感历史）
- 生成热力图数据
- 缓存统计结果（Redis）
- 提供数据导出功能


### Component 6: Audit Logging Service

**Purpose**: 记录所有敏感操作、提供审计日志查询

**Interface**:
```java
public interface AuditLoggingService {
    /**
     * 记录审计日志
     * @param log 审计日志对象
     */
    void log(AuditLog log);
    
    /**
     * 查询审计日志
     * @param criteria 查询条件（用户ID、操作类型、时间范围等）
     * @param requesterId 请求人ID（仅管理员可查询）
     * @return 审计日志列表
     * @throws PermissionDeniedException 无权限时抛出
     */
    Page<AuditLog> queryLogs(AuditLogQueryCriteria criteria, Long requesterId)
        throws PermissionDeniedException;
    
    /**
     * 导出审计日志
     * @param criteria 查询条件
     * @param requesterId 请求人ID
     * @return 导出文件路径
     */
    String exportLogs(AuditLogQueryCriteria criteria, Long requesterId)
        throws PermissionDeniedException;
}
```

**Responsibilities**:
- 记录用户登录/登出
- 记录数据访问操作
- 记录权限变更
- 记录敏感数据修改
- 提供日志查询接口（仅管理员）
- 支持日志导出


## Data Models

### Model 1: User

```java
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(unique = true, length = 20)
    private String phone;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // GUARDIAN, CAREGIVER, ADMIN
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status; // ACTIVE, INACTIVE, PENDING_APPROVAL
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**Validation Rules**:
- username: 必填，唯一，长度3-50字符
- phone: 可选，唯一，符合手机号格式（11位数字）
- email: 可选，唯一，符合邮箱格式
- phone和email至少提供一个
- passwordHash: 必填，使用BCrypt加密
- role: 必填，枚举值（GUARDIAN/CAREGIVER/ADMIN）
- status: 必填，默认PENDING_APPROVAL


### Model 2: Elder

```java
@Entity
@Table(name = "elder")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Elder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender; // MALE, FEMALE, OTHER
    
    @Column(nullable = false)
    private LocalDate birthDate;
    
    @Column(length = 500)
    private String healthStatus;
    
    @Column(nullable = false)
    private Boolean privacyEnabled = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**Validation Rules**:
- name: 必填，长度2-50字符
- gender: 必填，枚举值（MALE/FEMALE/OTHER）
- birthDate: 必填，必须早于当前日期，年龄范围60-120岁
- healthStatus: 可选，最大500字符
- privacyEnabled: 必填，默认true


### Model 3: ElderGuardian

```java
@Entity
@Table(name = "elder_guardian")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElderGuardian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long elderId;
    
    @Column(nullable = false)
    private Long guardianId;
    
    @Column(nullable = false, length = 50)
    private String relationship; // 子女、配偶、护理员等
    
    @Column(nullable = false)
    private Boolean authorized = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Validation Rules**:
- elderId: 必填，必须存在于elder表
- guardianId: 必填，必须存在于user表
- relationship: 必填，长度2-50字符
- authorized: 必填，默认false（需要管理员审核）
- 唯一约束：(elderId, guardianId)组合唯一


### Model 4: EmotionRecord

```java
@Entity
@Table(name = "emotion_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long elderId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmotionType emotionType; // HAPPY, CALM, SAD, ANXIOUS, ANGRY
    
    @Column(nullable = false)
    private Double confidenceScore; // 0.0 - 1.0
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DataSource dataSource; // VOICE, IMAGE, VIDEO, TEXT, SENSOR
    
    @Column(length = 500)
    private String rawDataUrl; // 加密后的原始数据存储路径
    
    @Column(nullable = false)
    private LocalDateTime analyzedAt;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Validation Rules**:
- elderId: 必填，必须存在于elder表
- emotionType: 必填，枚举值（HAPPY/CALM/SAD/ANXIOUS/ANGRY）
- confidenceScore: 必填，范围0.0-1.0
- dataSource: 必填，枚举值（VOICE/IMAGE/VIDEO/TEXT/SENSOR）
- rawDataUrl: 可选，存储加密后的数据路径
- analyzedAt: 必填，AI分析完成时间


### Model 5: AlertRecord

```java
@Entity
@Table(name = "alert_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long elderId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertType alertType; // NEGATIVE_EMOTION, ABNORMAL_BEHAVIOR
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status; // PENDING, HANDLED, IGNORED
    
    @Column
    private Long handledBy;
    
    @Column
    private LocalDateTime handledAt;
    
    @Column(length = 1000)
    private String handleNote;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Validation Rules**:
- elderId: 必填，必须存在于elder表
- alertType: 必填，枚举值（NEGATIVE_EMOTION/ABNORMAL_BEHAVIOR）
- severity: 必填，枚举值（LOW/MEDIUM/HIGH/CRITICAL）
- message: 必填，长度10-500字符
- status: 必填，默认PENDING
- handledBy: 可选，处理时必填
- handledAt: 可选，处理时自动填充
- handleNote: 可选，最大1000字符


### Model 6: AuditLog

```java
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String action; // LOGIN, LOGOUT, CREATE_ELDER, UPDATE_ELDER, etc.
    
    @Column(nullable = false, length = 50)
    private String resourceType; // USER, ELDER, EMOTION_RECORD, etc.
    
    @Column
    private Long resourceId;
    
    @Column(length = 50)
    private String ipAddress;
    
    @Column(length = 1000)
    private String details; // JSON格式的详细信息
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

**Validation Rules**:
- userId: 必填，必须存在于user表
- action: 必填，长度5-100字符
- resourceType: 必填，长度3-50字符
- resourceId: 可选，根据操作类型决定
- ipAddress: 可选，符合IP地址格式
- details: 可选，JSON格式，最大1000字符
- createdAt: 必填，自动填充


### Model 7: SystemConfig

```java
@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String configKey;
    
    @Column(nullable = false, length = 500)
    private String configValue;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**Validation Rules**:
- configKey: 必填，唯一，长度5-100字符
- configValue: 必填，长度1-500字符
- description: 可选，最大200字符
- updatedAt: 必填，自动更新

**预定义配置项**:
- `alert.threshold.negative_emotion`: 负面情绪预警阈值（0.0-1.0）
- `alert.threshold.critical_emotion`: 严重情绪预警阈值（0.0-1.0）
- `notification.email.enabled`: 是否启用邮件通知（true/false）
- `notification.sms.enabled`: 是否启用短信通知（true/false）
- `ai.service.provider`: AI服务提供商（baidu/aliyun/xunfei）
- `data.retention.days`: 数据保留天数（默认365）


## Main Algorithm/Workflow

### User Authentication Algorithm

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant UserRepository
    participant PasswordEncoder
    participant JWTManager
    participant RedisCache
    participant AuditService
    
    Client->>AuthController: POST /api/auth/login
    AuthController->>AuthService: authenticate(credentials)
    
    AuthService->>UserRepository: findByPhoneOrEmail(identifier)
    UserRepository-->>AuthService: User entity
    
    alt User Not Found
        AuthService-->>AuthController: throw UserNotFoundException
        AuthController-->>Client: 401 Unauthorized
    end
    
    AuthService->>PasswordEncoder: matches(rawPassword, encodedPassword)
    PasswordEncoder-->>AuthService: boolean result
    
    alt Password Invalid
        AuthService->>AuditService: log(LOGIN_FAILED)
        AuthService-->>AuthController: throw InvalidPasswordException
        AuthController-->>Client: 401 Unauthorized
    end
    
    AuthService->>JWTManager: generateToken(userId, role)
    JWTManager-->>AuthService: JWT token
    
    AuthService->>RedisCache: set(token:userId, token, TTL=7days)
    AuthService->>AuditService: log(LOGIN_SUCCESS)
    
    AuthService-->>AuthController: AuthResponse(token, userInfo)
    AuthController-->>Client: 200 OK
```


## Key Functions with Formal Specifications

### Function 1: authenticate()

```java
public AuthResponse authenticate(LoginCredentials credentials) 
    throws AuthenticationException
```

**Preconditions:**
- `credentials` is non-null
- `credentials.identifier` (phone or email) is non-empty string
- `credentials.password` is non-empty string
- Database connection is available

**Postconditions:**
- If authentication succeeds:
  - Returns valid `AuthResponse` with non-null JWT token
  - JWT token is stored in Redis with 7-day TTL
  - Audit log entry is created with action=LOGIN_SUCCESS
  - User status must be ACTIVE
- If authentication fails:
  - Throws `AuthenticationException` with descriptive message
  - Audit log entry is created with action=LOGIN_FAILED
  - No token is generated or stored
- No side effects on user password or status

**Loop Invariants:** N/A (no loops in this function)

**Security Constraints:**
- Password comparison must use constant-time algorithm (BCrypt)
- Failed login attempts must not reveal whether user exists
- All exceptions must use generic error messages to prevent user enumeration


### Function 2: analyzeEmotion()

```java
public EmotionAnalysisResult analyzeEmotion(EmotionAnalysisRequest request, Long operatorId)
    throws AIServiceException, PermissionDeniedException
```

**Preconditions:**
- `request` is non-null and validated
- `request.elderId` exists in database
- `request.dataType` is valid enum value (VOICE/IMAGE/VIDEO/TEXT/SENSOR)
- `request.rawData` is non-empty
- `operatorId` has permission to access elder data
- AI service is available and configured

**Postconditions:**
- If analysis succeeds:
  - Returns `EmotionAnalysisResult` with emotion type and confidence score
  - `result.emotionType` is valid enum value
  - `result.confidenceScore` is in range [0.0, 1.0]
  - Raw data is encrypted using AES-256 before storage
  - `EmotionRecord` is persisted to database
  - If negative emotion detected (SAD/ANXIOUS/ANGRY) with confidence > threshold:
    - Alert is created and pushed via SSE
  - Audit log entry is created
- If analysis fails:
  - Throws `AIServiceException` with error details
  - No database records are created
  - Audit log entry is created with failure reason
- If permission denied:
  - Throws `PermissionDeniedException`
  - No AI service call is made
  - Audit log entry is created

**Loop Invariants:** N/A (no loops in main logic)

**Performance Constraints:**
- AI service call timeout: 30 seconds
- Total function execution time: < 35 seconds
- Encryption operation: < 1 second


### Function 3: checkAndCreateAlert()

```java
public Optional<AlertRecord> checkAndCreateAlert(Long elderId, EmotionType emotionType, 
    Double confidenceScore)
```

**Preconditions:**
- `elderId` is non-null and exists in database
- `emotionType` is non-null and valid enum value
- `confidenceScore` is non-null and in range [0.0, 1.0]
- System configuration for alert thresholds is loaded

**Postconditions:**
- If alert should be triggered:
  - Returns `Optional.of(AlertRecord)` with created alert
  - `AlertRecord` is persisted to database with status=PENDING
  - Alert severity is calculated based on emotion type and confidence
  - SSE event is pushed to all authorized guardians
  - Email/SMS notification is sent if enabled in system config
- If alert should not be triggered:
  - Returns `Optional.empty()`
  - No database records are created
  - No notifications are sent
- No side effects on emotion records or elder data

**Loop Invariants:**
- When iterating through guardians for notification:
  - All previously notified guardians have valid authorization
  - Notification state remains consistent

**Alert Triggering Rules:**
- Negative emotions (SAD/ANXIOUS/ANGRY):
  - confidence >= 0.7: Create alert with severity=MEDIUM
  - confidence >= 0.85: Create alert with severity=HIGH
  - confidence >= 0.95: Create alert with severity=CRITICAL
- Positive emotions (HAPPY/CALM):
  - No alert is created


### Function 4: validateToken()

```java
public UserContext validateToken(String token) throws InvalidTokenException
```

**Preconditions:**
- `token` is non-null and non-empty string
- Redis connection is available

**Postconditions:**
- If token is valid:
  - Returns `UserContext` with userId, username, and role
  - `UserContext.userId` is non-null and exists in database
  - `UserContext.role` is valid enum value
  - Token expiration is checked against current time
  - Token in Redis matches provided token
- If token is invalid:
  - Throws `InvalidTokenException` with reason (expired/malformed/not found)
  - No user context is created
  - Audit log entry is created for invalid token attempt
- No side effects on token storage or user data

**Loop Invariants:** N/A (no loops in this function)

**Security Constraints:**
- Token signature must be verified using HMAC-SHA256
- Token expiration must be strictly enforced
- Token must exist in Redis (prevents replay attacks after logout)
- Constant-time comparison for token matching


## Algorithmic Pseudocode

### Main Emotion Analysis Algorithm

```java
ALGORITHM analyzeEmotionWorkflow(request, operatorId)
INPUT: request of type EmotionAnalysisRequest, operatorId of type Long
OUTPUT: result of type EmotionAnalysisResult

BEGIN
  // Step 1: Validate permissions
  ASSERT hasPermission(operatorId, request.elderId)
  
  elder ← elderRepository.findById(request.elderId)
  IF elder IS NULL THEN
    THROW ElderNotFoundException
  END IF
  
  IF NOT elder.privacyEnabled THEN
    LOG_AUDIT(operatorId, "PRIVACY_DISABLED_ACCESS", request.elderId)
  END IF
  
  // Step 2: Call AI service for emotion analysis
  TRY
    aiResponse ← aiServiceClient.analyze(request.rawData, request.dataType)
    ASSERT aiResponse.emotionType IS VALID
    ASSERT aiResponse.confidenceScore >= 0.0 AND aiResponse.confidenceScore <= 1.0
  CATCH AIServiceException e
    LOG_AUDIT(operatorId, "AI_SERVICE_FAILED", request.elderId)
    THROW e
  END TRY
  
  // Step 3: Encrypt raw data
  encryptedDataUrl ← encryptionService.encrypt(request.rawData)
  
  // Step 4: Save emotion record
  emotionRecord ← NEW EmotionRecord(
    elderId: request.elderId,
    emotionType: aiResponse.emotionType,
    confidenceScore: aiResponse.confidenceScore,
    dataSource: request.dataType,
    rawDataUrl: encryptedDataUrl,
    analyzedAt: NOW()
  )
  savedRecord ← emotionRepository.save(emotionRecord)
  
  // Step 5: Check and create alert if needed
  alertOptional ← checkAndCreateAlert(
    request.elderId, 
    aiResponse.emotionType, 
    aiResponse.confidenceScore
  )
  
  // Step 6: Update cache
  cacheService.invalidate("emotion:trend:" + request.elderId)
  
  // Step 7: Log audit
  LOG_AUDIT(operatorId, "EMOTION_ANALYZED", savedRecord.id)
  
  RETURN EmotionAnalysisResult(
    recordId: savedRecord.id,
    emotionType: aiResponse.emotionType,
    confidenceScore: aiResponse.confidenceScore,
    alertCreated: alertOptional.isPresent()
  )
END
```

**Preconditions:**
- request is validated and non-null
- operatorId has valid permission to access elder data
- AI service is configured and available
- Database and Redis connections are active

**Postconditions:**
- Emotion record is persisted to database
- Raw data is encrypted before storage
- Alert is created if negative emotion threshold is exceeded
- Cache is invalidated for affected elder
- Audit log entry is created

**Loop Invariants:** N/A (no explicit loops in main algorithm)


### Permission Validation Algorithm

```java
ALGORITHM validatePermission(userId, elderId, requiredAction)
INPUT: userId of type Long, elderId of type Long, requiredAction of type String
OUTPUT: isAuthorized of type boolean

BEGIN
  // Step 1: Load user and check status
  user ← userRepository.findById(userId)
  IF user IS NULL OR user.status != ACTIVE THEN
    RETURN false
  END IF
  
  // Step 2: Admin has full access
  IF user.role == ADMIN THEN
    RETURN true
  END IF
  
  // Step 3: Check guardian relationship
  relationship ← elderGuardianRepository.findByElderIdAndGuardianId(elderId, userId)
  IF relationship IS NULL THEN
    RETURN false
  END IF
  
  // Step 4: Check authorization status
  IF NOT relationship.authorized THEN
    RETURN false
  END IF
  
  // Step 5: Check action-specific permissions
  IF requiredAction == "DELETE" AND user.role != ADMIN THEN
    RETURN false
  END IF
  
  IF requiredAction == "MODIFY_PRIVACY" THEN
    IF user.role != ADMIN AND relationship.relationship != "PRIMARY_GUARDIAN" THEN
      RETURN false
    END IF
  END IF
  
  // All checks passed
  RETURN true
END
```

**Preconditions:**
- userId is non-null and valid
- elderId is non-null and valid
- requiredAction is non-null string
- Database connection is available

**Postconditions:**
- Returns true if and only if user has permission for the action
- No side effects on user or elder data
- Permission check is consistent with RBAC rules

**Loop Invariants:** N/A (no loops in this algorithm)


### Alert Creation and Notification Algorithm

```java
ALGORITHM checkAndCreateAlertWorkflow(elderId, emotionType, confidenceScore)
INPUT: elderId of type Long, emotionType of type EmotionType, confidenceScore of type Double
OUTPUT: alertOptional of type Optional<AlertRecord>

BEGIN
  // Step 1: Load alert threshold configuration
  config ← systemConfigRepository.findByKey("alert.threshold.negative_emotion")
  threshold ← parseDouble(config.configValue) // Default: 0.7
  
  // Step 2: Check if emotion is negative
  IF emotionType NOT IN [SAD, ANXIOUS, ANGRY] THEN
    RETURN Optional.empty()
  END IF
  
  // Step 3: Check if confidence exceeds threshold
  IF confidenceScore < threshold THEN
    RETURN Optional.empty()
  END IF
  
  // Step 4: Calculate severity
  severity ← calculateSeverity(emotionType, confidenceScore)
  
  // Step 5: Create alert record
  alert ← NEW AlertRecord(
    elderId: elderId,
    alertType: NEGATIVE_EMOTION,
    severity: severity,
    message: formatAlertMessage(emotionType, confidenceScore),
    status: PENDING,
    createdAt: NOW()
  )
  savedAlert ← alertRepository.save(alert)
  
  // Step 6: Find all authorized guardians
  guardians ← elderGuardianRepository.findByElderIdAndAuthorized(elderId, true)
  
  // Step 7: Push notifications to guardians
  FOR each guardian IN guardians DO
    ASSERT guardian.authorized == true
    
    // SSE push
    sseEmitter.send(guardian.guardianId, savedAlert)
    
    // Email notification (if enabled)
    IF isEmailEnabled() THEN
      emailService.sendAlertEmail(guardian.guardianId, savedAlert)
    END IF
  END FOR
  
  // Step 8: Log audit
  LOG_AUDIT(SYSTEM_USER_ID, "ALERT_CREATED", savedAlert.id)
  
  RETURN Optional.of(savedAlert)
END

FUNCTION calculateSeverity(emotionType, confidenceScore)
  IF confidenceScore >= 0.95 THEN
    RETURN CRITICAL
  ELSE IF confidenceScore >= 0.85 THEN
    RETURN HIGH
  ELSE IF confidenceScore >= 0.7 THEN
    RETURN MEDIUM
  ELSE
    RETURN LOW
  END IF
END FUNCTION
```

**Preconditions:**
- elderId exists in database
- emotionType is valid enum value
- confidenceScore is in range [0.0, 1.0]
- System configuration is loaded

**Postconditions:**
- If alert is created:
  - AlertRecord is persisted with status=PENDING
  - All authorized guardians receive SSE notification
  - Email notifications are sent if enabled
  - Audit log entry is created
- If alert is not created:
  - No database records are created
  - No notifications are sent

**Loop Invariants:**
- All previously notified guardians have authorized=true
- Notification state remains consistent throughout iteration
- No duplicate notifications are sent to the same guardian


## Example Usage

### Example 1: User Login

```java
// Client request
POST /api/auth/login
Content-Type: application/json

{
  "identifier": "13800138000",
  "password": "SecurePassword123!"
}

// Controller handling
@PostMapping("/api/auth/login")
public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginCredentials credentials) {
    try {
        AuthResponse response = authenticationService.authenticate(credentials);
        return ResponseEntity.ok(response);
    } catch (AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Authentication failed"));
    }
}

// Service implementation
@Override
public AuthResponse authenticate(LoginCredentials credentials) throws AuthenticationException {
    // Find user by phone or email
    User user = userRepository.findByPhoneOrEmail(credentials.getIdentifier())
        .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
    
    // Validate password
    if (!passwordEncoder.matches(credentials.getPassword(), user.getPasswordHash())) {
        auditService.log(new AuditLog(user.getId(), "LOGIN_FAILED", "USER", user.getId()));
        throw new AuthenticationException("Invalid credentials");
    }
    
    // Check user status
    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new AuthenticationException("Account is not active");
    }
    
    // Generate JWT token
    String token = jwtManager.generateToken(user.getId(), user.getRole());
    
    // Store token in Redis
    redisTemplate.opsForValue().set(
        "token:" + user.getId(), 
        token, 
        7, 
        TimeUnit.DAYS
    );
    
    // Log successful login
    auditService.log(new AuditLog(user.getId(), "LOGIN_SUCCESS", "USER", user.getId()));
    
    return new AuthResponse(token, user.toUserInfo());
}
```


### Example 2: Emotion Analysis

```java
// Client request
POST /api/emotion/analyze
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "elderId": 1,
  "dataType": "VOICE",
  "rawData": "base64_encoded_audio_data..."
}

// Controller handling
@PostMapping("/api/emotion/analyze")
public ResponseEntity<EmotionAnalysisResult> analyzeEmotion(
    @RequestBody @Valid EmotionAnalysisRequest request,
    @AuthenticationPrincipal UserContext userContext) {
    
    try {
        EmotionAnalysisResult result = emotionService.analyzeEmotion(
            request, 
            userContext.getUserId()
        );
        return ResponseEntity.ok(result);
    } catch (PermissionDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (AIServiceException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("AI service temporarily unavailable"));
    }
}

// Service implementation
@Override
public EmotionAnalysisResult analyzeEmotion(EmotionAnalysisRequest request, Long operatorId) 
    throws AIServiceException, PermissionDeniedException {
    
    // Validate permission
    if (!permissionService.hasPermission(operatorId, request.getElderId(), "ANALYZE")) {
        throw new PermissionDeniedException("No permission to analyze this elder's data");
    }
    
    // Call AI service
    AIResponse aiResponse = aiServiceClient.analyze(
        request.getRawData(), 
        request.getDataType()
    );
    
    // Encrypt raw data
    String encryptedUrl = encryptionService.encrypt(request.getRawData());
    
    // Save emotion record
    EmotionRecord record = new EmotionRecord();
    record.setElderId(request.getElderId());
    record.setEmotionType(aiResponse.getEmotionType());
    record.setConfidenceScore(aiResponse.getConfidenceScore());
    record.setDataSource(request.getDataType());
    record.setRawDataUrl(encryptedUrl);
    record.setAnalyzedAt(LocalDateTime.now());
    
    EmotionRecord saved = emotionRepository.save(record);
    
    // Check and create alert
    Optional<AlertRecord> alert = alertService.checkAndCreateAlert(
        request.getElderId(),
        aiResponse.getEmotionType(),
        aiResponse.getConfidenceScore()
    );
    
    // Invalidate cache
    cacheService.evict("emotion:trend:" + request.getElderId());
    
    // Log audit
    auditService.log(new AuditLog(
        operatorId, 
        "EMOTION_ANALYZED", 
        "EMOTION_RECORD", 
        saved.getId()
    ));
    
    return new EmotionAnalysisResult(
        saved.getId(),
        aiResponse.getEmotionType(),
        aiResponse.getConfidenceScore(),
        alert.isPresent()
    );
}
```


### Example 3: Real-time Alert Subscription (SSE)

```java
// Client request
GET /api/alerts/subscribe
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: text/event-stream

// Controller handling
@GetMapping(value = "/api/alerts/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribeAlerts(@AuthenticationPrincipal UserContext userContext) {
    return alertService.subscribeAlerts(userContext.getUserId());
}

// Service implementation
@Override
public SseEmitter subscribeAlerts(Long userId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    
    // Store emitter in concurrent map
    activeEmitters.put(userId, emitter);
    
    // Handle completion and timeout
    emitter.onCompletion(() -> activeEmitters.remove(userId));
    emitter.onTimeout(() -> activeEmitters.remove(userId));
    emitter.onError((e) -> activeEmitters.remove(userId));
    
    // Send initial connection message
    try {
        emitter.send(SseEmitter.event()
            .name("connected")
            .data("Alert subscription established"));
    } catch (IOException e) {
        emitter.completeWithError(e);
    }
    
    return emitter;
}

// Push alert to subscribers
public void pushAlert(Long elderId, AlertRecord alert) {
    // Find all guardians for this elder
    List<ElderGuardian> guardians = elderGuardianRepository
        .findByElderIdAndAuthorized(elderId, true);
    
    for (ElderGuardian guardian : guardians) {
        SseEmitter emitter = activeEmitters.get(guardian.getGuardianId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("alert")
                    .data(alert));
            } catch (IOException e) {
                activeEmitters.remove(guardian.getGuardianId());
            }
        }
    }
}
```


## Correctness Properties

### Property 1: Authentication Security

**Universal Quantification:**
```
∀ user ∈ Users, ∀ credentials ∈ LoginCredentials:
  authenticate(credentials) succeeds ⟹ 
    (credentials.password matches user.passwordHash) ∧
    (user.status = ACTIVE) ∧
    (generated token is stored in Redis) ∧
    (audit log entry exists with action=LOGIN_SUCCESS)
```

**Invariant:**
- No user can authenticate with incorrect password
- Inactive users cannot obtain valid tokens
- All successful authentications are logged

**Validates: Requirements 1.1, 1.2, 1.7, 1.8, 1.9, 9.1, 9.2**

### Property 2: Permission Enforcement

**Universal Quantification:**
```
∀ user ∈ Users, ∀ elder ∈ Elders, ∀ operation ∈ Operations:
  canAccess(user, elder, operation) ⟹
    (user.role = ADMIN) ∨
    (∃ relationship ∈ ElderGuardian: 
      relationship.elderId = elder.id ∧
      relationship.guardianId = user.id ∧
      relationship.authorized = true)
```

**Invariant:**
- Only authorized users can access elder data
- Admin users have unrestricted access
- All data access attempts are validated before execution

**Validates: Requirements 3.5, 3.6, 4.9, 10.1, 10.2, 10.3, 10.5, 10.6, 10.10**

### Property 3: Data Encryption

**Universal Quantification:**
```
∀ emotionRecord ∈ EmotionRecords:
  emotionRecord.rawDataUrl ≠ null ⟹
    isEncrypted(emotionRecord.rawDataUrl, AES-256) = true
```

**Invariant:**
- All raw emotion data is encrypted before storage
- Encryption algorithm is AES-256
- Decryption requires valid encryption key

**Validates: Requirements 5.6, 11.1, 11.2, 11.3, 11.4, 11.6, 11.9, 11.10**


### Property 4: Alert Triggering

**Universal Quantification:**
```
∀ emotionRecord ∈ EmotionRecords:
  (emotionRecord.emotionType ∈ {SAD, ANXIOUS, ANGRY}) ∧
  (emotionRecord.confidenceScore >= alertThreshold) ⟹
    ∃ alert ∈ AlertRecords:
      alert.elderId = emotionRecord.elderId ∧
      alert.status = PENDING ∧
      alert.createdAt ≥ emotionRecord.analyzedAt
```

**Invariant:**
- All negative emotions above threshold trigger alerts
- Alerts are created immediately after emotion analysis
- Alert severity is proportional to confidence score

**Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.10**

### Property 5: Audit Logging

**Universal Quantification:**
```
∀ sensitiveOperation ∈ {LOGIN, LOGOUT, CREATE_ELDER, UPDATE_ELDER, 
                         ANALYZE_EMOTION, HANDLE_ALERT}:
  operationExecuted(sensitiveOperation, user, resource) ⟹
    ∃ log ∈ AuditLogs:
      log.userId = user.id ∧
      log.action = sensitiveOperation ∧
      log.resourceId = resource.id ∧
      log.createdAt = operationTimestamp
```

**Invariant:**
- All sensitive operations are logged
- Audit logs are immutable (no updates or deletes)
- Audit logs include user, action, resource, and timestamp

**Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9, 9.10, 9.11**

### Property 6: Token Validity

**Universal Quantification:**
```
∀ token ∈ JWTTokens:
  validateToken(token) succeeds ⟹
    (token.signature is valid) ∧
    (token.expirationTime > currentTime) ∧
    (∃ cachedToken ∈ Redis: cachedToken = token) ∧
    (token.userId exists in Users)
```

**Invariant:**
- Only valid, non-expired tokens pass validation
- Tokens must exist in Redis (prevents replay after logout)
- Token signature is cryptographically verified

**Validates: Requirements 1.3, 1.4, 1.5, 1.8, 1.10**

### Property 7: Input Validation

**Universal Quantification:**
```
∀ input ∈ UserInputs:
  (input.type = USERNAME ⟹ 3 ≤ length(input.value) ≤ 50) ∧
  (input.type = PHONE ⟹ length(input.value) = 11 ∧ isNumeric(input.value)) ∧
  (input.type = EMAIL ⟹ isValidEmailFormat(input.value)) ∧
  (input.type = PASSWORD ⟹ 
    length(input.value) ≥ 8 ∧
    hasUppercase(input.value) ∧
    hasLowercase(input.value) ∧
    hasDigit(input.value)) ∧
  (input.type = ELDER_NAME ⟹ 2 ≤ length(input.value) ≤ 50) ∧
  (input.type = ELDER_AGE ⟹ 60 ≤ calculateAge(input.value) ≤ 120)
```

**Invariant:**
- All user inputs are validated before processing
- Invalid inputs are rejected with descriptive error messages
- Validation rules are consistently enforced across all endpoints

**Validates: Requirements 2.1, 2.2, 2.4, 2.5, 3.1, 3.2, 3.3, 4.2, 16.6**

### Property 8: Data Uniqueness

**Universal Quantification:**
```
∀ user1, user2 ∈ Users:
  (user1.id ≠ user2.id) ⟹
    (user1.phone ≠ user2.phone ∨ user1.phone = null ∨ user2.phone = null) ∧
    (user1.email ≠ user2.email ∨ user1.email = null ∨ user2.email = null)

∀ relationship1, relationship2 ∈ ElderGuardian:
  (relationship1.id ≠ relationship2.id) ⟹
    (relationship1.elderId ≠ relationship2.elderId ∨ 
     relationship1.guardianId ≠ relationship2.guardianId)
```

**Invariant:**
- Phone numbers are unique across all users (when not null)
- Email addresses are unique across all users (when not null)
- Elder-Guardian relationships are unique per (elderId, guardianId) pair

**Validates: Requirements 2.3, 4.5**

### Property 9: Emotion Analysis Workflow

**Universal Quantification:**
```
∀ analysisRequest ∈ EmotionAnalysisRequests:
  processAnalysis(analysisRequest) succeeds ⟹
    (∃ emotionRecord ∈ EmotionRecords:
      emotionRecord.elderId = analysisRequest.elderId ∧
      emotionRecord.emotionType ∈ {HAPPY, CALM, SAD, ANXIOUS, ANGRY} ∧
      0.0 ≤ emotionRecord.confidenceScore ≤ 1.0 ∧
      emotionRecord.dataSource = analysisRequest.dataType ∧
      isEncrypted(emotionRecord.rawDataUrl)) ∧
    (∃ auditLog ∈ AuditLogs:
      auditLog.action = "EMOTION_ANALYZED" ∧
      auditLog.resourceId = emotionRecord.id)
```

**Invariant:**
- Every successful emotion analysis creates an emotion record
- Emotion type is always one of the five valid types
- Confidence score is always between 0.0 and 1.0
- Raw data is always encrypted before storage
- Every analysis is logged in audit logs

**Validates: Requirements 5.1, 5.2, 5.4, 5.5, 5.6, 5.7, 5.10**

### Property 10: Alert Severity Calculation

**Universal Quantification:**
```
∀ emotionRecord ∈ EmotionRecords:
  (emotionRecord.emotionType ∈ {SAD, ANXIOUS, ANGRY}) ⟹
    (emotionRecord.confidenceScore ≥ 0.95 ⟹ alert.severity = CRITICAL) ∧
    (0.85 ≤ emotionRecord.confidenceScore < 0.95 ⟹ alert.severity = HIGH) ∧
    (0.7 ≤ emotionRecord.confidenceScore < 0.85 ⟹ alert.severity = MEDIUM) ∧
    (emotionRecord.confidenceScore < 0.7 ⟹ ¬∃ alert)
```

**Invariant:**
- Alert severity is deterministically calculated from confidence score
- Confidence scores below 0.7 do not trigger alerts
- Higher confidence scores result in higher severity levels

**Validates: Requirements 6.1, 6.2, 6.3, 6.4**

### Property 11: Cache Consistency

**Universal Quantification:**
```
∀ elder ∈ Elders:
  updateElder(elder) ⟹
    ¬∃ cachedElder ∈ RedisCache: cachedElder.id = elder.id

∀ emotionRecord ∈ EmotionRecords:
  createEmotionRecord(emotionRecord) ⟹
    ¬∃ cachedTrend ∈ RedisCache: 
      cachedTrend.elderId = emotionRecord.elderId

∀ relationship ∈ ElderGuardian:
  updateAuthorization(relationship) ⟹
    ¬∃ cachedPermission ∈ RedisCache:
      cachedPermission.userId = relationship.guardianId ∧
      cachedPermission.elderId = relationship.elderId
```

**Invariant:**
- Cache is invalidated when underlying data changes
- Stale cache entries are never served after data updates
- Cache invalidation is atomic with data updates

**Validates: Requirements 3.10, 4.7, 5.8, 8.8, 13.10**

### Property 12: Guardian Relationship Management

**Universal Quantification:**
```
∀ elder ∈ Elders, ∀ guardian ∈ Users:
  bindGuardian(elder, guardian) ⟹
    (∃ relationship ∈ ElderGuardian:
      relationship.elderId = elder.id ∧
      relationship.guardianId = guardian.id ∧
      relationship.authorized = false ∧
      relationship.createdAt ≤ currentTime)

∀ relationship ∈ ElderGuardian:
  authorizeRelationship(relationship) ⟹
    relationship.authorized = true ∧
    (∃ auditLog ∈ AuditLogs:
      auditLog.action = "AUTHORIZE_GUARDIAN" ∧
      auditLog.resourceId = relationship.id)
```

**Invariant:**
- New guardian relationships default to unauthorized
- Authorization requires explicit admin action
- All authorization changes are logged
- Multiple guardians can be bound to a single elder

**Validates: Requirements 4.1, 4.3, 4.4, 4.6, 4.8, 4.10**

### Property 13: Data Visualization Consistency

**Universal Quantification:**
```
∀ elder ∈ Elders, ∀ period ∈ {DAY, WEEK, MONTH}:
  getEmotionDistribution(elder, period) ⟹
    (∑(distribution.values) = 100.0) ∧
    (∀ emotionType ∈ {HAPPY, CALM, SAD, ANXIOUS, ANGRY}:
      emotionType ∈ distribution.keys)

∀ elder ∈ Elders:
  getHealthScore(elder) ⟹
    0 ≤ healthScore ≤ 100
```

**Invariant:**
- Emotion distribution percentages always sum to 100%
- All five emotion types are included in distribution
- Health score is always between 0 and 100
- Visualization data is consistent with underlying emotion records

**Validates: Requirements 8.2, 8.3, 8.4, 8.5**

### Property 14: Concurrent Operation Safety

**Universal Quantification:**
```
∀ elder ∈ Elders, ∀ user1, user2 ∈ Users:
  (updateElder(elder, user1) ∥ updateElder(elder, user2)) ⟹
    (finalState = update(user1) ∨ finalState = update(user2)) ∧
    ¬(finalState = inconsistentMerge(update(user1), update(user2)))

∀ alert ∈ AlertRecords, ∀ handler1, handler2 ∈ Users:
  (handleAlert(alert, handler1) ∥ handleAlert(alert, handler2)) ⟹
    (alert.handledBy = handler1.id ∨ alert.handledBy = handler2.id) ∧
    ¬(alert.handledBy = null)
```

**Invariant:**
- Concurrent updates to the same elder use optimistic locking
- Concurrent alert handling uses pessimistic locking
- No data corruption occurs from concurrent operations
- Final state is always consistent

**Validates: Requirements 19.1, 19.2, 19.8**

### Property 15: Error Handling Consistency

**Universal Quantification:**
```
∀ operation ∈ Operations, ∀ error ∈ Errors:
  (operation fails with error) ⟹
    (error.httpStatus ∈ {400, 401, 403, 404, 500, 503}) ∧
    (error.message ≠ null) ∧
    (¬containsSensitiveInfo(error.message)) ∧
    (∃ log ∈ ApplicationLogs: log.error = error)
```

**Invariant:**
- All errors return appropriate HTTP status codes
- Error messages never expose sensitive information
- All errors are logged to application logs
- Error responses are consistent across all endpoints

**Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.11**


## Error Handling

### Error Scenario 1: Authentication Failure

**Condition**: User provides invalid credentials (wrong password or non-existent user)

**Response**: 
- Return HTTP 401 Unauthorized
- Generic error message: "Invalid credentials" (no user enumeration)
- Audit log entry with action=LOGIN_FAILED

**Recovery**: 
- User can retry with correct credentials
- No account lockout (to prevent DoS attacks on elderly users)
- Rate limiting applied at API gateway level

### Error Scenario 2: Permission Denied

**Condition**: User attempts to access elder data without authorization

**Response**:
- Return HTTP 403 Forbidden
- Error message: "You do not have permission to access this resource"
- Audit log entry with action=PERMISSION_DENIED

**Recovery**:
- User must request authorization from admin
- Admin can grant permission via ElderGuardian relationship
- No automatic retry mechanism

### Error Scenario 3: AI Service Unavailable

**Condition**: External AI service (Baidu/Aliyun/Xunfei) is down or times out

**Response**:
- Return HTTP 503 Service Unavailable
- Error message: "Emotion analysis service temporarily unavailable"
- Audit log entry with action=AI_SERVICE_FAILED
- Raw data is NOT stored (no analysis result)

**Recovery**:
- Implement retry mechanism with exponential backoff (3 attempts)
- Fallback to alternative AI service provider if configured
- Queue request for later processing if all providers fail
- Notify admin via email if service is down for > 5 minutes


### Error Scenario 4: Database Connection Failure

**Condition**: MySQL database is unreachable or connection pool exhausted

**Response**:
- Return HTTP 500 Internal Server Error
- Error message: "Service temporarily unavailable"
- Log error with stack trace to application logs
- Do NOT expose database details to client

**Recovery**:
- Connection pool automatically retries connection
- Circuit breaker pattern: fail fast after 3 consecutive failures
- Health check endpoint reports degraded status
- Alert admin via monitoring system
- Graceful degradation: read-only operations use Redis cache if available

### Error Scenario 5: Token Expired

**Condition**: User's JWT token has exceeded 7-day TTL

**Response**:
- Return HTTP 401 Unauthorized
- Error message: "Token expired, please login again"
- Remove token from Redis cache

**Recovery**:
- User must re-authenticate to obtain new token
- Frontend automatically redirects to login page
- No automatic token refresh (security requirement)

### Error Scenario 6: Data Encryption Failure

**Condition**: AES-256 encryption fails due to key unavailable or corruption

**Response**:
- Return HTTP 500 Internal Server Error
- Error message: "Failed to process data securely"
- Audit log entry with action=ENCRYPTION_FAILED
- Transaction rollback: no data is stored

**Recovery**:
- Alert admin immediately (critical security issue)
- Verify encryption key configuration
- Check key management service availability
- Do NOT proceed with unencrypted data storage


## Testing Strategy

### Unit Testing Approach

**Test Framework**: JUnit 5 + Mockito

**Coverage Goals**:
- Core business logic: 80%+ line coverage
- Security-critical code (authentication, authorization, encryption): 95%+ coverage
- Service layer: 85%+ coverage
- Repository layer: Basic CRUD tests only

**Key Test Cases**:

1. **Authentication Service Tests**
   - Valid login with phone number
   - Valid login with email
   - Invalid password
   - Non-existent user
   - Inactive user account
   - Token generation and validation
   - Logout clears Redis token

2. **Permission Service Tests**
   - Admin has full access
   - Guardian can access authorized elder
   - Guardian cannot access unauthorized elder
   - Caregiver role permissions
   - Permission denied scenarios

3. **Emotion Analysis Service Tests**
   - Successful emotion analysis
   - AI service timeout handling
   - Data encryption before storage
   - Alert triggering for negative emotions
   - Cache invalidation after analysis

4. **Alert Service Tests**
   - Alert creation for negative emotions
   - Severity calculation based on confidence
   - SSE notification to guardians
   - Email notification (if enabled)
   - Alert handling workflow


### Property-Based Testing Approach

**Property Test Library**: jqwik (Java property-based testing framework)

**Key Properties to Test**:

1. **Authentication Properties**
   ```java
   @Property
   void validCredentialsAlwaysGenerateToken(@ForAll("validUsers") User user, 
                                             @ForAll String password) {
       // Property: Valid credentials always produce a token
       String hashedPassword = passwordEncoder.encode(password);
       user.setPasswordHash(hashedPassword);
       
       AuthResponse response = authService.authenticate(
           new LoginCredentials(user.getPhone(), password)
       );
       
       assertThat(response.getToken()).isNotNull();
       assertThat(jwtManager.validateToken(response.getToken())).isNotNull();
   }
   ```

2. **Permission Properties**
   ```java
   @Property
   void adminAlwaysHasAccess(@ForAll("adminUsers") User admin, 
                              @ForAll("elders") Elder elder) {
       // Property: Admin users always have access to any elder
       boolean hasAccess = permissionService.hasPermission(
           admin.getId(), 
           elder.getId(), 
           "READ"
       );
       
       assertThat(hasAccess).isTrue();
   }
   ```

3. **Encryption Properties**
   ```java
   @Property
   void encryptionIsReversible(@ForAll String rawData) {
       // Property: Encrypted data can always be decrypted to original
       String encrypted = encryptionService.encrypt(rawData);
       String decrypted = encryptionService.decrypt(encrypted);
       
       assertThat(decrypted).isEqualTo(rawData);
   }
   ```

4. **Alert Triggering Properties**
   ```java
   @Property
   void negativeEmotionAboveThresholdTriggersAlert(
       @ForAll("negativeEmotions") EmotionType emotion,
       @ForAll @DoubleRange(min = 0.7, max = 1.0) double confidence) {
       
       // Property: Negative emotions with high confidence always trigger alerts
       Optional<AlertRecord> alert = alertService.checkAndCreateAlert(
           1L, 
           emotion, 
           confidence
       );
       
       assertThat(alert).isPresent();
       assertThat(alert.get().getStatus()).isEqualTo(AlertStatus.PENDING);
   }
   ```


### Integration Testing Approach

**Test Framework**: Spring Boot Test + Testcontainers (MySQL, Redis)

**Integration Test Scenarios**:

1. **End-to-End Authentication Flow**
   - User registration → Email verification → Login → Token validation
   - Test with real MySQL and Redis containers
   - Verify audit logs are created

2. **Emotion Analysis Workflow**
   - Upload emotion data → AI service call (mocked) → Encryption → Database storage → Alert creation → SSE push
   - Test with real database transactions
   - Verify cache invalidation

3. **Permission Boundary Tests**
   - Guardian attempts to access unauthorized elder (should fail)
   - Admin accesses any elder (should succeed)
   - Caregiver role-specific permissions
   - Test with real database relationships

4. **Alert Notification Flow**
   - Negative emotion detected → Alert created → SSE push to multiple guardians → Email sent
   - Test with real SSE emitters
   - Verify notification delivery

5. **Data Visualization API**
   - Query emotion trends with date range
   - Calculate health score based on historical data
   - Test with realistic data sets (100+ emotion records)
   - Verify cache hit/miss behavior

**Test Data Management**:
- Use Testcontainers for isolated database instances
- Seed test data using SQL scripts
- Clean up after each test class
- Use realistic data volumes (not just 1-2 records)


## Performance Considerations

### Response Time Requirements

- **Authentication**: < 500ms (p95)
- **Emotion Analysis**: < 35 seconds (including AI service call)
- **Data Visualization Queries**: < 2 seconds (p95)
- **Alert Creation**: < 1 second
- **SSE Push**: < 100ms

### Caching Strategy

**Redis Cache Usage**:

1. **User Session Cache**
   - Key: `token:{userId}`
   - TTL: 7 days
   - Eviction: Manual on logout

2. **Elder Info Cache**
   - Key: `elder:info:{elderId}`
   - TTL: 1 hour
   - Eviction: On update

3. **Emotion Trend Cache**
   - Key: `emotion:trend:{elderId}:{period}`
   - TTL: 5 minutes
   - Eviction: On new emotion record

4. **Permission Cache**
   - Key: `permission:{userId}:{elderId}`
   - TTL: 10 minutes
   - Eviction: On relationship change

### Database Optimization

**Indexes**:
- `user(phone)` - UNIQUE INDEX
- `user(email)` - UNIQUE INDEX
- `elder_guardian(elder_id, guardian_id)` - UNIQUE INDEX
- `emotion_record(elder_id, analyzed_at)` - COMPOSITE INDEX
- `alert_record(elder_id, status, created_at)` - COMPOSITE INDEX
- `audit_log(user_id, created_at)` - COMPOSITE INDEX

**Query Optimization**:
- Use pagination for large result sets (default page size: 20)
- Limit date range queries to 90 days maximum
- Use database-level aggregation for statistics
- Implement read replicas for reporting queries (future)

### Concurrency Handling

- Use optimistic locking for elder info updates
- Use pessimistic locking for alert handling (prevent duplicate handling)
- Connection pool size: 20 (adjust based on load testing)
- Redis connection pool size: 10


## Security Considerations

### Authentication & Authorization

**JWT Token Security**:
- Algorithm: HMAC-SHA256
- Token expiration: 7 days
- Token stored in Redis (enables revocation)
- Token includes: userId, role, issuedAt, expiresAt
- Secret key: 256-bit random key stored in environment variable

**Password Security**:
- Hashing algorithm: BCrypt with cost factor 12
- Password requirements: Minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 digit
- No password history (to avoid complexity for elderly users)
- No account lockout (to prevent DoS)

**Session Management**:
- Single session per user (new login invalidates old token)
- Logout immediately removes token from Redis
- Token validation on every request

### Data Encryption

**At Rest**:
- Raw emotion data (voice/image/video): AES-256-GCM
- Encryption key: Stored in AWS KMS or environment variable
- Key rotation: Every 90 days (manual process)

**In Transit**:
- All API endpoints: HTTPS only (TLS 1.2+)
- Certificate: Let's Encrypt or commercial CA
- HSTS header enabled

### Input Validation

**Request Validation**:
- Use Spring Validation annotations (@Valid, @NotNull, @Size, etc.)
- Custom validators for phone numbers, emails, dates
- Sanitize all user inputs to prevent XSS
- Parameterized queries to prevent SQL injection

**File Upload Validation**:
- Maximum file size: 10MB for images, 50MB for videos, 5MB for audio
- Allowed MIME types: image/jpeg, image/png, video/mp4, audio/wav, audio/mp3
- Virus scanning: Integrate ClamAV or similar (future enhancement)


### API Security

**Rate Limiting**:
- Authentication endpoints: 5 requests per minute per IP
- Emotion analysis: 10 requests per minute per user
- Data query endpoints: 60 requests per minute per user
- Implement using Spring Cloud Gateway or Bucket4j

**CORS Configuration**:
- Allowed origins: Frontend domain only (no wildcard)
- Allowed methods: GET, POST, PUT, DELETE
- Allowed headers: Authorization, Content-Type
- Credentials: true

**Security Headers**:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `Content-Security-Policy: default-src 'self'`

### Audit & Monitoring

**Audit Logging**:
- Log all authentication attempts (success and failure)
- Log all data access operations
- Log all permission changes
- Log all system configuration changes
- Logs are immutable (append-only)
- Log retention: 90 days minimum

**Security Monitoring**:
- Alert on multiple failed login attempts (> 5 in 5 minutes)
- Alert on permission denied attempts (> 10 in 1 hour)
- Alert on AI service failures (> 3 consecutive failures)
- Alert on database connection failures
- Alert on encryption failures (immediate)

### Compliance

**Privacy Compliance**:
- GDPR-compliant data deletion (right to be forgotten)
- Data export functionality (right to data portability)
- Consent management for data collection
- Privacy policy acceptance required on registration

**Data Retention**:
- Emotion records: 365 days (configurable)
- Audit logs: 90 days minimum
- Alert records: 180 days
- User accounts: Indefinite (until deletion request)


## Dependencies

### Core Dependencies

**Spring Boot Ecosystem**:
- `spring-boot-starter-web` (4.0.6) - REST API framework
- `spring-boot-starter-data-jpa` (4.0.6) - JPA/Hibernate ORM
- `spring-boot-starter-data-redis` (4.0.6) - Redis integration
- `spring-boot-starter-validation` (4.0.6) - Bean validation
- `spring-boot-starter-security` (4.0.6) - Security framework
- `spring-boot-starter-test` (4.0.6) - Testing framework

**Database**:
- `mysql-connector-j` (8.0.33) - MySQL JDBC driver
- `flyway-core` (9.16.0) - Database migration tool

**Security**:
- `jjwt-api` (0.11.5) - JWT token generation/validation
- `jjwt-impl` (0.11.5) - JWT implementation
- `jjwt-jackson` (0.11.5) - JWT JSON processing
- `spring-security-crypto` (6.0.2) - Password encryption (BCrypt)

**Caching**:
- `lettuce-core` (6.2.3) - Redis client (included in spring-boot-starter-data-redis)

**Utilities**:
- `lombok` (1.18.26) - Boilerplate code reduction
- `mapstruct` (1.5.3.Final) - Object mapping
- `commons-lang3` (3.12.0) - Utility functions
- `guava` (31.1-jre) - Google core libraries

**Testing**:
- `junit-jupiter` (5.9.2) - Unit testing framework
- `mockito-core` (5.2.0) - Mocking framework
- `testcontainers` (1.17.6) - Integration testing with containers
- `jqwik` (1.7.3) - Property-based testing
- `rest-assured` (5.3.0) - REST API testing


### External Services

**AI Service Providers** (one of the following):
- **Baidu AI**: 
  - SDK: `baidu-aip-java-sdk` (4.16.14)
  - Services: Speech Recognition, Face Recognition, NLP
  - API Key required
  
- **Aliyun AI**:
  - SDK: `aliyun-java-sdk-core` (4.6.3)
  - Services: Intelligent Speech Interaction, Face Recognition
  - Access Key required
  
- **iFlytek (讯飞)**:
  - SDK: `msc-java-sdk` (custom)
  - Services: Speech Recognition, Emotion Recognition
  - App ID required

**Email Service** (optional):
- `spring-boot-starter-mail` (4.0.6) - Email sending
- SMTP server configuration required

**SMS Service** (optional, future):
- Aliyun SMS SDK or Twilio SDK
- API credentials required

### Development Tools

**Code Quality**:
- `spotbugs-maven-plugin` (4.7.3) - Static analysis
- `jacoco-maven-plugin` (0.8.8) - Code coverage
- `checkstyle` (10.9.3) - Code style enforcement

**API Documentation**:
- `springdoc-openapi-starter-webmvc-ui` (2.1.0) - OpenAPI 3.0 documentation
- Swagger UI included

**Monitoring** (future):
- `spring-boot-starter-actuator` (4.0.6) - Health checks and metrics
- `micrometer-registry-prometheus` (1.10.5) - Prometheus metrics

### Infrastructure Requirements

**Minimum System Requirements**:
- Java 17 or higher
- MySQL 8.0 or higher
- Redis 6.0 or higher
- 2GB RAM minimum (4GB recommended)
- 10GB disk space

**Production Deployment**:
- Application server: Tomcat 10 (embedded in Spring Boot)
- Reverse proxy: Nginx or Apache
- SSL certificate: Let's Encrypt or commercial CA
- Monitoring: Prometheus + Grafana (recommended)
- Log aggregation: ELK Stack or Loki (recommended)


## Package Structure (Low-Level Design)

```
top.publicnote.eldermoodai.backend/
├── config/                          # Configuration classes
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── RedisConfig.java             # Redis configuration
│   ├── JwtConfig.java               # JWT configuration
│   ├── WebMvcConfig.java            # Web MVC configuration
│   └── AIServiceConfig.java         # AI service client configuration
│
├── controller/                      # REST API controllers
│   ├── AuthController.java          # Authentication endpoints
│   ├── UserController.java          # User management endpoints
│   ├── ElderController.java         # Elder management endpoints
│   ├── EmotionController.java       # Emotion analysis endpoints
│   ├── AlertController.java         # Alert management endpoints
│   ├── DataVisualizationController.java  # Data visualization endpoints
│   ├── AuditController.java         # Audit log endpoints
│   └── SystemConfigController.java  # System configuration endpoints
│
├── service/                         # Business logic layer
│   ├── AuthenticationService.java
│   ├── UserService.java
│   ├── ElderManagementService.java
│   ├── EmotionAnalysisService.java
│   ├── AlertManagementService.java
│   ├── DataVisualizationService.java
│   ├── AuditLoggingService.java
│   ├── PermissionService.java
│   ├── EncryptionService.java
│   └── CacheService.java
│
├── service/impl/                    # Service implementations
│   ├── AuthenticationServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── ElderManagementServiceImpl.java
│   ├── EmotionAnalysisServiceImpl.java
│   ├── AlertManagementServiceImpl.java
│   ├── DataVisualizationServiceImpl.java
│   ├── AuditLoggingServiceImpl.java
│   ├── PermissionServiceImpl.java
│   ├── EncryptionServiceImpl.java
│   └── CacheServiceImpl.java
│
├── repository/                      # Data access layer (JPA)
│   ├── UserRepository.java
│   ├── ElderRepository.java
│   ├── ElderGuardianRepository.java
│   ├── EmotionRecordRepository.java
│   ├── AlertRecordRepository.java
│   ├── AuditLogRepository.java
│   └── SystemConfigRepository.java
│
├── entity/                          # JPA entities
│   ├── User.java
│   ├── Elder.java
│   ├── ElderGuardian.java
│   ├── EmotionRecord.java
│   ├── AlertRecord.java
│   ├── AuditLog.java
│   └── SystemConfig.java
│
├── dto/                             # Data Transfer Objects
│   ├── request/
│   │   ├── LoginCredentials.java
│   │   ├── RegisterRequest.java
│   │   ├── CreateElderRequest.java
│   │   ├── UpdateElderRequest.java
│   │   ├── EmotionAnalysisRequest.java
│   │   └── AuditLogQueryCriteria.java
│   └── response/
│       ├── AuthResponse.java
│       ├── ElderResponse.java
│       ├── EmotionAnalysisResult.java
│       ├── EmotionTrendData.java
│       ├── EmotionDistributionData.java
│       ├── HealthScore.java
│       └── ErrorResponse.java
│
├── security/                        # Security components
│   ├── JwtAuthenticationFilter.java # JWT validation filter
│   ├── JwtManager.java              # JWT token generation/validation
│   ├── UserContext.java             # Current user context
│   └── PermissionEvaluator.java     # Permission evaluation logic
│
├── integration/                     # External service integration
│   ├── ai/
│   │   ├── AIServiceClient.java     # AI service interface
│   │   ├── BaiduAIClient.java       # Baidu AI implementation
│   │   ├── AliyunAIClient.java      # Aliyun AI implementation
│   │   └── XunfeiAIClient.java      # Xunfei AI implementation
│   ├── notification/
│   │   ├── EmailService.java        # Email notification
│   │   └── SmsService.java          # SMS notification (future)
│   └── sse/
│       └── SseEmitterManager.java   # SSE connection management
│
├── exception/                       # Custom exceptions
│   ├── AuthenticationException.java
│   ├── PermissionDeniedException.java
│   ├── ElderNotFoundException.java
│   ├── UserNotFoundException.java
│   ├── AIServiceException.java
│   ├── EncryptionException.java
│   └── GlobalExceptionHandler.java  # Global exception handler
│
├── enums/                           # Enumerations
│   ├── UserRole.java                # GUARDIAN, CAREGIVER, ADMIN
│   ├── UserStatus.java              # ACTIVE, INACTIVE, PENDING_APPROVAL
│   ├── Gender.java                  # MALE, FEMALE, OTHER
│   ├── EmotionType.java             # HAPPY, CALM, SAD, ANXIOUS, ANGRY
│   ├── DataSource.java              # VOICE, IMAGE, VIDEO, TEXT, SENSOR
│   ├── AlertType.java               # NEGATIVE_EMOTION, ABNORMAL_BEHAVIOR
│   ├── Severity.java                # LOW, MEDIUM, HIGH, CRITICAL
│   ├── AlertStatus.java             # PENDING, HANDLED, IGNORED
│   └── StatisticsPeriod.java        # DAY, WEEK, MONTH
│
├── util/                            # Utility classes
│   ├── DateTimeUtil.java            # Date/time utilities
│   ├── ValidationUtil.java          # Validation utilities
│   ├── EncryptionUtil.java          # Encryption utilities
│   └── JsonUtil.java                # JSON utilities
│
└── BackendApplication.java          # Spring Boot main class
```


## Core Class Design

### Class 1: JwtManager

```java
@Component
public class JwtManager {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:604800000}") // 7 days in milliseconds
    private Long expirationTime;
    
    /**
     * Generate JWT token for authenticated user
     * 
     * @param userId User ID
     * @param role User role
     * @return JWT token string
     */
    public String generateToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role.name())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    /**
     * Validate JWT token and extract user context
     * 
     * @param token JWT token string
     * @return UserContext containing userId and role
     * @throws InvalidTokenException if token is invalid or expired
     */
    public UserContext validateToken(String token) throws InvalidTokenException {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
            
            Long userId = Long.parseLong(claims.getSubject());
            UserRole role = UserRole.valueOf(claims.get("role", String.class));
            
            return new UserContext(userId, role);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token expired");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }
    
    /**
     * Extract user ID from token without full validation
     * 
     * @param token JWT token string
     * @return User ID
     */
    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
```


### Class 2: EncryptionService

```java
@Service
public class EncryptionServiceImpl implements EncryptionService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        // Initialize secret key from base64-encoded string
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        this.secretKey = new SecretKeySpec(decodedKey, "AES");
    }
    
    /**
     * Encrypt data using AES-256-GCM
     * 
     * @param plainText Plain text data
     * @return Base64-encoded encrypted data with IV prepended
     * @throws EncryptionException if encryption fails
     */
    @Override
    public String encrypt(String plainText) throws EncryptionException {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Encrypt data
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Prepend IV to encrypted data
            byte[] encryptedDataWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedDataWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedDataWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            // Return base64-encoded result
            return Base64.getEncoder().encodeToString(encryptedDataWithIv);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Decrypt data using AES-256-GCM
     * 
     * @param encryptedText Base64-encoded encrypted data with IV prepended
     * @return Decrypted plain text
     * @throws EncryptionException if decryption fails
     */
    @Override
    public String decrypt(String encryptedText) throws EncryptionException {
        try {
            // Decode base64
            byte[] encryptedDataWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedDataWithIv, 0, iv, 0, GCM_IV_LENGTH);
            
            // Extract encrypted data
            byte[] encryptedData = new byte[encryptedDataWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedDataWithIv, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Decrypt data
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }
}
```


### Class 3: PermissionService

```java
@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ElderGuardianRepository elderGuardianRepository;
    
    @Autowired
    private RedisTemplate<String, Boolean> redisTemplate;
    
    /**
     * Check if user has permission to access elder data
     * 
     * @param userId User ID
     * @param elderId Elder ID
     * @param action Action type (READ, WRITE, DELETE, etc.)
     * @return true if user has permission, false otherwise
     */
    @Override
    public boolean hasPermission(Long userId, Long elderId, String action) {
        // Check cache first
        String cacheKey = String.format("permission:%d:%d:%s", userId, elderId, action);
        Boolean cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // Load user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            cacheResult(cacheKey, false);
            return false;
        }
        
        // Admin has full access
        if (user.getRole() == UserRole.ADMIN) {
            cacheResult(cacheKey, true);
            return true;
        }
        
        // Check guardian relationship
        Optional<ElderGuardian> relationship = elderGuardianRepository
            .findByElderIdAndGuardianId(elderId, userId);
        
        if (relationship.isEmpty() || !relationship.get().getAuthorized()) {
            cacheResult(cacheKey, false);
            return false;
        }
        
        // Check action-specific permissions
        boolean hasPermission = checkActionPermission(user, relationship.get(), action);
        cacheResult(cacheKey, hasPermission);
        
        return hasPermission;
    }
    
    private boolean checkActionPermission(User user, ElderGuardian relationship, String action) {
        switch (action) {
            case "DELETE":
                // Only admin can delete
                return user.getRole() == UserRole.ADMIN;
                
            case "MODIFY_PRIVACY":
                // Only admin or primary guardian can modify privacy settings
                return user.getRole() == UserRole.ADMIN || 
                       "PRIMARY_GUARDIAN".equals(relationship.getRelationship());
                
            case "READ":
            case "WRITE":
            case "ANALYZE":
                // All authorized guardians and caregivers can read/write/analyze
                return true;
                
            default:
                return false;
        }
    }
    
    private void cacheResult(String cacheKey, boolean result) {
        redisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Invalidate permission cache for user-elder pair
     * 
     * @param userId User ID
     * @param elderId Elder ID
     */
    @Override
    public void invalidatePermissionCache(Long userId, Long elderId) {
        String pattern = String.format("permission:%d:%d:*", userId, elderId);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```


### Class 4: SseEmitterManager

```java
@Component
public class SseEmitterManager {
    
    private final Map<Long, SseEmitter> activeEmitters = new ConcurrentHashMap<>();
    
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterManager.class);
    
    /**
     * Create and register SSE emitter for user
     * 
     * @param userId User ID
     * @return SseEmitter instance
     */
    public SseEmitter createEmitter(Long userId) {
        // Remove existing emitter if present
        removeEmitter(userId);
        
        // Create new emitter with no timeout
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Register callbacks
        emitter.onCompletion(() -> {
            logger.info("SSE connection completed for user: {}", userId);
            activeEmitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            logger.warn("SSE connection timeout for user: {}", userId);
            activeEmitters.remove(userId);
        });
        
        emitter.onError((e) -> {
            logger.error("SSE connection error for user: {}", userId, e);
            activeEmitters.remove(userId);
        });
        
        // Store emitter
        activeEmitters.put(userId, emitter);
        
        // Send initial connection message
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("Alert subscription established"));
        } catch (IOException e) {
            logger.error("Failed to send initial SSE message", e);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    /**
     * Send alert to user via SSE
     * 
     * @param userId User ID
     * @param alert Alert record
     * @return true if sent successfully, false otherwise
     */
    public boolean sendAlert(Long userId, AlertRecord alert) {
        SseEmitter emitter = activeEmitters.get(userId);
        if (emitter == null) {
            logger.debug("No active SSE connection for user: {}", userId);
            return false;
        }
        
        try {
            emitter.send(SseEmitter.event()
                .name("alert")
                .data(alert));
            logger.info("Alert sent to user {} via SSE", userId);
            return true;
        } catch (IOException e) {
            logger.error("Failed to send alert via SSE to user: {}", userId, e);
            activeEmitters.remove(userId);
            return false;
        }
    }
    
    /**
     * Send alert to multiple users
     * 
     * @param userIds List of user IDs
     * @param alert Alert record
     * @return Number of successful sends
     */
    public int sendAlertToMultiple(List<Long> userIds, AlertRecord alert) {
        int successCount = 0;
        for (Long userId : userIds) {
            if (sendAlert(userId, alert)) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * Remove emitter for user
     * 
     * @param userId User ID
     */
    public void removeEmitter(Long userId) {
        SseEmitter emitter = activeEmitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }
    
    /**
     * Get count of active connections
     * 
     * @return Number of active SSE connections
     */
    public int getActiveConnectionCount() {
        return activeEmitters.size();
    }
}
```


## API Interface Definitions

### Authentication APIs

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    /**
     * User login
     * POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "identifier": "13800138000",  // phone or email
     *   "password": "SecurePassword123!"
     * }
     * 
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "user": {
     *     "id": 1,
     *     "username": "张三",
     *     "role": "GUARDIAN",
     *     "status": "ACTIVE"
     *   }
     * }
     * 
     * Error Responses:
     * - 401 Unauthorized: Invalid credentials
     * - 403 Forbidden: Account not active
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginCredentials credentials);
    
    /**
     * User registration
     * POST /api/auth/register
     * 
     * Request Body:
     * {
     *   "username": "张三",
     *   "phone": "13800138000",
     *   "email": "zhangsan@example.com",
     *   "password": "SecurePassword123!",
     *   "role": "GUARDIAN"
     * }
     * 
     * Response (201 Created):
     * {
     *   "userId": 1,
     *   "message": "Registration successful, pending admin approval"
     * }
     * 
     * Error Responses:
     * - 400 Bad Request: Validation errors
     * - 409 Conflict: Phone or email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request);
    
    /**
     * User logout
     * POST /api/auth/logout
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "message": "Logout successful"
     * }
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal UserContext userContext);
}
```


### Elder Management APIs

```java
@RestController
@RequestMapping("/api/elders")
public class ElderController {
    
    /**
     * Create elder
     * POST /api/elders
     * Authorization: Bearer {token}
     * 
     * Request Body:
     * {
     *   "name": "李奶奶",
     *   "gender": "FEMALE",
     *   "birthDate": "1950-05-15",
     *   "healthStatus": "高血压，糖尿病",
     *   "privacyEnabled": true
     * }
     * 
     * Response (201 Created):
     * {
     *   "id": 1,
     *   "name": "李奶奶",
     *   "gender": "FEMALE",
     *   "age": 74,
     *   "healthStatus": "高血压，糖尿病",
     *   "privacyEnabled": true,
     *   "createdAt": "2024-01-15T10:30:00"
     * }
     */
    @PostMapping
    public ResponseEntity<ElderResponse> createElder(
        @RequestBody @Valid CreateElderRequest request,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Get elder by ID
     * GET /api/elders/{elderId}
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "id": 1,
     *   "name": "李奶奶",
     *   "gender": "FEMALE",
     *   "age": 74,
     *   "healthStatus": "高血压，糖尿病",
     *   "privacyEnabled": true,
     *   "guardians": [
     *     {
     *       "guardianId": 2,
     *       "guardianName": "李明",
     *       "relationship": "子女",
     *       "authorized": true
     *     }
     *   ]
     * }
     * 
     * Error Responses:
     * - 403 Forbidden: No permission to access
     * - 404 Not Found: Elder not found
     */
    @GetMapping("/{elderId}")
    public ResponseEntity<ElderResponse> getElder(
        @PathVariable Long elderId,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Update elder
     * PUT /api/elders/{elderId}
     * Authorization: Bearer {token}
     * 
     * Request Body:
     * {
     *   "name": "李奶奶",
     *   "healthStatus": "高血压，糖尿病，关节炎"
     * }
     * 
     * Response (200 OK): Updated elder object
     */
    @PutMapping("/{elderId}")
    public ResponseEntity<ElderResponse> updateElder(
        @PathVariable Long elderId,
        @RequestBody @Valid UpdateElderRequest request,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Bind guardian to elder
     * POST /api/elders/{elderId}/guardians
     * Authorization: Bearer {token}
     * 
     * Request Body:
     * {
     *   "guardianId": 2,
     *   "relationship": "子女"
     * }
     * 
     * Response (200 OK):
     * {
     *   "message": "Guardian bound successfully, pending authorization"
     * }
     */
    @PostMapping("/{elderId}/guardians")
    public ResponseEntity<MessageResponse> bindGuardian(
        @PathVariable Long elderId,
        @RequestBody @Valid BindGuardianRequest request,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Update privacy status
     * PATCH /api/elders/{elderId}/privacy
     * Authorization: Bearer {token}
     * 
     * Request Body:
     * {
     *   "privacyEnabled": false
     * }
     * 
     * Response (200 OK):
     * {
     *   "message": "Privacy status updated"
     * }
     */
    @PatchMapping("/{elderId}/privacy")
    public ResponseEntity<MessageResponse> updatePrivacyStatus(
        @PathVariable Long elderId,
        @RequestBody @Valid UpdatePrivacyRequest request,
        @AuthenticationPrincipal UserContext userContext);
}
```


### Emotion Analysis APIs

```java
@RestController
@RequestMapping("/api/emotion")
public class EmotionController {
    
    /**
     * Analyze emotion
     * POST /api/emotion/analyze
     * Authorization: Bearer {token}
     * Content-Type: application/json
     * 
     * Request Body:
     * {
     *   "elderId": 1,
     *   "dataType": "VOICE",
     *   "rawData": "base64_encoded_audio_data..."
     * }
     * 
     * Response (200 OK):
     * {
     *   "recordId": 123,
     *   "emotionType": "SAD",
     *   "confidenceScore": 0.85,
     *   "dataSource": "VOICE",
     *   "analyzedAt": "2024-01-15T14:30:00",
     *   "alertCreated": true
     * }
     * 
     * Error Responses:
     * - 403 Forbidden: No permission
     * - 503 Service Unavailable: AI service error
     */
    @PostMapping("/analyze")
    public ResponseEntity<EmotionAnalysisResult> analyzeEmotion(
        @RequestBody @Valid EmotionAnalysisRequest request,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Query emotion records
     * GET /api/emotion/records?elderId=1&startTime=2024-01-01T00:00:00&endTime=2024-01-31T23:59:59
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "records": [
     *     {
     *       "id": 123,
     *       "emotionType": "HAPPY",
     *       "confidenceScore": 0.92,
     *       "dataSource": "IMAGE",
     *       "analyzedAt": "2024-01-15T10:00:00"
     *     },
     *     ...
     *   ],
     *   "total": 150,
     *   "page": 1,
     *   "pageSize": 20
     * }
     */
    @GetMapping("/records")
    public ResponseEntity<Page<EmotionRecordResponse>> queryRecords(
        @RequestParam Long elderId,
        @RequestParam LocalDateTime startTime,
        @RequestParam LocalDateTime endTime,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Get emotion statistics
     * GET /api/emotion/statistics?elderId=1&period=WEEK
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "period": "WEEK",
     *   "startDate": "2024-01-08",
     *   "endDate": "2024-01-14",
     *   "distribution": {
     *     "HAPPY": 45,
     *     "CALM": 30,
     *     "SAD": 15,
     *     "ANXIOUS": 8,
     *     "ANGRY": 2
     *   },
     *   "totalRecords": 100,
     *   "averageConfidence": 0.87
     * }
     */
    @GetMapping("/statistics")
    public ResponseEntity<EmotionStatistics> getStatistics(
        @RequestParam Long elderId,
        @RequestParam StatisticsPeriod period,
        @AuthenticationPrincipal UserContext userContext);
}
```


### Alert Management APIs

```java
@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    /**
     * Subscribe to real-time alerts (SSE)
     * GET /api/alerts/subscribe
     * Authorization: Bearer {token}
     * Accept: text/event-stream
     * 
     * Response: SSE stream
     * 
     * Event Types:
     * - connected: Initial connection message
     * - alert: New alert notification
     * 
     * Example SSE Event:
     * event: alert
     * data: {"id":1,"elderId":1,"alertType":"NEGATIVE_EMOTION","severity":"HIGH",...}
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeAlerts(@AuthenticationPrincipal UserContext userContext);
    
    /**
     * Query alerts
     * GET /api/alerts?elderId=1&status=PENDING
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "alerts": [
     *     {
     *       "id": 1,
     *       "elderId": 1,
     *       "elderName": "李奶奶",
     *       "alertType": "NEGATIVE_EMOTION",
     *       "severity": "HIGH",
     *       "message": "检测到高置信度负面情绪（悲伤，置信度：0.92）",
     *       "status": "PENDING",
     *       "createdAt": "2024-01-15T14:30:00"
     *     },
     *     ...
     *   ],
     *   "total": 25,
     *   "page": 1,
     *   "pageSize": 20
     * }
     */
    @GetMapping
    public ResponseEntity<Page<AlertResponse>> queryAlerts(
        @RequestParam Long elderId,
        @RequestParam(required = false) AlertStatus status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int pageSize,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Handle alert
     * POST /api/alerts/{alertId}/handle
     * Authorization: Bearer {token}
     * 
     * Request Body:
     * {
     *   "handleNote": "已联系家属，情况已改善"
     * }
     * 
     * Response (200 OK):
     * {
     *   "message": "Alert handled successfully"
     * }
     * 
     * Error Responses:
     * - 403 Forbidden: No permission
     * - 404 Not Found: Alert not found
     */
    @PostMapping("/{alertId}/handle")
    public ResponseEntity<MessageResponse> handleAlert(
        @PathVariable Long alertId,
        @RequestBody @Valid HandleAlertRequest request,
        @AuthenticationPrincipal UserContext userContext);
}
```


### Data Visualization APIs

```java
@RestController
@RequestMapping("/api/visualization")
public class DataVisualizationController {
    
    /**
     * Get emotion trend data
     * GET /api/visualization/trend?elderId=1&period=WEEK
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "period": "WEEK",
     *   "dataPoints": [
     *     {
     *       "date": "2024-01-08",
     *       "emotions": {
     *         "HAPPY": 12,
     *         "CALM": 8,
     *         "SAD": 3,
     *         "ANXIOUS": 1,
     *         "ANGRY": 0
     *       },
     *       "averageConfidence": 0.88
     *     },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/trend")
    public ResponseEntity<EmotionTrendData> getEmotionTrend(
        @RequestParam Long elderId,
        @RequestParam StatisticsPeriod period,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Get emotion distribution (pie chart data)
     * GET /api/visualization/distribution?elderId=1&period=MONTH
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "period": "MONTH",
     *   "distribution": [
     *     {"emotionType": "HAPPY", "count": 180, "percentage": 45.0},
     *     {"emotionType": "CALM", "count": 120, "percentage": 30.0},
     *     {"emotionType": "SAD", "count": 60, "percentage": 15.0},
     *     {"emotionType": "ANXIOUS", "count": 32, "percentage": 8.0},
     *     {"emotionType": "ANGRY", "count": 8, "percentage": 2.0}
     *   ],
     *   "totalRecords": 400
     * }
     */
    @GetMapping("/distribution")
    public ResponseEntity<EmotionDistributionData> getEmotionDistribution(
        @RequestParam Long elderId,
        @RequestParam StatisticsPeriod period,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Calculate health score
     * GET /api/visualization/health-score?elderId=1
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "score": 78.5,
     *   "level": "GOOD",
     *   "factors": {
     *     "positiveEmotionRatio": 0.75,
     *     "negativeEmotionRatio": 0.25,
     *     "emotionStability": 0.82,
     *     "recentTrend": "IMPROVING"
     *   },
     *   "calculatedAt": "2024-01-15T15:00:00"
     * }
     */
    @GetMapping("/health-score")
    public ResponseEntity<HealthScore> calculateHealthScore(
        @RequestParam Long elderId,
        @AuthenticationPrincipal UserContext userContext);
    
    /**
     * Get emotion heatmap data
     * GET /api/visualization/heatmap?elderId=1&startDate=2024-01-01&endDate=2024-01-31
     * Authorization: Bearer {token}
     * 
     * Response (200 OK):
     * {
     *   "startDate": "2024-01-01",
     *   "endDate": "2024-01-31",
     *   "heatmapData": [
     *     {"date": "2024-01-01", "hour": 0, "emotionScore": 0.75},
     *     {"date": "2024-01-01", "hour": 1, "emotionScore": 0.80},
     *     ...
     *   ]
     * }
     */
    @GetMapping("/heatmap")
    public ResponseEntity<HeatmapData> getEmotionHeatmap(
        @RequestParam Long elderId,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @AuthenticationPrincipal UserContext userContext);
}
```


## Database Schema (DDL)

```sql
-- User table
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Elder table
CREATE TABLE elder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_date DATE NOT NULL,
    health_status VARCHAR(500),
    privacy_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Elder-Guardian relationship table
CREATE TABLE elder_guardian (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    guardian_id BIGINT NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    authorized BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_elder_guardian (elder_id, guardian_id),
    INDEX idx_elder_id (elder_id),
    INDEX idx_guardian_id (guardian_id),
    INDEX idx_authorized (authorized),
    FOREIGN KEY (elder_id) REFERENCES elder(id) ON DELETE CASCADE,
    FOREIGN KEY (guardian_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Emotion record table
CREATE TABLE emotion_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    emotion_type VARCHAR(20) NOT NULL,
    confidence_score DOUBLE NOT NULL,
    data_source VARCHAR(20) NOT NULL,
    raw_data_url VARCHAR(500),
    analyzed_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder_analyzed (elder_id, analyzed_at),
    INDEX idx_emotion_type (emotion_type),
    INDEX idx_analyzed_at (analyzed_at),
    FOREIGN KEY (elder_id) REFERENCES elder(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alert record table
CREATE TABLE alert_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    handled_by BIGINT,
    handled_at DATETIME,
    handle_note VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder_status_created (elder_id, status, created_at),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (elder_id) REFERENCES elder(id) ON DELETE CASCADE,
    FOREIGN KEY (handled_by) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit log table
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    ip_address VARCHAR(50),
    details VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_action (action),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System config table
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default system configurations
INSERT INTO system_config (config_key, config_value, description) VALUES
('alert.threshold.negative_emotion', '0.7', '负面情绪预警阈值'),
('alert.threshold.critical_emotion', '0.9', '严重情绪预警阈值'),
('notification.email.enabled', 'false', '是否启用邮件通知'),
('notification.sms.enabled', 'false', '是否启用短信通知'),
('ai.service.provider', 'baidu', 'AI服务提供商（baidu/aliyun/xunfei）'),
('data.retention.days', '365', '数据保留天数');
```
