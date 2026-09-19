<template>
  <view class="container">
    <!-- 白色导航 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-left">
        <view class="nav-btn" @click="goBack">←</view>
      </view>
      <text class="nav-title">员工详情</text>
      <view class="nav-right">
        <view class="nav-btn" @click="onEdit">✏️</view>
        <view class="nav-btn" @click="onMore">⋯</view>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- 员工头 -->
      <view class="empd-head">
        <view class="empd-avatar" :style="{ background: avatarBg }">{{ employee.initial || '?' }}</view>
        <view class="empd-main">
          <view class="empd-name-line">
            <text class="empd-name">{{ employee.name || '—' }}</text>
            <text class="wb-tag" :class="employee.status">{{ getStatusText(employee.status) }}</text>
          </view>
          <text class="empd-sub">{{ employee.gender || '—' }} | {{ employee.age || '—' }}岁 | {{ maskPhone(employee.phone) }}</text>
        </view>
      </view>

      <!-- mini 统计 -->
      <view class="empd-mini-stats">
        <view class="empd-mini">
          <text class="empd-mini-num">{{ employee.totalAttendDays || 0 }}</text>
          <text class="empd-mini-label">累计出勤</text>
        </view>
        <view class="empd-mini">
          <text class="empd-mini-num">¥{{ formatYuan(employee.totalPaid) }}</text>
          <text class="empd-mini-label">累计发薪</text>
        </view>
        <view class="empd-mini">
          <text class="empd-mini-num">{{ employee.workDays || 0 }}</text>
          <text class="empd-mini-label">在岗天数</text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="empd-block-title">👤 基本信息</view>
      <view class="empd-card">
        <view class="empd-line"><text class="empd-key">真实姓名</text><text class="empd-val">{{ employee.name || '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">手机号</text><text class="empd-val">{{ employee.phone || '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">身份证号</text><text class="empd-val">{{ employee.idCard ? maskIdCard(employee.idCard) : '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">性别</text><text class="empd-val">{{ employee.gender || '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">年龄</text><text class="empd-val">{{ employee.age ? employee.age + '岁' : '—' }}</text></view>
        <view class="empd-line">
          <text class="empd-key">实名认证</text>
          <text class="empd-val cert" v-if="employee.certified">✓ 已认证</text>
          <text class="empd-val" v-else>未认证</text>
        </view>
      </view>

      <!-- 任职信息 -->
      <view class="empd-block-title">💼 任职信息</view>
      <view class="empd-card">
        <view class="empd-line"><text class="empd-key">所属项目</text><text class="empd-val">{{ employee.projectName || '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">岗位</text><text class="empd-val">{{ employee.position || '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">入职时间</text><text class="empd-val">{{ formatDate(employee.joinDate) }}</text></view>
        <view class="empd-line"><text class="empd-key">用工类型</text><text class="empd-val">{{ employee.employmentType || '—' }}</text></view>
        <view class="empd-line"><text class="empd-key">日薪标准</text><text class="empd-val">{{ employee.dailyWage ? (employee.dailyWage / 100).toFixed(0) + '元/天' : '—' }}</text></view>
      </view>

      <!-- 最近发薪 -->
      <view class="empd-block-title">
        💰 最近发薪
        <text class="more-link" @click="onViewAllPay">全部 ›</text>
      </view>
      <view v-if="!payRecords.length" class="empty-block">暂无发薪记录</view>
      <view class="payroll-row" v-for="(row, idx) in payRecords" :key="row.id || idx">
        <view class="payroll-row-top">
          <text class="payroll-row-title">{{ row.title || (row.job ? row.job + '工资' : '工资') }}</text>
          <text class="payroll-row-amount">¥{{ formatYuan(row.amount) }}</text>
        </view>
        <view class="payroll-row-sub">
          <text>出勤 {{ row.attendDays || 0 }} 天</text>
          <text>{{ getDetailStatus(row.status) }}</text>
          <text>{{ row.payTime ? formatDate(row.payTime) : '' }}</text>
        </view>
      </view>

      <!-- 最近考勤 -->
      <view class="empd-block-title">
        📅 最近考勤
        <text class="more-link" @click="onViewAllAttend">全部 ›</text>
      </view>
      <view v-if="!attendances.length" class="empty-block">暂无考勤记录</view>
      <view class="attend-row" v-for="(att, i) in attendances" :key="att.id || i">
        <view>
          <text class="attend-date">{{ formatAttendDate(att.attendDate) }}</text>
          <text class="attend-week">{{ formatAttendWeek(att.attendDate) }}</text>
        </view>
        <text class="attend-info">{{ formatAttendInfo(att) }}</text>
        <text class="attend-status" :class="attendClass(att.status)">{{ attendLabel(att.status) }}</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="btn-outline" @click="onContact">📞 联系</view>
      <view class="btn-primary" @click="onQuickPay">💰 一键发薪</view>
    </view>
  </view>
</template>

<script>
import { getPayrollEmployeeDetail } from "@/api/backend";

function parsePayload(data) {
  return data?.data ?? data ?? {};
}

const AVATAR_COLORS = [
  "linear-gradient(135deg, #FFB84D, #F09A3E)",
  "linear-gradient(135deg, #FF7743, #FF5C33)",
  "linear-gradient(135deg, #FFA94D, #FF8C42)",
  "linear-gradient(135deg, #F09A3E, #FFB84D)",
];

export default {
  data() {
    return {
      statusBarHeight: 0,
      employeeId: "",
      employee: {},
      payRecords: [],
      attendances: [],
    };
  },
  computed: {
    avatarBg() {
      if (this.employee.status === "temp") return "linear-gradient(135deg, #FFA94D, #FF8C42)";
      if (this.employee.status === "left") return "linear-gradient(135deg, #BFBFBF, #8C8C8C)";
      return AVATAR_COLORS[0];
    },
  },
  onLoad(query) {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.employeeId = query.id;
    this.loadDetail();
  },
  methods: {
    async loadDetail() {
      try {
        const data = await getPayrollEmployeeDetail(this.employeeId);
        const body = parsePayload(data);
        const emp = body.employee || {};
        if (emp.name) emp.initial = emp.name.slice(0, 1);
        this.employee = emp;
        this.payRecords = Array.isArray(body.payRecords) ? body.payRecords : [];
        this.attendances = Array.isArray(body.attendances) ? body.attendances : [];
      } catch (error) {
        uni.showToast({ title: "员工详情加载失败", icon: "none" });
      }
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/payroll-employees" }) });
    },
    onEdit() { uni.showToast({ title: "原型演示：编辑员工信息", icon: "none" }); },
    onMore() { uni.showToast({ title: "原型演示：更多操作", icon: "none" }); },
    onContact() { uni.showToast({ title: "原型演示：联系员工", icon: "none" }); },
    onQuickPay() {
      const wage = this.employee.dailyWage ? (this.employee.dailyWage / 100).toFixed(0) : 180;
      uni.showToast({ title: `原型演示：给 ${this.employee.name || '员工'} 发薪 ¥${wage}`, icon: "none" });
    },
    onViewAllPay() { uni.showToast({ title: "原型演示：查看全部发薪记录", icon: "none" }); },
    onViewAllAttend() { uni.showToast({ title: "原型演示：查看全部考勤记录", icon: "none" }); },
    formatAttendDate(value) {
      if (!value) return "—";
      const s = String(value).substring(0, 10);
      const parts = s.split("-");
      return parts.length === 3 ? `${Number(parts[1])}/${Number(parts[2])}` : s;
    },
    formatAttendWeek(value) {
      if (!value) return "";
      const d = new Date(String(value).substring(0, 10));
      const weeks = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
      return weeks[d.getDay()] || "";
    },
    formatAttendInfo(att) {
      if (att.status === "leave") return "请假一天";
      const inTime = att.signInTime ? String(att.signInTime).replace("T", " ").substring(11, 16) : "—";
      const outTime = att.signOutTime ? String(att.signOutTime).replace("T", " ").substring(11, 16) : "—";
      const job = this.employee.position ? `\n${this.employee.position}` : "";
      return `签到 ${inTime} · 签退 ${outTime}${job}`;
    },
    attendClass(status) {
      const map = { on: "present", late: "late", leave: "leave", absent: "absent" };
      return map[status] || "present";
    },
    attendLabel(status) {
      const map = { on: "✓ 出勤", late: "⚠ 迟到", leave: "⏸ 请假", absent: "✕ 缺卡" };
      return map[status] || "✓ 出勤";
    },
    maskPhone(phone) {
      if (!phone || phone.length < 7) return phone || "—";
      return `${phone.substring(0, 3)}****${phone.substring(phone.length - 4)}`;
    },
    maskIdCard(id) {
      if (!id || id.length < 12) return id || "—";
      return `${id.substring(0, 4)}****${id.substring(id.length - 4)}`;
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return n.toFixed(2);
    },
    formatDate(value) {
      if (!value) return "—";
      return String(value).substring(0, 10);
    },
    getStatusText(s) {
      const map = { active: "在职", temp: "临时", left: "离职" };
      return map[s] || "在职";
    },
    getDetailStatus(s) {
      const map = { pending: "处理中", success: "已到账", failed: "转账失败" };
      return map[s] || "已到账";
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f3f4f6;
}
.nav-bar {
  display: flex; align-items: center; justify-content: space-between;
  height: 44px; padding: 0 16px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.nav-left, .nav-right { display: flex; align-items: center; gap: 8px; }
.nav-btn {
  width: 32px; height: 32px; border-radius: 50%;
  background: #F5F5F5; display: flex; align-items: center; justify-content: center;
  color: #333; font-size: 14px;
}
.nav-title { font-size: 16px; font-weight: 600; color: #222; }

.body { flex: 1; padding: 0 16px; overflow-y: auto; }

.empd-head {
  background: #fff; border-radius: 16px; padding: 18px 16px; margin-top: 12px; margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px;
}
.empd-avatar {
  width: 60px; height: 60px; border-radius: 16px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 24px; font-weight: 700;
}
.empd-main { flex: 1; min-width: 0; }
.empd-name-line { display: flex; align-items: center; gap: 8px; }
.empd-name { font-size: 19px; font-weight: 700; color: #222; }
.wb-tag { font-size: 11px; padding: 2px 8px; border-radius: 10px; font-weight: 500; }
.wb-tag.active { background: #E8F7EF; color: #16A34A; }
.wb-tag.temp { background: #FFF1DB; color: #B45309; }
.wb-tag.left { background: #F5F5F5; color: #8C8C8C; }
.empd-sub { font-size: 12px; color: #999; margin-top: 6px; display: block; }

.empd-mini-stats { display: flex; gap: 10px; margin-bottom: 12px; }
.empd-mini {
  flex: 1; background: #fff; border-radius: 12px; padding: 12px 10px; text-align: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.empd-mini-num { font-size: 18px; font-weight: 700; color: #FF6B35; }
.empd-mini-label { font-size: 11px; color: #999; margin-top: 3px; }

.empd-block-title {
  font-size: 14px; font-weight: 600; color: #333; padding: 10px 0 4px;
  display: flex; align-items: center; gap: 6px;
}
.more-link { margin-left: auto; font-size: 12px; color: #FF6B35; font-weight: 500; }
.empty-block { font-size: 12px; color: #999; text-align: center; padding: 10px 0; }

.empd-card {
  background: #fff; border-radius: 12px; padding: 4px 16px; margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.empd-line { display: flex; padding: 12px 0; border-bottom: 0.5px solid #F6F6F6; font-size: 13.5px; }
.empd-line:last-child { border-bottom: none; }
.empd-key { width: 84px; color: #999; flex-shrink: 0; }
.empd-val { flex: 1; color: #333; }
.empd-val.cert { color: #16A34A; }

.payroll-row {
  background: #fff; border-radius: 12px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.payroll-row-top { display: flex; align-items: center; justify-content: space-between; }
.payroll-row-title { font-size: 14px; font-weight: 600; color: #333; }
.payroll-row-amount { font-size: 17px; font-weight: 700; color: #FF6B35; }
.payroll-row-sub { font-size: 12px; color: #999; margin-top: 5px; display: flex; gap: 10px; }

.attend-row {
  background: #fff; border-radius: 12px; padding: 12px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 12px;
}
.attend-date { font-size: 15px; font-weight: 600; color: #333; width: 60px; }
.attend-week { font-size: 11px; color: #999; display: block; margin-top: 2px; }
.attend-info { flex: 1; font-size: 12px; color: #666; line-height: 1.6; }
.attend-status { font-size: 12px; font-weight: 500; padding: 3px 10px; border-radius: 10px; white-space: nowrap; }
.attend-status.present { background: #E8F7EF; color: #16A34A; }
.attend-status.absent { background: #FEE2E2; color: #DC2626; }
.attend-status.leave { background: #FEF3C7; color: #D97706; }
.attend-status.late { background: #FFF1DB; color: #B45309; }

.bottom-space { height: 100px; }

.bottom-bar {
  display: flex; gap: 10px; padding: 12px 16px; padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee; box-shadow: 0 -2px 10px rgba(0,0,0,0.04);
}
.btn-outline, .btn-primary {
  flex: 1; height: 44px; border-radius: 22px; font-size: 14px; font-weight: 600;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-outline { background: #fff; border: 1.5px solid #FF6B35; color: #FF6B35; }
.btn-primary { background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff; box-shadow: 0 4px 12px rgba(255,107,53,0.3); flex: 2; }
</style>
