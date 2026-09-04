// ============ Mock 数据层 ============

// 统计卡片数据
export const statCards = [
  { title: '零工总数', value: '28,654', change: '+12.5%', up: true, icon: 'fa-user-friends', iconClass: '' },
  { title: '雇主总数', value: '3,218', change: '+8.3%', up: true, icon: 'fa-building', iconClass: 'blue' },
  { title: '今日订单', value: '1,856', change: '+23.1%', up: true, icon: 'fa-clipboard-check', iconClass: 'green' },
  { title: '累计营收', value: '¥286,450', change: '+5.2%', up: true, icon: 'fa-coins', iconClass: 'yellow' }
]

// 订单趋势数据
export const orderTrendData = {
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  values: [120, 145, 168, 142, 190, 210, 185]
}

// 工种分布数据
export const jobTypeData = [
  { name: '电子厂', value: 40, color: '#FF6B35' },
  { name: '物流', value: 25, color: '#2563EB' },
  { name: '餐饮', value: 20, color: '#10B981' },
  { name: '其他', value: 15, color: '#F59E0B' }
]

// 实时订单列表
export const recentOrders = [
  { id: 'KM20240315001', employer: '深圳富士康科技集团', job: '电子厂装配工', worker: '张建国', amount: '¥280', status: '已完成', statusClass: 'success', time: '2024-03-15 18:30', avatarColor: 'linear-gradient(135deg,#FF8C42,#FF6B35)', avatarLetter: '张' },
  { id: 'KM20240315002', employer: '顺丰速运有限公司', job: '快递分拣员', worker: '李美丽', amount: '¥220', status: '进行中', statusClass: 'info', time: '2024-03-15 17:45', avatarColor: 'linear-gradient(135deg,#3B82F6,#2563EB)', avatarLetter: '李' },
  { id: 'KM20240315003', employer: '肯德基餐饮管理', job: '餐厅服务员', worker: '王小刚', amount: '¥180', status: '待处理', statusClass: 'warning', time: '2024-03-15 17:20', avatarColor: 'linear-gradient(135deg,#10B981,#059669)', avatarLetter: '王' },
  { id: 'KM20240315004', employer: '京东物流仓储', job: '仓库理货员', worker: '陈大海', amount: '¥320', status: '已完成', statusClass: 'success', time: '2024-03-15 16:50', avatarColor: 'linear-gradient(135deg,#8B5CF6,#6D28D9)', avatarLetter: '陈' },
  { id: 'KM20240315005', employer: '比亚迪汽车工业', job: '电子厂操作工', worker: '刘芳', amount: '¥260', status: '进行中', statusClass: 'info', time: '2024-03-15 16:15', avatarColor: 'linear-gradient(135deg,#F59E0B,#D97706)', avatarLetter: '刘' },
  { id: 'KM20240315006', employer: '麦当劳食品有限公司', job: '后厨助手', worker: '赵小红', amount: '¥160', status: '待处理', statusClass: 'warning', time: '2024-03-15 15:40', avatarColor: 'linear-gradient(135deg,#EC4899,#BE185D)', avatarLetter: '赵' },
  { id: 'KM20240315007', employer: '顺丰速运有限公司', job: '司机', worker: '周志华', amount: '¥450', status: '已完成', statusClass: 'success', time: '2024-03-15 14:30', avatarColor: 'linear-gradient(135deg,#06B6D4,#0891B2)', avatarLetter: '周' },
  { id: 'KM20240315008', employer: '格力电器制造', job: '装配钳工', worker: '吴志强', amount: '¥300', status: '已取消', statusClass: 'danger', time: '2024-03-15 13:20', avatarColor: 'linear-gradient(135deg,#64748B,#475569)', avatarLetter: '吴' }
]

// 零工管理数据
export const workersData = [
  { id: 'WK001', name: '张建国', phone: '138****8888', realName: '已认证', skills: ['电子厂', '装配'], creditScore: 85, creditProgress: 85, orders: 156, registerTime: '2023-06-15', status: '正常', avatarColor: 'linear-gradient(135deg,#FF8C42,#FF6B35)', avatarLetter: '张' },
  { id: 'WK002', name: '李美丽', phone: '139****6666', realName: '已认证', skills: ['物流', '分拣'], creditScore: 92, creditProgress: 92, orders: 234, registerTime: '2023-04-20', status: '正常', avatarColor: 'linear-gradient(135deg,#3B82F6,#2563EB)', avatarLetter: '李' },
  { id: 'WK003', name: '王小刚', phone: '136****3333', realName: '已认证', skills: ['餐饮', '服务员'], creditScore: 78, creditProgress: 78, orders: 89, registerTime: '2023-08-10', status: '正常', avatarColor: 'linear-gradient(135deg,#10B981,#059669)', avatarLetter: '王' },
  { id: 'WK004', name: '陈大海', phone: '137****5555', realName: '已认证', skills: ['仓储', '理货'], creditScore: 88, creditProgress: 88, orders: 178, registerTime: '2023-05-22', status: '正常', avatarColor: 'linear-gradient(135deg,#8B5CF6,#6D28D9)', avatarLetter: '陈' },
  { id: 'WK005', name: '刘芳', phone: '135****7777', realName: '已认证', skills: ['电子厂', '操作工'], creditScore: 95, creditProgress: 95, orders: 312, registerTime: '2023-02-18', status: '正常', avatarColor: 'linear-gradient(135deg,#F59E0B,#D97706)', avatarLetter: '刘' },
  { id: 'WK006', name: '赵小红', phone: '138****2222', realName: '已认证', skills: ['餐饮', '后厨'], creditScore: 72, creditProgress: 72, orders: 67, registerTime: '2023-09-05', status: '正常', avatarColor: 'linear-gradient(135deg,#EC4899,#BE185D)', avatarLetter: '赵' },
  { id: 'WK007', name: '周志华', phone: '139****9999', realName: '已认证', skills: ['物流', '司机'], creditScore: 90, creditProgress: 90, orders: 198, registerTime: '2023-03-30', status: '正常', avatarColor: 'linear-gradient(135deg,#06B6D4,#0891B2)', avatarLetter: '周' },
  { id: 'WK008', name: '吴志强', phone: '136****1111', realName: '已认证', skills: ['制造', '钳工'], creditScore: 65, creditProgress: 65, orders: 45, registerTime: '2023-11-12', status: '冻结', avatarColor: 'linear-gradient(135deg,#64748B,#475569)', avatarLetter: '吴' },
  { id: 'WK009', name: '郑小龙', phone: '137****4444', realName: '未认证', skills: ['电子厂'], creditScore: 50, creditProgress: 50, orders: 23, registerTime: '2024-01-08', status: '正常', avatarColor: 'linear-gradient(135deg,#FF6B35,#E55A2B)', avatarLetter: '郑' },
  { id: 'WK010', name: '孙美玲', phone: '135****8888', realName: '已认证', skills: ['服务业', '促销'], creditScore: 82, creditProgress: 82, orders: 134, registerTime: '2023-07-25', status: '正常', avatarColor: 'linear-gradient(135deg,#14B8A6,#0D9488)', avatarLetter: '孙' }
]

// 老板管理数据
export const bossesData = [
  { id: 'BS001', name: '深圳富士康科技集团', company: '富士康科技集团', industry: '电子厂', contact: '张经理', phone: '138****8888', jobs: 28, creditScore: 85, certStatus: '已认证', status: '正常' },
  { id: 'BS002', name: '顺丰速运有限公司', company: '顺丰速运', industry: '物流', contact: '李主管', phone: '139****6666', jobs: 45, creditScore: 92, certStatus: '已认证', status: '正常' },
  { id: 'BS003', name: '肯德基餐饮管理', company: '百胜餐饮', industry: '餐饮', contact: '王店长', phone: '136****3333', jobs: 18, creditScore: 78, certStatus: '已认证', status: '正常' },
  { id: 'BS004', name: '京东物流仓储', company: '京东集团', industry: '仓储', contact: '陈经理', phone: '137****5555', jobs: 32, creditScore: 88, certStatus: '已认证', status: '正常' },
  { id: 'BS005', name: '比亚迪汽车工业', company: '比亚迪', industry: '制造业', contact: '刘主任', phone: '135****7777', jobs: 56, creditScore: 95, certStatus: '已认证', status: '正常' },
  { id: 'BS006', name: '麦当劳食品有限公司', company: '麦当劳', industry: '餐饮', contact: '赵店长', phone: '138****2222', jobs: 12, creditScore: 72, certStatus: '待审核', status: '正常' },
  { id: 'BS007', name: '格力电器制造', company: '格力电器', industry: '制造业', contact: '周主管', phone: '139****9999', jobs: 24, creditScore: 90, certStatus: '已认证', status: '正常' },
  { id: 'BS008', name: '菜鸟网络科技', company: '菜鸟网络', industry: '物流', contact: '吴经理', phone: '136****1111', jobs: 38, creditScore: 85, certStatus: '已认证', status: '正常' }
]

// 招工管理数据
export const jobsData = [
  { id: 'JB001', type: '电子厂装配工', employer: '深圳富士康科技集团', price: '280/天', count: 50, location: '深圳龙华区', status: '进行中', statusClass: 'info', time: '2024-03-15 09:00', applications: 35 },
  { id: 'JB002', type: '快递分拣员', employer: '顺丰速运有限公司', price: '25/小时', count: 30, location: '深圳宝安区', status: '进行中', statusClass: 'info', time: '2024-03-15 08:30', applications: 28 },
  { id: 'JB003', type: '餐厅服务员', employer: '肯德基餐饮管理', price: '180/天', count: 15, location: '深圳南山区', status: '已结束', statusClass: 'default', time: '2024-03-14 18:00', applications: 15 },
  { id: 'JB004', type: '仓库理货员', employer: '京东物流仓储', price: '320/天', count: 40, location: '深圳龙岗区', status: '进行中', statusClass: 'info', time: '2024-03-15 10:00', applications: 42 },
  { id: 'JB005', type: '电子厂操作工', employer: '比亚迪汽车工业', price: '260/天', count: 60, location: '深圳坪山区', status: '待审核', statusClass: 'warning', time: '2024-03-15 11:30', applications: 0 },
  { id: 'JB006', type: '后厨助手', employer: '麦当劳食品有限公司', price: '160/天', count: 10, location: '深圳福田区', status: '已通过', statusClass: 'success', time: '2024-03-15 07:00', applications: 8 },
  { id: 'JB007', type: '司机', employer: '顺丰速运有限公司', price: '450/天', count: 8, location: '深圳全市', status: '进行中', statusClass: 'info', time: '2024-03-15 06:00', applications: 12 },
  { id: 'JB008', type: '装配钳工', employer: '格力电器制造', price: '300/天', count: 20, location: '珠海香洲区', status: '审核拒绝', statusClass: 'danger', time: '2024-03-14 16:00', applications: 0 }
]

// 订单管理数据
export const ordersData = [
  { id: 'KM20240315001', employer: '深圳富士康科技集团', worker: '张建国', job: '电子厂装配工', amount: 280, status: '已完成', statusClass: 'success', startTime: '2024-03-15 08:00', endTime: '2024-03-15 18:00' },
  { id: 'KM20240315002', employer: '顺丰速运有限公司', worker: '李美丽', job: '快递分拣员', amount: 220, status: '进行中', statusClass: 'info', startTime: '2024-03-15 08:30', endTime: '-' },
  { id: 'KM20240315003', employer: '肯德基餐饮管理', worker: '王小刚', job: '餐厅服务员', amount: 180, status: '待确认', statusClass: 'warning', startTime: '2024-03-16 10:00', endTime: '-' },
  { id: 'KM20240315004', employer: '京东物流仓储', worker: '陈大海', job: '仓库理货员', amount: 320, status: '已完成', statusClass: 'success', startTime: '2024-03-15 07:00', endTime: '2024-03-15 17:00' },
  { id: 'KM20240315005', employer: '比亚迪汽车工业', worker: '刘芳', job: '电子厂操作工', amount: 260, status: '进行中', statusClass: 'info', startTime: '2024-03-15 09:00', endTime: '-' },
  { id: 'KM20240315006', employer: '麦当劳食品有限公司', worker: '赵小红', job: '后厨助手', amount: 160, status: '纠纷', statusClass: 'danger', startTime: '2024-03-14 11:00', endTime: '2024-03-14 19:00' },
  { id: 'KM20240315007', employer: '顺丰速运有限公司', worker: '周志华', job: '司机', amount: 450, status: '已完成', statusClass: 'success', startTime: '2024-03-15 06:00', endTime: '2024-03-15 14:00' },
  { id: 'KM20240315008', employer: '格力电器制造', worker: '吴志强', job: '装配钳工', amount: 300, status: '已取消', statusClass: 'default', startTime: '-', endTime: '-' }
]

// 结算管理数据
export const settlementData = [
  { id: 'JS20240315001', orderId: 'KM20240315001', employer: '深圳富士康科技集团', worker: '张建国', amount: 280, platformFee: 28, actualAmount: 252, status: '已结算', statusClass: 'success', method: '微信', time: '2024-03-15 18:30' },
  { id: 'JS20240315002', orderId: 'KM20240315004', employer: '京东物流仓储', worker: '陈大海', amount: 320, platformFee: 32, actualAmount: 288, status: '已结算', statusClass: 'success', method: '银行卡', time: '2024-03-15 17:30' },
  { id: 'JS20240315003', orderId: 'KM20240315007', employer: '顺丰速运有限公司', worker: '周志华', amount: 450, platformFee: 45, actualAmount: 405, status: '结算中', statusClass: 'info', method: '支付宝', time: '2024-03-15 15:00' },
  { id: 'JS20240315004', orderId: 'KM20240315002', employer: '顺丰速运有限公司', worker: '李美丽', amount: 220, platformFee: 22, actualAmount: 198, status: '待结算', statusClass: 'warning', method: '-', time: '2024-03-15 08:30' },
  { id: 'JS20240315005', orderId: 'KM20240315005', employer: '比亚迪汽车工业', worker: '刘芳', amount: 260, platformFee: 26, actualAmount: 234, status: '待结算', statusClass: 'warning', method: '-', time: '2024-03-15 09:00' },
  { id: 'JS20240315006', orderId: 'KM20240315006', employer: '麦当劳食品有限公司', worker: '赵小红', amount: 160, platformFee: 16, actualAmount: 144, status: '结算失败', statusClass: 'danger', method: '微信', time: '2024-03-14 20:00' },
  { id: 'JS20240314001', orderId: 'KM20240314001', employer: '肯德基餐饮管理', worker: '王小刚', amount: 180, platformFee: 18, actualAmount: 162, status: '已结算', statusClass: 'success', method: '现金', time: '2024-03-14 19:00' },
  { id: 'JS20240314002', orderId: 'KM20240314005', employer: '格力电器制造', worker: '郑小龙', amount: 300, platformFee: 30, actualAmount: 270, status: '已结算', statusClass: 'success', method: '银行卡', time: '2024-03-14 18:00' }
]

// 财务数据
export const financeStats = [
  { title: '累计营收', value: '¥5,286,450', change: '+15.2%', up: true },
  { title: '平台收入', value: '¥528,645', change: '+12.8%', up: true },
  { title: '零工收入', value: '¥4,757,805', change: '+18.5%', up: true },
  { title: '雇主支出', value: '¥5,286,450', change: '+15.2%', up: true }
]

export const revenueTrendData = {
  labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
  platform: [35, 42, 48, 52, 65, 72, 78, 85, 92, 98, 105, 120],
  total: [350, 420, 480, 520, 650, 720, 780, 850, 920, 980, 1050, 1200]
}

// 认证审核数据
export const certificationData = [
  { id: 'RZ001', type: '零工认证', applicant: '张建国', applyTime: '2024-03-15 10:30', status: '待审核', statusClass: 'warning' },
  { id: 'RZ002', type: '雇主认证', applicant: '麦当劳食品有限公司', applyTime: '2024-03-15 09:15', status: '待审核', statusClass: 'warning' },
  { id: 'RZ003', type: '零工认证', applicant: '郑小龙', applyTime: '2024-03-14 16:45', status: '已通过', statusClass: 'success' },
  { id: 'RZ004', type: '雇主认证', applicant: '菜鸟网络科技', applyTime: '2024-03-14 14:20', status: '已通过', statusClass: 'success' },
  { id: 'RZ005', type: '零工认证', applicant: '孙美玲', applyTime: '2024-03-13 11:00', status: '已拒绝', statusClass: 'danger' },
  { id: 'RZ006', type: '雇主认证', applicant: '新美餐饮有限公司', applyTime: '2024-03-12 09:30', status: '待审核', statusClass: 'warning' }
]

// Banner数据
export const bannersData = [
  { id: 1, title: '千日结奖励活动', position: '零工端首页', weight: 10, startTime: '2024-03-01', endTime: '2024-03-31', status: '展示中', statusClass: 'success' },
  { id: 2, title: '新雇主首单立减', position: '老板端首页', weight: 9, startTime: '2024-02-15', endTime: '2024-04-15', status: '展示中', statusClass: 'success' },
  { id: 3, title: '妇女节特别活动', position: '通用', weight: 5, startTime: '2024-03-08', endTime: '2024-03-08', status: '已下架', statusClass: 'default' },
  { id: 4, title: '春季招工旺季', position: '老板端首页', weight: 8, startTime: '2024-03-01', endTime: '2024-05-31', status: '展示中', statusClass: 'success' }
]

// 公告数据
export const noticesData = [
  { id: 1, title: '平台上线零工保险服务', type: '系统', typeClass: 'info', scope: '全平台', status: '已发布', statusClass: 'success', publishTime: '2024-03-15' },
  { id: 2, title: '春节后招工旺季即将开始', type: '活动', typeClass: 'warning', scope: '老板端', status: '已发布', statusClass: 'success', publishTime: '2024-03-10' },
  { id: 3, title: '关于加强实名认证的通知', type: '政策', typeClass: 'success', scope: '全平台', status: '已发布', statusClass: 'success', publishTime: '2024-03-01' },
  { id: 4, title: '平台V2.0版本升级公告', type: '系统', typeClass: 'info', scope: '全平台', status: '草稿', statusClass: 'default', publishTime: '-' },
  { id: 5, title: '清明节放假安排通知', type: '系统', typeClass: 'info', scope: '全平台', status: '已下架', statusClass: 'warning', publishTime: '2024-03-05' }
]

// 规则数据
export const rulesData = [
  { id: 1, title: '零工信用评定规则', category: '信用评定', version: 'v1.2', status: '已发布', statusClass: 'success', effectiveTime: '2024-01-01' },
  { id: 2, title: '平台收费标准', category: '收费标准', version: 'v2.0', status: '已发布', statusClass: 'success', effectiveTime: '2024-02-01' },
  { id: 3, title: '交易纠纷处理规则', category: '交易规则', version: 'v1.5', status: '已发布', statusClass: 'success', effectiveTime: '2023-12-01' },
  { id: 4, title: '飞单认定与处罚', category: '交易规则', version: 'v1.0', status: '已发布', statusClass: 'success', effectiveTime: '2024-03-01' },
  { id: 5, title: '提现规则说明', category: '收费标准', version: 'v1.1', status: '草稿', statusClass: 'default', effectiveTime: '-' }
]

// 管理员数据
export const adminUserData = [
  { id: 1, name: '管理员', account: 'admin', role: '超级管理员', dept: '技术部', lastLogin: '2024-03-15 09:00', status: '启用', statusClass: 'success' },
  { id: 2, name: '运营小张', account: 'zhang', role: '管理员', dept: '运营部', lastLogin: '2024-03-15 08:30', status: '启用', statusClass: 'success' },
  { id: 3, name: '客服小李', account: 'li', role: '编辑员', dept: '客服部', lastLogin: '2024-03-14 17:00', status: '启用', statusClass: 'success' },
  { id: 4, name: '审计小王', account: 'wang', role: '查看员', dept: '审计部', lastLogin: '2024-03-10 14:00', status: '禁用', statusClass: 'danger' }
]

// 操作日志数据
export const logsData = [
  { id: 1, operator: '管理员', type: '登录', target: '系统登录', ip: '192.168.1.100', time: '2024-03-15 09:00', result: '成功', resultClass: 'success' },
  { id: 2, operator: '运营小张', type: '审核', target: '认证申请 RZ001', ip: '192.168.1.101', time: '2024-03-15 10:30', result: '通过', resultClass: 'success' },
  { id: 3, operator: '管理员', type: '数据修改', target: '招工 JB001', ip: '192.168.1.100', time: '2024-03-15 11:00', result: '成功', resultClass: 'success' },
  { id: 4, operator: '客服小李', type: '权限变更', target: '管理员 小王', ip: '192.168.1.102', time: '2024-03-14 16:00', result: '成功', resultClass: 'success' },
  { id: 5, operator: '管理员', type: '删除', target: '公告（草稿）', ip: '192.168.1.100', time: '2024-03-14 14:00', result: '成功', resultClass: 'success' },
  { id: 6, operator: '审计小王', type: '登录', target: '系统登录', ip: '192.168.1.103', time: '2024-03-10 14:00', result: '成功', resultClass: 'success' }
]

// 客服会话数据
export const serviceData = [
  { id: 'CH001', user: '张建国', userType: '零工', type: '工资纠纷', waitTime: '5分钟', status: '进行中', statusClass: 'info' },
  { id: 'CH002', user: '深圳富士康', userType: '雇主', type: '招工咨询', waitTime: '12分钟', status: '等待中', statusClass: 'warning' },
  { id: 'CH003', user: '李美丽', userType: '零工', type: '实名认证', waitTime: '3分钟', status: '进行中', statusClass: 'info' },
  { id: 'CH004', user: '肯德基餐饮', userType: '雇主', type: '结算问题', waitTime: '-', status: '已结束', statusClass: 'default' }
]

// 消息模板数据
export const messageTemplates = [
  { id: 1, name: '新订单通知', event: '订单生成', channel: '双渠道', status: '启用', lastUsed: '2024-03-15' },
  { id: 2, name: '认证审核结果', event: '认证审核', channel: '双渠道', status: '启用', lastUsed: '2024-03-14' },
  { id: 3, name: '招工审核结果', event: '招工审核', channel: '站内信', status: '启用', lastUsed: '2024-03-13' },
  { id: 4, name: '结算成功通知', event: '结算完成', channel: '双渠道', status: '启用', lastUsed: '2024-03-15' }
]

// 积分管理数据
export const pointsData = [
  { id: 'JF001', user: '刘芳', change: '+200', type: '完成订单', orderId: 'KM20240315005', balance: 1850, operator: '系统', time: '2024-03-15 18:00' },
  { id: 'JF002', user: '比亚迪汽车', change: '+500', type: '充值', orderId: '-', balance: 5500, operator: '管理员', time: '2024-03-15 10:00' },
  { id: 'JF003', user: '张建国', change: '-50', type: '信用扣分', orderId: '-', balance: 850, operator: '系统', time: '2024-03-14 16:30' },
  { id: 'JF004', user: '陈大海', change: '+100', type: '奖励赠送', orderId: '-', balance: 1200, operator: '运营小张', time: '2024-03-14 14:00' }
]
