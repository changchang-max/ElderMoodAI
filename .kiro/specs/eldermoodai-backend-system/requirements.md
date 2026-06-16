# Requirements Document

## Introduction

ElderMoodAI（居家老人情感分析及可视化系统）后端系统是一个企业级应用，旨在为居家老人提供多模态情感分析、实时预警、数据可视化和隐私保护功能。系统基于SpringBoot 4.0.6构建，集成MySQL持久化存储、Redis缓存、外部AI服务（百度/阿里云/讯飞）、JWT身份认证和AES-256数据加密。

系统支持三种用户角色（家属/护理员/管理员），实现基于角色的访问控制（RBAC），通过SSE（Server-Sent Events）实现实时预警推送，通过审计日志记录所有敏感操作，确保老年用户敏感数据的安全性和系统的可维护性。

## Glossary

- **System**: ElderMoodAI后端系统
- **User**: 系统用户，包括家属（Guardian）、护理员（Caregiver）、管理员（Admin）
- **Elder**: 被监护的老人
- **Guardian**: 家属用户，负责监护老人
- **Caregiver**: 护理员用户，负责照护老人
- **Admin**: 管理员用户，负责系统管理
- **JWT_Token**: JSON Web Token，用于用户身份认证
- **Emotion_Record**: 情感分析记录
- **Alert_Record**: 预警记录
- **AI_Service**: 外部AI服务提供商（百度/阿里云/讯飞）
- **SSE**: Server-Sent Events，服务器推送事件
- **RBAC**: Role-Based Access Control，基于角色的访问控制
- **AES-256**: Advanced Encryption Standard 256-bit，高级加密标准
- **Audit_Log**: 审计日志
- **Redis_Cache**: Redis缓存系统
- **MySQL_Database**: MySQL数据库
- **Confidence_Score**: 置信度分数，范围0.0-1.0
- **Privacy_Mode**: 隐私保护模式
- **Authorization_Status**: 授权状态

## Requirements

### Requirement 1: 用户认证与授权

**User Story:** 作为系统用户，我希望能够安全地登录系统并获得相应的访问权限，以便我可以访问授权的功能和数据。

#### Acceptance Criteria

1. WHEN a User provides valid credentials (phone or email and password), THE System SHALL authenticate the User and generate a JWT_Token
2. WHEN a User provides invalid credentials, THE System SHALL reject the authentication and return an error message without revealing whether the User exists
3. WHEN a User's JWT_Token is valid and not expired, THE System SHALL grant access to authorized resources
4. WHEN a User's JWT_Token is expired or invalid, THE System SHALL reject the request and return 401 Unauthorized
5. WHEN a User logs out, THE System SHALL invalidate the JWT_Token in Redis_Cache
6. WHEN a User registers, THE System SHALL create a User account with status PENDING_APPROVAL
7. WHEN a User's account status is not ACTIVE, THE System SHALL reject authentication attempts
8. THE System SHALL store JWT_Token in Redis_Cache with 7-day TTL
9. THE System SHALL use BCrypt algorithm for password hashing with cost factor 12
10. THE System SHALL use HMAC-SHA256 algorithm for JWT_Token signature

### Requirement 2: 用户注册与管理

**User Story:** 作为管理员，我希望能够管理用户账号，以便控制系统访问权限和用户角色分配。

#### Acceptance Criteria

1. WHEN a new User registers, THE System SHALL validate that username is 3-50 characters
2. WHEN a new User registers, THE System SHALL validate that phone number is 11 digits or email is valid format
3. WHEN a new User registers, THE System SHALL ensure phone or email is unique in the system
4. WHEN a new User registers, THE System SHALL require at least one of phone or email
5. WHEN a new User registers, THE System SHALL validate password is minimum 8 characters with at least 1 uppercase, 1 lowercase, and 1 digit
6. WHEN an Admin approves a User account, THE System SHALL change User status from PENDING_APPROVAL to ACTIVE
7. WHEN an Admin disables a User account, THE System SHALL change User status to INACTIVE and invalidate all JWT_Tokens
8. THE System SHALL assign one of three roles to each User: GUARDIAN, CAREGIVER, or ADMIN
9. THE System SHALL record User creation and update timestamps
10. THE System SHALL create Audit_Log entries for User registration, approval, and status changes

### Requirement 3: 老人信息管理

**User Story:** 作为家属或护理员，我希望能够管理老人的基本信息，以便系统能够提供个性化的情感分析服务。

#### Acceptance Criteria

1. WHEN a User creates an Elder record, THE System SHALL validate that name is 2-50 characters
2. WHEN a User creates an Elder record, THE System SHALL validate that birthDate is earlier than current date and age is between 60-120 years
3. WHEN a User creates an Elder record, THE System SHALL validate that gender is one of MALE, FEMALE, or OTHER
4. WHEN a User creates an Elder record, THE System SHALL set privacyEnabled to true by default
5. WHEN a User updates an Elder record, THE System SHALL verify the User has permission to modify the Elder data
6. WHEN a User queries an Elder record, THE System SHALL verify the User has permission to access the Elder data
7. THE System SHALL allow healthStatus field up to 500 characters
8. THE System SHALL record Elder creation and update timestamps
9. WHEN an Elder record is created or updated, THE System SHALL create Audit_Log entry
10. THE System SHALL cache Elder information in Redis_Cache with 1-hour TTL

### Requirement 4: 监护人关系管理

**User Story:** 作为管理员，我希望能够管理老人与监护人的关系，以便控制数据访问权限。

#### Acceptance Criteria

1. WHEN a Guardian is bound to an Elder, THE System SHALL create an ElderGuardian relationship record
2. WHEN a Guardian is bound to an Elder, THE System SHALL validate that relationship description is 2-50 characters
3. WHEN a Guardian is bound to an Elder, THE System SHALL set authorized status to false by default
4. WHEN an Admin authorizes a Guardian relationship, THE System SHALL set authorized status to true
5. THE System SHALL enforce unique constraint on (elderId, guardianId) combination
6. WHEN a Guardian relationship is created, THE System SHALL record creation timestamp
7. WHEN a Guardian relationship authorization status changes, THE System SHALL invalidate permission cache for the User-Elder pair
8. THE System SHALL create Audit_Log entry when Guardian relationships are created or modified
9. WHEN a User attempts to access Elder data, THE System SHALL verify an authorized ElderGuardian relationship exists
10. THE System SHALL allow multiple Guardians to be bound to a single Elder

### Requirement 5: 多模态情感分析

**User Story:** 作为家属或护理员，我希望系统能够分析老人的情感状态，以便及时了解老人的心理健康状况。

#### Acceptance Criteria

1. WHEN a User submits emotion analysis request, THE System SHALL validate the User has permission to analyze the Elder's data
2. WHEN a User submits emotion analysis request, THE System SHALL accept one of five data types: VOICE, IMAGE, VIDEO, TEXT, or SENSOR
3. WHEN the System receives raw data, THE System SHALL call AI_Service to perform emotion analysis within 30 seconds timeout
4. WHEN AI_Service returns analysis result, THE System SHALL validate emotionType is one of HAPPY, CALM, SAD, ANXIOUS, or ANGRY
5. WHEN AI_Service returns analysis result, THE System SHALL validate Confidence_Score is between 0.0 and 1.0
6. WHEN the System receives raw data, THE System SHALL encrypt the data using AES-256-GCM before storage
7. WHEN emotion analysis completes, THE System SHALL create Emotion_Record with elderId, emotionType, Confidence_Score, dataSource, rawDataUrl, and analyzedAt
8. WHEN emotion analysis completes, THE System SHALL invalidate emotion trend cache for the Elder
9. WHEN AI_Service call fails, THE System SHALL return 503 Service Unavailable and create Audit_Log entry
10. WHEN emotion analysis completes, THE System SHALL create Audit_Log entry with action EMOTION_ANALYZED
11. THE System SHALL complete total emotion analysis workflow within 35 seconds

### Requirement 6: 智能预警管理

**User Story:** 作为家属，我希望在老人出现负面情绪时收到实时预警，以便及时采取关怀措施。

#### Acceptance Criteria

1. WHEN Emotion_Record has emotionType of SAD, ANXIOUS, or ANGRY and Confidence_Score >= 0.7, THE System SHALL create Alert_Record
2. WHEN Confidence_Score is between 0.7 and 0.85, THE System SHALL set Alert_Record severity to MEDIUM
3. WHEN Confidence_Score is between 0.85 and 0.95, THE System SHALL set Alert_Record severity to HIGH
4. WHEN Confidence_Score is >= 0.95, THE System SHALL set Alert_Record severity to CRITICAL
5. WHEN Emotion_Record has emotionType of HAPPY or CALM, THE System SHALL not create Alert_Record
6. WHEN Alert_Record is created, THE System SHALL set status to PENDING
7. WHEN Alert_Record is created, THE System SHALL find all authorized Guardians for the Elder
8. WHEN Alert_Record is created, THE System SHALL push SSE event to all authorized Guardians
9. IF email notification is enabled in system configuration, THEN THE System SHALL send email notification to all authorized Guardians
10. WHEN Alert_Record is created, THE System SHALL create Audit_Log entry with action ALERT_CREATED
11. WHEN a User handles an Alert_Record, THE System SHALL update status to HANDLED and record handledBy, handledAt, and handleNote
12. THE System SHALL allow Alert_Record message up to 500 characters
13. THE System SHALL allow handleNote up to 1000 characters

### Requirement 7: 实时推送服务

**User Story:** 作为家属，我希望能够订阅实时预警推送，以便第一时间收到老人的异常情况通知。

#### Acceptance Criteria

1. WHEN a User subscribes to alerts, THE System SHALL create SSE connection with no timeout
2. WHEN SSE connection is established, THE System SHALL send initial "connected" event
3. WHEN Alert_Record is created for an Elder, THE System SHALL push SSE event to all authorized Guardians within 100ms
4. WHEN SSE connection is closed, THE System SHALL remove the connection from active connections
5. WHEN SSE connection times out, THE System SHALL remove the connection from active connections
6. WHEN SSE connection encounters error, THE System SHALL remove the connection from active connections
7. THE System SHALL maintain concurrent map of active SSE connections indexed by userId
8. WHEN SSE push fails, THE System SHALL remove the failed connection and continue pushing to other Guardians
9. THE System SHALL support multiple concurrent SSE connections per User
10. THE System SHALL include Alert_Record details in SSE event data

### Requirement 8: 数据可视化与统计

**User Story:** 作为家属或护理员，我希望能够查看老人的情感趋势和统计数据，以便了解老人的长期心理健康状况。

#### Acceptance Criteria

1. WHEN a User requests emotion trend data, THE System SHALL verify the User has permission to access the Elder's data
2. WHEN a User requests emotion trend data, THE System SHALL accept period parameter of DAY, WEEK, or MONTH
3. WHEN a User requests emotion trend data, THE System SHALL return time series data for the specified period
4. WHEN a User requests emotion distribution data, THE System SHALL return percentage distribution of all five emotion types
5. WHEN a User requests health score, THE System SHALL calculate score between 0-100 based on emotion history
6. WHEN a User requests emotion heatmap, THE System SHALL return daily emotion data for the specified date range
7. THE System SHALL cache emotion trend data in Redis_Cache with 5-minute TTL
8. WHEN new Emotion_Record is created, THE System SHALL invalidate emotion trend cache for the Elder
9. THE System SHALL complete data visualization queries within 2 seconds (p95)
10. THE System SHALL support pagination for emotion record queries with default page size of 20
11. THE System SHALL limit date range queries to maximum 90 days

### Requirement 9: 审计日志记录

**User Story:** 作为管理员，我希望系统能够记录所有敏感操作，以便进行安全审计和合规性检查。

#### Acceptance Criteria

1. WHEN a User logs in successfully, THE System SHALL create Audit_Log entry with action LOGIN_SUCCESS
2. WHEN a User login fails, THE System SHALL create Audit_Log entry with action LOGIN_FAILED
3. WHEN a User logs out, THE System SHALL create Audit_Log entry with action LOGOUT
4. WHEN an Elder record is created, THE System SHALL create Audit_Log entry with action CREATE_ELDER
5. WHEN an Elder record is updated, THE System SHALL create Audit_Log entry with action UPDATE_ELDER
6. WHEN emotion analysis is performed, THE System SHALL create Audit_Log entry with action EMOTION_ANALYZED
7. WHEN an Alert_Record is created, THE System SHALL create Audit_Log entry with action ALERT_CREATED
8. WHEN an Alert_Record is handled, THE System SHALL create Audit_Log entry with action ALERT_HANDLED
9. THE System SHALL record userId, action, resourceType, resourceId, ipAddress, details, and createdAt in each Audit_Log
10. THE System SHALL store details field in JSON format with maximum 1000 characters
11. THE System SHALL make Audit_Log records immutable (no updates or deletes allowed)
12. WHEN an Admin queries Audit_Logs, THE System SHALL verify the User has ADMIN role
13. THE System SHALL retain Audit_Log records for minimum 90 days
14. THE System SHALL support Audit_Log export functionality for Admins

### Requirement 10: 权限控制与验证

**User Story:** 作为系统，我需要确保用户只能访问其有权限的数据，以便保护老人的隐私和数据安全。

#### Acceptance Criteria

1. WHEN a User with ADMIN role requests access to any Elder data, THE System SHALL grant access
2. WHEN a User with GUARDIAN or CAREGIVER role requests access to Elder data, THE System SHALL verify an authorized ElderGuardian relationship exists
3. WHEN a User requests access to Elder data without authorized relationship, THE System SHALL return 403 Forbidden
4. WHEN a User's account status is not ACTIVE, THE System SHALL deny all data access requests
5. WHEN a User attempts DELETE operation, THE System SHALL verify the User has ADMIN role
6. WHEN a User attempts to modify Privacy_Mode, THE System SHALL verify the User has ADMIN role or is PRIMARY_GUARDIAN
7. THE System SHALL cache permission check results in Redis_Cache with 10-minute TTL
8. WHEN ElderGuardian relationship authorization status changes, THE System SHALL invalidate permission cache
9. THE System SHALL create Audit_Log entry when permission is denied with action PERMISSION_DENIED
10. THE System SHALL validate JWT_Token on every API request before permission check

### Requirement 11: 数据加密与安全

**User Story:** 作为系统，我需要确保敏感数据的安全存储和传输，以便保护老人的隐私。

#### Acceptance Criteria

1. WHEN the System stores raw emotion data, THE System SHALL encrypt the data using AES-256-GCM algorithm
2. WHEN the System encrypts data, THE System SHALL generate random 12-byte IV for each encryption operation
3. WHEN the System encrypts data, THE System SHALL prepend IV to encrypted data before base64 encoding
4. WHEN the System decrypts data, THE System SHALL extract IV from encrypted data and use it for decryption
5. THE System SHALL store encryption key in environment variable or AWS KMS
6. THE System SHALL use 256-bit encryption key for AES-256-GCM
7. THE System SHALL enforce HTTPS for all API endpoints using TLS 1.2 or higher
8. THE System SHALL include security headers: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, Strict-Transport-Security, Content-Security-Policy
9. WHEN encryption operation fails, THE System SHALL return 500 Internal Server Error and create Audit_Log entry
10. WHEN encryption operation fails, THE System SHALL not store unencrypted data

### Requirement 12: 系统配置管理

**User Story:** 作为管理员，我希望能够配置系统参数，以便调整预警阈值和通知方式。

#### Acceptance Criteria

1. THE System SHALL store configuration in SystemConfig table with configKey, configValue, description, and updatedAt
2. THE System SHALL enforce unique constraint on configKey
3. THE System SHALL support configuration key "alert.threshold.negative_emotion" with value range 0.0-1.0
4. THE System SHALL support configuration key "alert.threshold.critical_emotion" with value range 0.0-1.0
5. THE System SHALL support configuration key "notification.email.enabled" with boolean value
6. THE System SHALL support configuration key "notification.sms.enabled" with boolean value
7. THE System SHALL support configuration key "ai.service.provider" with values baidu, aliyun, or xunfei
8. THE System SHALL support configuration key "data.retention.days" with integer value (default 365)
9. WHEN an Admin updates system configuration, THE System SHALL update updatedAt timestamp
10. WHEN an Admin updates system configuration, THE System SHALL create Audit_Log entry
11. THE System SHALL validate configKey is 5-100 characters
12. THE System SHALL validate configValue is 1-500 characters

### Requirement 13: 性能与缓存

**User Story:** 作为系统，我需要提供快速的响应时间，以便用户获得良好的使用体验。

#### Acceptance Criteria

1. THE System SHALL complete authentication requests within 500ms (p95)
2. THE System SHALL complete emotion analysis requests within 35 seconds including AI_Service call
3. THE System SHALL complete data visualization queries within 2 seconds (p95)
4. THE System SHALL complete alert creation within 1 second
5. THE System SHALL complete SSE push within 100ms
6. THE System SHALL cache JWT_Token in Redis_Cache with key format "token:{userId}" and 7-day TTL
7. THE System SHALL cache Elder information in Redis_Cache with key format "elder:info:{elderId}" and 1-hour TTL
8. THE System SHALL cache emotion trend data in Redis_Cache with key format "emotion:trend:{elderId}:{period}" and 5-minute TTL
9. THE System SHALL cache permission check results in Redis_Cache with key format "permission:{userId}:{elderId}" and 10-minute TTL
10. WHEN cached data is updated, THE System SHALL invalidate relevant cache entries
11. THE System SHALL use database connection pool with size 20
12. THE System SHALL use Redis connection pool with size 10

### Requirement 14: 数据库索引与优化

**User Story:** 作为系统，我需要优化数据库查询性能，以便支持大量数据的高效访问。

#### Acceptance Criteria

1. THE System SHALL create unique index on user(phone)
2. THE System SHALL create unique index on user(email)
3. THE System SHALL create unique index on elder_guardian(elder_id, guardian_id)
4. THE System SHALL create composite index on emotion_record(elder_id, analyzed_at)
5. THE System SHALL create composite index on alert_record(elder_id, status, created_at)
6. THE System SHALL create composite index on audit_log(user_id, created_at)
7. THE System SHALL use pagination for large result sets with default page size 20
8. THE System SHALL limit date range queries to maximum 90 days
9. THE System SHALL use database-level aggregation for statistics calculations
10. THE System SHALL use optimistic locking for Elder record updates

### Requirement 15: 错误处理与恢复

**User Story:** 作为系统，我需要优雅地处理错误情况，以便提供清晰的错误信息并保持系统稳定。

#### Acceptance Criteria

1. WHEN authentication fails, THE System SHALL return 401 Unauthorized with generic error message
2. WHEN permission is denied, THE System SHALL return 403 Forbidden with descriptive error message
3. WHEN resource is not found, THE System SHALL return 404 Not Found with descriptive error message
4. WHEN validation fails, THE System SHALL return 400 Bad Request with validation error details
5. WHEN AI_Service is unavailable, THE System SHALL return 503 Service Unavailable and retry 3 times with exponential backoff
6. WHEN database connection fails, THE System SHALL return 500 Internal Server Error without exposing database details
7. WHEN encryption fails, THE System SHALL return 500 Internal Server Error and rollback transaction
8. WHEN JWT_Token is expired, THE System SHALL return 401 Unauthorized with message "Token expired"
9. THE System SHALL implement circuit breaker pattern for AI_Service calls with 3 consecutive failure threshold
10. THE System SHALL log all errors with stack trace to application logs
11. THE System SHALL not expose sensitive information in error responses

### Requirement 16: API安全与限流

**User Story:** 作为系统，我需要防止API滥用和攻击，以便保护系统资源和数据安全。

#### Acceptance Criteria

1. THE System SHALL limit authentication endpoints to 5 requests per minute per IP address
2. THE System SHALL limit emotion analysis endpoints to 10 requests per minute per User
3. THE System SHALL limit data query endpoints to 60 requests per minute per User
4. THE System SHALL validate file upload size: maximum 10MB for images, 50MB for videos, 5MB for audio
5. THE System SHALL validate file upload MIME types: image/jpeg, image/png, video/mp4, audio/wav, audio/mp3
6. THE System SHALL sanitize all user inputs to prevent XSS attacks
7. THE System SHALL use parameterized queries to prevent SQL injection
8. THE System SHALL configure CORS to allow only frontend domain (no wildcard)
9. THE System SHALL allow CORS methods: GET, POST, PUT, DELETE
10. THE System SHALL allow CORS headers: Authorization, Content-Type
11. THE System SHALL set CORS credentials to true

### Requirement 17: 监控与告警

**User Story:** 作为管理员，我希望系统能够监控运行状态并在异常时发送告警，以便及时处理问题。

#### Acceptance Criteria

1. WHEN multiple failed login attempts occur (>5 in 5 minutes), THE System SHALL send alert to Admin
2. WHEN multiple permission denied attempts occur (>10 in 1 hour), THE System SHALL send alert to Admin
3. WHEN AI_Service failures occur (>3 consecutive failures), THE System SHALL send alert to Admin
4. WHEN database connection fails, THE System SHALL send alert to Admin
5. WHEN encryption fails, THE System SHALL send immediate alert to Admin
6. THE System SHALL expose health check endpoint at /actuator/health
7. THE System SHALL expose metrics endpoint at /actuator/metrics
8. THE System SHALL report degraded status when database is unreachable but Redis_Cache is available
9. THE System SHALL log monitoring events to application logs
10. THE System SHALL support integration with Prometheus for metrics collection

### Requirement 18: 数据保留与清理

**User Story:** 作为系统，我需要管理数据生命周期，以便遵守数据保留政策和节省存储空间。

#### Acceptance Criteria

1. THE System SHALL retain Emotion_Record for configurable days (default 365 days)
2. THE System SHALL retain Audit_Log for minimum 90 days
3. THE System SHALL retain Alert_Record for 180 days
4. THE System SHALL retain User accounts indefinitely until deletion request
5. WHEN data retention period expires, THE System SHALL archive or delete expired records
6. WHEN a User requests data deletion (GDPR right to be forgotten), THE System SHALL delete all associated data
7. WHEN a User requests data export (GDPR right to data portability), THE System SHALL export all associated data
8. THE System SHALL require privacy policy acceptance on User registration
9. THE System SHALL support consent management for data collection
10. THE System SHALL create Audit_Log entry when data is deleted or exported

### Requirement 19: 并发控制

**User Story:** 作为系统，我需要处理并发访问，以便保证数据一致性和系统稳定性。

#### Acceptance Criteria

1. WHEN multiple Users update the same Elder record concurrently, THE System SHALL use optimistic locking to prevent conflicts
2. WHEN multiple Users handle the same Alert_Record concurrently, THE System SHALL use pessimistic locking to prevent duplicate handling
3. THE System SHALL use connection pool size 20 for database connections
4. THE System SHALL use connection pool size 10 for Redis connections
5. THE System SHALL use ConcurrentHashMap for SSE connection management
6. WHEN SSE connection is added or removed, THE System SHALL ensure thread-safe operations
7. THE System SHALL support multiple concurrent emotion analysis requests per Elder
8. THE System SHALL ensure Audit_Log entries are written atomically
9. THE System SHALL use database transactions for multi-step operations
10. WHEN transaction fails, THE System SHALL rollback all changes

### Requirement 20: 部署与环境

**User Story:** 作为运维人员，我需要了解系统的部署要求，以便正确配置生产环境。

#### Acceptance Criteria

1. THE System SHALL require Java 17 or higher
2. THE System SHALL require MySQL 8.0 or higher
3. THE System SHALL require Redis 6.0 or higher
4. THE System SHALL require minimum 2GB RAM (4GB recommended)
5. THE System SHALL require minimum 10GB disk space
6. THE System SHALL use embedded Tomcat 10 as application server
7. THE System SHALL support deployment behind Nginx or Apache reverse proxy
8. THE System SHALL require SSL certificate (Let's Encrypt or commercial CA)
9. THE System SHALL support environment-specific configuration via application.properties
10. THE System SHALL support database migration via Flyway
11. THE System SHALL expose OpenAPI 3.0 documentation via Swagger UI
12. THE System SHALL support health checks for load balancer integration
