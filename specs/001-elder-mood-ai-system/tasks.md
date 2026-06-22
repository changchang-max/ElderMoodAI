# 任务: 居家老人情感分析及可视化系统（ElderMoodAI）

**输入**: 来自 `specs/001-elder-mood-ai-system/` 的设计文档
**前置条件**: plan.md ✅、spec.md ✅、data-model.md ✅、contracts/ ✅

> ⚠️ **范围**: 纯前端实现，使用 Mock.js 模拟所有数据，无真实后端。

## 格式: `[ID] [P] [Story] 描述`
- **[P]**: 可并行运行（不同文件，无依赖关系）
- **[Story]**: 对应用户故事（US1~US7）

---

## 阶段 1: 项目初始化与基础设置

**目的**: 创建 Vue 3 项目骨架，配置所有依赖和主题色

- [x] T001 在 `frontend/` 目录使用 Vite 初始化 Vue 3 项目（`npm create vite@latest frontend -- --template vue`）
- [x] T002 安装核心依赖：`vue-router@4`、`pinia`、`element-plus`、`echarts`、`mockjs`、`axios`、`sass`
- [x] T003 [P] 在 `frontend/src/assets/styles/variables.scss` 中配置主题色变量（主色 `#FF6B35`、夕阳金 `#F7C59F`、背景 `#f5f7fa`、卡片 `#ffffff`）
- [x] T004 [P] 在 `frontend/src/assets/styles/global.scss` 中配置全局样式（Element Plus 主题色覆盖、基础重置样式）
- [x] T005 [P] 在 `frontend/vite.config.js` 中配置路径别名（`@` → `src/`）和 SCSS 全局变量注入
- [x] T006 在 `frontend/src/main.js` 中注册 Element Plus、Pinia、Vue Router，引入全局样式

**检查点**: `npm run dev` 可正常启动，Element Plus 组件可渲染，主题色生效

---

## 阶段 2: Mock 数据层（基础，阻塞所有用户故事）

**目的**: 建立完整的 Mock 数据层，所有页面依赖此层提供数据

**⚠️ 关键**: 此阶段完成前，所有页面开发均无法进行

- [x] T007 创建 `frontend/src/mock/users.js`：预置3个账号（管理员 admin/123456、护理员 caregiver/123456、家属 family/123456），含角色、姓名、头像字段
- [x] T008 [P] 创建 `frontend/src/mock/elders.js`：生成10条老人 Mock 数据，含姓名、年龄、性别、联系方式、授权状态（6已授权/4未授权）、个人预警阈值（部分为 null）
- [x] T009 [P] 创建 `frontend/src/mock/emotions.js`：生成90条情感记录（覆盖近30天），含情感标签（开心/平静/低落/焦虑/愤怒按比例分布）、置信度、三模态得分、健康评分
- [x] T010 [P] 创建 `frontend/src/mock/alerts.js`：生成15条预警记录（10条未处理/5条已处理），含预警等级（高/中/低）、关联老人、触发时间
- [x] T011 [P] 创建 `frontend/src/mock/dashboard.js`：生成首页概览数据（今日监测人数、今日预警次数、平均健康分、系统状态、近7天趋势数据）
- [x] T012 创建 `frontend/src/mock/index.js`：使用 Mock.js 拦截所有 Axios 请求，注册所有 Mock 模块（依赖 T007~T011）
- [x] T013 创建 `frontend/src/utils/mockAnalyze.js`：实现模拟情感分析函数（`setTimeout` 2~5秒随机延迟，随机返回情感结果，焦虑/愤怒时触发预警标志）

**检查点**: 在浏览器控制台可调用 Mock 接口并获得正确格式的数据

---

## 阶段 3: 全局布局与路由（基础，阻塞所有页面）

**目的**: 实现整体框架（顶部导航 + 侧边栏 + 主内容区）和路由权限守卫

**⚠️ 关键**: 此阶段完成前，所有页面无法正确嵌入布局

- [x] T014 创建 `frontend/src/stores/auth.js`（Pinia）：管理登录状态、用户信息、角色，提供 `login()`/`logout()`/`hasPermission()` 方法，使用 `localStorage` 持久化
- [x] T015 [P] 创建 `frontend/src/stores/notification.js`（Pinia）：管理站内通知列表、未读数量，提供 `addAlert()`/`markRead()` 方法
- [x] T016 创建 `frontend/src/components/layout/AppHeader.vue`：顶部导航栏（高度60px，固定置顶），含项目名称、顶部菜单、右侧头像/用户名/消息通知图标（带未读徽章）/退出登录（依赖 T014、T015）
- [x] T017 创建 `frontend/src/components/layout/AppSidebar.vue`：左侧侧边栏（宽220px，可折叠），含8个菜单项（首页概览/实时情感监测/历史数据可视化/预警通知/老人信息管理/数据采集上传/系统设置/帮助中心），高亮当前路由，根据角色隐藏管理后台入口
- [x] T018 创建 `frontend/src/components/layout/AppLayout.vue`：整体布局容器，组合 AppHeader + AppSidebar + `<router-view>`，实现侧边栏折叠时主内容区自适应宽度
- [x] T019 创建 `frontend/src/router/index.js`：配置所有路由（`/login`、`/dashboard`、`/monitor`、`/visualization`、`/alerts`、`/elders`、`/settings`、`/help`、`/admin`），实现导航守卫（未登录跳转 `/login`，无权限跳转403页面，`/admin` 仅 admin 角色可访问）

**检查点**: 登录后可看到完整布局框架，侧边栏菜单可点击跳转，家属角色访问 `/admin` 被拦截

---

## 阶段 4: 公共组件库

**目的**: 实现所有页面复用的基础组件

- [x] T020 [P] 创建 `frontend/src/components/common/StatCard.vue`：数据总览卡片组件，接收 `title`/`value`/`icon`/`color`/`trend` props，支持数值动画效果
- [x] T021 [P] 创建 `frontend/src/components/common/EmotionTag.vue`：情感标签组件，根据情感类型显示对应颜色（开心=绿、平静=蓝、低落=灰、焦虑=橙、愤怒=红）和图标
- [x] T022 [P] 创建 `frontend/src/components/common/AlertBadge.vue`：预警等级徽章组件，高=红色、中=橙色、低=黄色
- [x] T023 [P] 创建 `frontend/src/components/charts/TrendLine.vue`：情感趋势折线图（ECharts），接收时间序列数据，支持日/周/月切换，主题色橙色
- [x] T024 [P] 创建 `frontend/src/components/charts/EmotionPie.vue`：情感占比饼图（ECharts），展示各情感类型占比，含图例
- [x] T025 [P] 创建 `frontend/src/components/charts/DailyBar.vue`：每日情感统计柱状图（ECharts），展示每日监测次数和平均健康分
- [x] T026 [P] 创建 `frontend/src/components/charts/HeatMap.vue`：时段分布热力图（ECharts），展示一天24小时各时段情感分布
- [x] T027 [P] 创建 `frontend/src/components/charts/HealthGauge.vue`：情感健康评分仪表盘（ECharts），0~100分，颜色随分值变化（绿/黄/红）

**检查点**: 所有组件可独立渲染，图表在 Mock 数据下正确展示

---

## 阶段 5: 用户故事 1 - 登录与权限管理 (P1) 🎯 MVP

**目标**: 实现完整的登录流程和三级角色权限演示

**独立测试**: 用三个预置账号分别登录，验证菜单权限差异和越权拦截

### 用户故事 1 的实施

- [x] T028 [US1] 创建 `frontend/src/views/Login.vue`：登录页（无侧边栏，居中卡片布局），含手机号+验证码和邮箱+密码两个 Tab，"获取验证码"按钮（60秒倒计时 Mock），登录按钮调用 Mock 接口，成功后跳转 `/dashboard`，失败显示错误提示
- [x] T029 [US1] 创建 `frontend/src/views/403.vue`：权限不足提示页，显示"您没有权限访问此页面"，提供返回首页按钮
- [-] T030 [US1] 在 `frontend/src/router/index.js` 中完善权限守卫逻辑：家属角色访问 `/admin` 时跳转403，未登录访问任何页面时跳转 `/login`，登录后访问 `/login` 自动跳转 `/dashboard`

**检查点**: 三个账号均可登录，家属账号访问 `/admin` 被拦截至403页面，退出登录后跳转登录页

---

## 阶段 6: 用户故事 2 - 老人信息管理 (P1) 🎯 MVP

**目标**: 实现老人信息的增删改查和隐私授权开关

**独立测试**: 添加老人、编辑信息、开关隐私授权，验证列表实时更新

### 用户故事 2 的实施

- [~] T031 [US2] 创建 `frontend/src/views/ElderManage.vue`：老人信息管理页，含顶部搜索框+添加按钮，Element Plus Table 展示老人列表（姓名/年龄/监护人/联系方式/授权状态/操作列），支持分页（每页10条）
- [~] T032 [US2] 在 `ElderManage.vue` 中实现新增/编辑弹窗（`el-dialog`）：表单含姓名、年龄、性别、联系方式、地址字段，表单验证（姓名必填、年龄60~120），提交后更新 Mock 数据并刷新列表
- [~] T033 [US2] 在 `ElderManage.vue` 中实现隐私授权开关（`el-switch`）：切换时弹出确认对话框（"确认开启/关闭隐私授权？"），确认后更新授权状态，授权时间记录为当前时间，未授权老人的操作列显示"采集已禁用"提示

**检查点**: 可完整演示添加老人→开启授权→编辑信息的完整流程，列表数据实时更新

---

## 阶段 7: 用户故事 3 - 多模态数据采集与情感分析 (P1) 🎯 MVP

**目标**: 实现数据采集 UI 和模拟情感分析结果展示

**独立测试**: 选择已授权老人，提交文本/语音/图像，等待分析结果展示

### 用户故事 3 的实施

- [~] T034 [US3] 创建 `frontend/src/views/EmotionMonitor.vue`：实时情感监测页，上半部分为采集区（老人选择下拉框，仅显示已授权老人），下半部分为结果展示区（初始为空状态）
- [~] T035 [US3] 在 `EmotionMonitor.vue` 中实现三种采集方式 UI：文本输入框（`el-input` 多行）、语音上传按钮（`el-upload`，接受 mp3/wav，显示文件名和大小，模拟上传进度条）、图像上传（`el-upload`，接受 jpg/png，显示缩略图预览）
- [~] T036 [US3] 在 `EmotionMonitor.vue` 中实现"开始分析"按钮：点击后显示加载动画（`el-progress` 进度条 + "分析中..."文字），调用 `mockAnalyze.js` 模拟2~5秒延迟，完成后展示结果
- [~] T037 [US3] 在 `EmotionMonitor.vue` 中实现分析结果展示区：显示情感标签（`EmotionTag` 组件）、置信度进度条、三模态分项得分（文本/语音/图像各一个进度条，未提交的模态显示"未采集"）、`HealthGauge` 仪表盘
- [~] T038 [US3] 在 `EmotionMonitor.vue` 中实现预警触发逻辑：分析结果为"焦虑"或"愤怒"且置信度 ≥0.7 时，调用 `notification` store 的 `addAlert()` 方法，同时弹出 `el-notification` 预警提示（红色，含老人姓名和情感状态）

**检查点**: 选择已授权老人→输入文本→点击分析→等待2~5秒→看到情感结果和健康评分；选择未授权老人时"开始分析"按钮禁用并提示

---

## 阶段 8: 用户故事 4 - 数据可视化大屏 (P2)

**目标**: 实现首页概览大屏和历史数据可视化页

**独立测试**: 查看首页4个统计卡片和图表，切换可视化页的时间范围验证图表更新

### 用户故事 4 的实施

- [~] T039 [US4] 创建 `frontend/src/views/Dashboard.vue`：首页概览大屏，顶部4个 `StatCard` 组件（今日监测人数/今日预警次数/平均情感健康分/系统运行状态），使用 `dashboard.js` Mock 数据
- [~] T040 [US4] 在 `Dashboard.vue` 中实现中部图表区：左侧 `TrendLine`（近7天情感趋势折线图，占60%宽度），右侧 `EmotionPie`（情感类型占比饼图，占40%宽度）
- [~] T041 [US4] 在 `Dashboard.vue` 中实现底部列表区：左侧最新预警列表（显示最近5条，含 `AlertBadge` 等级标识，点击跳转预警中心），右侧最近监测记录（显示最近5条，含 `EmotionTag`）
- [~] T042 [US4] 创建 `frontend/src/views/Visualization.vue`：历史数据可视化页，顶部筛选栏（老人姓名下拉 + 时间范围单选：日/周/月），筛选条件变更时所有图表数据同步更新
- [~] T043 [US4] 在 `Visualization.vue` 中实现图表区：`TrendLine`（情感趋势曲线）、`DailyBar`（每日统计柱状图）、`HeatMap`（时段分布热力图），三图表响应筛选条件
- [~] T044 [US4] 在 `Visualization.vue` 中实现底部历史记录表格：Element Plus Table，含情感标签/置信度/健康评分/分析时间列，支持分页，无数据时显示空状态提示"暂无数据，请先进行情感采集"

**检查点**: 首页图表正确展示 Mock 数据；可视化页切换时间范围后图表数据更新；无数据时显示空状态

---

## 阶段 9: 用户故事 5 - 智能预警与提醒 (P2)

**目标**: 实现预警中心页面和站内通知功能

**独立测试**: 查看预警列表，标记已处理，验证状态更新；触发情感分析预警验证通知

### 用户故事 5 的实施

- [~] T045 [US5] 创建 `frontend/src/views/AlertCenter.vue`：预警中心页，顶部筛选 Tab（全部/未处理/已处理），列表展示预警记录（预警时间/老人姓名/情感状态 `EmotionTag`/预警等级 `AlertBadge`/处理状态/操作列）
- [~] T046 [US5] 在 `AlertCenter.vue` 中实现"标记已处理"操作：点击后弹出确认对话框，确认后更新 Mock 数据中该记录状态为"已处理"，记录处理时间为当前时间，列表实时刷新
- [~] T047 [US5] 在 `AlertCenter.vue` 中实现右侧批量操作区：勾选多条未处理记录后，"批量标记已处理"按钮激活，点击后批量更新状态
- [~] T048 [US5] 完善 `AppHeader.vue` 中的消息通知功能：点击铃铛图标弹出通知下拉面板，展示最近5条预警通知（含老人姓名、情感状态、时间），未读数量徽章实时更新，点击"查看全部"跳转预警中心

**检查点**: 预警列表按状态筛选正确；标记已处理后列表状态更新；顶部通知铃铛显示未读数量

---

## 阶段 10: 用户故事 6 - 数据安全与隐私管理 (P2)

**目标**: 实现系统设置页（含隐私管理、推送配置）

**独立测试**: 操作隐私授权开关、配置推送方式（选邮件后显示授权码输入框）

### 用户故事 6 的实施

- [~] T049 [US6] 创建 `frontend/src/views/Settings.vue`：系统设置页，使用 `el-tabs` 分为4个 Tab：账号信息/预警阈值/数据隐私/推送方式
- [~] T050 [US6] 在 `Settings.vue` 的"账号信息" Tab 中实现：展示当前用户姓名/角色/手机号/邮箱，提供修改密码表单（旧密码/新密码/确认密码，Mock 验证）
- [~] T051 [US6] 在 `Settings.vue` 的"预警阈值" Tab 中实现：展示全局预警阈值配置（焦虑/低落/愤怒各情感的置信度阈值滑块），保存后 Mock 更新 SystemConfig
- [~] T052 [US6] 在 `Settings.vue` 的"数据隐私" Tab 中实现：展示数据加密状态（"AES-256 已启用"绿色标识）、数据保留期限设置、"导出我的数据"按钮（Mock 下载提示）、"删除数据"按钮（二次确认弹窗，确认后 Mock 清除数据并提示"操作已记录至审计日志"）
- [~] T053 [US6] 在 `Settings.vue` 的"推送方式" Tab 中实现：三个开关（站内信/短信/邮件），选择邮件时动态显示"邮件授权码"输入框（`el-input` type=password），保存后 Mock 更新用户推送配置

**检查点**: 四个 Tab 均可正常切换；选择邮件推送后授权码输入框出现；删除数据二次确认弹窗正常工作

---

## 阶段 11: 用户故事 7 - 管理后台 (P3)

**目标**: 实现管理员专属后台（用户管理 + 系统配置 + AI状态监控）

**独立测试**: 用管理员账号登录，禁用账号、修改预警阈值、查看AI状态

### 用户故事 7 的实施

- [~] T054 [US7] 创建 `frontend/src/views/admin/AdminPanel.vue`：管理后台主页，使用 `el-tabs` 分为3个 Tab：用户管理/系统配置/AI状态监控
- [~] T055 [US7] 在 `AdminPanel.vue` 的"用户管理" Tab 中实现：用户列表表格（姓名/角色/手机号/状态/最后登录时间/操作），操作列含"启用/禁用"开关和"修改角色"下拉，Mock 更新用户状态，禁用自己的账号时提示"不能禁用当前登录账号"
- [~] T056 [US7] 在 `AdminPanel.vue` 的"系统配置" Tab 中实现：全局预警阈值配置表单（与 Settings.vue 中的阈值设置一致，但此处为管理员视角），数据保留天数设置，保存后 Mock 更新 SystemConfig 并提示"配置已生效"
- [~] T057 [US7] 在 `AdminPanel.vue` 的"AI状态监控" Tab 中实现：服务状态卡片（正常=绿色/异常=红色）、模拟识别准确率（85%）、模拟推理耗时（1.2秒）、最近错误日志列表（Mock 数据，含时间和错误信息），"刷新状态"按钮（Mock 随机返回正常/异常状态）

**检查点**: 仅管理员账号可访问此页面；禁用账号操作有 Mock 反馈；AI状态刷新有随机状态变化效果

---

## 阶段 12: 完善与横切关注点

**目的**: 帮助中心页、移动端适配、整体体验优化

- [~] T058 [P] 创建 `frontend/src/views/Help.vue`：帮助中心页，使用 `el-collapse` 展示常见问题（FAQ），含系统介绍、角色说明、操作指引
- [~] T059 [P] 实现移动端极简布局适配：在 `AppLayout.vue` 中添加响应式断点（`<768px`），小屏时侧边栏改为汉堡菜单抽屉（`el-drawer`），图表自适应缩小，按钮放大
- [~] T060 [P] 在 `frontend/src/router/index.js` 中添加路由过渡动画（`<transition name="fade">`），页面切换时淡入淡出效果
- [~] T061 [P] 全局错误处理：在 `main.js` 中配置 Vue 全局错误处理，Mock 接口失败时显示 `el-message` 错误提示
- [~] T062 更新 `frontend/src/mock/index.js`：为所有 Mock 接口添加随机延迟（50~200ms），模拟真实网络请求感
- [~] T063 [P] 在 `frontend/public/` 中添加 `favicon.ico`（橙色主题图标），更新 `index.html` 的 `<title>` 为"居家老人情感分析及可视化系统"

---

## 依赖关系与执行顺序

### 阶段依赖关系

```
阶段1（项目初始化）
    ↓
阶段2（Mock数据层）+ 阶段3（布局路由）← 可并行
    ↓
阶段4（公共组件库）← 依赖阶段1~3
    ↓
阶段5（US1 登录）← MVP起点，依赖阶段3
阶段6（US2 老人管理）← 依赖阶段2、4
阶段7（US3 情感分析）← 依赖阶段2、4、6（需要老人数据）
    ↓
阶段8（US4 可视化）← 依赖阶段2、4
阶段9（US5 预警）← 依赖阶段2、4、7（需要情感分析触发预警）
阶段10（US6 隐私设置）← 依赖阶段3
    ↓
阶段11（US7 管理后台）← 依赖阶段2、5
    ↓
阶段12（完善）← 依赖所有阶段
```

### 用户故事依赖关系

- **US1（P1）**: 仅依赖布局和路由，可最先完成
- **US2（P1）**: 依赖 Mock 数据层，可与 US1 并行
- **US3（P1）**: 依赖 US2（需要老人列表），US2 完成后开始
- **US4（P2）**: 依赖 Mock 数据，可与 US2/US3 并行
- **US5（P2）**: 依赖 US3（情感分析触发预警），US3 完成后开始
- **US6（P2）**: 依赖布局，可与 US4 并行
- **US7（P3）**: 依赖 US1（权限守卫），US1 完成后开始

---

## 实施策略

### MVP（阶段1~7，用户故事1~3）

1. 完成阶段1：项目初始化
2. 完成阶段2+3：Mock数据层 + 布局路由
3. 完成阶段4：公共组件库
4. 完成阶段5：登录与权限 → **可演示登录流程**
5. 完成阶段6：老人信息管理 → **可演示老人管理**
6. 完成阶段7：情感分析 → **MVP完成，核心功能可演示**

### 完整交付（阶段1~12）

在 MVP 基础上依次完成阶段8~12，每个阶段完成后均可独立演示对应功能。

---

## 注意事项

- 所有 Mock 数据操作（增删改）需同步更新内存中的 Mock 数据，确保页面刷新前数据一致
- ECharts 图表需在组件 `onMounted` 后初始化，`onUnmounted` 时销毁实例，防止内存泄漏
- Element Plus 按需引入（使用 `unplugin-vue-components` 自动导入），减小打包体积
- 所有表单提交需有加载状态（`el-button :loading`），避免重复提交
- 图表组件需监听窗口 `resize` 事件，调用 `chart.resize()` 保持响应式
