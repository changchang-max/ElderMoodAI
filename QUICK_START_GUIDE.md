# ElderMoodAI 用户注册功能快速启动指南

## 前提条件

在启动项目之前，请确保以下服务已安装并运行：

- ✅ Java 17 或更高版本
- ✅ Node.js 16 或更高版本
- ✅ MySQL 8.0
- ✅ Redis 6.0 或更高版本
- ✅ Maven 3.6 或更高版本

## 配置检查

### 1. 数据库配置

确保MySQL数据库已创建并可访问：

```sql
CREATE DATABASE IF NOT EXISTS eldermoodai 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 2. Redis配置

确保Redis服务正在运行：

```bash
# Windows
redis-server

# 检查连接
redis-cli ping
# 应返回: PONG
```

### 3. 环境变量配置

检查 `backend/environment.env` 文件：

```env
QQ_EMAIL_SENDER="3338366373@qq.com"
QQ_EMAIL_AUTH_CODE="aytvmjfwmnhocich"
MySQL_USER="root"
MySQL_PASSWORLD="1234QwQ!"
```

**重要提示：** 
- QQ邮箱授权码需要在QQ邮箱设置中开启SMTP服务获取
- 获取方法：登录QQ邮箱 → 设置 → 账户 → POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务 → 开启SMTP服务 → 生成授权码

## 启动步骤

### 第一步：启动后端服务

```bash
# 进入后端目录
cd backend

# 使用Maven编译和运行
mvn clean install
mvn spring-boot:run

# 或者使用Maven wrapper（Windows）
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

**后端服务将在 http://localhost:8080 启动**

查看日志确认：
- ✅ 数据库连接成功
- ✅ Redis连接成功
- ✅ Flyway迁移完成
- ✅ Spring Boot应用启动成功

### 第二步：启动前端服务

打开新的终端窗口：

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

**前端服务将在 http://localhost:5173 启动**

## 功能测试

### 1. 访问登录页面

浏览器打开: http://localhost:5173

### 2. 测试注册流程

#### 步骤 1: 点击注册按钮
在登录页面底部找到"还没有账号？立即注册"链接并点击

#### 步骤 2: 填写注册信息
- **用户名**: 输入3-50个字符（例如：zhangsan123）
- **邮箱**: 输入有效的邮箱地址（例如：yourname@qq.com）

#### 步骤 3: 获取验证码
点击"获取验证码"按钮，系统会向您的邮箱发送6位数字验证码

**注意：** 
- 验证码有效期为5分钟
- 60秒内不能重复发送

#### 步骤 4: 完成注册
- **验证码**: 输入邮箱收到的6位数字
- **密码**: 输入6-50个字符的密码
- **确认密码**: 再次输入相同密码

点击"注册"按钮完成注册

#### 步骤 5: 登录
注册成功后，邮箱会自动填入登录表单，输入密码即可登录

## API测试（可选）

如果您想直接测试API接口，可以使用以下工具：

### 使用REST Client（VS Code插件）

1. 安装VS Code的REST Client插件
2. 打开 `test_registration.http` 文件
3. 点击每个请求上方的"Send Request"按钮

### 使用cURL

```bash
# 1. 发送验证码
curl -X POST http://localhost:8080/api/auth/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# 2. 注册用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"testuser",
    "email":"test@example.com",
    "password":"Test123456",
    "verificationCode":"123456"
  }'
```

### 使用Postman

1. 导入以下请求到Postman

**发送验证码：**
```
POST http://localhost:8080/api/auth/send-verification-code
Content-Type: application/json

{
  "email": "test@example.com"
}
```

**用户注册：**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123456",
  "verificationCode": "123456"
}
```

## 常见问题排查

### 问题 1: 后端启动失败

**可能原因：**
- MySQL未启动或连接失败
- Redis未启动或连接失败
- 端口8080已被占用

**解决方法：**
```bash
# 检查MySQL
mysql -u root -p
# 输入密码: 1234QwQ!

# 检查Redis
redis-cli ping

# 检查端口占用（Windows）
netstat -ano | findstr :8080
```

### 问题 2: 邮件发送失败

**可能原因：**
- QQ邮箱授权码错误
- SMTP服务未开启
- 网络连接问题

**解决方法：**
1. 重新生成QQ邮箱授权码
2. 检查 `application.properties` 中的邮箱配置
3. 查看后端日志中的详细错误信息

### 问题 3: 前端请求失败

**可能原因：**
- 后端服务未启动
- CORS配置问题
- 网络请求被拦截

**解决方法：**
1. 确认后端服务运行在 http://localhost:8080
2. 检查浏览器控制台的网络请求
3. 检查 `SecurityConfig.java` 中的CORS配置

### 问题 4: 验证码未收到

**检查步骤：**
1. 查看邮箱垃圾箱
2. 检查后端日志是否有邮件发送记录
3. 确认邮箱地址正确
4. 检查Redis中是否存储了验证码：
   ```bash
   redis-cli
   keys verification_code:*
   get verification_code:your-email@example.com
   ```

### 问题 5: 注册成功但无法登录

**可能原因：**
- 登录功能未实现
- 密码验证逻辑问题

**解决方法：**
1. 检查数据库中用户是否创建成功
   ```sql
   SELECT * FROM user WHERE email = 'your-email@example.com';
   ```
2. 确认用户状态为 'ACTIVE'
3. 查看后端日志中的登录请求处理

## 验证安装成功

✅ 后端服务正常启动（http://localhost:8080）  
✅ 前端服务正常启动（http://localhost:5173）  
✅ 可以访问登录页面  
✅ 可以点击"立即注册"打开注册对话框  
✅ 可以成功发送验证码到邮箱  
✅ 可以完成用户注册  
✅ 注册后可以在数据库中看到新用户  

## 下一步

注册功能测试通过后，您可以：

1. 实现完整的登录功能
2. 添加用户个人信息管理
3. 实现忘记密码功能
4. 添加更多的用户验证规则
5. 实现邮箱激活链接方式

## 技术支持

如遇到其他问题，请检查：

1. **后端日志**: `backend/logs/` 或控制台输出
2. **前端控制台**: 浏览器开发者工具 → Console
3. **数据库日志**: MySQL错误日志
4. **Redis日志**: Redis服务器日志

更多详细信息请参考 `REGISTRATION_FEATURE.md` 文档。
