<template>
  <div class="academy-page">
    <div class="page-header">
      <h1 class="page-title">学堂管理</h1>
      <p class="page-desc">管理接单课堂教学内容：模拟接单全流程视频、新人答题测试题库、新手如何接单视频课程</p>
    </div>

    <div class="tabs-bar">
      <button v-for="tab in tabs" :key="tab.key" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        <i :class="['fas', tab.icon]"></i>{{ tab.label }}
      </button>
    </div>

    <template v-if="activeTab === 'simulate'">
      <div class="stat-cards">
        <div v-for="item in simulateStats" :key="item.label" class="stat-card">
          <div class="stat-card-header"><span>{{ item.label }}</span><div class="stat-card-icon" :class="item.color"><i :class="['fas', item.icon]"></i></div></div>
          <div class="stat-card-value">{{ item.value }}</div><div class="stat-card-change"><span class="text-muted">{{ item.note }}</span></div>
        </div>
      </div>
      <div class="academy-card">
        <div class="academy-toolbar">
          <span class="muted">零工在「接单课堂 - 模拟接单-体验全流程」中按顺序学习以下视频</span>
          <button class="btn btn-primary btn-sm" @click="openVideo('add')"><i class="fas fa-upload"></i> 上传视频</button>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead><tr><th class="index-col">序号</th><th>视频</th><th class="time-col">时长</th><th class="upload-col">上传时间</th><th class="learn-col">学习人次</th><th class="status-col">状态</th><th class="ops-col">操作</th></tr></thead>
            <tbody>
              <tr v-for="(row, index) in videos" :key="row.id">
                <td class="muted index-cell">{{ index + 1 }}</td><td>
                  <div class="video-cell">
                    <button v-if="row.url" class="video-thumb" @click="previewVideo(row)"><i class="fas fa-film"></i><span class="play-mask"><i class="fas fa-play"></i></span><span v-if="row.duration" class="dur-tag">{{ formatDuration(row.duration) }}</span></button>
                    <div v-else class="video-thumb video-thumb-empty"><i class="fas fa-film"></i></div>
                    <div class="video-meta"><div class="video-name" :class="{ 'video-name-empty': !row.url }">{{ row.title }}</div><div class="video-sub">{{ row.url ? `${row.ext?.toUpperCase() || 'MP4'} · ${formatSize(row.size)}` : '待上传' }}</div></div>
                  </div>
                </td>
                <td>{{ formatDuration(row.duration) }}</td>
                <td>{{ formatDateTime(row.createdAt) }}</td>
                <td>{{ formatNumber(row.learners) }}</td>
                <td class="status-cell"><button type="button" class="academy-toggle" :class="{ on: row.enabled }" :aria-label="row.enabled ? '下线' : '上线'" @click="toggleVideo(row)"></button></td>
                <td>
                  <div class="action-btns">
                    <button v-if="row.url" class="btn btn-outline btn-sm" @click="previewVideo(row)"><i class="fas fa-play"></i> 预览</button>
                    <button class="btn btn-outline btn-sm" @click="openVideo(row.url ? 'replace' : 'add', row)"><i :class="['fas', row.url ? 'fa-rotate' : 'fa-upload']"></i> {{ row.url ? '替换' : '上传' }}</button>
                    <button class="btn btn-outline btn-sm danger-text" @click="removeVideo(row)"><i class="fas fa-trash"></i> 删除</button>
                  </div>
                </td>
              </tr>
            <tr v-if="!videos.length"><td colspan="7" class="empty-row">暂无视频</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <template v-else-if="activeTab === 'quiz'">
      <div class="stat-cards">
        <div v-for="item in quizStats" :key="item.label" class="stat-card">
          <div class="stat-card-header"><span>{{ item.label }}</span><div class="stat-card-icon" :class="item.color"><i :class="['fas', item.icon]"></i></div></div>
          <div class="stat-card-value">{{ item.value }}</div><div class="stat-card-change"><span class="text-muted">{{ item.note }}</span></div>
        </div>
      </div>
      <div class="academy-card">
        <div class="academy-toolbar">
          <div class="quiz-filters">
            <button v-for="filter in quizFilters" :key="filter.key" class="btn btn-sm" :class="quizFilter === filter.key ? 'btn-primary' : 'btn-outline'" @click="switchQuizFilter(filter.key)">{{ filter.label }}</button>
          </div>
          <div class="toolbar-actions"><button class="btn btn-outline btn-sm" @click="ElMessage.info('批量导入功能暂未开放')"><i class="fas fa-file-import"></i> 导入题目</button><button class="btn btn-primary btn-sm" @click="openQuiz()"><i class="fas fa-plus"></i> 新建题目</button></div>
        </div>
        <div class="table-wrap"><table class="data-table">
          <thead><tr><th class="index-col">题号</th><th>题目内容</th><th class="type-col">题型</th><th class="score-col">分值</th><th class="answer-col">正确答案</th><th class="ops-col">操作</th></tr></thead>
          <tbody>
            <tr v-for="(quiz, index) in quizzes" :key="quiz.id">
              <td class="muted">Q{{ index + 1 }}</td>
              <td>
                <div class="q-stem">{{ quiz.stem }}</div>
                <div class="q-opts"><span v-for="(option, optionIndex) in quiz.options" :key="optionIndex" :class="{ correct: quiz.answer.includes(optionIndex) }">{{ letters[optionIndex] }}. {{ option }}</span></div>
              </td>
              <td><span class="type-badge" :class="`type-${quiz.type}`">{{ typeMap[quiz.type] }}</span></td>
              <td>{{ quiz.score }} 分</td>
              <td class="ans-badge">{{ quiz.answer.map(item => letters[item]).join('、') }}</td>
              <td><div class="action-btns"><button class="btn btn-outline btn-sm" @click="openQuiz(quiz)"><i class="fas fa-edit"></i> 编辑</button><button class="btn btn-outline btn-sm danger-text" @click="removeQuiz(quiz)"><i class="fas fa-trash"></i> 删除</button></div></td>
            </tr>
            <tr v-if="!quizzes.length"><td colspan="6" class="empty-row">暂无题目</td></tr>
          </tbody>
        </table></div>
      </div>
    </template>

    <template v-else>
      <div class="stat-cards">
        <div v-for="item in lessonStats" :key="item.label" class="stat-card">
          <div class="stat-card-header"><span>{{ item.label }}</span><div class="stat-card-icon" :class="item.color"><i :class="['fas', item.icon]"></i></div></div>
          <div class="stat-card-value">{{ item.value }}</div><div class="stat-card-change"><span class="text-muted">{{ item.note }}</span></div>
        </div>
      </div>
      <div class="academy-card">
        <div class="academy-toolbar"><span class="muted">新手必修课程视频，上传后自动对新人开放学习</span></div>
        <div class="table-wrap"><table class="data-table">
            <thead><tr><th class="index-col">序号</th><th>课程视频</th><th class="time-col">时长</th><th class="upload-col">上传时间</th><th class="learn-col">学习人次</th><th class="status-col">状态</th><th class="ops-col">操作</th></tr></thead>
          <tbody>
            <tr v-for="(lesson, index) in lessons" :key="lesson.key">
              <td class="muted index-cell">{{ index + 1 }}</td><td><div class="video-cell">
                <button v-if="lesson.video" class="video-thumb" @click="previewLesson(lesson)"><i class="fas fa-film"></i><span class="play-mask"><i class="fas fa-play"></i></span><span v-if="lesson.duration" class="dur-tag">{{ formatDuration(lesson.duration) }}</span></button>
                <div v-else class="video-thumb video-thumb-empty"><i class="fas fa-film"></i></div>
                <div class="video-meta"><div class="video-name" :class="{ 'video-name-empty': !lesson.video }" :title="lesson.desc">{{ lesson.title }}</div><div class="video-sub">{{ lesson.video ? `${lesson.ext?.toUpperCase() || 'MP4'} · ${formatSize(lesson.size)}` : '待上传' }}</div></div>
              </div></td>
              <td>{{ lesson.duration ? formatDuration(lesson.duration) : '—' }}</td>
              <td>{{ formatDateTime(lesson.uploadedAt) }}</td>
              <td>{{ formatNumber(lesson.learners) }}</td>
                <td class="status-cell"><button type="button" class="academy-toggle" :class="{ on: lesson.enabled }" :aria-label="lesson.enabled ? '下线' : '上线'" @click="toggleLesson(lesson)"></button></td>
              <td><div class="action-btns">
                <button class="btn btn-outline btn-sm" @click="openVideo('lesson', lesson)"><i :class="['fas', lesson.video ? 'fa-rotate' : 'fa-upload']"></i> {{ lesson.video ? '替换' : '上传' }}</button>
                <button v-if="lesson.video" class="btn btn-outline btn-sm" @click="previewLesson(lesson)"><i class="fas fa-play"></i> 预览</button>
                <button v-if="lesson.video" class="btn btn-outline btn-sm danger-text" @click="removeLesson(lesson)"><i class="fas fa-trash"></i> 删除</button>
              </div></td>
            </tr>
          </tbody>
        </table></div>
      </div>
    </template>

    <el-dialog v-model="videoVisible" :title="videoTitle" width="560px" destroy-on-close class="academy-dialog">
      <el-form label-position="top">
        <el-form-item label="视频标题"><el-input v-model="videoForm.title" placeholder="如：第一步 · 浏览并报名岗位" /></el-form-item>
        <el-form-item label="视频文件">
          <input ref="fileRef" type="file" accept=".mp4,.mov,.webm" hidden @change="pickFile">
          <div class="upload-zone" @click="fileRef?.click()"><i class="fas fa-cloud-arrow-up"></i><div>点击选择视频，或将视频拖拽到此处</div><small>支持 MP4 / MOV / WebM，单个文件不超过 200MB</small></div>
          <div v-if="videoForm.file" class="file-picked"><i class="fas fa-film"></i><span>{{ videoForm.file.name }}</span><button class="icon-btn" @click.stop="videoForm.file = null"><i class="fas fa-xmark"></i></button></div>
        </el-form-item>
        <div v-if="videoMode !== 'lesson'" class="switch-row"><span>立即上线</span><el-switch v-model="videoForm.enabled" /></div>
      </el-form>
      <template #footer><el-button @click="videoVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveVideo">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="previewVisible" :title="preview.title || '视频预览'" width="680px" class="academy-dialog">
      <div class="preview-box"><video v-if="preview.url" :src="preview.url" controls /></div>
      <div class="preview-meta">{{ preview.ext?.toUpperCase() || 'MP4' }} · {{ formatSize(preview.size) }} · {{ preview.duration ? formatDuration(preview.duration) : '—' }}</div>
    </el-dialog>

    <el-dialog v-model="quizVisible" :title="quizForm.id ? '编辑题目' : '新建测试题目'" width="600px" destroy-on-close class="academy-dialog quiz-dialog">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="题目类型"><el-select v-model="quizForm.type" style="width:100%" @change="onQuizTypeChange"><el-option label="单选题" value="single" /><el-option label="多选题" value="multi" /><el-option label="判断题" value="judge" /></el-select></el-form-item>
          <el-form-item label="题目分值"><el-input-number v-model="quizForm.score" :min="1" :max="100" style="width:100%" /></el-form-item>
        </div>
        <el-form-item label="题干"><el-input v-model="quizForm.stem" type="textarea" :rows="2" placeholder="请输入题目内容" /></el-form-item>
        <el-form-item label="选项（点击左侧圆圈/方块设置正确答案）">
          <div class="opt-editor">
            <div v-for="(option, index) in quizForm.options" :key="index" class="opt-row">
              <button type="button" class="opt-mark" :class="{ correct: quizForm.answer.includes(index), square: quizForm.type === 'multi' }" @click="toggleAnswer(index)">{{ quizForm.type === 'judge' ? (index === 0 ? '✓' : '✕') : letters[index] }}</button>
              <el-input v-model="quizForm.options[index]" :placeholder="`选项 ${letters[index]}`" />
              <button v-if="quizForm.type !== 'judge' && quizForm.options.length > 2" type="button" class="icon-btn opt-del" @click="removeOption(index)"><i class="fas fa-trash"></i></button>
            </div>
            <el-button v-if="quizForm.type !== 'judge' && quizForm.options.length < 6" @click="quizForm.options.push('')">添加选项</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="quizVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveQuiz"><i class="fas fa-check"></i> 保存题目</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createQuiz, createSimulateVideo, deleteLessonVideo, deleteQuiz, deleteSimulateVideo,
  listLessons, listQuizzes, listSimulateVideos, saveLessonVideo, toggleLesson as toggleLessonApi,
  toggleSimulateVideo, updateQuiz, updateSimulateVideo, uploadAcademyVideo
} from '@/api/academy'

const tabs = [
  { key: 'simulate', label: '模拟接单-体验全流程', icon: 'fa-gamepad' },
  { key: 'quiz', label: '答题测试', icon: 'fa-file-circle-question' },
  { key: 'howto', label: '如何接单', icon: 'fa-circle-play' }
]
const quizFilters = [{ key: '', label: '全部' }, { key: 'single', label: '单选题' }, { key: 'multi', label: '多选题' }, { key: 'judge', label: '判断题' }]
const typeMap = { single: '单选题', multi: '多选题', judge: '判断题' }
const letters = ['A', 'B', 'C', 'D', 'E', 'F']
const activeTab = ref('simulate')
const videos = ref([])
const quizzes = ref([])
const lessons = ref([])
const quizFilter = ref('')
const fileRef = ref()
const videoVisible = ref(false)
const previewVisible = ref(false)
const quizVisible = ref(false)
const videoMode = ref('add')
const currentVideo = ref(null)
const currentLesson = ref(null)
const saving = ref(false)
const preview = reactive({})
const videoForm = reactive({ title: '', file: null, enabled: true })
const quizForm = reactive({ id: null, type: 'single', score: 10, stem: '', options: ['', ''], answer: [0] })

const simulateStats = computed(() => [
  { label: '教学视频', value: videos.value.length, note: `共 ${videos.value.length} 个流程节点`, icon: 'fa-film' },
  { label: '已上线', value: videos.value.filter(item => item.enabled).length, note: '下线后零工端不可见', icon: 'fa-circle-check', color: 'green' },
  { label: '视频总时长', value: formatDuration(videos.value.reduce((sum, item) => sum + Number(item.duration || 0), 0)), note: '全部流程学习时长', icon: 'fa-clock', color: 'blue' },
  { label: '累计学习人次', value: formatNumber(videos.value.reduce((sum, item) => sum + Number(item.learners || 0), 0)), note: '服务端统计', icon: 'fa-users', color: 'yellow' }
])
const quizStats = computed(() => [
  { label: '题目总数', value: quizzes.value.length, note: `当前筛选满分 ${quizzes.value.reduce((sum, item) => sum + item.score, 0)} 分`, icon: 'fa-list-check' },
  { label: '单选题', value: quizzes.value.filter(item => item.type === 'single').length, icon: 'fa-circle-dot', color: 'blue' },
  { label: '多选题', value: quizzes.value.filter(item => item.type === 'multi').length, icon: 'fa-square-check' },
  { label: '判断题', value: quizzes.value.filter(item => item.type === 'judge').length, icon: 'fa-scale-balanced', color: 'green' }
])
const lessonStats = computed(() => [
  { label: '课程总数', value: lessons.value.length, note: '服务端课程槽位', icon: 'fa-book-open' },
  { label: '已上传', value: lessons.value.filter(item => item.video).length, icon: 'fa-circle-check', color: 'green' },
  { label: '待上传', value: lessons.value.filter(item => !item.video).length, icon: 'fa-hourglass-half', color: 'yellow' },
  { label: '累计学习人次', value: formatNumber(lessons.value.reduce((sum, item) => sum + Number(item.learners || 0), 0)), icon: 'fa-users', color: 'blue' }
])
const videoTitle = computed(() => videoMode.value === 'lesson' ? (currentLesson.value?.video ? '替换课程视频' : '上传课程视频') : (videoMode.value === 'replace' ? '替换视频' : '上传视频'))

const rows = result => result?.data || result || []
const formatNumber = value => Number(value || 0).toLocaleString('zh-CN')
const formatDuration = value => {
  const seconds = Math.max(0, Math.round(Number(value || 0)))
  return `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`
}
const formatSize = value => value ? `${(Number(value) / 1024 / 1024).toFixed(1)}MB` : '—'
const formatDateTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '—'

async function loadVideos() { videos.value = rows(await listSimulateVideos()) }
async function loadQuizzes() { quizzes.value = rows(await listQuizzes(quizFilter.value || undefined)) }
async function loadLessons() { lessons.value = rows(await listLessons()) }
function switchQuizFilter(type) { quizFilter.value = type; loadQuizzes() }

function openVideo(mode, row = null) {
  videoMode.value = mode
  currentVideo.value = mode === 'lesson' ? null : row
  currentLesson.value = mode === 'lesson' ? row : null
  Object.assign(videoForm, { title: row?.title || '', file: null, enabled: row?.enabled ?? true })
  videoVisible.value = true
}
function pickFile(event) { videoForm.file = event.target.files?.[0] || null }

async function saveVideo() {
  if (!videoForm.title) return ElMessage.warning('请输入视频标题')
  if (!videoForm.file) return ElMessage.warning('请选择视频文件')
  saving.value = true
  try {
    const upload = rows(await uploadAcademyVideo(videoForm.file, videoForm.title, videoMode.value === 'lesson' ? 'lesson' : 'simulate'))
    if (videoMode.value === 'lesson') {
      await saveLessonVideo(currentLesson.value.key, { title: videoForm.title, url: upload.url, duration: upload.duration, size: upload.size, ext: upload.ext })
      await loadLessons()
    } else if (videoMode.value === 'replace') {
      await updateSimulateVideo(currentVideo.value.id, { title: videoForm.title, url: upload.url, duration: upload.duration, size: upload.size, ext: upload.ext, enabled: videoForm.enabled, sort: currentVideo.value.sort, learners: currentVideo.value.learners })
      await loadVideos()
    } else {
      await createSimulateVideo({ title: videoForm.title, url: upload.url, duration: upload.duration, size: upload.size, ext: upload.ext, sort: videos.value.length + 1, enabled: videoForm.enabled, learners: 0 })
      await loadVideos()
    }
    videoVisible.value = false
    ElMessage.success('视频已保存')
  } finally { saving.value = false }
}

function previewVideo(row) { Object.assign(preview, row); previewVisible.value = true }
function previewLesson(lesson) { Object.assign(preview, { ...lesson, url: lesson.video }); previewVisible.value = true }
async function toggleVideo(row) { await toggleSimulateVideo(row.id); await loadVideos() }
async function removeVideo(row) {
  await ElMessageBox.confirm(`确定删除视频「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteSimulateVideo(row.id); await loadVideos(); ElMessage.success('视频已删除')
}
async function toggleLesson(lesson) {
  if (!lesson.video) return ElMessage.warning('请先上传课程视频')
  await toggleLessonApi(lesson.key)
  await loadLessons()
}
async function removeLesson(lesson) {
  await ElMessageBox.confirm(`确定删除课程「${lesson.title}」的视频吗？`, '提示', { type: 'warning' })
  await deleteLessonVideo(lesson.key); await loadLessons(); ElMessage.success('课程视频已删除')
}

function openQuiz(quiz = null) {
  Object.assign(quizForm, quiz ? JSON.parse(JSON.stringify({ id: quiz.id, type: quiz.type, score: quiz.score, stem: quiz.stem, options: quiz.options, answer: quiz.answer })) : { id: null, type: 'single', score: 10, stem: '', options: ['', ''], answer: [0] })
  quizVisible.value = true
}
function onQuizTypeChange(type) {
  if (type === 'judge') {
    quizForm.options = ['正确', '错误']
    quizForm.answer = [0]
  } else if (quizForm.options.length < 2 || quizForm.options.every(item => item === '正确' || item === '错误')) {
    quizForm.options = ['', '']
    quizForm.answer = [0]
  } else if (type === 'single') {
    quizForm.answer = [quizForm.answer[0] ?? 0]
  }
}
function removeOption(index) {
  quizForm.options.splice(index, 1)
  quizForm.answer = quizForm.answer.filter(item => item !== index).map(item => item > index ? item - 1 : item)
}
function toggleAnswer(index) {
  if (quizForm.type === 'single' || quizForm.type === 'judge') quizForm.answer = [index]
  else quizForm.answer = quizForm.answer.includes(index) ? quizForm.answer.filter(item => item !== index) : [...quizForm.answer, index]
}
async function saveQuiz() {
  if (!quizForm.stem) return ElMessage.warning('请输入题干')
  saving.value = true
  try {
    if (quizForm.id) await updateQuiz(quizForm.id, { ...quizForm })
    else await createQuiz({ ...quizForm })
    quizVisible.value = false; await loadQuizzes(); ElMessage.success('题目已保存')
  } finally { saving.value = false }
}
async function removeQuiz(quiz) {
  await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  await deleteQuiz(quiz.id); await loadQuizzes(); ElMessage.success('题目已删除')
}

onMounted(() => Promise.all([loadVideos(), loadQuizzes(), loadLessons()]))
</script>

<style scoped>
.index-col{width:60px}.time-col{width:90px}.upload-col{width:150px}.learn-col{width:110px}.status-col{width:90px}.ops-col{width:190px}.type-col{width:90px}.score-col{width:80px}.answer-col{width:150px}.index-cell,.status-cell{white-space:nowrap}.toolbar-actions{display:flex;gap:8px}.academy-toggle{position:relative;width:38px;height:21px;padding:0;border:0;border-radius:11px;background:#D1D5DB;cursor:pointer;transition:background .2s}.academy-toggle::after{content:'';position:absolute;top:2px;left:2px;width:17px;height:17px;border-radius:50%;background:#fff;box-shadow:0 1px 3px rgba(0,0,0,.2);transition:left .2s}.academy-toggle.on{background:var(--primary)}.academy-toggle.on::after{left:19px}
:deep(.academy-dialog .el-dialog__header){margin-right:0;padding:20px 24px;border-bottom:1px solid var(--border)}
:deep(.academy-dialog .el-dialog__title){font-size:16px;font-weight:600;color:var(--text-primary)}
:deep(.academy-dialog .el-dialog__headerbtn){top:17px;right:20px;width:30px;height:30px;border-radius:6px}
:deep(.academy-dialog .el-dialog__headerbtn:hover){background:var(--bg-page)}
:deep(.academy-dialog .el-dialog__body){padding:24px;max-height:65vh;overflow:auto}
:deep(.academy-dialog .el-dialog__footer){padding:16px 24px;border-top:1px solid var(--border)}
:deep(.academy-dialog .el-dialog__footer .el-button){min-width:82px;height:36px;border-radius:8px}
.quiz-dialog .form-grid{align-items:start}.quiz-dialog .el-form-item{margin-bottom:18px}.quiz-dialog .el-form-item__label{font-size:13px;font-weight:500;color:var(--text-primary);line-height:20px;padding-bottom:7px}.quiz-dialog .el-textarea__inner{line-height:1.6}.quiz-dialog .opt-editor{padding:2px 0}
.tabs-bar{display:flex;gap:4px;background:#fff;border:1px solid var(--border);border-radius:12px;padding:0 12px;margin:4px 0 18px;box-shadow:0 1px 2px rgba(0,0,0,.03)}.tab-btn{padding:14px 20px;border:0;border-bottom:2px solid transparent;background:none;color:var(--text-secondary);cursor:pointer;font-size:14px;font-weight:500}.tab-btn.active,.tab-btn:hover{color:var(--primary)}.tab-btn.active{border-bottom-color:var(--primary);font-weight:600}.tab-btn i{margin-right:6px}
.academy-card{padding:16px 18px;background:#fff;border:1px solid var(--border);border-radius:14px}.academy-toolbar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:12px}.action-btns,.quiz-filters{display:flex;gap:6px;white-space:nowrap}.action-btns .btn{padding:4px 9px;font-size:12px}.muted,.video-sub{color:var(--text-muted);font-size:12px}.video-cell{display:flex;align-items:center;gap:12px}.video-meta{min-width:0}.video-thumb{position:relative;display:flex;align-items:center;justify-content:center;width:88px;height:52px;flex-shrink:0;border:0;border-radius:8px;background:linear-gradient(135deg,#4B5563,#1F2937);color:rgba(255,255,255,.9);cursor:pointer;overflow:hidden}.video-thumb .play-mask{position:absolute;inset:0;display:flex;align-items:center;justify-content:center;background:rgba(0,0,0,.28);font-size:16px;color:#fff}.video-thumb-empty{background:#F3F4F6;color:#9CA3AF;cursor:default;border:1px dashed #D1D5DB}.dur-tag{position:absolute;right:4px;bottom:4px;padding:1px 5px;border-radius:4px;background:rgba(0,0,0,.65);font-size:10px;line-height:1.4}.video-name{max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px;font-weight:600;color:var(--text-primary)}.video-name-empty{color:var(--text-muted);font-weight:400}.video-sub{margin-top:3px}.online{color:#16A34A}.offline{color:var(--text-muted)}.danger-text{color:#DC2626}.q-stem{max-width:380px;line-height:1.6;font-size:13px}.q-opts{display:flex;flex-direction:column;margin-top:4px;color:var(--text-muted);font-size:12px;line-height:1.7}.q-opts .correct{color:#16A34A;font-weight:600}.type-badge{display:inline-block;padding:3px 10px;border-radius:6px;font-size:12px}.type-single{color:#2563EB;background:#EFF6FF}.type-multi{color:#7C3AED;background:#F5F3FF}.type-judge{color:#16A34A;background:#F0FDF4}.ans-badge{color:#16A34A;font-weight:600}.empty-row{padding:44px 0;text-align:center;color:var(--text-muted)}
.upload-zone{padding:30px 16px;border:1.5px dashed #D1D5DB;border-radius:12px;background:#FAFAFA;text-align:center;cursor:pointer}.upload-zone i{color:var(--primary);font-size:34px}.upload-zone small{display:block;margin-top:5px;color:var(--text-muted)}.file-picked{display:flex;align-items:center;gap:10px;margin-top:10px;padding:12px 14px;border:1px solid var(--border);border-radius:10px}.file-picked span{flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.icon-btn{border:0;background:none;color:var(--text-muted);cursor:pointer}.switch-row{display:flex;align-items:center;gap:10px}.preview-box{display:flex;align-items:center;justify-content:center;aspect-ratio:16/9;border-radius:10px;background:#000}.preview-box video{width:100%;height:100%}.preview-meta{margin-top:10px;color:var(--text-muted);font-size:12px}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px}.opt-editor{display:flex;flex-direction:column;gap:8px;width:100%}.opt-row{display:flex;align-items:center;gap:8px}.opt-row .el-input{flex:1}.opt-mark{width:30px;height:30px;border:1px solid var(--border);border-radius:50%;background:#fff;color:var(--text-muted)}.opt-mark.correct{border-color:#16A34A;background:#16A34A;color:#fff}.table-wrap{overflow:auto}
.academy-page .table-wrap{width:100%;overflow-x:auto}.academy-page .data-table{width:100%;min-width:860px;border-collapse:collapse;table-layout:fixed}.academy-page .data-table th{white-space:nowrap}.academy-page .data-table td{vertical-align:middle}.academy-page .data-table .q-stem{max-width:none}
</style>
