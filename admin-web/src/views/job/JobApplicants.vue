<template>
  <div>
    <div class="page-header">
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-button text @click="router.back()"><i class="fas fa-arrow-left"></i></el-button>
        <div>
          <h1 class="page-title">报名人员管理</h1>
          <p class="page-desc">招工ID：{{ route.params.id }} - 查看和管理报名零工</p>
        </div>
      </div>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待确认" name="pending">
          <el-table :data="pagedPending" stripe>
            <el-table-column label="用户" min-width="160">
              <template #default="{ row }">
                <div class="user-cell">
                  <span class="mini-avatar">{{ row.avatar }}</span>
                  <div>
                    <div style="font-weight: 500;">{{ row.name }}</div>
                    <div class="text-muted" style="font-size: 12px;">{{ row.phone }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="realName" label="实名" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.realName === '已认证'" type="success" effect="light">已认证</el-tag>
                <el-tag v-else type="info" effect="light">未认证</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="技能标签" min-width="140">
              <template #default="{ row }">
                <el-tag v-for="skill in row.skills" :key="skill" size="small" type="warning" style="margin-right: 4px;">{{ skill }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditScore" label="信用分" width="100" />
            <el-table-column prop="applyTime" label="报名时间" width="160" />
            <el-table-column label="操作" width="200">
              <template #default>
                <el-button link type="primary" size="small">查看资料</el-button>
                <el-button link type="success" size="small">录用</el-button>
                <el-button link type="danger" size="small">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">共 {{ pendingList.length }} 条记录</div>
            <el-pagination
              v-model:current-page="pendingPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pendingList.length"
              layout="sizes, prev, pager, next, jumper"
              background
              @current-change="(p) => pendingPage = p"
              @size-change="(s) => { pageSize = s; pendingPage = 1 }"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="已录用" name="accepted">
          <el-table :data="pagedAccepted" stripe>
            <el-table-column label="用户" min-width="160">
              <template #default="{ row }">
                <div class="user-cell">
                  <span class="mini-avatar">{{ row.avatar }}</span>
                  <div>
                    <div style="font-weight: 500;">{{ row.name }}</div>
                    <div class="text-muted" style="font-size: 12px;">{{ row.phone }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="creditScore" label="信用分" width="100" />
            <el-table-column prop="acceptTime" label="录用时间" width="160" />
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button link type="primary" size="small">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">共 {{ acceptedList.length }} 条记录</div>
            <el-pagination
              v-model:current-page="acceptedPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="acceptedList.length"
              layout="sizes, prev, pager, next, jumper"
              background
              @current-change="(p) => acceptedPage = p"
              @size-change="(s) => { pageSize = s; acceptedPage = 1 }"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="已拒绝" name="rejected">
          <el-table :data="pagedRejected" stripe>
            <el-table-column label="用户" min-width="160">
              <template #default="{ row }">
                <div class="user-cell">
                  <span class="mini-avatar">{{ row.avatar }}</span>
                  <div>
                    <div style="font-weight: 500;">{{ row.name }}</div>
                    <div class="text-muted" style="font-size: 12px;">{{ row.phone }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="rejectReason" label="拒绝原因" min-width="140" />
            <el-table-column prop="rejectTime" label="拒绝时间" width="160" />
          </el-table>
          <div class="pagination">
            <div class="pagination-info">共 {{ rejectedList.length }} 条记录</div>
            <el-pagination
              v-model:current-page="rejectedPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="rejectedList.length"
              layout="sizes, prev, pager, next, jumper"
              background
              @current-change="(p) => rejectedPage = p"
              @size-change="(s) => { pageSize = s; rejectedPage = 1 }"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeTab = ref('pending')
const pageSize = ref(10)
const pendingPage = ref(1)
const acceptedPage = ref(1)
const rejectedPage = ref(1)

const pendingList = ref([
  { name: '张建国', phone: '138****8888', avatar: '张', realName: '已认证', skills: ['装配', '电子'], creditScore: 85, applyTime: '2024-03-15 10:30' },
  { name: '郑小龙', phone: '137****4444', avatar: '郑', realName: '已认证', skills: ['电子'], creditScore: 50, applyTime: '2024-03-15 10:45' }
])

const acceptedList = ref([
  { name: '刘芳', phone: '135****7777', avatar: '刘', creditScore: 95, acceptTime: '2024-03-15 11:00' },
  { name: '孙美玲', phone: '135****8888', avatar: '孙', creditScore: 82, acceptTime: '2024-03-15 11:15' }
])

const rejectedList = ref([
  { name: '吴志强', phone: '136****1111', avatar: '吴', rejectReason: '信用分过低', rejectTime: '2024-03-14 16:00' }
])

const slicePage = (list, page, size) => {
  const start = (page - 1) * size
  return list.slice(start, start + size)
}

const pagedPending = computed(() => slicePage(pendingList.value, pendingPage.value, pageSize.value))
const pagedAccepted = computed(() => slicePage(acceptedList.value, acceptedPage.value, pageSize.value))
const pagedRejected = computed(() => slicePage(rejectedList.value, rejectedPage.value, pageSize.value))
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8C42, #FF6B35);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
