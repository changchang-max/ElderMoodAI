# 🚀 ElderMoodAI 注册功能部署检查清单

## 📋 部署前检查清单

### 1. 环境准备 ✅

#### 开发工具
- [ ] JDK 17 或更高版本已安装
- [ ] Maven 3.6+ 已安装
- [ ] Node.js 16+ 已安装
- [ ] npm 或 yarn 已安装
- [ ] IDE (IntelliJ IDEA / VS Code) 已配置

#### 数据库服务
- [ ] MySQL 8.0 已安装并运行
- [ ] Redis 6.0+ 已安装并运行
- [ ] 数据库 `eldermoodai` 已创建
- [ ] 数据库用户权限已配置

#### 邮箱服务
- [ ] QQ邮箱账号可用
- [ ] QQ邮箱SMTP服务已开启
- [ ] 邮箱授权码已获取
- [ ] 授权码已配置到 `environment.env`

---

### 2. 配置文件检查 ✅

#### backend/environment.env
```bash
- [ ] QQ_EMAIL_SENDER 已配置
- [ ] QQ_EMAIL_AUTH_CODE 已配置
- [ ] MySQL_USER 已配置
- [ ] MySQL_PASSWORLD 已配置
```

#### backend/src/main/resources/application.properties
```properties
- [ ] spring.mail.username 正确
- [ ] spring.mail.password 正确
- [ ] spring.datasource.url 正确
- [ ] spring.datasource.username 正确
- [ ] spring.datasource.password 正确
- [ ] spring.data.redis.host 正确
- [ ] spring.data.redis.port 正确
```

---

### 3. 数据库初始化 ✅

```sql
-- 检查数据库
- [ ] 数据库 eldermoodai 存在
- [ ] 字符集为 utf8mb4
- [ ] 排序规则为 utf8mb4_unicode_ci

-- 检查表结构
- [ ] user 表已创建
- [ ] user 表包含所有必需字段
- [ ] username 字段有唯一索引
- [ ] email 字段有唯一索引
- [ ] Flyway迁移记录存在
```

**验证命令**:
```sql
USE eldermoodai;
SHOW TABLES;
DESCRIBE user;
SELECT * FROM flyway_schema_history;
```

---

### 4. Redis配置检查 ✅

```bash
# 检查Redis连接
- [ ] Redis服务正在运行
- [ ] Redis端口6379可访问
- [ ] Redis可以执行PING命令

# 测试Redis
redis-cli
> PING
> SET test "value"
> GET test
> DEL test
> EXIT
```

---

### 5. 后端编译和启动 ✅

```bash
# 进入后端目录
cd backend

# 清理和编译
- [ ] mvn clean 执行成功
- [ ] mvn compile 执行成功
- [ ] mvn package 执行成功
- [ ] target/backend-0.0.1-SNAPSHOT.jar 生成

# 启动应用
- [ ] mvn spring-boot:run 启动成功
- [ ] 端口8080未被占用
- [ ] 控制台无错误信息
- [ ] 看到 "Started BackendApplication"
```

**检查启动日志**:
```
- [ ] Tomcat started on port(s): 8080
- [ ] HikariPool-1 - Start completed
- [ ] Redis connection established
- [ ] Flyway migration completed
- [ ] Mapped "{[/api/auth/register]}"
- [ ] Mapped "{[/api/auth/send-verification-code]}"
```

---

### 6. 前端安装和启动 ✅

```bash
# 进入前端目录
cd frontend

# 安装依赖
- [ ] npm install 执行成功
- [ ] node_modules 目录生成
- [ ] package-lock.json 更新

# 启动开发服务器
- [ ] npm run dev 启动成功
- [ ] 端口5173未被占用
- [ ] 控制台无错误信息
- [ ] 看到 "Local: http://localhost:5173"
```

---

### 7. 功能测试 ✅

#### 7.1 访问测试
```
- [ ] 打开 http://localhost:5173
- [ ] 登录页面正常显示
- [ ] 页面布局正常
- [ ] 无JavaScript错误
```

#### 7.2 注册按钮测试
```
- [ ] "立即注册"按钮可见
- [ ] 点击按钮打开注册对话框
- [ ] 对话框包含所有字段
- [ ] 对话框样式正常
```

#### 7.3 验证码发送测试
```
- [ ] 输入有效邮箱地址
- [ ] 点击"获取验证码"按钮
- [ ] 按钮显示倒计时
- [ ] 收到成功提示
- [ ] 邮箱收到验证码邮件
- [ ] 邮件内容正确
- [ ] 验证码为6位数字
```

#### 7.4 表单验证测试
```
- [ ] 用户名为空时提示错误
- [ ] 用户名少于3字符时提示错误
- [ ] 邮箱格式错误时提示错误
- [ ] 密码为空时提示错误
- [ ] 密码少于6字符时提示错误
- [ ] 确认密码不一致时提示错误
- [ ] 验证码为空时提示错误
```

#### 7.5 注册流程测试
```
测试场景1: 正常注册
- [ ] 填写所有必填字段
- [ ] 输入正确验证码
- [ ] 点击注册按钮
- [ ] 显示注册成功提示
- [ ] 对话框自动关闭
- [ ] 邮箱自动填入登录表单

测试场景2: 重复用户名
- [ ] 使用已存在的用户名
- [ ] 显示"用户名已存在"错误

测试场景3: 重复邮箱
- [ ] 使用已注册的邮箱
- [ ] 显示"邮箱已被注册"错误

测试场景4: 错误验证码
- [ ] 输入错误的验证码
- [ ] 显示"验证码错误"提示

测试场景5: 过期验证码
- [ ] 等待5分钟后提交
- [ ] 显示"验证码已过期"提示
```

---

### 8. 数据库验证 ✅

```sql
-- 查看注册的用户
SELECT * FROM user ORDER BY created_at DESC LIMIT 10;

-- 验证字段
- [ ] username 正确
- [ ] email 正确
- [ ] password_hash 是BCrypt哈希（以$2a$开头）
- [ ] role 为 'GUARDIAN'
- [ ] status 为 'ACTIVE'
- [ ] created_at 有值
- [ ] updated_at 有值
```

---

### 9. Redis验证 ✅

```bash
# 连接Redis
redis-cli

# 检查验证码
- [ ] 发送验证码后立即检查
      KEYS verification_code:*
- [ ] 验证码存在
      GET verification_code:your-email@example.com
- [ ] 5分钟后验证码自动删除
      TTL verification_code:your-email@example.com
```

---

### 10. API接口测试 ✅

#### 使用test_registration.http

```
- [ ] 打开 test_registration.http
- [ ] 测试发送验证码接口
- [ ] 测试注册接口
- [ ] 测试各种错误场景
- [ ] 所有响应格式正确
```

#### 使用cURL测试

```bash
# 发送验证码
- [ ] curl命令执行成功
- [ ] 返回JSON格式响应
- [ ] success字段为true

# 用户注册
- [ ] curl命令执行成功
- [ ] 返回用户信息
- [ ] 密码未在响应中返回
```

---

### 11. 安全检查 ✅

```
密码安全:
- [ ] 密码在数据库中加密存储
- [ ] 密码哈希以$2a$12$开头
- [ ] 密码长度为60字符
- [ ] 密码明文未在日志中出现

验证码安全:
- [ ] 验证码在Redis中存储
- [ ] 验证码5分钟后自动过期
- [ ] 验证码使用后立即删除
- [ ] 60秒内不能重复发送

接口安全:
- [ ] 注册接口可公开访问
- [ ] 其他接口需要认证
- [ ] CORS配置正确
- [ ] 参数验证正常工作
```

---

### 12. 日志检查 ✅

```
后端日志:
- [ ] 无ERROR级别日志
- [ ] 无异常堆栈跟踪
- [ ] 邮件发送日志存在
- [ ] 用户注册日志存在

前端控制台:
- [ ] 无JavaScript错误
- [ ] 无网络请求失败
- [ ] 无警告信息
```

---

### 13. 性能检查 ✅

```
响应时间:
- [ ] 发送验证码接口 < 3秒
- [ ] 注册接口 < 2秒
- [ ] 页面加载 < 2秒

资源使用:
- [ ] 后端内存使用正常
- [ ] Redis内存使用正常
- [ ] MySQL连接数正常
- [ ] CPU使用率正常
```

---

### 14. 浏览器兼容性 ✅

```
- [ ] Chrome 最新版
- [ ] Firefox 最新版
- [ ] Edge 最新版
- [ ] Safari (Mac)
```

---

### 15. 错误处理测试 ✅

```
网络异常:
- [ ] 停止后端服务
- [ ] 前端显示友好错误提示

数据库异常:
- [ ] 停止MySQL服务
- [ ] 后端返回适当错误信息

Redis异常:
- [ ] 停止Redis服务
- [ ] 验证码功能返回错误

邮件服务异常:
- [ ] 使用错误的授权码
- [ ] 返回"邮件发送失败"提示
```

---

## ✅ 最终确认

### 核心功能
- [ ] ✅ 用户可以注册账号
- [ ] ✅ 验证码可以正常发送
- [ ] ✅ 密码正确加密存储
- [ ] ✅ 数据正确保存到数据库
- [ ] ✅ 所有验证规则生效

### 安全性
- [ ] ✅ 密码加密
- [ ] ✅ 验证码有效期
- [ ] ✅ 唯一性约束
- [ ] ✅ 输入验证

### 用户体验
- [ ] ✅ 界面友好
- [ ] ✅ 提示信息清晰
- [ ] ✅ 操作流畅
- [ ] ✅ 响应及时

---

## 🐛 常见问题快速修复

| 问题 | 检查项 | 解决方法 |
|------|--------|----------|
| 后端启动失败 | MySQL/Redis连接 | 确保服务运行，检查配置 |
| 邮件发送失败 | 授权码 | 重新获取QQ邮箱授权码 |
| 验证码未收到 | 垃圾箱 | 检查邮箱垃圾箱 |
| 注册失败 | 浏览器控制台 | 查看错误信息 |
| 密码未加密 | PasswordEncoderService | 检查Bean注入 |

---

## 📞 支持联系

如果所有检查项都已完成且测试通过，恭喜！🎉  
注册功能已成功部署并可以投入使用。

如遇到问题，请检查：
1. 后端日志文件
2. 前端浏览器控制台
3. 数据库连接状态
4. Redis连接状态

---

**部署检查完成时间**: __________  
**部署人员**: __________  
**备注**: __________
