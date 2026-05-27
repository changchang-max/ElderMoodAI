<template>
  <div>
    <div class="page-title">老人信息管理</div>
    <div class="page-card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索姓名" clearable style="width:220px" :prefix-icon="Search" @input="handleSearch" />
        <el-button type="primary" :icon="Plus" @click="openDialog()">添加老人</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ row.gender === 'male' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系方式" width="130" />
        <el-table-column label="授权状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.privacyAuthorized ? 'success' : 'info'" size="small">
              {{ row.privacyAuthorized ? '已授权' : '待授权' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="隐私授权" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.privacyAuthorized" @change="(v) => toggleAuth(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="!row.privacyAuthorized" text type="info" size="small" disabled>采集已禁用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" class="pagination" @current-change="loadData" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingElder ? '编辑老人信息' : '添加老人'" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="年龄" prop="age"><el-input-number v-model="form.age" :min="60" :max="120" style="width:100%" /></el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveElder">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const keyword = ref(''), page = ref(1), total = ref(0), loading = ref(false)
const tableData = ref([]), dialogVisible = ref(false), saving = ref(false)
const editingElder = ref(null), formRef = ref()
const form = reactive({ name: '', age: 75, gender: 'female', phone: '', address: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age:  [{ required: true, message: '请输入年龄', trigger: 'blur' }],
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await axios.get(`/api/elders?page=${page.value}&pageSize=10&keyword=${keyword.value}`)
    tableData.value = res.data.data.list
    total.value = res.data.data.total
  } finally { loading.value = false }
}

function handleSearch() { page.value = 1; loadData() }

function openDialog(elder = null) {
  editingElder.value = elder
  if (elder) Object.assign(form, elder)
  else Object.assign(form, { name: '', age: 75, gender: 'female', phone: '', address: '' })
  dialogVisible.value = true
}

async function saveElder() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingElder.value) {
      await axios.put(`/api/elders/${editingElder.value.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await axios.post('/api/elders', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}

async function toggleAuth(elder, val) {
  try {
    await ElMessageBox.confirm(`确认${val ? '开启' : '关闭'}「${elder.name}」的隐私授权？`, '提示', { type: 'warning' })
    await axios.patch(`/api/elders/${elder.id}/authorization`, { authorized: val })
    ElMessage.success(val ? '授权已开启' : '授权已关闭')
    loadData()
  } catch {
    elder.privacyAuthorized = !val
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: $spacing-md; }
.pagination { margin-top: $spacing-md; justify-content: flex-end; display: flex; }
</style>
