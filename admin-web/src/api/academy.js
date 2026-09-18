import request from './request'

export function listSimulateVideos() {
  return request.get('/admin/academy/simulate-videos')
}

export function createSimulateVideo(data) {
  return request.post('/admin/academy/simulate-videos', data)
}

export function updateSimulateVideo(id, data) {
  return request.put(`/admin/academy/simulate-videos/${id}`, data)
}

export function deleteSimulateVideo(id) {
  return request.delete(`/admin/academy/simulate-videos/${id}`)
}

export function toggleSimulateVideo(id) {
  return request.put(`/admin/academy/simulate-videos/${id}/toggle`)
}

export function listQuizzes(type) {
  return request.get('/admin/academy/quizzes', { params: { type } })
}

export function createQuiz(data) {
  return request.post('/admin/academy/quizzes', data)
}

export function updateQuiz(id, data) {
  return request.put(`/admin/academy/quizzes/${id}`, data)
}

export function deleteQuiz(id) {
  return request.delete(`/admin/academy/quizzes/${id}`)
}

export function listLessons() {
  return request.get('/admin/academy/lessons')
}

export function saveLessonVideo(key, data) {
  return request.post(`/admin/academy/lessons/${key}/video`, data)
}

export function deleteLessonVideo(key) {
  return request.delete(`/admin/academy/lessons/${key}/video`)
}

export function toggleLesson(key) {
  return request.put(`/admin/academy/lessons/${key}/toggle`)
}

export function uploadAcademyVideo(file, title, type) {
  const form = new FormData()
  form.append('file', file)
  form.append('title', title)
  form.append('type', type)
  return request.post('/admin/academy/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
