<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">零工详情</text>
      <view class="nav-right">
        <text class="nav-right-ico">📞</text>
        <text class="nav-right-ico">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 零工信息卡 -->
      <view class="profile-card">
        <view class="avatar-lg" :style="{ background: avatarBg }">{{ talent.initial || '零' }}</view>
        <view class="profile-info">
          <view class="profile-name">
            {{ talent.name || '—' }}
            <text class="tag-green" v-if="talent.type === 'skilled'">熟练工</text>
            <text class="tag-blue" v-else>新零工</text>
          </view>
          <text class="profile-phone">📞 {{ talent.phone || '—' }} · {{ shortRegion }}</text>
          <text class="profile-rate">⭐ {{ talent.rating || '—' }} · {{ rateText }} · 已完成 {{ talent.completedOrders || 0 }} 单</text>
        </view>
      </view>

      <!-- 数据统计 -->
      <view class="stat-row">
        <view class="stat-item">
          <text class="stat-num">{{ talent.completedOrders || 0 }}</text>
          <text class="stat-label">完成订单</text>
        </view>
        <view class="stat-item">
          <text class="stat-num green">{{ talent.goodRate || '—' }}</text>
          <text class="stat-label">好评率</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ talent.rating || '—' }}</text>
          <text class="stat-label">综合评分</text>
        </view>
        <view class="stat-item">
          <text class="stat-num green">{{ talent.arrivalRate || '—' }}</text>
          <text class="stat-label">到岗率</text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="section-card">
        <view class="section-title"><text class="section-ico">👤</text>基本信息</view>
        <view class="info-row">
          <text class="info-label">真实姓名</text>
          <text class="info-value">{{ talent.name || '—' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">工作经验</text>
          <text class="info-value">{{ experienceText }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">常驻区域</text>
          <text class="info-value">{{ talent.region || '—' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">可工作时间</text>
          <text class="info-value">{{ talent.availableTime || '—' }}</text>
        </view>
      </view>

      <!-- 擅长技能 -->
      <view class="section-card">
        <view class="section-title"><text class="section-ico">🏷️</text>擅长技能</view>
        <view class="skill-tags">
          <text class="skill-tag" v-for="(skill, i) in skills" :key="i">{{ skill }}</text>
        </view>
      </view>

      <!-- 雇主评价 -->
      <view class="section-card">
        <view class="section-title"><text class="section-ico">⭐</text>雇主评价</view>
        <view v-if="!reviews.length" class="empty-review">暂无评价</view>
        <view class="work-item" v-for="review in reviews" :key="review.id">
          <view class="work-title">{{ review.jobTitle || '岗位' }}</view>
          <view class="work-meta">
            <text>{{ formatDate(review.reviewDate) }}</text>
            <text>{{ review.location }}</text>
          </view>
          <view class="work-rate">{{ stars(review.stars) }} {{ review.content }}</view>
        </view>
      </view>

      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部操作 -->
    <view class="bottom-bar">
      <button class="btn-outline-msg" @click="openChat">💬 消息</button>
      <button class="btn-hire" @click="openHireModal">立即雇佣</button>
    </view>

    <!-- 雇佣弹窗 -->
    <view v-if="showHireModal" class="modal-mask" @click="closeHireModal">
      <view class="modal-sheet" @click.stop>
        <view class="modal-title">雇佣 {{ talent.name }}</view>
        <view class="modal-desc">请确认雇佣信息，提交后该零工将收到通知</view>
        <view class="form-row">
          <text class="form-label">关联岗位（选填）</text>
          <input class="form-input" v-model="hireForm.jobName" placeholder="选择已发布的招工岗位" @click="pickJob" />
        </view>
        <view class="form-row">
          <text class="form-label">工作日期</text>
          <picker mode="date" :value="hireForm.workDate" @change="onDateChange">
            <view class="form-input picker-text">{{ hireForm.workDate || '请选择工作日期' }}</view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">备注（选填）</text>
          <input class="form-input" v-model="hireForm.note" placeholder="给零工留言" />
        </view>
        <button class="hire-btn" :disabled="submitting" @click="confirmHire">{{ submitting ? '提交中...' : '确认雇佣' }}</button>
      </view>
    </view>
  </view>
</template>

<script>
import { getTalentPoolDetail, hireTalentWorker } from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";

export default {
  data() {
    return {
      statusBarHeight: 0,
      talentId: "",
      talent: {},
      reviews: [],
      hireRecords: [],
      showHireModal: false,
      submitting: false,
      hireForm: {
        jobName: "",
        workDate: "",
        note: "",
      },
    };
  },
  computed: {
    avatarBg() {
      const color = this.talent.avatarColor || "#FF6B35,#FF8C5A";
      const parts = color.split(",").map((s) => s.trim());
      if (parts.length >= 2) {
        return `linear-gradient(135deg, ${parts[0]}, ${parts[1]})`;
      }
      return `linear-gradient(135deg, ${color}, #FF8C5A)`;
    },
    skills() {
      const raw = this.talent.skills;
      return Array.isArray(raw) ? raw : String(raw || "").split(/[,，]/).filter(Boolean);
    },
    shortRegion() {
      const region = this.talent.region || "";
      return region.split("·")[0].trim() || "—";
    },
    rateText() {
      const rate = this.talent.goodRate;
      if (!rate || rate === "无") return "首次接单";
      return `好评率 ${rate}`;
    },
    experienceText() {
      const years = this.talent.workYears;
      if (years && years > 0) {
        return `${years}年${this.talent.category || ''}经验`;
      }
      return this.talent.experience || "—";
    },
  },
  onLoad(query) {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo()
      : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.talentId = query.id;
    this.loadDetail();
  },
  methods: {
    async loadDetail() {
      try {
        const data = await getTalentPoolDetail(this.talentId);
        const body = data?.data ?? data ?? {};
        this.talent = body.talent || {};
        this.reviews = Array.isArray(body.reviews) ? body.reviews : [];
        this.hireRecords = Array.isArray(body.hireRecords) ? body.hireRecords : [];
        if (this.talent.name) {
          this.talent.initial = this.talent.name.slice(0, 1);
        }
        uni.setNavigationBarTitle && uni.setNavigationBarTitle({ title: this.talent.name || '零工详情' });
      } catch (error) {
        this.handleRequestError(error, "零工详情加载失败");
      }
    },
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/talent-list" }),
      });
    },
    formatDate(value) {
      if (!value) return "";
      return String(value).substring(0, 10);
    },
    stars(count) {
      const n = Math.min(Math.max(Number(count) || 5, 1), 5);
      return "⭐".repeat(n);
    },
    openChat() {
      uni.showToast({ title: "原型演示：打开聊天窗口", icon: "none" });
    },
    pickJob() {
      uni.showToast({ title: "原型演示：选择岗位弹窗", icon: "none" });
    },
    onDateChange(e) {
      this.hireForm.workDate = e.detail.value;
    },
    openHireModal() {
      this.hireForm = { jobName: "", workDate: "", note: "" };
      this.showHireModal = true;
    },
    closeHireModal() {
      this.showHireModal = false;
    },
    async confirmHire() {
      if (this.submitting) return;
      this.submitting = true;
      try {
        await hireTalentWorker(this.talentId, {
          jobName: this.hireForm.jobName.trim() || null,
          workDate: this.hireForm.workDate || null,
          note: this.hireForm.note.trim() || null,
        });
        uni.showToast({ title: `雇佣成功！${this.talent.name} 将收到通知`, icon: "success" });
        this.closeHireModal();
      } catch (error) {
        this.handleRequestError(error, "雇佣失败，请重试");
      } finally {
        this.submitting = false;
      }
    },
    handleRequestError(error, fallback) {
      const status = Number(error?.code || error?.statusCode);
      if (status === 401) {
        handleTokenInvalid({ role: "boss" });
        return;
      }
      const title = status === 403
        ? "无权操作"
        : status === 404
          ? "零工不存在"
          : error?.message || fallback;
      uni.showToast({ title, icon: "none" });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #333;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  gap: 14px;
  align-items: center;
}

.nav-right-ico {
  font-size: 16px;
}

.content {
  flex: 1;
  overflow-y: auto;
}

.profile-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 14px;
  padding: 18px;
  display: flex;
  gap: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.avatar-lg {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26px;
  font-weight: 600;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-green {
  background: #f6ffed;
  color: #52c41a;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.tag-blue {
  background: #e6f7ff;
  color: #1890ff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.profile-phone {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
  display: block;
}

.profile-rate {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.stat-row {
  display: flex;
  justify-content: space-around;
  background: #fff;
  margin: 0 16px 12px;
  border-radius: 14px;
  padding: 16px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  display: block;
}

.stat-num.green {
  color: #52c41a;
}

.stat-label {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.section-card {
  background: #fff;
  margin: 0 16px 12px;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-ico {
  font-size: 13px;
}

.info-row {
  display: flex;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.6;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  color: #999;
}

.info-value {
  flex: 1;
  color: #333;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  font-size: 12px;
  padding: 4px 12px;
  background: #fff8e6;
  color: #d48806;
  border-radius: 14px;
}

.empty-review {
  font-size: 13px;
  color: #999;
  text-align: center;
  padding: 12px 0;
}

.work-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.work-item:last-child {
  border-bottom: none;
}

.work-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.work-meta {
  font-size: 11px;
  color: #999;
  margin-top: 3px;
  display: flex;
  gap: 12px;
}

.work-rate {
  font-size: 12px;
  color: #52c41a;
  font-weight: 500;
  margin-top: 4px;
}

.bottom-space {
  height: 20px;
}

.bottom-bar {
  background: #fff;
  border-top: 1px solid #eee;
  padding: 12px 16px 22px;
  display: flex;
  gap: 10px;
}

.btn-outline-msg {
  flex: 1;
  height: 44px;
  border-radius: 7px;
  border: 1px solid #ddd;
  background: #fff;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-hire {
  flex: 2;
  height: 44px;
  border-radius: 7px;
  border: none;
  background: #ffc95a;
  color: #040600;
  font-size: 15px;
  font-weight: 600;
}

/* 雇佣弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.modal-sheet {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 20px;
  padding-bottom: 40px;
  animation: slideUp 0.25s;
}

@keyframes slideUp {
  from { transform: translateY(30px); }
  to { transform: translateY(0); }
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 4px;
}

.modal-desc {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-bottom: 16px;
  display: block;
}

.form-row {
  margin-bottom: 12px;
}

.form-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
  display: block;
}

.form-input {
  width: 100%;
  height: 38px;
  border: 1px solid #eaeaea;
  border-radius: 7px;
  padding: 0 12px;
  font-size: 14px;
  box-sizing: border-box;
  color: #333;
  display: flex;
  align-items: center;
}

.picker-text {
  color: #333;
  line-height: 38px;
}

.hire-btn {
  width: 100%;
  height: 44px;
  background: #ffc95a;
  color: #040600;
  border: none;
  border-radius: 7px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
}

.hire-btn[disabled] {
  opacity: 0.6;
}
</style>
