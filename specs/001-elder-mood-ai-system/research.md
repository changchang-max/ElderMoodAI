# 研究报告: 居家老人情感分析及可视化系统

**日期**: 2026-05-06
**分支**: `001-elder-mood-ai-system`

## 1. 外部AI情感分析API选型

**Decision**: 主选**讯飞开放平台**情感分析API，备选百度AI情感倾向分析。

**Rationale**:
- 讯飞在语音识别和情感分析领域有成熟的中文支持，适合老年用户的普通话/方言场景。
- 提供语音情感分析（声学特征）、文本情感分析两个独立接口，与本系统多模态需求匹配。
- 图像/表情分析可使用百度AI人脸情绪识别API（讯飞图像情感能力较弱）。
- 两者均提供免费额度，适合开发阶段使用。

**Alternatives considered**:
- 阿里云NLP情感分析：文本情感较强，但语音和图像能力不如讯飞+百度组合。
- 自研模型：成本过高，超出项目范围（已在澄清中排除）。

**集成方案**:
- 后端 `emotionService.js` 统一封装三个API调用（语音→讯飞、图像→百度、文本→讯飞/百度）。
- API Key 通过环境变量注入，禁止硬编码（章程安全要求）。
- 实现故障降级：某模态API不可用时，返回该模态"不可用"状态，不影响其他模态结果。

---

## 2. 前端技术栈确认

**Decision**: Vue 3 + Element Plus + ECharts 5 + Pinia

**Rationale**:
- Vue 3 Composition API 适合复杂状态管理（多角色权限、实时数据更新）。
- Element Plus 提供完整的后台管理组件库，与页面布局文档（左右分栏+顶部导航）高度匹配。
- ECharts 5 支持折线图、饼图、柱状图、热力图、仪表盘，覆盖所有可视化需求。
- Pinia 替代 Vuex，更轻量，适合中型项目。

**Alternatives considered**:
- React + Ant Design：功能相当，但团队熟悉度不如 Vue + Element Plus。
- 纯 CSS 自定义图表：开发成本过高，ECharts 已是最简方案。

---

## 3. 后端技术栈确认

**Decision**: Node.js 18 + Express 4 + Sequelize ORM + MySQL 8.0

**Rationale**:
- Express 轻量，适合 REST API 开发，无需 Spring Boot 级别的复杂度（简单性原则）。
- Sequelize 提供 ORM 支持，简化多对多关联（ElderGuardian）和迁移管理。
- MySQL 8.0 支持 JSON 字段（存储情感分析原始结果）、窗口函数（统计查询）。
- Node.js 与前端同语言，降低全栈开发切换成本。

**Alternatives considered**:
- Python + FastAPI：AI相关项目常用，但本项目AI能力外包给API，无需Python生态。
- MongoDB：文档型数据库对关系型数据（用户-老人-权限）不如MySQL直观。

---

## 4. 认证与权限方案

**Decision**: JWT（无状态）+ 角色中间件 + 路由级权限守卫

**Rationale**:
- JWT 适合前后端分离架构，无需服务端Session存储。
- Access Token（2小时）+ Refresh Token（7天）双Token方案，平衡安全性与用户体验。
- 后端中间件统一校验角色权限，前端路由守卫控制页面访问。
- 所有权限校验失败记录至审计日志（章程原则II要求）。

**Alternatives considered**:
- Session + Cookie：有状态，不适合未来可能的多实例部署。
- OAuth2：过于复杂，本系统无第三方登录需求（简单性原则）。

---

## 5. 数据加密方案

**Decision**: 静态数据字段级AES-256加密（敏感字段）+ TLS 1.3传输加密

**Rationale**:
- 对情感记录、老人个人信息等敏感字段在数据库层面加密存储（章程原则I强制要求）。
- 使用 Node.js `crypto` 模块实现 AES-256-GCM 加密，密钥通过环境变量管理。
- 生产环境部署时配置 HTTPS（TLS 1.3），开发环境可使用 HTTP。

**Alternatives considered**:
- 全库加密（透明数据加密TDE）：需要数据库企业版，成本过高。
- 仅传输加密：不满足章程"静态数据加密不低于AES-256"要求。

---

## 6. 文件存储方案

**Decision**: 本地文件系统存储（`uploads/` 目录）+ 文件名哈希化

**Rationale**:
- 章程原则I要求数据在本地或受控环境处理，禁止上传至第三方云存储。
- 使用 multer 处理文件上传，文件名用 UUID 哈希化防止路径遍历攻击。
- 文件访问通过后端接口代理，不直接暴露文件路径。

**Alternatives considered**:
- 阿里云OSS / 腾讯云COS：违反章程隐私优先原则，排除。
- 数据库BLOB存储：大文件存数据库性能差，排除。

---

## 7. 推送通知方案

**Decision**: 站内信（WebSocket/轮询）+ 邮件（Nodemailer）+ 短信（第三方SMS API）

**Rationale**:
- 站内信使用 Server-Sent Events（SSE）实现实时推送，比 WebSocket 更简单（简单性原则）。
- 邮件使用 Nodemailer + SMTP，支持用户配置邮件授权码（页面布局文档要求）。
- 短信使用阿里云SMS或腾讯云SMS，按需集成。
- 三种方式均可在系统设置中独立开关。

**Alternatives considered**:
- WebSocket：双向通信对本场景（服务端推送）过于复杂，SSE 已足够。
- 第三方推送服务（极光/个推）：增加外部依赖，短信API已满足需求。
