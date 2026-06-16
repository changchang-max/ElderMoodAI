# 用户注册功能说明

## 功能概述

本系统已实现通过QQ邮箱验证码的用户注册功能。用户可以在登录页面点击"立即注册"按钮，填写注册信息并接收邮箱验证码完成注册。

## 功能特性

### 1. 前端功能
- ✅ 在登录页面新增"还没有账号？立即注册"按钮
- ✅ 注册对话框包含以下字段：
  - 用户名（3-50字符）
  - 邮箱地址
  - 验证码（6位数字）
  - 密码（6-50字符）
  - 确认密码
- ✅ 实时表单验证
- ✅ 验证码60秒倒计时功能
- ✅ 注册成功后自动填充邮箱到登录表单

### 2. 后端功能
- ✅ RESTful API接口：
  - `POST /api/auth/send-verification-code` - 发送验证码
  - `POST /api/auth/register` - 用户注册
- ✅ 邮箱验证码生成与发送（使用QQ邮箱SMTP服务）
- ✅ 验证码存储在Redis中，5分钟有效期
- ✅ 密码使用BCrypt加密（cost factor=12）
- ✅ 用户名和邮箱唯一性验证
- ✅ 注册后用户默认角色为GUARDIAN（家属）
- ✅ 注册后用户状态为ACTIVE（活跃）

## 技术实现

### 依赖配置

#### Maven依赖（pom.xml）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### 邮件服务配置（application.properties）
```properties
# Mail Configuration (QQ Mail)
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=${QQ_EMAIL_SENDER:3338366373@qq.com}
spring.mail.password=${QQ_EMAIL_AUTH_CODE:aytvmjfwmnhocich}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.qq.com
spring.mail.default-encoding=UTF-8
```

### 核心类说明

#### 后端类
1. **Controller层**
   - `AuthController.java` - 认证控制器，处理注册和验证码请求

2. **Service层**
   - `UserService.java` / `UserServiceImpl.java` - 用户服务
   - `EmailService.java` / `EmailServiceImpl.java` - 邮件服务
   - `VerificationCodeService.java` / `VerificationCodeServiceImpl.java` - 验证码服务
   - `PasswordEncoderService.java` / `PasswordEncoderServiceImpl.java` - 密码加密服务

3. **DTO层**
   - `RegisterRequest.java` - 注册请求DTO
   - `SendVerificationCodeRequest.java` - 发送验证码请求DTO
   - `ApiResponse.java` - 统一API响应DTO

4. **Config层**
   - `SecurityConfig.java` - Spring Security配置，允许注册端点公开访问

#### 前端组件
- `Login.vue` - 登录页面，包含注册对话框

## 环境变量配置

确保 `backend/environment.env` 文件包含以下配置：

```env
QQ_EMAIL_SENDER="3338366373@qq.com"
QQ_EMAIL_AUTH_CODE="aytvmjfwmnhocich"
MySQL_USER="root"
MySQL_PASSWORLD="1234QwQ!"
```

## 使用流程

### 用户注册流程
1. 用户打开登录页面
2. 点击"还没有账号？立即注册"按钮
3. 在注册对话框中填写：
   - 用户名
   - 邮箱地址
4. 点击"获取验证码"按钮
5. 系统向用户邮箱发送6位数字验证码
6. 用户在邮箱中查看验证码
7. 在注册表单中输入验证码、密码和确认密码
8. 点击"注册"按钮
9. 系统验证信息并创建用户账号
10. 注册成功后，邮箱自动填入登录表单

### API接口示例

#### 1. 发送验证码
```bash
POST /api/auth/send-verification-code
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "验证码已发送，请查收邮箱",
  "data": null
}
```

#### 2. 用户注册
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "user@example.com",
  "password": "password123",
  "verificationCode": "123456"
}
```

**响应示例：**
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "user@example.com",
    "role": "GUARDIAN",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T12:00:00"
  }
}
```

## 安全特性

1. **密码安全**
   - 使用BCrypt算法加密密码
   - Cost factor设置为12，提供强安全性
   - 密码明文永不存储

2. **验证码安全**
   - 6位随机数字验证码
   - 存储在Redis中，5分钟自动过期
   - 验证成功后立即删除

3. **输入验证**
   - 用户名长度限制（3-50字符）
   - 密码长度限制（6-50字符）
   - 邮箱格式验证
   - 前后端双重验证

4. **唯一性检查**
   - 用户名唯一性验证
   - 邮箱唯一性验证

## 注意事项

1. **邮件服务**
   - 确保QQ邮箱授权码有效
   - 检查SMTP服务器连接
   - 注意QQ邮箱发送频率限制

2. **Redis服务**
   - 确保Redis服务正常运行
   - 验证码默认存储在Redis database 0

3. **MySQL数据库**
   - 确保user表已创建
   - 检查数据库连接配置

## 测试建议

1. 测试正常注册流程
2. 测试重复用户名注册
3. 测试重复邮箱注册
4. 测试错误验证码
5. 测试过期验证码（5分钟后）
6. 测试密码加密是否正确
7. 测试邮件发送功能

## 未来改进建议

1. 添加图形验证码防止机器人注册
2. 添加手机号注册方式
3. 增强密码强度验证（大小写、数字、特殊字符）
4. 实现邮箱验证链接方式
5. 添加注册日志审计
6. 实现用户协议和隐私政策确认
