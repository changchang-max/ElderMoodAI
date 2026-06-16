# Implementation Plan: ElderMoodAI Backend System

## Overview

本实施计划将ElderMoodAI后端系统的开发分为10个阶段，遵循TDD（测试驱动开发）原则，每个功能模块先编写测试，再实现功能。系统基于SpringBoot 4.0.6 + MySQL 8.0 + Redis 6.0构建，实现多模态情感分析、实时预警、数据可视化和隐私保护功能。

实施策略：
- 优先构建基础设施和核心安全组件
- 按照依赖关系逐步实现业务模块
- 每个阶段包含单元测试、集成测试和API测试
- 关键节点设置检查点，确保质量
- 标注MVP必需任务，支持快速迭代

## Tasks

### Phase 1: 项目基础设施搭建

- [x] 1. 配置项目基础环境
  - [x] 1.1 配置Maven依赖和项目结构
    - 添加Spring Boot Starter Web、Data JPA、Security、Redis、Validation依赖
    - 添加MySQL Connector、Lombok、JWT库（jjwt-api, jjwt-impl, jjwt-jackson）
    - 添加测试依赖：JUnit 5、Mockito、Spring Boot Test、H2 Database（测试用）
    - 添加Swagger/OpenAPI依赖用于API文档
    - 配置Maven Compiler Plugin支持Java 17
    - _Requirements: 20.1, 20.2, 20.3, 20.11_
    - _MVP: 必需_

  - [ ]* 1.2 编写项目配置测试
    - 测试Spring Boot应用上下文加载成功
    - 测试所有必需的Bean能够正确注入
    - _Requirements: 20.1_

  - [x] 1.3 配置application.properties
    - 配置数据库连接（MySQL 8.0）：URL、用户名、密码、连接池大小20
    - 配置Redis连接：主机、端口、连接池大小10
    - 配置JPA：Hibernate DDL auto、SQL日志、命名策略
    - 配置服务器端口、上下文路径
    - 配置JWT密钥（从环境变量读取）、Token过期时间7天
    - 配置文件上传限制：10MB（图片）、50MB（视频）、5MB（音频）
    - 配置CORS允许的域名、方法、头部
    - _Requirements: 20.9, 13.6, 13.11, 13.12, 16.4_
    - _MVP: 必需_


- [x] 2. 创建数据库Schema和初始化脚本
  - [x] 2.1 创建数据库表结构SQL脚本
    - 创建user表：id, username, phone, email, password_hash, role, status, created_at, updated_at
    - 创建elder表：id, name, gender, birth_date, health_status, privacy_enabled, created_at, updated_at
    - 创建elder_guardian表：id, elder_id, guardian_id, relationship, authorized, created_at
    - 创建emotion_record表：id, elder_id, emotion_type, confidence_score, data_source, raw_data_url, analyzed_at, created_at
    - 创建alert_record表：id, elder_id, alert_type, severity, message, status, handled_by, handled_at, handle_note, created_at
    - 创建audit_log表：id, user_id, action, resource_type, resource_id, ip_address, details, created_at
    - 创建system_config表：id, config_key, config_value, description, updated_at
    - _Requirements: 2.9, 3.8, 4.6, 5.7, 6.12, 9.9, 12.1_
    - _MVP: 必需_

  - [x] 2.2 创建数据库索引
    - user表：唯一索引(phone)、唯一索引(email)
    - elder_guardian表：唯一索引(elder_id, guardian_id)
    - emotion_record表：复合索引(elder_id, analyzed_at)
    - alert_record表：复合索引(elder_id, status, created_at)
    - audit_log表：复合索引(user_id, created_at)
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6_
    - _MVP: 必需_

  - [x] 2.3 创建系统配置初始化数据
    - 插入alert.threshold.negative_emotion = 0.7
    - 插入alert.threshold.critical_emotion = 0.95
    - 插入notification.email.enabled = false
    - 插入notification.sms.enabled = false
    - 插入ai.service.provider = baidu
    - 插入data.retention.days = 365
    - _Requirements: 12.3, 12.4, 12.5, 12.6, 12.7, 12.8_
    - _MVP: 必需_

  - [ ]* 2.4 编写数据库迁移测试
    - 测试Flyway迁移脚本能够成功执行
    - 测试所有表和索引创建成功
    - 测试初始配置数据插入成功
    - _Requirements: 20.10_

- [x] 3. Checkpoint - 基础设施验证
  - 确保Maven构建成功，所有依赖下载完成
  - 确保数据库连接成功，表结构创建完成
  - 确保Redis连接成功
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 2: 核心安全组件实现

- [x] 4. 实现数据加密服务
  - [x] 4.1 创建EncryptionService接口和实现类
    - 定义encrypt(String plainText)方法：使用AES-256-GCM加密
    - 定义decrypt(String encryptedText)方法：解密数据
    - 实现随机12字节IV生成
    - 实现IV与密文拼接后Base64编码
    - 从环境变量读取256位加密密钥
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_
    - _MVP: 必需_

  - [ ]* 4.2 编写加密服务单元测试
    - 测试加密后数据不等于原始数据
    - 测试解密后数据等于原始数据
    - 测试相同数据多次加密结果不同（IV随机性）
    - 测试空字符串加密抛出异常
    - 测试无效密文解密抛出异常
    - _Requirements: 11.1, 11.9_

- [x] 5. 实现JWT Token管理
  - [x] 5.1 创建JWTManager接口和实现类
    - 定义generateToken(Long userId, UserRole role)方法：生成JWT Token
    - 定义validateToken(String token)方法：验证Token并返回UserContext
    - 使用HMAC-SHA256算法签名
    - 设置Token过期时间为7天
    - Token payload包含：userId、role、issuedAt、expiresAt
    - _Requirements: 1.1, 1.3, 1.8, 1.10, 10.10_
    - _MVP: 必需_

  - [ ]* 5.2 编写JWT Manager单元测试
    - 测试生成的Token包含正确的userId和role
    - 测试有效Token验证成功
    - 测试过期Token验证失败并抛出TokenExpiredException
    - 测试篡改Token验证失败并抛出InvalidTokenException
    - 测试Token签名使用HMAC-SHA256算法
    - _Requirements: 1.3, 1.4, 10.10_

- [x] 6. 实现密码加密服务
  - [x] 6.1 创建PasswordEncoder接口和实现类
    - 定义encode(String rawPassword)方法：使用BCrypt加密，cost factor 12
    - 定义matches(String rawPassword, String encodedPassword)方法：验证密码
    - 使用常量时间比较算法防止时序攻击
    - _Requirements: 1.9, 2.5_
    - _MVP: 必需_

  - [ ]* 6.2 编写密码加密服务单元测试
    - 测试相同密码多次加密结果不同（盐随机性）
    - 测试正确密码验证成功
    - 测试错误密码验证失败
    - 测试BCrypt cost factor为12
    - _Requirements: 1.9_

- [x] 7. Checkpoint - 安全组件验证
  - 确保加密解密功能正常
  - 确保JWT Token生成和验证正常
  - 确保密码加密和验证正常
  - 确保所有单元测试通过
  - 如有问题，询问用户


### Phase 3: 数据模型和Repository层

- [ ] 8. 创建JPA实体类
  - [ ] 8.1 创建User实体类
    - 定义字段：id, username, phone, email, passwordHash, role, status, createdAt, updatedAt
    - 添加JPA注解：@Entity, @Table, @Id, @GeneratedValue, @Column
    - 添加Lombok注解：@Data, @NoArgsConstructor, @AllArgsConstructor
    - 添加字段验证注解：@NotNull, @Size, @Email, @Pattern
    - 定义UserRole枚举：GUARDIAN, CAREGIVER, ADMIN
    - 定义UserStatus枚举：ACTIVE, INACTIVE, PENDING_APPROVAL
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.8_
    - _MVP: 必需_

  - [ ] 8.2 创建Elder实体类
    - 定义字段：id, name, gender, birthDate, healthStatus, privacyEnabled, createdAt, updatedAt
    - 添加JPA注解和Lombok注解
    - 添加字段验证注解
    - 定义Gender枚举：MALE, FEMALE, OTHER
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.7_
    - _MVP: 必需_

  - [ ] 8.3 创建ElderGuardian实体类
    - 定义字段：id, elderId, guardianId, relationship, authorized, createdAt
    - 添加JPA注解和Lombok注解
    - 添加唯一约束：@UniqueConstraint(columnNames = {"elder_id", "guardian_id"})
    - _Requirements: 4.1, 4.2, 4.3, 4.5_
    - _MVP: 必需_

  - [ ] 8.4 创建EmotionRecord实体类
    - 定义字段：id, elderId, emotionType, confidenceScore, dataSource, rawDataUrl, analyzedAt, createdAt
    - 定义EmotionType枚举：HAPPY, CALM, SAD, ANXIOUS, ANGRY
    - 定义DataSource枚举：VOICE, IMAGE, VIDEO, TEXT, SENSOR
    - _Requirements: 5.2, 5.4, 5.5, 5.7_
    - _MVP: 必需_

  - [ ] 8.5 创建AlertRecord实体类
    - 定义字段：id, elderId, alertType, severity, message, status, handledBy, handledAt, handleNote, createdAt
    - 定义AlertType枚举：NEGATIVE_EMOTION, ABNORMAL_BEHAVIOR
    - 定义Severity枚举：LOW, MEDIUM, HIGH, CRITICAL
    - 定义AlertStatus枚举：PENDING, HANDLED, IGNORED
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.6, 6.12, 6.13_
    - _MVP: 必需_

  - [ ] 8.6 创建AuditLog实体类
    - 定义字段：id, userId, action, resourceType, resourceId, ipAddress, details, createdAt
    - 添加@Immutable注解（只读实体）
    - _Requirements: 9.1, 9.9, 9.10, 9.11_
    - _MVP: 必需_

  - [ ] 8.7 创建SystemConfig实体类
    - 定义字段：id, configKey, configValue, description, updatedAt
    - 添加唯一约束：@UniqueConstraint(columnNames = {"config_key"})
    - _Requirements: 12.1, 12.2, 12.11, 12.12_
    - _MVP: 必需_

  - [ ]* 8.8 编写实体类验证测试
    - 测试User实体字段验证规则
    - 测试Elder实体字段验证规则
    - 测试ElderGuardian唯一约束
    - 测试EmotionRecord字段验证规则
    - 测试AlertRecord字段验证规则
    - _Requirements: 2.1, 3.1, 4.5, 5.5, 6.12_


- [ ] 9. 创建Repository接口
  - [ ] 9.1 创建UserRepository接口
    - 继承JpaRepository<User, Long>
    - 定义findByPhone(String phone)方法
    - 定义findByEmail(String email)方法
    - 定义findByPhoneOrEmail(String phone, String email)方法
    - 定义existsByPhone(String phone)方法
    - 定义existsByEmail(String email)方法
    - _Requirements: 2.3, 14.1, 14.2_
    - _MVP: 必需_

  - [ ] 9.2 创建ElderRepository接口
    - 继承JpaRepository<Elder, Long>
    - 定义findByIdAndPrivacyEnabled(Long id, Boolean privacyEnabled)方法
    - _Requirements: 3.4, 14.7_
    - _MVP: 必需_

  - [ ] 9.3 创建ElderGuardianRepository接口
    - 继承JpaRepository<ElderGuardian, Long>
    - 定义findByElderIdAndGuardianId(Long elderId, Long guardianId)方法
    - 定义findByElderIdAndAuthorized(Long elderId, Boolean authorized)方法
    - 定义findByGuardianIdAndAuthorized(Long guardianId, Boolean authorized)方法
    - 定义existsByElderIdAndGuardianIdAndAuthorized(Long elderId, Long guardianId, Boolean authorized)方法
    - _Requirements: 4.5, 10.2, 14.3_
    - _MVP: 必需_

  - [ ] 9.4 创建EmotionRecordRepository接口
    - 继承JpaRepository<EmotionRecord, Long>
    - 定义findByElderIdAndAnalyzedAtBetween(Long elderId, LocalDateTime start, LocalDateTime end)方法
    - 定义findByElderIdOrderByAnalyzedAtDesc(Long elderId, Pageable pageable)方法
    - 定义countByElderIdAndEmotionTypeAndAnalyzedAtBetween方法（用于统计）
    - _Requirements: 5.7, 8.10, 14.4, 14.7_
    - _MVP: 必需_

  - [ ] 9.5 创建AlertRecordRepository接口
    - 继承JpaRepository<AlertRecord, Long>
    - 定义findByElderIdAndStatus(Long elderId, AlertStatus status)方法
    - 定义findByElderIdAndStatusOrderByCreatedAtDesc(Long elderId, AlertStatus status, Pageable pageable)方法
    - 定义countByElderIdAndStatusAndCreatedAtAfter方法（用于监控）
    - _Requirements: 6.11, 14.5, 14.7_
    - _MVP: 必需_

  - [ ] 9.6 创建AuditLogRepository接口
    - 继承JpaRepository<AuditLog, Long>
    - 定义findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable)方法
    - 定义findByActionAndCreatedAtAfter(String action, LocalDateTime after)方法（用于监控）
    - _Requirements: 9.12, 14.6_
    - _MVP: 必需_

  - [ ] 9.7 创建SystemConfigRepository接口
    - 继承JpaRepository<SystemConfig, Long>
    - 定义findByConfigKey(String configKey)方法
    - _Requirements: 12.2_
    - _MVP: 必需_

  - [ ]* 9.8 编写Repository集成测试
    - 测试UserRepository查询方法
    - 测试ElderGuardianRepository唯一约束
    - 测试EmotionRecordRepository分页查询
    - 测试AlertRecordRepository状态过滤
    - 使用H2内存数据库进行测试
    - _Requirements: 14.7_

- [ ] 10. Checkpoint - 数据层验证
  - 确保所有实体类创建成功
  - 确保所有Repository接口创建成功
  - 确保实体验证规则正确
  - 确保Repository测试通过
  - 如有问题，询问用户


### Phase 4: 用户认证与授权模块

- [ ] 11. 实现认证服务
  - [ ] 11.1 创建DTO类
    - 创建LoginCredentials：identifier（phone或email）、password
    - 创建RegisterRequest：username、phone、email、password、role
    - 创建AuthResponse：token、userId、username、role
    - 创建RegisterResponse：userId、status
    - 创建UserContext：userId、username、role
    - 添加验证注解：@NotBlank, @Email, @Pattern, @Size
    - _Requirements: 1.1, 2.1, 2.2, 2.4, 2.5_
    - _MVP: 必需_

  - [ ] 11.2 创建AuthenticationService接口和实现类
    - 实现authenticate(LoginCredentials credentials)方法
    - 实现register(RegisterRequest request)方法
    - 实现validateToken(String token)方法
    - 实现logout(Long userId)方法
    - 注入UserRepository、PasswordEncoder、JWTManager、RedisTemplate
    - _Requirements: 1.1, 1.3, 1.5, 1.6_
    - _MVP: 必需_

  - [ ] 11.3 实现authenticate方法逻辑
    - 根据identifier查询用户（phone或email）
    - 验证用户存在性（不存在抛出UserNotFoundException）
    - 验证用户状态为ACTIVE（否则抛出AccountInactiveException）
    - 使用PasswordEncoder验证密码（失败抛出InvalidPasswordException）
    - 生成JWT Token
    - 存储Token到Redis，key="token:{userId}"，TTL=7天
    - 记录审计日志：LOGIN_SUCCESS或LOGIN_FAILED
    - 返回AuthResponse
    - _Requirements: 1.1, 1.2, 1.7, 1.8, 9.1, 9.2, 13.1_
    - _MVP: 必需_

  - [ ] 11.4 实现register方法逻辑
    - 验证username长度3-50字符
    - 验证phone或email至少提供一个
    - 验证phone或email唯一性
    - 验证密码强度（最少8字符，包含大小写字母和数字）
    - 使用PasswordEncoder加密密码
    - 创建User实体，status=PENDING_APPROVAL
    - 保存到数据库
    - 记录审计日志：USER_REGISTERED
    - 返回RegisterResponse
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.10_
    - _MVP: 必需_

  - [ ] 11.5 实现validateToken方法逻辑
    - 使用JWTManager验证Token签名和过期时间
    - 从Redis查询Token，key="token:{userId}"
    - 验证Token匹配（防止登出后Token仍有效）
    - 返回UserContext
    - Token无效时抛出InvalidTokenException
    - _Requirements: 1.3, 1.4, 10.10_
    - _MVP: 必需_

  - [ ] 11.6 实现logout方法逻辑
    - 从Redis删除Token，key="token:{userId}"
    - 记录审计日志：LOGOUT
    - _Requirements: 1.5, 9.3_
    - _MVP: 必需_

  - [ ]* 11.7 编写AuthenticationService单元测试
    - 测试有效凭证登录成功
    - 测试无效凭证登录失败
    - 测试用户不存在登录失败
    - 测试用户状态非ACTIVE登录失败
    - 测试注册成功创建用户
    - 测试重复phone注册失败
    - 测试重复email注册失败
    - 测试弱密码注册失败
    - 测试有效Token验证成功
    - 测试过期Token验证失败
    - 测试登出后Token失效
    - 使用Mockito模拟依赖
    - _Requirements: 1.1, 1.2, 1.4, 1.5, 2.3, 2.5_


- [ ] 12. 实现认证过滤器和拦截器
  - [ ] 12.1 创建JwtAuthenticationFilter
    - 继承OncePerRequestFilter
    - 从请求头提取Authorization Bearer Token
    - 调用AuthenticationService.validateToken验证Token
    - 将UserContext存储到SecurityContextHolder
    - Token无效时返回401 Unauthorized
    - 排除公开端点：/api/auth/login、/api/auth/register、/actuator/health
    - _Requirements: 1.3, 1.4, 10.10, 15.1, 15.8_
    - _MVP: 必需_

  - [ ] 12.2 创建RoleBasedAccessControlFilter
    - 实现HandlerInterceptor接口
    - 从SecurityContextHolder获取UserContext
    - 根据请求路径和HTTP方法检查权限
    - ADMIN角色：访问所有资源
    - GUARDIAN/CAREGIVER角色：仅访问授权的Elder数据
    - DELETE操作：仅ADMIN角色
    - 权限不足时返回403 Forbidden并记录审计日志
    - _Requirements: 10.1, 10.2, 10.3, 10.5, 10.9, 15.2_
    - _MVP: 必需_

  - [ ] 12.3 配置Spring Security
    - 配置SecurityFilterChain
    - 添加JwtAuthenticationFilter到过滤器链
    - 配置公开端点不需要认证
    - 配置CORS：允许前端域名、允许方法GET/POST/PUT/DELETE、允许头Authorization/Content-Type
    - 配置CSRF禁用（使用JWT无需CSRF保护）
    - 配置会话管理为STATELESS
    - _Requirements: 16.8, 16.9, 16.10_
    - _MVP: 必需_

  - [ ]* 12.4 编写认证过滤器集成测试
    - 测试有效Token请求通过过滤器
    - 测试无效Token请求返回401
    - 测试无Token请求返回401
    - 测试公开端点无需Token
    - 测试CORS配置正确
    - _Requirements: 1.3, 1.4, 16.8_

- [ ] 13. 实现认证API Controller
  - [ ] 13.1 创建AuthController
    - 定义POST /api/auth/login端点
    - 定义POST /api/auth/register端点
    - 定义POST /api/auth/logout端点
    - 注入AuthenticationService
    - 添加@RestController和@RequestMapping注解
    - 添加@Validated注解启用参数验证
    - _Requirements: 1.1, 2.6_
    - _MVP: 必需_

  - [ ] 13.2 实现login端点
    - 接收LoginCredentials请求体
    - 调用AuthenticationService.authenticate
    - 返回200 OK和AuthResponse
    - 捕获异常返回401 Unauthorized
    - 记录客户端IP地址
    - _Requirements: 1.1, 1.2, 9.9, 15.1_
    - _MVP: 必需_

  - [ ] 13.3 实现register端点
    - 接收RegisterRequest请求体
    - 调用AuthenticationService.register
    - 返回201 Created和RegisterResponse
    - 捕获异常返回400 Bad Request
    - _Requirements: 2.6, 15.4_
    - _MVP: 必需_

  - [ ] 13.4 实现logout端点
    - 从SecurityContextHolder获取当前用户ID
    - 调用AuthenticationService.logout
    - 返回200 OK
    - _Requirements: 1.5, 9.3_
    - _MVP: 必需_

  - [ ]* 13.5 编写AuthController API测试
    - 测试POST /api/auth/login成功返回Token
    - 测试POST /api/auth/login失败返回401
    - 测试POST /api/auth/register成功返回userId
    - 测试POST /api/auth/register重复phone返回400
    - 测试POST /api/auth/logout成功返回200
    - 使用MockMvc进行测试
    - _Requirements: 1.1, 1.2, 2.6_

- [ ] 14. Checkpoint - 认证授权验证
  - 确保用户登录功能正常
  - 确保用户注册功能正常
  - 确保JWT Token验证正常
  - 确保权限拦截正常
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 5: 老人信息管理模块

- [ ] 15. 实现老人管理服务
  - [ ] 15.1 创建DTO类
    - 创建CreateElderRequest：name、gender、birthDate、healthStatus
    - 创建UpdateElderRequest：name、gender、birthDate、healthStatus、privacyEnabled
    - 创建ElderResponse：id、name、gender、birthDate、age、healthStatus、privacyEnabled、createdAt、updatedAt
    - 创建BindGuardianRequest：elderId、guardianId、relationship
    - 添加验证注解
    - _Requirements: 3.1, 3.2, 3.3, 3.7, 4.2_
    - _MVP: 必需_

  - [ ] 15.2 创建ElderManagementService接口和实现类
    - 定义createElder(CreateElderRequest request, Long operatorId)方法
    - 定义updateElder(Long elderId, UpdateElderRequest request, Long operatorId)方法
    - 定义getElderInfo(Long elderId, Long requesterId)方法
    - 定义bindGuardian(Long elderId, Long guardianId, String relationship, Long operatorId)方法
    - 定义updatePrivacyStatus(Long elderId, Boolean privacyEnabled, Long operatorId)方法
    - 注入ElderRepository、ElderGuardianRepository、AuditLoggingService、RedisTemplate
    - _Requirements: 3.5, 3.6, 4.1_
    - _MVP: 必需_

  - [ ] 15.3 实现createElder方法逻辑
    - 验证name长度2-50字符
    - 验证birthDate早于当前日期且年龄60-120岁
    - 验证gender为有效枚举值
    - 创建Elder实体，privacyEnabled=true
    - 保存到数据库
    - 记录审计日志：CREATE_ELDER
    - 返回ElderResponse
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.8, 9.4_
    - _MVP: 必需_

  - [ ] 15.4 实现updateElder方法逻辑
    - 查询Elder实体（不存在抛出ElderNotFoundException）
    - 验证操作人权限（调用权限检查方法）
    - 更新字段：name、gender、birthDate、healthStatus、privacyEnabled
    - 使用乐观锁防止并发冲突
    - 保存到数据库
    - 使缓存失效：删除Redis key "elder:info:{elderId}"
    - 记录审计日志：UPDATE_ELDER
    - 返回ElderResponse
    - _Requirements: 3.5, 3.8, 3.9, 3.10, 14.10, 19.1_
    - _MVP: 必需_

  - [ ] 15.5 实现getElderInfo方法逻辑
    - 尝试从Redis缓存获取，key="elder:info:{elderId}"
    - 缓存未命中则查询数据库
    - 验证请求人权限（ADMIN或授权的Guardian/Caregiver）
    - 权限不足抛出PermissionDeniedException
    - 将结果缓存到Redis，TTL=1小时
    - 返回ElderResponse
    - _Requirements: 3.6, 3.10, 10.2, 10.3, 13.7_
    - _MVP: 必需_

  - [ ] 15.6 实现bindGuardian方法逻辑
    - 验证Elder和Guardian存在性
    - 验证relationship长度2-50字符
    - 创建ElderGuardian实体，authorized=false
    - 保存到数据库（唯一约束防止重复绑定）
    - 记录审计日志：BIND_GUARDIAN
    - _Requirements: 4.1, 4.2, 4.3, 4.5, 4.6, 4.8_
    - _MVP: 必需_

  - [ ] 15.7 实现updatePrivacyStatus方法逻辑
    - 查询Elder实体
    - 验证操作人权限（ADMIN或PRIMARY_GUARDIAN）
    - 更新privacyEnabled字段
    - 保存到数据库
    - 使缓存失效
    - 记录审计日志：UPDATE_PRIVACY_STATUS
    - _Requirements: 3.4, 10.6_
    - _MVP: 可选_

  - [ ]* 15.8 编写ElderManagementService单元测试
    - 测试创建老人成功
    - 测试创建老人年龄验证失败
    - 测试更新老人成功
    - 测试更新老人权限不足失败
    - 测试查询老人信息成功
    - 测试查询老人信息权限不足失败
    - 测试绑定监护人成功
    - 测试重复绑定监护人失败
    - 测试缓存命中和失效
    - 使用Mockito模拟依赖
    - _Requirements: 3.1, 3.2, 3.5, 3.6, 4.1, 4.5_


- [ ] 16. 实现老人管理API Controller
  - [ ] 16.1 创建ElderController
    - 定义POST /api/elders端点（创建老人）
    - 定义PUT /api/elders/{id}端点（更新老人）
    - 定义GET /api/elders/{id}端点（查询老人信息）
    - 定义POST /api/elders/{id}/guardians端点（绑定监护人）
    - 定义PUT /api/elders/{id}/privacy端点（更新隐私状态）
    - 注入ElderManagementService
    - 添加@RestController和@RequestMapping注解
    - _Requirements: 3.5, 3.6, 4.1_
    - _MVP: 必需_

  - [ ] 16.2 实现创建老人端点
    - 接收CreateElderRequest请求体
    - 从SecurityContextHolder获取操作人ID
    - 调用ElderManagementService.createElder
    - 返回201 Created和ElderResponse
    - 捕获异常返回400 Bad Request
    - _Requirements: 3.1, 3.8, 15.4_
    - _MVP: 必需_

  - [ ] 16.3 实现更新老人端点
    - 接收路径参数elderId和UpdateElderRequest请求体
    - 从SecurityContextHolder获取操作人ID
    - 调用ElderManagementService.updateElder
    - 返回200 OK和ElderResponse
    - 捕获ElderNotFoundException返回404
    - 捕获PermissionDeniedException返回403
    - _Requirements: 3.5, 15.2, 15.3_
    - _MVP: 必需_

  - [ ] 16.4 实现查询老人信息端点
    - 接收路径参数elderId
    - 从SecurityContextHolder获取请求人ID
    - 调用ElderManagementService.getElderInfo
    - 返回200 OK和ElderResponse
    - 捕获PermissionDeniedException返回403
    - _Requirements: 3.6, 15.2, 15.3_
    - _MVP: 必需_

  - [ ] 16.5 实现绑定监护人端点
    - 接收路径参数elderId和BindGuardianRequest请求体
    - 从SecurityContextHolder获取操作人ID
    - 调用ElderManagementService.bindGuardian
    - 返回201 Created
    - 捕获异常返回400 Bad Request
    - _Requirements: 4.1, 4.8, 15.4_
    - _MVP: 必需_

  - [ ]* 16.6 编写ElderController API测试
    - 测试POST /api/elders成功返回201
    - 测试PUT /api/elders/{id}成功返回200
    - 测试PUT /api/elders/{id}权限不足返回403
    - 测试GET /api/elders/{id}成功返回200
    - 测试GET /api/elders/{id}权限不足返回403
    - 测试POST /api/elders/{id}/guardians成功返回201
    - 使用MockMvc进行测试
    - _Requirements: 3.5, 3.6, 4.1_

- [ ] 17. Checkpoint - 老人管理验证
  - 确保老人信息CRUD功能正常
  - 确保监护人绑定功能正常
  - 确保权限控制正常
  - 确保缓存功能正常
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 6: 情感分析模块

- [ ] 18. 实现AI服务客户端
  - [ ] 18.1 创建AI服务接口和DTO
    - 创建AIServiceClient接口：analyze(String rawData, DataSource dataSource)方法
    - 创建AIAnalysisRequest：rawData、dataSource
    - 创建AIAnalysisResponse：emotionType、confidenceScore、rawResponse
    - 定义超时时间30秒
    - _Requirements: 5.3, 5.4, 5.5_
    - _MVP: 必需_

  - [ ] 18.2 实现BaiduAIServiceClient
    - 实现AIServiceClient接口
    - 配置百度AI API密钥（从配置读取）
    - 实现语音情感分析调用
    - 实现图像情感分析调用
    - 实现文本情感分析调用
    - 解析API响应并映射到EmotionType
    - 超时30秒抛出AIServiceException
    - _Requirements: 5.3, 5.9, 15.5_
    - _MVP: 必需_

  - [ ] 18.3 实现Circuit Breaker模式
    - 使用Resilience4j实现熔断器
    - 配置失败阈值：连续3次失败触发熔断
    - 配置熔断时长：30秒
    - 配置重试策略：指数退避，最多3次
    - 熔断时抛出CircuitBreakerOpenException
    - _Requirements: 15.5, 15.9_
    - _MVP: 必需_

  - [ ]* 18.4 编写AI服务客户端单元测试
    - 测试成功调用返回有效结果
    - 测试API超时抛出AIServiceException
    - 测试API返回错误抛出AIServiceException
    - 测试熔断器在3次失败后打开
    - 测试熔断器打开后拒绝请求
    - 使用WireMock模拟外部API
    - _Requirements: 5.3, 5.9, 15.5_

- [ ] 19. 实现情感分析服务
  - [ ] 19.1 创建DTO类
    - 创建EmotionAnalysisRequest：elderId、dataType、rawData
    - 创建EmotionAnalysisResult：recordId、elderId、emotionType、confidenceScore、analyzedAt
    - 创建EmotionRecord查询DTO：elderId、startTime、endTime、pageSize、pageNumber
    - 创建EmotionStatistics：emotionDistribution（Map<EmotionType, Integer>）、totalRecords、period
    - 添加验证注解
    - _Requirements: 5.1, 5.2, 5.7, 8.4, 8.5_
    - _MVP: 必需_

  - [ ] 19.2 创建EmotionAnalysisService接口和实现类
    - 定义analyzeEmotion(EmotionAnalysisRequest request, Long operatorId)方法
    - 定义queryEmotionRecords(Long elderId, LocalDateTime startTime, LocalDateTime endTime, Long requesterId)方法
    - 定义getEmotionStatistics(Long elderId, StatisticsPeriod period, Long requesterId)方法
    - 注入AIServiceClient、EncryptionService、EmotionRecordRepository、AlertManagementService、AuditLoggingService、RedisTemplate
    - _Requirements: 5.1, 5.7, 8.4_
    - _MVP: 必需_

  - [ ] 19.3 实现analyzeEmotion方法逻辑
    - 验证操作人权限（调用权限检查方法）
    - 调用AIServiceClient.analyze进行情感分析
    - 验证返回的emotionType和confidenceScore
    - 使用EncryptionService加密rawData
    - 创建EmotionRecord实体并保存到数据库
    - 使缓存失效：删除Redis key "emotion:trend:{elderId}:*"
    - 如果检测到负面情绪（SAD/ANXIOUS/ANGRY）且confidence >= 0.7，调用AlertManagementService.checkAndCreateAlert
    - 记录审计日志：EMOTION_ANALYZED
    - 返回EmotionAnalysisResult
    - 总执行时间 < 35秒
    - _Requirements: 5.1, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.10, 5.11, 13.2_
    - _MVP: 必需_

  - [ ] 19.4 实现queryEmotionRecords方法逻辑
    - 验证请求人权限
    - 验证日期范围不超过90天
    - 查询EmotionRecordRepository，使用分页（默认20条/页）
    - 返回EmotionRecord列表
    - _Requirements: 5.7, 8.10, 8.11, 14.7, 14.8_
    - _MVP: 必需_

  - [ ] 19.5 实现getEmotionStatistics方法逻辑
    - 验证请求人权限
    - 根据period计算时间范围（DAY/WEEK/MONTH）
    - 使用数据库聚合查询统计各情感类型数量
    - 计算情感占比
    - 返回EmotionStatistics
    - _Requirements: 8.4, 8.5, 14.9_
    - _MVP: 必需_

  - [ ]* 19.6 编写EmotionAnalysisService单元测试
    - 测试情感分析成功创建记录
    - 测试负面情绪触发预警
    - 测试正面情绪不触发预警
    - 测试AI服务失败抛出异常
    - 测试权限不足抛出异常
    - 测试查询情感记录成功
    - 测试日期范围超过90天失败
    - 测试统计数据计算正确
    - 使用Mockito模拟依赖
    - _Requirements: 5.1, 5.7, 5.9, 8.11_


- [ ] 20. 实现情感分析API Controller
  - [ ] 20.1 创建EmotionController
    - 定义POST /api/emotions/analyze端点（分析情感）
    - 定义GET /api/emotions端点（查询情感记录）
    - 定义GET /api/emotions/statistics端点（获取统计数据）
    - 注入EmotionAnalysisService
    - 添加@RestController和@RequestMapping注解
    - _Requirements: 5.1, 5.7, 8.4_
    - _MVP: 必需_

  - [ ] 20.2 实现分析情感端点
    - 接收EmotionAnalysisRequest请求体
    - 从SecurityContextHolder获取操作人ID
    - 调用EmotionAnalysisService.analyzeEmotion
    - 返回200 OK和EmotionAnalysisResult
    - 捕获AIServiceException返回503 Service Unavailable
    - 捕获PermissionDeniedException返回403
    - 记录客户端IP地址
    - _Requirements: 5.1, 5.9, 9.9, 15.2, 15.3, 15.6_
    - _MVP: 必需_

  - [ ] 20.3 实现查询情感记录端点
    - 接收查询参数：elderId、startTime、endTime、pageSize、pageNumber
    - 从SecurityContextHolder获取请求人ID
    - 调用EmotionAnalysisService.queryEmotionRecords
    - 返回200 OK和分页结果
    - 捕获PermissionDeniedException返回403
    - _Requirements: 5.7, 8.10, 14.7_
    - _MVP: 必需_

  - [ ] 20.4 实现获取统计数据端点
    - 接收查询参数：elderId、period（DAY/WEEK/MONTH）
    - 从SecurityContextHolder获取请求人ID
    - 调用EmotionAnalysisService.getEmotionStatistics
    - 返回200 OK和EmotionStatistics
    - 捕获PermissionDeniedException返回403
    - _Requirements: 8.4, 8.5_
    - _MVP: 必需_

  - [ ]* 20.5 编写EmotionController API测试
    - 测试POST /api/emotions/analyze成功返回200
    - 测试POST /api/emotions/analyze AI服务失败返回503
    - 测试POST /api/emotions/analyze权限不足返回403
    - 测试GET /api/emotions成功返回分页结果
    - 测试GET /api/emotions/statistics成功返回统计数据
    - 使用MockMvc进行测试
    - _Requirements: 5.1, 5.7, 5.9, 8.4_

- [ ] 21. Checkpoint - 情感分析验证
  - 确保AI服务调用正常
  - 确保情感分析功能正常
  - 确保数据加密存储正常
  - 确保预警触发正常
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 7: 预警管理模块

- [ ] 22. 实现SSE推送服务
  - [ ] 22.1 创建SseEmitterManager
    - 使用ConcurrentHashMap存储活跃连接：Map<Long, List<SseEmitter>>（userId -> emitters）
    - 定义addEmitter(Long userId, SseEmitter emitter)方法
    - 定义removeEmitter(Long userId, SseEmitter emitter)方法
    - 定义sendEvent(Long userId, String eventName, Object data)方法
    - 定义sendEventToMultipleUsers(List<Long> userIds, String eventName, Object data)方法
    - 处理连接超时和错误，自动移除失效连接
    - _Requirements: 7.1, 7.4, 7.5, 7.6, 7.7, 7.8, 19.5_
    - _MVP: 必需_

  - [ ]* 22.2 编写SseEmitterManager单元测试
    - 测试添加连接成功
    - 测试移除连接成功
    - 测试发送事件到单个用户
    - 测试发送事件到多个用户
    - 测试失败连接自动移除
    - 测试并发添加和移除连接
    - _Requirements: 7.4, 7.5, 7.6, 7.8, 19.6_

- [ ] 23. 实现预警管理服务
  - [ ] 23.1 创建DTO类
    - 创建AlertRecord查询DTO：elderId、status、pageSize、pageNumber
    - 创建HandleAlertRequest：alertId、handleNote
    - 创建AlertNotification：alertId、elderId、elderName、alertType、severity、message、createdAt
    - 添加验证注解
    - _Requirements: 6.11, 6.12, 6.13_
    - _MVP: 必需_

  - [ ] 23.2 创建AlertManagementService接口和实现类
    - 定义checkAndCreateAlert(Long elderId, EmotionType emotionType, Double confidenceScore)方法
    - 定义queryAlerts(Long elderId, AlertStatus status, Long requesterId)方法
    - 定义handleAlert(Long alertId, Long handlerId, String handleNote)方法
    - 定义subscribeAlerts(Long userId)方法
    - 注入AlertRecordRepository、ElderGuardianRepository、SseEmitterManager、EmailService、AuditLoggingService、SystemConfigRepository
    - _Requirements: 6.1, 6.11, 7.1_
    - _MVP: 必需_

  - [ ] 23.3 实现checkAndCreateAlert方法逻辑
    - 从SystemConfigRepository读取预警阈值配置
    - 判断emotionType是否为负面情绪（SAD/ANXIOUS/ANGRY）
    - 判断confidenceScore是否 >= 阈值
    - 根据confidenceScore计算severity：0.7-0.85=MEDIUM, 0.85-0.95=HIGH, >=0.95=CRITICAL
    - 创建AlertRecord实体，status=PENDING
    - 保存到数据库
    - 查询所有授权的Guardian（ElderGuardianRepository）
    - 调用SseEmitterManager.sendEventToMultipleUsers推送预警
    - 如果email通知启用，调用EmailService发送邮件
    - 记录审计日志：ALERT_CREATED
    - 返回Optional<AlertRecord>
    - 执行时间 < 1秒
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10, 13.4_
    - _MVP: 必需_

  - [ ] 23.4 实现queryAlerts方法逻辑
    - 验证请求人权限
    - 查询AlertRecordRepository，按status过滤，按createdAt降序排序
    - 使用分页（默认20条/页）
    - 返回AlertRecord列表
    - _Requirements: 6.11, 14.7_
    - _MVP: 必需_

  - [ ] 23.5 实现handleAlert方法逻辑
    - 查询AlertRecord（不存在抛出AlertNotFoundException）
    - 验证处理人权限
    - 使用悲观锁防止并发处理（SELECT FOR UPDATE）
    - 更新status=HANDLED、handledBy、handledAt、handleNote
    - 保存到数据库
    - 记录审计日志：ALERT_HANDLED
    - _Requirements: 6.11, 9.8, 19.2_
    - _MVP: 必需_

  - [ ] 23.6 实现subscribeAlerts方法逻辑
    - 创建SseEmitter，无超时
    - 发送初始"connected"事件
    - 调用SseEmitterManager.addEmitter添加连接
    - 设置连接完成、超时、错误回调，自动移除连接
    - 返回SseEmitter
    - _Requirements: 7.1, 7.2, 7.4, 7.5, 7.6, 7.9_
    - _MVP: 必需_

  - [ ]* 23.7 编写AlertManagementService单元测试
    - 测试负面情绪confidence >= 0.7创建预警
    - 测试负面情绪confidence < 0.7不创建预警
    - 测试正面情绪不创建预警
    - 测试预警severity计算正确
    - 测试预警推送到所有授权Guardian
    - 测试查询预警成功
    - 测试处理预警成功
    - 测试并发处理预警使用悲观锁
    - 使用Mockito模拟依赖
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.11, 19.2_


- [ ] 24. 实现预警管理API Controller
  - [ ] 24.1 创建AlertController
    - 定义GET /api/alerts端点（查询预警）
    - 定义PUT /api/alerts/{id}/handle端点（处理预警）
    - 定义GET /api/alerts/subscribe端点（订阅SSE推送）
    - 注入AlertManagementService
    - 添加@RestController和@RequestMapping注解
    - _Requirements: 6.11, 7.1_
    - _MVP: 必需_

  - [ ] 24.2 实现查询预警端点
    - 接收查询参数：elderId、status、pageSize、pageNumber
    - 从SecurityContextHolder获取请求人ID
    - 调用AlertManagementService.queryAlerts
    - 返回200 OK和分页结果
    - 捕获PermissionDeniedException返回403
    - _Requirements: 6.11, 15.2_
    - _MVP: 必需_

  - [ ] 24.3 实现处理预警端点
    - 接收路径参数alertId和HandleAlertRequest请求体
    - 从SecurityContextHolder获取处理人ID
    - 调用AlertManagementService.handleAlert
    - 返回200 OK
    - 捕获AlertNotFoundException返回404
    - 捕获PermissionDeniedException返回403
    - _Requirements: 6.11, 15.2, 15.3_
    - _MVP: 必需_

  - [ ] 24.4 实现订阅SSE推送端点
    - 从SecurityContextHolder获取用户ID
    - 调用AlertManagementService.subscribeAlerts
    - 返回SseEmitter（Content-Type: text/event-stream）
    - _Requirements: 7.1, 7.2, 7.10_
    - _MVP: 必需_

  - [ ]* 24.5 编写AlertController API测试
    - 测试GET /api/alerts成功返回分页结果
    - 测试PUT /api/alerts/{id}/handle成功返回200
    - 测试PUT /api/alerts/{id}/handle预警不存在返回404
    - 测试GET /api/alerts/subscribe成功建立SSE连接
    - 测试SSE推送事件到客户端
    - 使用MockMvc进行测试
    - _Requirements: 6.11, 7.1, 7.3_

- [ ] 25. Checkpoint - 预警管理验证
  - 确保预警创建功能正常
  - 确保SSE推送功能正常
  - 确保预警处理功能正常
  - 确保并发控制正常
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 8: 数据可视化模块

- [ ] 26. 实现数据可视化服务
  - [ ] 26.1 创建DTO类
    - 创建EmotionTrendData：时间序列数据（List<TrendPoint>），TrendPoint包含date和emotionCounts（Map<EmotionType, Integer>）
    - 创建EmotionDistributionData：情感占比（Map<EmotionType, Double>）、总记录数、时间周期
    - 创建HealthScore：分数（0-100）、评级（EXCELLENT/GOOD/FAIR/POOR）、计算依据
    - 创建HeatmapData：日期网格数据（List<HeatmapCell>），HeatmapCell包含date、emotionType、intensity
    - 定义StatisticsPeriod枚举：DAY, WEEK, MONTH
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_
    - _MVP: 必需_

  - [ ] 26.2 创建DataVisualizationService接口和实现类
    - 定义getEmotionTrend(Long elderId, StatisticsPeriod period, Long requesterId)方法
    - 定义getEmotionDistribution(Long elderId, StatisticsPeriod period, Long requesterId)方法
    - 定义calculateHealthScore(Long elderId, Long requesterId)方法
    - 定义getEmotionHeatmap(Long elderId, LocalDate startDate, LocalDate endDate, Long requesterId)方法
    - 注入EmotionRecordRepository、RedisTemplate
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.6_
    - _MVP: 必需_

  - [ ] 26.3 实现getEmotionTrend方法逻辑
    - 验证请求人权限
    - 尝试从Redis缓存获取，key="emotion:trend:{elderId}:{period}"
    - 缓存未命中则根据period计算时间范围
    - 使用数据库聚合查询按日期分组统计各情感类型数量
    - 构建时间序列数据
    - 将结果缓存到Redis，TTL=5分钟
    - 返回EmotionTrendData
    - 执行时间 < 2秒（p95）
    - _Requirements: 8.1, 8.3, 8.7, 8.9, 13.3, 13.8, 14.9_
    - _MVP: 必需_

  - [ ] 26.4 实现getEmotionDistribution方法逻辑
    - 验证请求人权限
    - 根据period计算时间范围
    - 使用数据库聚合查询统计各情感类型数量
    - 计算各情感类型占比（百分比）
    - 返回EmotionDistributionData
    - 执行时间 < 2秒（p95）
    - _Requirements: 8.2, 8.4, 13.3, 14.9_
    - _MVP: 必需_

  - [ ] 26.5 实现calculateHealthScore方法逻辑
    - 验证请求人权限
    - 查询最近30天的情感记录
    - 计算健康评分算法：
      - 正面情绪（HAPPY/CALM）权重：+1
      - 负面情绪（SAD/ANXIOUS/ANGRY）权重：-1
      - 加权平均后映射到0-100分
    - 根据分数确定评级：>=80=EXCELLENT, 60-79=GOOD, 40-59=FAIR, <40=POOR
    - 返回HealthScore
    - _Requirements: 8.3_
    - _MVP: 必需_

  - [ ] 26.6 实现getEmotionHeatmap方法逻辑
    - 验证请求人权限
    - 验证日期范围不超过90天
    - 查询指定日期范围内的情感记录
    - 按日期分组，计算每天的主要情感类型和强度
    - 构建热力图数据
    - 返回HeatmapData
    - _Requirements: 8.6, 8.11_
    - _MVP: 可选_

  - [ ]* 26.7 编写DataVisualizationService单元测试
    - 测试获取情感趋势成功
    - 测试获取情感占比成功
    - 测试计算健康评分成功
    - 测试健康评分评级正确
    - 测试获取热力图成功
    - 测试日期范围超过90天失败
    - 测试缓存命中和失效
    - 测试权限不足抛出异常
    - 使用Mockito模拟依赖
    - _Requirements: 8.1, 8.2, 8.3, 8.6, 8.11_


- [ ] 27. 实现数据可视化API Controller
  - [ ] 27.1 创建DataVisualizationController
    - 定义GET /api/visualization/trend端点（获取情感趋势）
    - 定义GET /api/visualization/distribution端点（获取情感占比）
    - 定义GET /api/visualization/health-score端点（获取健康评分）
    - 定义GET /api/visualization/heatmap端点（获取热力图）
    - 注入DataVisualizationService
    - 添加@RestController和@RequestMapping注解
    - _Requirements: 8.1, 8.2, 8.3, 8.6_
    - _MVP: 必需_

  - [ ] 27.2 实现获取情感趋势端点
    - 接收查询参数：elderId、period（DAY/WEEK/MONTH）
    - 从SecurityContextHolder获取请求人ID
    - 调用DataVisualizationService.getEmotionTrend
    - 返回200 OK和EmotionTrendData
    - 捕获PermissionDeniedException返回403
    - _Requirements: 8.1, 8.3, 15.2_
    - _MVP: 必需_

  - [ ] 27.3 实现获取情感占比端点
    - 接收查询参数：elderId、period
    - 从SecurityContextHolder获取请求人ID
    - 调用DataVisualizationService.getEmotionDistribution
    - 返回200 OK和EmotionDistributionData
    - 捕获PermissionDeniedException返回403
    - _Requirements: 8.2, 8.4, 15.2_
    - _MVP: 必需_

  - [ ] 27.4 实现获取健康评分端点
    - 接收查询参数：elderId
    - 从SecurityContextHolder获取请求人ID
    - 调用DataVisualizationService.calculateHealthScore
    - 返回200 OK和HealthScore
    - 捕获PermissionDeniedException返回403
    - _Requirements: 8.3, 15.2_
    - _MVP: 必需_

  - [ ] 27.5 实现获取热力图端点
    - 接收查询参数：elderId、startDate、endDate
    - 从SecurityContextHolder获取请求人ID
    - 调用DataVisualizationService.getEmotionHeatmap
    - 返回200 OK和HeatmapData
    - 捕获PermissionDeniedException返回403
    - _Requirements: 8.6, 15.2_
    - _MVP: 可选_

  - [ ]* 27.6 编写DataVisualizationController API测试
    - 测试GET /api/visualization/trend成功返回200
    - 测试GET /api/visualization/distribution成功返回200
    - 测试GET /api/visualization/health-score成功返回200
    - 测试GET /api/visualization/heatmap成功返回200
    - 测试权限不足返回403
    - 使用MockMvc进行测试
    - _Requirements: 8.1, 8.2, 8.3, 8.6_

- [ ] 28. Checkpoint - 数据可视化验证
  - 确保情感趋势查询正常
  - 确保情感占比统计正常
  - 确保健康评分计算正常
  - 确保缓存功能正常
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 9: 审计日志与系统配置模块

- [ ] 29. 实现审计日志服务
  - [ ] 29.1 创建DTO类
    - 创建AuditLogQueryCriteria：userId、action、resourceType、startTime、endTime、pageSize、pageNumber
    - 添加验证注解
    - _Requirements: 9.12_
    - _MVP: 必需_

  - [ ] 29.2 创建AuditLoggingService接口和实现类
    - 定义log(AuditLog log)方法
    - 定义queryLogs(AuditLogQueryCriteria criteria, Long requesterId)方法
    - 定义exportLogs(AuditLogQueryCriteria criteria, Long requesterId)方法
    - 注入AuditLogRepository
    - _Requirements: 9.1, 9.12, 9.14_
    - _MVP: 必需_

  - [ ] 29.3 实现log方法逻辑
    - 创建AuditLog实体
    - 保存到数据库（不可变记录）
    - 异步执行，不阻塞主流程
    - _Requirements: 9.1, 9.9, 9.11_
    - _MVP: 必需_

  - [ ] 29.4 实现queryLogs方法逻辑
    - 验证请求人角色为ADMIN
    - 根据criteria构建查询条件
    - 使用分页查询（默认20条/页）
    - 按createdAt降序排序
    - 返回Page<AuditLog>
    - _Requirements: 9.12, 9.13, 14.6_
    - _MVP: 必需_

  - [ ] 29.5 实现exportLogs方法逻辑
    - 验证请求人角色为ADMIN
    - 根据criteria查询审计日志
    - 生成CSV文件
    - 返回文件路径
    - _Requirements: 9.14_
    - _MVP: 可选_

  - [ ]* 29.6 编写AuditLoggingService单元测试
    - 测试记录审计日志成功
    - 测试查询审计日志成功
    - 测试非ADMIN用户查询失败
    - 测试导出审计日志成功
    - 测试审计日志不可修改
    - 使用Mockito模拟依赖
    - _Requirements: 9.1, 9.11, 9.12_

- [ ] 30. 实现系统配置服务
  - [ ] 30.1 创建DTO类
    - 创建UpdateConfigRequest：configKey、configValue
    - 添加验证注解
    - _Requirements: 12.9, 12.11, 12.12_
    - _MVP: 必需_

  - [ ] 30.2 创建SystemConfigService接口和实现类
    - 定义getConfig(String configKey)方法
    - 定义updateConfig(String configKey, String configValue, Long operatorId)方法
    - 定义getAllConfigs()方法
    - 注入SystemConfigRepository、AuditLoggingService、RedisTemplate
    - _Requirements: 12.1, 12.9_
    - _MVP: 必需_

  - [ ] 30.3 实现getConfig方法逻辑
    - 尝试从Redis缓存获取，key="config:{configKey}"
    - 缓存未命中则查询数据库
    - 将结果缓存到Redis，TTL=1小时
    - 返回configValue
    - _Requirements: 12.1_
    - _MVP: 必需_

  - [ ] 30.4 实现updateConfig方法逻辑
    - 验证操作人角色为ADMIN
    - 查询SystemConfig实体
    - 验证configKey和configValue格式
    - 更新configValue和updatedAt
    - 保存到数据库
    - 使缓存失效：删除Redis key "config:{configKey}"
    - 记录审计日志：UPDATE_CONFIG
    - _Requirements: 12.9, 12.10, 12.11, 12.12_
    - _MVP: 必需_

  - [ ] 30.5 实现getAllConfigs方法逻辑
    - 验证请求人角色为ADMIN
    - 查询所有SystemConfig记录
    - 返回配置列表
    - _Requirements: 12.1_
    - _MVP: 必需_

  - [ ]* 30.6 编写SystemConfigService单元测试
    - 测试获取配置成功
    - 测试更新配置成功
    - 测试非ADMIN用户更新配置失败
    - 测试缓存命中和失效
    - 测试配置验证规则
    - 使用Mockito模拟依赖
    - _Requirements: 12.1, 12.9, 12.11, 12.12_


- [ ] 31. 实现审计日志和系统配置API Controller
  - [ ] 31.1 创建AuditLogController
    - 定义GET /api/audit-logs端点（查询审计日志）
    - 定义GET /api/audit-logs/export端点（导出审计日志）
    - 注入AuditLoggingService
    - 添加@RestController和@RequestMapping注解
    - _Requirements: 9.12, 9.14_
    - _MVP: 必需_

  - [ ] 31.2 实现查询审计日志端点
    - 接收查询参数：userId、action、resourceType、startTime、endTime、pageSize、pageNumber
    - 从SecurityContextHolder获取请求人ID
    - 调用AuditLoggingService.queryLogs
    - 返回200 OK和分页结果
    - 捕获PermissionDeniedException返回403
    - _Requirements: 9.12, 15.2_
    - _MVP: 必需_

  - [ ] 31.3 实现导出审计日志端点
    - 接收查询参数：userId、action、resourceType、startTime、endTime
    - 从SecurityContextHolder获取请求人ID
    - 调用AuditLoggingService.exportLogs
    - 返回文件下载响应
    - 捕获PermissionDeniedException返回403
    - _Requirements: 9.14, 15.2_
    - _MVP: 可选_

  - [ ] 31.4 创建SystemConfigController
    - 定义GET /api/configs端点（获取所有配置）
    - 定义GET /api/configs/{key}端点（获取单个配置）
    - 定义PUT /api/configs/{key}端点（更新配置）
    - 注入SystemConfigService
    - 添加@RestController和@RequestMapping注解
    - _Requirements: 12.1, 12.9_
    - _MVP: 必需_

  - [ ] 31.5 实现获取所有配置端点
    - 从SecurityContextHolder获取请求人ID
    - 调用SystemConfigService.getAllConfigs
    - 返回200 OK和配置列表
    - 捕获PermissionDeniedException返回403
    - _Requirements: 12.1, 15.2_
    - _MVP: 必需_

  - [ ] 31.6 实现获取单个配置端点
    - 接收路径参数configKey
    - 调用SystemConfigService.getConfig
    - 返回200 OK和configValue
    - _Requirements: 12.1_
    - _MVP: 必需_

  - [ ] 31.7 实现更新配置端点
    - 接收路径参数configKey和UpdateConfigRequest请求体
    - 从SecurityContextHolder获取操作人ID
    - 调用SystemConfigService.updateConfig
    - 返回200 OK
    - 捕获PermissionDeniedException返回403
    - _Requirements: 12.9, 12.10, 15.2_
    - _MVP: 必需_

  - [ ]* 31.8 编写AuditLogController和SystemConfigController API测试
    - 测试GET /api/audit-logs成功返回分页结果
    - 测试GET /api/audit-logs非ADMIN返回403
    - 测试GET /api/audit-logs/export成功返回文件
    - 测试GET /api/configs成功返回配置列表
    - 测试GET /api/configs/{key}成功返回配置值
    - 测试PUT /api/configs/{key}成功返回200
    - 测试PUT /api/configs/{key}非ADMIN返回403
    - 使用MockMvc进行测试
    - _Requirements: 9.12, 9.14, 12.1, 12.9_

- [ ] 32. Checkpoint - 审计日志和系统配置验证
  - 确保审计日志记录正常
  - 确保审计日志查询正常
  - 确保系统配置管理正常
  - 确保权限控制正常
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 10: 安全加固、监控与部署

- [ ] 33. 实现安全加固
  - [ ] 33.1 配置安全响应头
    - 添加SecurityHeadersFilter
    - 设置X-Content-Type-Options: nosniff
    - 设置X-Frame-Options: DENY
    - 设置X-XSS-Protection: 1; mode=block
    - 设置Strict-Transport-Security: max-age=31536000; includeSubDomains
    - 设置Content-Security-Policy: default-src 'self'
    - _Requirements: 11.8_
    - _MVP: 必需_

  - [ ] 33.2 实现API限流
    - 使用Bucket4j或Guava RateLimiter实现限流
    - 认证端点：5次/分钟/IP
    - 情感分析端点：10次/分钟/用户
    - 数据查询端点：60次/分钟/用户
    - 超过限制返回429 Too Many Requests
    - _Requirements: 16.1, 16.2, 16.3_
    - _MVP: 必需_

  - [ ] 33.3 实现文件上传验证
    - 验证文件大小：图片10MB、视频50MB、音频5MB
    - 验证MIME类型：image/jpeg、image/png、video/mp4、audio/wav、audio/mp3
    - 验证文件内容（防止伪造MIME类型）
    - 超过限制返回400 Bad Request
    - _Requirements: 16.4, 16.5_
    - _MVP: 必需_

  - [ ] 33.4 实现输入验证和XSS防护
    - 使用OWASP Java Encoder对所有用户输入进行编码
    - 使用Hibernate Validator验证所有DTO
    - 使用参数化查询防止SQL注入
    - _Requirements: 16.6, 16.7_
    - _MVP: 必需_

  - [ ]* 33.5 编写安全加固测试
    - 测试安全响应头正确设置
    - 测试API限流正常工作
    - 测试文件上传验证正常
    - 测试XSS攻击被阻止
    - 测试SQL注入被阻止
    - _Requirements: 11.8, 16.1, 16.4, 16.6, 16.7_

- [ ] 34. 实现监控和健康检查
  - [ ] 34.1 配置Spring Boot Actuator
    - 启用/actuator/health端点
    - 启用/actuator/metrics端点
    - 配置健康检查：数据库、Redis、AI服务
    - 配置自定义指标：API调用次数、响应时间、错误率
    - _Requirements: 17.6, 17.7, 17.10, 20.12_
    - _MVP: 必需_

  - [ ] 34.2 实现异常监控和告警
    - 监控多次登录失败（>5次/5分钟）
    - 监控多次权限拒绝（>10次/1小时）
    - 监控AI服务连续失败（>3次）
    - 监控数据库连接失败
    - 监控加密失败
    - 发送告警到管理员（邮件或日志）
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.9_
    - _MVP: 必需_

  - [ ] 34.3 实现健康状态降级
    - 数据库不可达但Redis可用：返回degraded状态
    - AI服务不可用：返回degraded状态
    - 所有依赖不可用：返回down状态
    - _Requirements: 17.8_
    - _MVP: 必需_

  - [ ]* 34.4 编写监控和健康检查测试
    - 测试健康检查端点返回正确状态
    - 测试异常监控触发告警
    - 测试健康状态降级逻辑
    - _Requirements: 17.6, 17.8_


- [ ] 35. 实现全局异常处理
  - [ ] 35.1 创建GlobalExceptionHandler
    - 使用@ControllerAdvice注解
    - 处理AuthenticationException：返回401 Unauthorized
    - 处理PermissionDeniedException：返回403 Forbidden
    - 处理ResourceNotFoundException：返回404 Not Found
    - 处理ValidationException：返回400 Bad Request
    - 处理AIServiceException：返回503 Service Unavailable
    - 处理DatabaseException：返回500 Internal Server Error
    - 处理EncryptionException：返回500 Internal Server Error
    - 处理TokenExpiredException：返回401 Unauthorized
    - 处理所有未捕获异常：返回500 Internal Server Error
    - 记录所有异常到日志
    - 不暴露敏感信息（数据库详情、堆栈跟踪）
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.10, 15.11_
    - _MVP: 必需_

  - [ ]* 35.2 编写全局异常处理测试
    - 测试各种异常返回正确的HTTP状态码
    - 测试错误响应不包含敏感信息
    - 测试异常被记录到日志
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.11_

- [ ] 36. 实现数据保留和清理
  - [ ] 36.1 创建数据清理定时任务
    - 使用@Scheduled注解创建定时任务
    - 每天凌晨2点执行
    - 删除超过保留期的EmotionRecord（默认365天）
    - 删除超过保留期的AlertRecord（180天）
    - 保留AuditLog至少90天
    - 从SystemConfig读取保留天数配置
    - 记录清理结果到日志
    - _Requirements: 18.1, 18.2, 18.3, 18.5_
    - _MVP: 可选_

  - [ ] 36.2 实现GDPR数据删除
    - 创建deleteUserData(Long userId)方法
    - 删除用户的所有EmotionRecord
    - 删除用户的所有AlertRecord
    - 删除用户的所有ElderGuardian关系
    - 删除用户的所有Elder记录（如果是唯一监护人）
    - 删除用户账号
    - 记录审计日志：DATA_DELETED
    - _Requirements: 18.6, 18.10_
    - _MVP: 可选_

  - [ ] 36.3 实现GDPR数据导出
    - 创建exportUserData(Long userId)方法
    - 导出用户的所有EmotionRecord
    - 导出用户的所有AlertRecord
    - 导出用户的所有Elder记录
    - 生成JSON格式文件
    - 记录审计日志：DATA_EXPORTED
    - _Requirements: 18.7, 18.10_
    - _MVP: 可选_

  - [ ]* 36.4 编写数据保留和清理测试
    - 测试定时任务正确删除过期数据
    - 测试GDPR数据删除功能
    - 测试GDPR数据导出功能
    - _Requirements: 18.1, 18.2, 18.3, 18.6, 18.7_

- [ ] 37. 配置Swagger API文档
  - [ ] 37.1 配置Swagger UI
    - 添加Springdoc OpenAPI依赖
    - 配置API文档标题、描述、版本
    - 配置JWT认证方式
    - 配置API分组：认证、老人管理、情感分析、预警、可视化、审计、配置
    - 为所有Controller和DTO添加@Schema注解
    - 为所有API端点添加@Operation注解
    - _Requirements: 20.11_
    - _MVP: 必需_

  - [ ] 37.2 验证API文档
    - 访问/swagger-ui.html验证文档可访问
    - 验证所有API端点都有文档
    - 验证JWT认证配置正确
    - _Requirements: 20.11_
    - _MVP: 必需_

- [ ] 38. Checkpoint - 安全加固和监控验证
  - 确保安全响应头正确设置
  - 确保API限流正常工作
  - 确保监控和健康检查正常
  - 确保全局异常处理正常
  - 确保Swagger文档可访问
  - 确保所有测试通过
  - 如有问题，询问用户


### Phase 11: 集成测试与部署准备

- [ ] 39. 编写端到端集成测试
  - [ ]* 39.1 编写用户认证流程集成测试
    - 测试完整的注册-审核-登录-登出流程
    - 测试JWT Token在整个流程中的有效性
    - 测试权限控制在各个端点的正确性
    - 使用TestContainers启动MySQL和Redis容器
    - _Requirements: 1.1, 1.5, 2.6, 10.10_

  - [ ]* 39.2 编写情感分析流程集成测试
    - 测试完整的情感分析-预警创建-SSE推送流程
    - 测试数据加密存储和解密读取
    - 测试缓存失效和更新
    - 模拟AI服务响应
    - _Requirements: 5.1, 5.6, 5.7, 6.1, 6.8, 11.1_

  - [ ]* 39.3 编写数据可视化流程集成测试
    - 测试完整的数据查询-统计-缓存流程
    - 测试健康评分计算准确性
    - 测试分页查询正确性
    - _Requirements: 8.1, 8.2, 8.3, 8.7, 14.7_

  - [ ]* 39.4 编写并发场景集成测试
    - 测试多用户同时更新Elder记录（乐观锁）
    - 测试多用户同时处理Alert记录（悲观锁）
    - 测试多用户同时订阅SSE推送
    - 测试高并发情感分析请求
    - _Requirements: 19.1, 19.2, 19.3, 19.5_

  - [ ]* 39.5 编写性能测试
    - 测试认证请求响应时间 < 500ms（p95）
    - 测试情感分析请求响应时间 < 35秒
    - 测试数据可视化查询响应时间 < 2秒（p95）
    - 测试SSE推送延迟 < 100ms
    - 使用JMeter或Gatling进行压力测试
    - _Requirements: 13.1, 13.2, 13.3, 13.5_

- [ ] 40. 准备部署配置
  - [ ] 40.1 创建生产环境配置文件
    - 创建application-prod.properties
    - 配置生产数据库连接（使用环境变量）
    - 配置生产Redis连接（使用环境变量）
    - 配置JWT密钥（使用环境变量或AWS KMS）
    - 配置加密密钥（使用环境变量或AWS KMS）
    - 配置AI服务API密钥（使用环境变量）
    - 配置日志级别：INFO
    - 配置SSL/TLS：启用HTTPS
    - _Requirements: 11.5, 11.7, 20.8, 20.9_
    - _MVP: 必需_

  - [ ] 40.2 创建Docker配置
    - 创建Dockerfile：基于openjdk:17-slim
    - 配置健康检查：/actuator/health
    - 配置环境变量占位符
    - 创建docker-compose.yml：包含应用、MySQL、Redis
    - 配置网络和卷
    - _Requirements: 20.1, 20.2, 20.3, 20.6_
    - _MVP: 必需_

  - [ ] 40.3 创建数据库迁移脚本
    - 使用Flyway管理数据库版本
    - 创建V1__initial_schema.sql（表结构）
    - 创建V2__create_indexes.sql（索引）
    - 创建V3__insert_system_config.sql（初始配置）
    - _Requirements: 20.10_
    - _MVP: 必需_

  - [ ] 40.4 创建部署文档
    - 编写README.md：项目介绍、技术栈、功能特性
    - 编写DEPLOYMENT.md：部署步骤、环境要求、配置说明
    - 编写API.md：API端点列表、请求示例、响应示例
    - 编写SECURITY.md：安全特性、加密说明、权限模型
    - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.5_
    - _MVP: 必需_

- [ ] 41. 最终验证和清理
  - [ ] 41.1 运行所有测试套件
    - 运行所有单元测试
    - 运行所有集成测试
    - 运行所有API测试
    - 确保测试覆盖率 > 80%
    - _MVP: 必需_

  - [ ] 41.2 验证所有功能端点
    - 使用Postman或curl测试所有API端点
    - 验证认证和授权正常
    - 验证情感分析流程正常
    - 验证预警推送正常
    - 验证数据可视化正常
    - 验证审计日志记录正常
    - _MVP: 必需_

  - [ ] 41.3 代码质量检查
    - 运行SonarQube或Checkstyle代码质量检查
    - 修复所有Critical和Major问题
    - 确保代码符合Java编码规范
    - 移除所有TODO和FIXME注释
    - _MVP: 必需_

  - [ ] 41.4 安全扫描
    - 运行OWASP Dependency Check扫描依赖漏洞
    - 更新所有有漏洞的依赖
    - 运行安全测试验证XSS和SQL注入防护
    - _Requirements: 16.6, 16.7_
    - _MVP: 必需_

- [ ] 42. Final Checkpoint - 项目完成验证
  - 确保所有MVP必需任务完成
  - 确保所有测试通过
  - 确保代码质量达标
  - 确保安全扫描通过
  - 确保部署文档完整
  - 项目可以成功部署到生产环境
  - 如有问题，询问用户


## Notes

### MVP任务标识
- 标记为"_MVP: 必需_"的任务是最小可行产品（MVP）的核心功能，必须优先完成
- 标记为"_MVP: 可选_"的任务是增强功能，可以在MVP之后实现
- 未标记MVP的任务通常是测试任务，建议完成但可以根据时间调整

### 测试任务说明
- 所有标记为"*"的子任务都是测试相关任务（单元测试、集成测试、API测试）
- 测试任务是可选的，但强烈建议完成以确保代码质量
- 遵循TDD原则时，应该先编写测试再实现功能

### 任务依赖关系
- Phase 1必须最先完成（基础设施）
- Phase 2依赖Phase 1（安全组件需要基础设施）
- Phase 3依赖Phase 1（数据模型需要数据库）
- Phase 4依赖Phase 2和Phase 3（认证需要安全组件和数据模型）
- Phase 5依赖Phase 4（老人管理需要认证）
- Phase 6依赖Phase 2、Phase 3、Phase 5（情感分析需要加密、数据模型、老人管理）
- Phase 7依赖Phase 6（预警依赖情感分析）
- Phase 8依赖Phase 6（数据可视化依赖情感分析）
- Phase 9可以与Phase 6-8并行（审计日志和配置相对独立）
- Phase 10依赖所有前置阶段（安全加固和监控是最后的增强）
- Phase 11是最终的集成测试和部署准备

### 并行开发建议
以下任务可以并行开发（不同开发人员）：
- Phase 3（数据模型）和Phase 2（安全组件）可以并行
- Phase 5（老人管理）、Phase 9（审计日志）可以在Phase 4完成后并行
- Phase 6（情感分析）、Phase 7（预警）、Phase 8（数据可视化）可以在各自依赖满足后并行

### Checkpoint说明
- 每个Phase结束都有Checkpoint任务
- Checkpoint是验证当前阶段工作的关键节点
- 如果Checkpoint发现问题，应该立即修复后再继续
- Checkpoint时应该运行所有相关测试确保质量

### 需求追溯
- 每个任务都标注了对应的需求编号（_Requirements: X.Y_）
- 可以通过需求编号追溯到requirements.md中的具体验收标准
- 确保所有需求都被任务覆盖

### 技术栈说明
- **后端框架**: Spring Boot 4.0.6
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0
- **安全**: JWT + BCrypt + AES-256-GCM
- **测试**: JUnit 5 + Mockito + Spring Boot Test + TestContainers
- **API文档**: Springdoc OpenAPI (Swagger)
- **数据库迁移**: Flyway
- **容器化**: Docker + Docker Compose

### 预计工作量
- Phase 1-3: 基础设施和数据层 - 约15-20小时
- Phase 4: 认证授权 - 约10-15小时
- Phase 5: 老人管理 - 约8-12小时
- Phase 6: 情感分析 - 约12-18小时
- Phase 7: 预警管理 - 约10-15小时
- Phase 8: 数据可视化 - 约8-12小时
- Phase 9: 审计日志和配置 - 约6-10小时
- Phase 10: 安全加固和监控 - 约8-12小时
- Phase 11: 集成测试和部署 - 约10-15小时
- **总计**: 约87-139小时（根据团队经验和是否包含测试任务）

### 风险提示
1. **AI服务集成**: 外部AI服务可能不稳定，需要实现熔断器和重试机制
2. **性能优化**: 大量情感数据可能导致查询性能问题，需要合理使用索引和缓存
3. **并发控制**: 多用户同时操作需要正确使用乐观锁和悲观锁
4. **数据安全**: 敏感数据加密和权限控制是核心要求，不能妥协
5. **SSE推送**: 长连接管理需要注意内存泄漏和连接清理

### 下一步行动
1. 打开tasks.md文件
2. 点击任务旁边的"Start task"按钮开始执行
3. 建议从Phase 1开始，按顺序执行
4. 每完成一个Phase，运行Checkpoint验证
5. 遇到问题及时询问用户

## Task Dependency Graph

```json
{
  "waves": [
    {
      "id": 0,
      "tasks": ["1.1", "1.3"]
    },
    {
      "id": 1,
      "tasks": ["1.2", "2.1", "2.2", "2.3"]
    },
    {
      "id": 2,
      "tasks": ["2.4", "4.1", "5.1", "6.1"]
    },
    {
      "id": 3,
      "tasks": ["4.2", "5.2", "6.2", "8.1", "8.2", "8.3", "8.4", "8.5", "8.6", "8.7"]
    },
    {
      "id": 4,
      "tasks": ["8.8", "9.1", "9.2", "9.3", "9.4", "9.5", "9.6", "9.7"]
    },
    {
      "id": 5,
      "tasks": ["9.8", "11.1", "11.2"]
    },
    {
      "id": 6,
      "tasks": ["11.3", "11.4", "11.5", "11.6"]
    },
    {
      "id": 7,
      "tasks": ["11.7", "12.1", "12.2", "12.3"]
    },
    {
      "id": 8,
      "tasks": ["12.4", "13.1", "13.2", "13.3", "13.4"]
    },
    {
      "id": 9,
      "tasks": ["13.5", "15.1", "15.2"]
    },
    {
      "id": 10,
      "tasks": ["15.3", "15.4", "15.5", "15.6", "15.7"]
    },
    {
      "id": 11,
      "tasks": ["15.8", "16.1"]
    },
    {
      "id": 12,
      "tasks": ["16.2", "16.3", "16.4", "16.5"]
    },
    {
      "id": 13,
      "tasks": ["16.6", "18.1", "18.2", "18.3"]
    },
    {
      "id": 14,
      "tasks": ["18.4", "19.1", "19.2"]
    },
    {
      "id": 15,
      "tasks": ["19.3", "19.4", "19.5"]
    },
    {
      "id": 16,
      "tasks": ["19.6", "20.1"]
    },
    {
      "id": 17,
      "tasks": ["20.2", "20.3", "20.4"]
    },
    {
      "id": 18,
      "tasks": ["20.5", "22.1"]
    },
    {
      "id": 19,
      "tasks": ["22.2", "23.1", "23.2"]
    },
    {
      "id": 20,
      "tasks": ["23.3", "23.4", "23.5", "23.6"]
    },
    {
      "id": 21,
      "tasks": ["23.7", "24.1"]
    },
    {
      "id": 22,
      "tasks": ["24.2", "24.3", "24.4"]
    },
    {
      "id": 23,
      "tasks": ["24.5", "26.1", "26.2"]
    },
    {
      "id": 24,
      "tasks": ["26.3", "26.4", "26.5", "26.6"]
    },
    {
      "id": 25,
      "tasks": ["26.7", "27.1"]
    },
    {
      "id": 26,
      "tasks": ["27.2", "27.3", "27.4", "27.5"]
    },
    {
      "id": 27,
      "tasks": ["27.6", "29.1", "29.2", "30.1", "30.2"]
    },
    {
      "id": 28,
      "tasks": ["29.3", "29.4", "29.5", "30.3", "30.4", "30.5"]
    },
    {
      "id": 29,
      "tasks": ["29.6", "30.6", "31.1", "31.4"]
    },
    {
      "id": 30,
      "tasks": ["31.2", "31.3", "31.5", "31.6", "31.7"]
    },
    {
      "id": 31,
      "tasks": ["31.8", "33.1", "33.2", "33.3", "33.4"]
    },
    {
      "id": 32,
      "tasks": ["33.5", "34.1", "34.2", "34.3"]
    },
    {
      "id": 33,
      "tasks": ["34.4", "35.1"]
    },
    {
      "id": 34,
      "tasks": ["35.2", "36.1", "36.2", "36.3"]
    },
    {
      "id": 35,
      "tasks": ["36.4", "37.1", "37.2"]
    },
    {
      "id": 36,
      "tasks": ["39.1", "39.2", "39.3", "39.4", "39.5"]
    },
    {
      "id": 37,
      "tasks": ["40.1", "40.2", "40.3", "40.4"]
    },
    {
      "id": 38,
      "tasks": ["41.1", "41.2", "41.3", "41.4"]
    }
  ]
}
```
