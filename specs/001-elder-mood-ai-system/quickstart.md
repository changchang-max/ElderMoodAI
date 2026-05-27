# 快速启动指南: ElderMoodAI

**日期**: 2026-05-06

## 前置条件

- Node.js 18+
- MySQL 8.0+
- npm 9+

## 1. 克隆与安装

```bash
# 安装后端依赖
cd backend
npm install

# 安装前端依赖
cd ../frontend
npm install
```

## 2. 环境变量配置

在 `backend/` 目录创建 `.env` 文件（不提交至 Git）：

```env
# 数据库
DB_HOST=localhost
DB_PORT=3306
DB_NAME=elder_mood_ai
DB_USER=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_min_32_chars
JWT_REFRESH_SECRET=your_refresh_secret_key_min_32_chars
JWT_EXPIRES_IN=2h
JWT_REFRESH_EXPIRES_IN=7d

# AES加密密钥（32字节）
AES_SECRET_KEY=your_32_byte_aes_key_here_exactly

# 外部AI API
XUNFEI_APP_ID=your_xunfei_app_id
XUNFEI_API_KEY=your_xunfei_api_key
XUNFEI_API_SECRET=your_xunfei_api_secret
BAIDU_API_KEY=your_baidu_api_key
BAIDU_SECRET_KEY=your_baidu_secret_key

# 文件上传
UPLOAD_DIR=./uploads
MAX_FILE_SIZE_MB=50

# 服务端口
PORT=3000
```

## 3. 数据库初始化

```bash
cd backend
# 创建数据库
mysql -u root -p -e "CREATE DATABASE elder_mood_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 运行迁移（Sequelize）
npm run db:migrate

# 初始化种子数据（创建默认管理员账号）
npm run db:seed
```

默认管理员账号：
- 手机号：`13800000000`
- 密码：`Admin@123456`（首次登录后请立即修改）

## 4. 启动服务

```bash
# 启动后端（开发模式）
cd backend
npm run dev
# 后端运行在 http://localhost:3000

# 启动前端（开发模式）
cd frontend
npm run dev
# 前端运行在 http://localhost:5173
```

## 5. 核心场景验证

### 场景1：管理员登录并添加老人

1. 访问 `http://localhost:5173`，使用管理员账号登录。
2. 进入「老人信息管理」，点击「添加老人」。
3. 填写姓名、年龄等信息，提交。
4. 在老人列表中找到新添加的老人，开启「隐私授权」开关。
5. 预期：老人状态变为「已授权」，审计日志记录操作。

### 场景2：情感分析全流程

1. 以家属角色登录（需先由管理员创建家属账号并绑定老人）。
2. 进入「实时情感监测」，选择已授权的老人。
3. 输入一段文本（如："今天心情不太好，有点担心"），点击「开始分析」。
4. 预期：30秒内返回情感标签（如：焦虑）、置信度、健康评分。
5. 若置信度超过预警阈值，预期：站内预警弹窗出现，预警中心新增记录。

### 场景3：数据可视化查看

1. 进入「历史数据可视化」，选择老人和时间范围（近7天）。
2. 预期：情感趋势折线图、占比饼图、历史记录表格正常展示。
3. 切换时间范围为「月」，预期：图表数据同步更新。

### 场景4：权限越级拦截

1. 以家属角色登录。
2. 直接访问 `http://localhost:5173/admin`（管理后台路由）。
3. 预期：页面跳转至403提示，审计日志记录越权尝试。

## 6. 运行测试

```bash
# 后端单元测试 + 集成测试
cd backend
npm test

# 前端单元测试
cd frontend
npm run test
```

## 7. 生产部署注意事项

- 配置 HTTPS（TLS 1.3），不在生产环境使用 HTTP。
- 将 `.env` 中的密钥替换为强随机值（至少32字符）。
- 配置 MySQL 定期备份。
- 设置 `uploads/` 目录的访问权限，仅后端进程可读写。
- 配置审计日志自动清理任务（保留90天）。
