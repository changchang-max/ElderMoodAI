# ElderMoodAI 注册功能项目结构

## 📂 完整项目结构

```
ElderMoodAI/
│
├── 📄 REGISTRATION_FEATURE.md          # 注册功能详细说明
├── 📄 QUICK_START_GUIDE.md             # 快速启动指南
├── 📄 IMPLEMENTATION_SUMMARY.md        # 实现总结
├── 📄 PROJECT_STRUCTURE.md             # 本文件
├── 📄 test_registration.http           # API测试脚本
│
├── 📁 backend/                         # 后端项目
│   ├── 📄 pom.xml                      # ✏️ 已修改：添加邮件依赖
│   ├── 📄 environment.env              # 环境变量配置
│   │
│   └── src/main/
│       ├── java/top/publicnote/eldermoodai/backend/
│       │   │
│       │   ├── 📁 config/              # ⭐ 新增：配置类
│       │   │   ├── SecurityConfig.java     # Spring Security配置
│       │   │   └── MailConfig.java         # 邮件配置
│       │   │
│       │   ├── 📁 controller/          # ⭐ 新增：控制器
│       │   │   └── AuthController.java     # 认证控制器
│       │   │
│       │   ├── 📁 dto/                 # ⭐ 新增：数据传输对象
│       │   │   ├── ApiResponse.java        # 统一响应DTO
│       │   │   ├── RegisterRequest.java    # 注册请求DTO
│       │   │   └── SendVerificationCodeRequest.java  # 验证码请求DTO
│       │   │
│       │   ├── 📁 entity/              # 实体类（已存在）
│       │   │   └── User.java               # 用户实体
│       │   │
│       │   ├── 📁 enums/               # 枚举类（已存在）
│       │   │   ├── UserRole.java           # 用户角色枚举
│       │   │   └── UserStatus.java         # 用户状态枚举
│       │   │
│       │   ├── 📁 exception/           # 异常处理
│       │   │   ├── EncryptionException.java    # 已存在
│       │   │   ├── InvalidTokenException.java  # 已存在
│       │   │   └── GlobalExceptionHandler.java # ⭐ 新增：全局异常处理
│       │   │
│       │   ├── 📁 repository/          # 数据访问层（已存在）
│       │   │   └── UserRepository.java     # 用户Repository
│       │   │
│       │   ├── 📁 service/             # 服务层接口
│       │   │   ├── PasswordEncoderService.java         # 已存在
│       │   │   ├── UserService.java                    # ⭐ 新增
│       │   │   ├── EmailService.java                   # ⭐ 新增
│       │   │   └── VerificationCodeService.java        # ⭐ 新增
│       │   │
│       │   └── 📁 service/impl/        # 服务层实现
│       │       ├── PasswordEncoderServiceImpl.java     # 已存在
│       │       ├── UserServiceImpl.java                # ⭐ 新增
│       │       ├── EmailServiceImpl.java               # ⭐ 新增
│       │       └── VerificationCodeServiceImpl.java    # ⭐ 新增
│       │
│       └── resources/
│           ├── application.properties          # ✏️ 已修改：添加邮件配置
│           └── db/migration/
│               └── V1__initial_schema.sql      # 数据库初始化脚本
│
└── 📁 frontend/                        # 前端项目
    └── src/
        ├── 📁 api/                     # ⭐ 新增：API封装
        │   └── auth.js                     # 认证API
        │
        └── 📁 views/
            └── Login.vue                   # ✏️ 已修改：添加注册功能
```

---

## 📋 文件变更详情

### ⭐ 新增文件 (17个)

#### 后端文件 (14个)

**配置类 (2个)**
1. `config/SecurityConfig.java` - Spring Security配置
2. `config/MailConfig.java` - 邮件服务配置

**控制器 (1个)**
3. `controller/AuthController.java` - 认证控制器

**DTO (3个)**
4. `dto/ApiResponse.java` - 统一API响应
5. `dto/RegisterRequest.java` - 注册请求
6. `dto/SendVerificationCodeRequest.java` - 验证码请求

**服务接口 (3个)**
7. `service/UserService.java` - 用户服务接口
8. `service/EmailService.java` - 邮件服务接口
9. `service/VerificationCodeService.java` - 验证码服务接口

**服务实现 (3个)**
10. `service/impl/UserServiceImpl.java` - 用户服务实现
11. `service/impl/EmailServiceImpl.java` - 邮件服务实现
12. `service/impl/VerificationCodeServiceImpl.java` - 验证码服务实现

**异常处理 (1个)**
13. `exception/GlobalExceptionHandler.java` - 全局异常处理器

**测试文件 (1个)**
14. `test_registration.http` - API测试脚本

#### 前端文件 (1个)
15. `src/api/auth.js` - 认证API封装

#### 文档文件 (4个)
16. `REGISTRATION_FEATURE.md` - 功能详细说明
17. `QUICK_START_GUIDE.md` - 快速启动指南
18. `IMPLEMENTATION_SUMMARY.md` - 实现总结
19. `PROJECT_STRUCTURE.md` - 项目结构说明

### ✏️ 修改文件 (3个)

1. **backend/pom.xml**
   - 添加 `spring-boot-starter-mail` 依赖

2. **backend/src/main/resources/application.properties**
   - 添加邮件服务配置
   - 配置QQ邮箱SMTP

3. **frontend/src/views/Login.vue**
   - 添加注册对话框UI
   - 添加注册相关逻辑
   - 添加验证码发送功能

---

## 🔗 文件依赖关系图

```
AuthController
    ├── 依赖 → UserService
    │          ├── 依赖 → UserRepository
    │          ├── 依赖 → PasswordEncoderService
    │          ├── 依赖 → VerificationCodeService
    │          └── 依赖 → EmailService
    │
    ├── 使用 → RegisterRequest (DTO)
    ├── 使用 → SendVerificationCodeRequest (DTO)
    └── 返回 → ApiResponse (DTO)

EmailService
    └── 使用 → JavaMailSender (Spring Mail)

VerificationCodeService
    └── 使用 → StringRedisTemplate (Redis)

PasswordEncoderService
    └── 使用 → BCryptPasswordEncoder (Spring Security)

UserRepository
    └── 操作 → User (Entity)

SecurityConfig
    ├── 配置 → SecurityFilterChain
    ├── 配置 → CorsConfiguration
    └── 创建 → BCryptPasswordEncoder Bean

MailConfig
    └── 创建 → JavaMailSender Bean

Login.vue (前端)
    └── 调用 → /api/auth/* (后端接口)
```

---

## 📦 核心依赖

### Maven依赖
```xml
<!-- 新增依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- 已有依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

## 🎯 功能流程图

```
用户操作流程:
    1. 访问登录页面
    2. 点击"立即注册"
    3. 输入用户名和邮箱
    4. 点击"获取验证码"
    5. 接收邮箱验证码
    6. 填写完整信息
    7. 提交注册
    8. 注册成功

后端处理流程:
    发送验证码:
        AuthController.sendVerificationCode()
            → UserService.sendRegistrationCode()
                → 检查邮箱是否已注册
                → VerificationCodeService.generateAndStoreCode()
                    → 生成6位随机码
                    → 存入Redis (5分钟TTL)
                → EmailService.sendVerificationCode()
                    → 通过QQ邮箱SMTP发送

    用户注册:
        AuthController.register()
            → UserService.register()
                → VerificationCodeService.verifyCode()
                    → 从Redis验证验证码
                → 检查用户名是否存在
                → 检查邮箱是否存在
                → PasswordEncoderService.encode()
                    → BCrypt加密密码
                → UserRepository.save()
                    → 保存到MySQL
                → VerificationCodeService.deleteCode()
                    → 删除已用验证码
```

---

## 🗄️ 数据流转

```
前端 (Login.vue)
    ↓ HTTP POST /api/auth/send-verification-code
    ↓ { email: "user@example.com" }
    ↓
后端 (AuthController)
    ↓
服务层 (UserService → VerificationCodeService)
    ↓
Redis (存储验证码, key: "verification_code:email", TTL: 5min)
    ↓
邮件服务 (EmailService)
    ↓
QQ邮箱SMTP (发送验证码邮件)
    ↓
用户邮箱 (接收验证码)

---

前端 (Login.vue)
    ↓ HTTP POST /api/auth/register
    ↓ { username, email, password, verificationCode }
    ↓
后端 (AuthController)
    ↓
服务层 (UserService → PasswordEncoderService)
    ↓
MySQL (user表, password_hash存储加密密码)
    ↓
返回 User对象
```

---

## 🔐 安全架构

```
请求 → SecurityFilterChain
         ├── 公开路径
         │   ├── /auth/register          ✅ 允许
         │   ├── /auth/send-verification-code  ✅ 允许
         │   └── /auth/login             ✅ 允许
         │
         └── 受保护路径
             └── 其他所有路径             🔒 需要认证

密码安全:
    原始密码 → BCryptPasswordEncoder(strength=12)
              → $2a$12$... (60字符哈希)
              → 存入数据库 password_hash字段

验证码安全:
    随机生成 → SecureRandom (6位数字)
            → Redis (key: verification_code:email, TTL: 5分钟)
            → 使用后删除
```

---

## 📊 技术栈总览

| 层级 | 技术 | 用途 |
|------|------|------|
| **前端** | Vue 3 | 前端框架 |
| | Element Plus | UI组件库 |
| | Axios | HTTP请求 |
| **后端** | Spring Boot 4.0.6 | 应用框架 |
| | Spring Security | 安全认证 |
| | Spring Mail | 邮件发送 |
| | Spring Data JPA | ORM框架 |
| | Spring Data Redis | Redis操作 |
| **数据库** | MySQL 8.0 | 关系型数据库 |
| | Redis | 缓存/会话存储 |
| **安全** | BCrypt | 密码加密 |
| | JWT | Token认证 |
| **通信** | QQ邮箱SMTP | 邮件服务 |

---

## 🎓 核心设计模式

1. **MVC模式**: Controller → Service → Repository
2. **DTO模式**: 数据传输对象封装
3. **依赖注入**: Spring IoC容器管理
4. **策略模式**: PasswordEncoderService接口
5. **单例模式**: Service层Bean
6. **工厂模式**: BCryptPasswordEncoder创建

---

## 📈 性能考虑

| 组件 | 优化措施 |
|------|----------|
| **Redis** | 验证码5分钟自动过期，减少存储 |
| **BCrypt** | Cost factor=12，平衡安全与性能 |
| **邮件** | 异步发送（可进一步优化） |
| **数据库** | 用户名和邮箱字段建立唯一索引 |
| **验证** | 60秒倒计时防止频繁请求 |

---

这个项目结构展示了完整的用户注册功能实现，所有文件和依赖关系都清晰可见。
