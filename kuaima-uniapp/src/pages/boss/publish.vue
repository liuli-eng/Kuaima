<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>9:41</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="navigateTo('boss-home')">
        <text style="color: #333">←</text>
      </view>
      <text class="nav-title">发布岗位</text>
      <text class="nav-right">草稿箱</text>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 岗位类型 -->
      <view class="form-section">
        <text class="form-label required">岗位类型</text>
        <view class="tag-group">
          <text
            v-for="(tag, index) in jobTypes"
            :key="index"
            class="tag-item"
            :class="{ active: jobTypeActive === index }"
            @click="jobTypeActive = index"
            >{{ tag }}</text
          >
        </view>
      </view>

      <!-- 岗位名称 -->
      <view class="form-section">
        <text class="form-label required">岗位名称</text>
        <input
          class="form-input"
          placeholder="请输入岗位名称，如：仓库分拣员"
          v-model="jobTitle"
        />
      </view>

      <!-- 薪资待遇 -->
      <view class="form-section">
        <text class="form-label required">薪资待遇</text>
        <view class="price-input">
          <input type="number" v-model="salary" />
          <text class="price-unit">元/天</text>
        </view>
        <view style="margin-top: 12px">
          <text class="form-label" style="font-weight: 500; font-size: 12px"
            >结算方式</text
          >
          <view class="tag-group" style="margin-top: 8px">
            <text
              v-for="(tag, index) in settleTypes"
              :key="index"
              class="tag-item"
              :class="{ active: settleTypeActive === index }"
              @click="settleTypeActive = index"
              >{{ tag }}</text
            >
          </view>
        </view>
      </view>

      <!-- 招聘人数 -->
      <view class="form-section">
        <text class="form-label required">招聘人数</text>
        <view class="number-input" style="max-width: 200px">
          <view class="number-btn" @click="changeNumber(-1)">
            <text>−</text>
          </view>
          <input type="number" v-model="peopleCount" />
          <view class="number-btn" @click="changeNumber(1)">
            <text>+</text>
          </view>
        </view>
      </view>

      <!-- 工作地点 -->
      <view class="form-section">
        <text class="form-label required">工作地点</text>
        <view class="location-input">
          <view class="location-city">
            <text style="color: #ff6b35">📍</text>
            <text>北京市</text>
          </view>
          <input
            class="location-detail"
            placeholder="请输入详细地址"
            v-model="address"
          />
        </view>
      </view>

      <!-- 工作时间 -->
      <view class="form-section">
        <text class="form-label required">工作时间</text>
        <view class="tag-group" style="margin-bottom: 12px">
          <text
            v-for="(tag, index) in workDates"
            :key="index"
            class="tag-item"
            :class="{ active: workDateActive === index }"
            @click="workDateActive = index"
            >{{ tag }}</text
          >
        </view>
        <view class="time-range">
          <view class="time-item">
            <text class="time-label">开始时间</text>
            <picker
              mode="time"
              :value="startTime"
              @change="startTime = $event.detail.value"
            >
              <input
                class="form-input"
                :value="startTime"
                placeholder="开始时间"
                disabled
              />
            </picker>
          </view>
          <text class="time-separator">至</text>
          <view class="time-item">
            <text class="time-label">结束时间</text>
            <picker
              mode="time"
              :value="endTime"
              @change="endTime = $event.detail.value"
            >
              <input
                class="form-input"
                :value="endTime"
                placeholder="结束时间"
                disabled
              />
            </picker>
          </view>
        </view>
      </view>

      <!-- 岗位要求 -->
      <view class="form-section">
        <text class="form-label">岗位要求</text>
        <view class="tag-group" style="margin-bottom: 12px">
          <text
            v-for="(tag, index) in jobRequirements"
            :key="index"
            class="tag-item"
            :class="{ active: reqActive.includes(index) }"
            @click="toggleReq(index)"
            >{{ tag }}</text
          >
        </view>
        <textarea
          class="form-input"
          rows="3"
          placeholder="请输入其他要求..."
          v-model="otherReq"
        ></textarea>
      </view>

      <!-- 福利待遇 -->
      <view class="form-section">
        <text class="form-label">福利待遇</text>
        <view class="tag-group">
          <text
            v-for="(tag, index) in benefits"
            :key="index"
            class="tag-item"
            :class="{ active: benefitActive.includes(index) }"
            @click="toggleBenefit(index)"
            >{{ tag }}</text
          >
        </view>
      </view>

      <!-- 岗位描述 -->
      <view class="form-section">
        <text class="form-label">岗位描述</text>
        <textarea
          class="form-input"
          rows="4"
          placeholder="请详细描述岗位工作内容..."
          v-model="jobDesc"
        ></textarea>
      </view>

      <!-- 联系方式 -->
      <view class="form-section">
        <text class="form-label required">联系方式</text>
        <view class="contact-row">
          <view class="contact-item">
            <text class="contact-label">联系人</text>
            <input class="form-input" v-model="contactName" />
          </view>
          <view class="contact-item">
            <text class="contact-label">联系电话</text>
            <input type="tel" class="form-input" v-model="contactPhone" />
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作按钮 -->
    <view class="submit-bar">
      <view class="save-btn" @click="saveDraft">
        <text>保存草稿</text>
      </view>
      <view class="publish-btn" @click="publishJob">
        <text>立即发布</text>
      </view>
    </view>
  </view>
</template>

<script>
import { createOrder } from "@/api/backend";

export default {
  data() {
    return {
      jobTypes: [
        "分拣搬运",
        "餐饮服务",
        "商超零售",
        "活动会展",
        "物流配送",
        "建筑工程",
        "保洁安保",
        "技术兼职",
      ],
      jobTypeActive: 0,
      jobTitle: "仓库分拣员",
      salary: 200,
      settleTypes: ["日结", "月结", "压薪日结"],
      settleTypeActive: 0,
      peopleCount: 5,
      address: "朝阳区亦庄经济开发区",
      workDates: ["今天", "明天", "本周", "指定日期"],
      workDateActive: 0,
      startTime: "08:00",
      endTime: "18:00",
      jobRequirements: ["无需经验", "男女不限", "18-45岁", "身体健康"],
      reqActive: [1],
      otherReq: "",
      benefits: [
        "包餐",
        "提供饮水",
        "交通补贴",
        "免费培训",
        "工作轻松",
        "环境舒适",
      ],
      benefitActive: [0, 1],
      jobDesc: "",
      contactName: "张经理",
      contactPhone: "138****8888",
      publishing: false,
    };
  },
  methods: {
    navigateTo(pageName) {
      // 原型使用 boss-home/boss-order 等别名，实际 uni-app 页面目录为 boss/home、boss/order。
      const routeMap = {
        "boss-home": "/pages/boss/home",
        "boss-order": "/pages/boss/order",
        "boss-message": "/pages/boss/message",
        "boss-profile": "/pages/boss/profile",
        "boss-publish": "/pages/boss/publish",
      };
      const url =
        routeMap[pageName] ||
        (pageName.startsWith("boss-")
          ? `/pages/boss/${pageName.slice(5)}`
          : `/pages/boss/${pageName}`);
      uni.navigateTo({
        url,
        fail: () => uni.showToast({ title: "页面暂不可用", icon: "none" }),
      });
    },
    changeNumber(delta) {
      this.peopleCount = Math.max(1, this.peopleCount + delta);
    },
    toggleReq(index) {
      const idx = this.reqActive.indexOf(index);
      if (idx >= 0) {
        this.reqActive.splice(idx, 1);
      } else {
        this.reqActive.push(index);
      }
    },
    toggleBenefit(index) {
      const idx = this.benefitActive.indexOf(index);
      if (idx >= 0) {
        this.benefitActive.splice(idx, 1);
      } else {
        this.benefitActive.push(index);
      }
    },
    saveDraft() {
      uni.showToast({ title: "草稿已保存！", icon: "success" });
    },
    async publishJob() {
      if (this.publishing) return;
      uni.showModal({
        title: "确认",
        content: "确认发布这个岗位？",
        success: async (res) => {
          if (res.confirm) {
            this.publishing = true;
            const typeMap = ["daily", "month", "heldBack"];
            const dateText = new Date().toISOString().slice(0, 10);
            const start = `${dateText} ${this.startTime}:00`;
            const end = `${dateText} ${this.endTime}:00`;
            try {
              await createOrder({
                orderTitle: this.jobTitle,
                type: typeMap[this.settleTypeActive] || "daily",
                postion: this.jobTitle,
                orderNum: Number(this.peopleCount),
                duration: 1,
                address: this.address,
                tags: this.jobRequirements
                  .filter((_, i) => this.reqActive.includes(i))
                  .join(","),
                startTime: start,
                endTime: end,
                salary: Number(this.salary),
                orderContent: this.jobDesc || this.otherReq,
                orderRemark: this.benefits
                  .filter((_, i) => this.benefitActive.includes(i))
                  .join(","),
              });
              uni.showToast({ title: "岗位发布成功！", icon: "success" });
              setTimeout(() => this.navigateTo("boss-home"), 800);
            } catch (error) {
              uni.showToast({
                title: error.message || "发布失败",
                icon: "none",
              });
            } finally {
              this.publishing = false;
            }
          }
        },
      });
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

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  background: white;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f5f5f5;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  font-size: 14px;
  color: #999;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin: 10px 16px 0;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.required::after {
  content: "*";
  color: #ff4d4f;
  margin-left: 4px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  background: #f5f5f5;
  color: #666;
}

.tag-item.active {
  background: #ff6b35;
  color: white;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.number-input {
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.number-btn {
  width: 40px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #666;
  background: #fafafa;
}

.number-input input {
  flex: 1;
  text-align: center;
  padding: 10px;
  border: none;
  outline: none;
  font-size: 16px;
  font-weight: 600;
}

.location-input {
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.location-city {
  padding: 12px;
  background: #f5f5f5;
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.location-detail {
  flex: 1;
  padding: 12px;
  font-size: 14px;
  border: none;
  outline: none;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-item {
  flex: 1;
}

.time-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.time-separator {
  font-size: 20px;
  color: #ddd;
  margin-top: 20px;
}

.contact-row {
  display: flex;
  gap: 12px;
}

.contact-item {
  flex: 1;
}

.contact-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.price-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-input input {
  flex: 1;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #ff6b35;
  outline: none;
  text-align: center;
}

.price-unit {
  padding: 8px 14px;
  background: #fff3ee;
  color: #ff6b35;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
}

.submit-bar {
  background: white;
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
}

.save-btn {
  flex: 1;
  padding: 14px;
  border-radius: 24px;
  background: #f5f5f5;
  color: #666;
  font-weight: 600;
  text-align: center;
}

.publish-btn {
  flex: 2;
  padding: 14px;
  border-radius: 24px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: white;
  font-weight: 600;
  text-align: center;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
}
</style>
