<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">403</div>
      <h2>您没有权限访问此页面</h2>
      <p>当前账号角色无法访问该功能，请联系管理员</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.error-page {
  min-height: 60vh; display: flex; align-items: center; justify-content: center;
  .error-content { text-align: center;
    .error-code { font-size: 80px; font-weight: 900; color: $primary-color; line-height: 1; }
    h2 { font-size: 22px; color: $text-primary; margin: $spacing-md 0 $spacing-sm; }
    p { color: $text-secondary; margin-bottom: $spacing-lg; }
  }
}
</style>
