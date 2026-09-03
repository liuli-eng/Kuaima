<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Banner管理</h1>
      <p class="page-desc">管理各端首页轮播Banner，支持发布、下架、排序</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
          <el-option label="展示中" value="展示中" />
          <el-option label="草稿" value="草稿" />
          <el-option label="已下架" value="已下架" />
        </el-select>
        <el-button type="primary" style="margin-left: auto;" @click="handleCreate">
          <i class="fas fa-plus" style="margin-right:4px;"></i>新增Banner
        </el-button>
      </div>

      <div class="banner-grid">
        <div v-for="banner in filteredBanners" :key="banner.id" class="banner-card">
          <div class="banner-thumb" :style="{ background: getThumbBg(banner.id) }">
            <div class="banner-overlay">
              <span :class="['status-badge', banner.statusClass]">{{ banner.status }}</span>
            </div>
          </div>
          <div class="banner-info">
            <div class="banner-title">{{ banner.title }}</div>
            <div class="banner-meta">
              <span><i class="fas fa-mobile-alt"></i> {{ banner.position }}</span>
              <span><i class="fas fa-weight-hanging"></i> {{ banner.weight }}</span>
            </div>
            <div class="banner-time">
              {{ banner.startTime }} ~ {{ banner.endTime }}
            </div>
          </div>
          <div class="banner-actions">
            <el-button link type="primary" size="small" @click="handleEdit(banner)">编辑</el-button>
            <el-button link type="primary" size="small">预览</el-button>
            <el-button v-if="banner.status === '展示中'" link type="warning" size="small" @click="handleToggleStatus(banner)">下架</el-button>
            <el-button v-else link type="success" size="small" @click="handleToggleStatus(banner)">上架</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(banner)">删除</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBanners, createBanner, updateBanner, deleteBanner } from '@/api/content'
import { bannersData as fallbackBanners } from '@/mock'

const banners = ref([...fallbackBanners])
const statusFilter = ref('')
const apiAvailable = ref(false)

const filteredBanners = computed(() => {
  if (!statusFilter.value) return banners.value
  return banners.value.filter(b => b.status === statusFilter.value)
})

const loadData = async () => {
  try {
    const res = await listBanners()
    if (res && Array.isArray(res)) {
      banners.value = res
    } else if (res && Array.isArray(res.data)) {
      banners.value = res.data
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listBanners 后端暂未接入，使用 mock 数据')
    banners.value = [...fallbackBanners]
  }
}

const handleCreate = () => {
  ElMessageBox.prompt('请输入Banner标题', '新增Banner', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    if (!value) return
    const newBanner = {
      title: value,
      position: '零工端首页',
      weight: 5,
      startTime: new Date().toISOString().slice(0, 10),
      endTime: new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10),
      status: '草稿',
      statusClass: 'default'
    }
    try {
      if (apiAvailable.value) {
        const res = await createBanner(newBanner)
        newBanner.id = res?.id || Date.now()
      } else {
        newBanner.id = Date.now()
      }
      banners.value.unshift(newBanner)
      ElMessage.success('新增成功')
    } catch (e) {
      console.warn('[API] createBanner 后端暂未接入')
      newBanner.id = Date.now()
      banners.value.unshift(newBanner)
      ElMessage.success('已在前端添加（mock 模式）')
    }
  }).catch(() => {})
}

const handleEdit = (banner) => {
  ElMessageBox.prompt('修改标题', '编辑Banner', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: banner.title
  }).then(async ({ value }) => {
    try {
      if (apiAvailable.value) {
        await updateBanner(banner.id, { ...banner, title: value })
      } else {
        console.warn('[API] updateBanner 后端暂未接入')
      }
      banner.title = value
      ElMessage.success('修改成功')
    } catch (e) {
      console.warn('[API] updateBanner 请求失败')
      banner.title = value
      ElMessage.success('已在前端修改（mock 模式）')
    }
  }).catch(() => {})
}

const handleToggleStatus = async (banner) => {
  const newStatus = banner.status === '展示中' ? '已下架' : '展示中'
  const newClass = newStatus === '展示中' ? 'success' : 'default'
  try {
    if (apiAvailable.value) {
      await updateBanner(banner.id, { ...banner, status: newStatus, statusClass: newClass })
    } else {
      console.warn('[API] updateBanner 后端暂未接入')
    }
    banner.status = newStatus
    banner.statusClass = newClass
    ElMessage.success('状态更新成功')
  } catch (e) {
    console.warn('[API] updateBanner 请求失败')
    banner.status = newStatus
    banner.statusClass = newClass
  }
}

const handleDelete = async (banner) => {
  try {
    await ElMessageBox.confirm('确定删除该 Banner？', '提示', { type: 'warning' })
    if (apiAvailable.value) {
      await deleteBanner(banner.id)
    } else {
      console.warn('[API] deleteBanner 后端暂未接入')
    }
    banners.value = banners.value.filter(b => b.id !== banner.id)
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] deleteBanner 请求失败')
      banners.value = banners.value.filter(b => b.id !== banner.id)
    }
  }
}

const getThumbBg = (id) => {
  const colors = [
    'linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%)',
    'linear-gradient(135deg, #2563EB 0%, #60A5FA 100%)',
    'linear-gradient(135deg, #10B981 0%, #34D399 100%)',
    'linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%)'
  ]
  return colors[id % colors.length]
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.banner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.banner-card {
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  transition: box-shadow 0.2s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
}

.banner-thumb {
  height: 160px;
  position: relative;
}

.banner-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
}

.banner-info {
  padding: 16px;
}

.banner-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.banner-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  
  span {
    i { margin-right: 4px; }
  }
}

.banner-time {
  font-size: 12px;
  color: var(--text-muted);
}

.banner-actions {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  background: #FAFAFA;
}
</style>
