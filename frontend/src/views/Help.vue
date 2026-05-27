<template>
  <div>
    <div class="page-title">帮助中心</div>
    <div class="page-card">
      <el-collapse>
        <el-collapse-item title="系统介绍" name="1">
          <p>居家老人情感分析及可视化系统（ElderMoodAI）是一套面向居家养老场景的智能情感监测平台，通过多模态数据采集（语音、图像、文本）分析老人的情感状态，为家属和护理人员提供实时监测、历史趋势分析和智能预警功能。</p>
        </el-collapse-item>
        <el-collapse-item title="角色说明" name="2">
          <ul>
            <li><strong>管理员</strong>：可访问所有功能，包括用户管理、系统配置、老人信息管理等。</li>
            <li><strong>护理员</strong>：可进行情感监测、查看数据可视化、处理预警，但不能访问管理后台。</li>
            <li><strong>家属</strong>：可查看绑定老人的情感状态和历史数据，管理隐私授权。</li>
          </ul>
        </el-collapse-item>
        <el-collapse-item title="如何进行情感分析？" name="3">
          <ol>
            <li>进入「老人信息管理」，确保老人已开启隐私授权。</li>
            <li>进入「实时情感监测」，选择已授权的老人。</li>
            <li>输入文本、上传语音或图像（至少一种）。</li>
            <li>点击「开始分析」，等待2~5秒获取结果。</li>
          </ol>
        </el-collapse-item>
        <el-collapse-item title="演示账号" name="4">
          <el-table :data="demoAccounts" border>
            <el-table-column prop="role" label="角色" width="100" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="password" label="密码" width="100" />
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
const demoAccounts = [
  { role: '管理员', email: 'admin@eldermood.com', password: '123456' },
  { role: '护理员', email: 'caregiver@eldermood.com', password: '123456' },
  { role: '家属',   email: 'family@eldermood.com', password: '123456' },
]
</script>
