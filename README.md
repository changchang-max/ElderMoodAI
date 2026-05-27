# ElderMoodAI 居家老人情感分析系统

> Elderly Emotion Analysis & Visualization System

[English](#english) | [中文](#中文)

---

## 🌐 Language / 语言切换

<!-- lang-switch -->
<div id="lang-toggle" style="margin: 20px 0; padding: 15px; background: #f5f5f5; border-radius: 8px;">
  <button onclick="toggleLanguage()" style="padding: 8px 16px; cursor: pointer; border: 1px solid #ccc; background: white; border-radius: 4px;">
    🌐 Switch Language / 切换语言
  </button>
</div>

<script>
const translations = {
  en: {
    projectName: "ElderMoodAI",
    subtitle: "Elderly Emotion Analysis & Visualization System",
    features: "Features",
    quickStart: "Quick Start",
    techStack: "Tech Stack",
    projectStructure: "Project Structure",
    contribution: "Contribution",
    license: "License",
    
    feature1Title: "Multi-modal Emotion Analysis",
    feature1Desc: "Supports voice, image, video, and text input for comprehensive emotion analysis",
    
    feature2Title: "Real-time Visualization",
    feature2Desc: "Interactive dashboards showing emotion trends, distribution charts, and health scores",
    
    feature3Title: "Smart Alerts",
    feature3Desc: "Automatic notifications when negative emotions are detected",
    
    feature4Title: "Role-based Access",
    feature4Desc: "Fine-grained permissions for family members, caregivers, and administrators",
    
    installDeps: "Install Dependencies",
    startDev: "Start Development Server",
    buildProj: "Build for Production",
    
    techVue3: "Vue 3 Composition API",
    techVite: "Vite Build Tool",
    techPinia: "Pinia State Management",
    techRouter: "Vue Router",
    techECharts: "ECharts Visualization",
    techSCSS: "SCSS Styling",
    
    prerequisite: "Prerequisites",
    nodeVersion: "Node.js 16+",
    pnpm: "pnpm (recommended)",
    
    clone: "Clone the repository",
    cdFrontend: "Navigate to frontend directory",
    install: "Install dependencies",
    start: "Start the development server",
    access: "Access the application"
  },
  zh: {
    projectName: "ElderMoodAI",
    subtitle: "居家老人情感分析与可视化系统",
    features: "功能特点",
    quickStart: "快速开始",
    techStack: "技术栈",
    projectStructure: "项目结构",
    contribution: "贡献指南",
    license: "许可证",
    
    feature1Title: "多模态情感分析",
    feature1Desc: "支持语音、图像、视频和文本输入，进行全面的情感分析",
    
    feature2Title: "实时可视化",
    feature2Desc: "交互式仪表盘展示情感趋势、分布图表和健康评分",
    
    feature3Title: "智能预警",
    feature3Desc: "检测到负面情绪时自动发送通知提醒",
    
    feature4Title: "角色权限管理",
    feature4Desc: "为家属、护理员和管理员提供细粒度的权限控制",
    
    installDeps: "安装依赖",
    startDev: "启动开发服务器",
    buildProj: "构建生产版本",
    
    techVue3: "Vue 3 组合式API",
    techVite: "Vite 构建工具",
    techPinia: "Pinia 状态管理",
    techRouter: "Vue Router 路由",
    techECharts: "ECharts 可视化",
    techSCSS: "SCSS 样式",
    
    prerequisite: "前置要求",
    nodeVersion: "Node.js 16+",
    pnpm: "pnpm (推荐)",
    
    clone: "克隆仓库",
    cdFrontend: "进入前端目录",
    install: "安装依赖",
    start: "启动开发服务器",
    access: "访问应用"
  }
};

let currentLang = 'zh';

function toggleLanguage() {
  currentLang = currentLang === 'zh' ? 'en' : 'zh';
  document.getElementById('lang-label').textContent = currentLang === 'zh' ? '当前: 中文' : 'Current: English';
  renderContent();
}

function renderContent() {
  const t = translations[currentLang];
  
  // Project Title
  document.getElementById('project-name').textContent = t.projectName;
  document.getElementById('subtitle').textContent = t.subtitle;
  
  // Sections
  document.getElementById('section-features').textContent = t.features;
  document.getElementById('section-quickstart').textContent = t.quickStart;
  document.getElementById('section-tech').textContent = t.techStack;
  document.getElementById('section-structure').textContent = t.projectStructure;
  document.getElementById('section-contribution').textContent = t.contribution;
  document.getElementById('section-license').textContent = t.license;
  
  // Features
  document.getElementById('feature1-title').textContent = t.feature1Title;
  document.getElementById('feature1-desc').textContent = t.feature1Desc;
  document.getElementById('feature2-title').textContent = t.feature2Title;
  document.getElementById('feature2-desc').textContent = t.feature2Desc;
  document.getElementById('feature3-title').textContent = t.feature3Title;
  document.getElementById('feature3-desc').textContent = t.feature3Desc;
  document.getElementById('feature4-title').textContent = t.feature4Title;
  document.getElementById('feature4-desc').textContent = t.feature4Desc;
  
  // Quick Start
  document.getElementById('prerequisite-label').textContent = t.prerequisite;
  document.getElementById('node-version').textContent = t.nodeVersion;
  document.getElementById('pnpm-rec').textContent = t.pnpm;
  document.getElementById('step1').textContent = t.clone;
  document.getElementById('step2').textContent = t.cdFrontend;
  document.getElementById('step3').textContent = t.install;
  document.getElementById('step4').textContent = t.start;
  document.getElementById('step5').textContent = t.access;
  
  // Tech Stack
  document.getElementById('tech1').textContent = t.techVue3;
  document.getElementById('tech2').textContent = t.techVite;
  document.getElementById('tech3').textContent = t.techPinia;
  document.getElementById('tech4').textContent = t.techRouter;
  document.getElementById('tech5').textContent = t.techECharts;
  document.getElementById('tech6').textContent = t.techSCSS;
}

// Initialize
document.addEventListener('DOMContentLoaded', renderContent);
</script>

---

## 📋 功能特点 / Features

### 🔹 <span id="feature1-title">多模态情感分析</span>
<span id="feature1-desc">支持语音、图像、视频和文本输入，进行全面的情感分析</span>

### 🔹 <span id="feature2-title">实时可视化</span>
<span id="feature2-desc">交互式仪表盘展示情感趋势、分布图表和健康评分</span>

### 🔹 <span id="feature3-title">智能预警</span>
<span id="feature3-desc">检测到负面情绪时自动发送通知提醒</span>

### 🔹 <span id="feature4-title">角色权限管理</span>
<span id="feature4-desc">为家属、护理员和管理员提供细粒度的权限控制</span>

---

## 🚀 快速开始 / Quick Start

### <span id="prerequisite-label">前置要求 / Prerequisites</span>

- <span id="node-version">Node.js 16+</span>
- <span id="pnpm-rec">pnpm (推荐 / recommended)</span>

### 安装与启动 / Installation & Running

```bash
# 1. 克隆仓库 / Clone the repository
git clone https://github.com/changchang-max/ElderMoodAI.git
cd ElderMoodAI

# 2. 进入前端目录 / Navigate to frontend
cd frontend

# 3. 安装依赖 / Install dependencies
pnpm install
# 或 / or
npm install

# 4. 启动开发服务器 / Start development server
pnpm dev
# 或 / or
npm run dev

# 5. 访问应用 / Access the application
# 打开浏览器访问 / Open browser at: http://localhost:5173
```

### 构建生产版本 / Build for Production

```bash
pnpm build
# 或 / or
npm run build
```

---

## 🛠️ 技术栈 / Tech Stack

| 技术 / Tech | 用途 / Usage |
|-------------|--------------|
| <span id="tech1">Vue 3 组合式API</span> | 前端框架 / Frontend Framework |
| <span id="tech2">Vite</span> | 构建工具 / Build Tool |
| <span id="tech3">Pinia</span> | 状态管理 / State Management |
| <span id="tech4">Vue Router</span> | 路由管理 / Routing |
| <span id="tech5">ECharts</span> | 数据可视化 / Data Visualization |
| <span id="tech6">SCSS</span> | 样式预处理 / Styling |

---

## 📁 项目结构 / Project Structure

```
ElderMoodAI/
├── frontend/                 # 前端项目 / Frontend Project
│   ├── src/
│   │   ├── assets/          # 静态资源 / Static Assets
│   │   ├── components/      # 组件 / Components
│   │   │   ├── charts/      # 图表组件 / Chart Components
│   │   │   ├── common/      # 通用组件 / Common Components
│   │   │   └── layout/      # 布局组件 / Layout Components
│   │   ├── views/           # 页面视图 / Page Views
│   │   ├── stores/          # 状态存储 / State Stores
│   │   ├── router/          # 路由配置 / Router Config
│   │   ├── utils/           # 工具函数 / Utilities
│   │   └── mock/            # 模拟数据 / Mock Data
│   └── package.json
├── specs/                   # 项目规范 / Project Specifications
│   └── 001-elder-mood-ai-system/
└── README.md
```

---

## 📄 许可证 / License

MIT License

---

*<span id="lang-label">当前: 中文</span>*