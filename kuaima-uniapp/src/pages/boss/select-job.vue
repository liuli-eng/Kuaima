<template>
  <view class="container">
    <!-- 导航栏 -->
    <view
      class="nav-bar"
      :style="{
        paddingTop: `${statusBarHeight}px`,
        height: `${50 + statusBarHeight}px`,
      }"
    >
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">选择工种</text>
      <view class="nav-right"><text>…</text></view>
    </view>

    <!-- 内容区 -->
    <view class="content">
      <text class="page-title">要招的工种</text>
      <text class="page-desc">选对工种，平台推荐熟手</text>

      <!-- 搜索框 -->
      <view class="search-box">
        <text style="color: #999; margin-right: 10px; font-size: 14px">🔍</text>
        <input
          type="text"
          placeholder="填写工种名称，如'普工'"
          v-model="searchText"
          @input="filterJobs"
        />
      </view>

      <!-- 热门工种 -->
      <view class="section-header">
        <text class="section-icon">●</text>
        <text class="section-title">热门工种</text>
      </view>

      <view class="hot-grid">
        <text
          class="hot-item"
          v-for="(job, index) in filteredJobs"
          :key="index"
          @click="selectJob(job)"
          >{{ job }}</text
        >
      </view>

      <view class="view-all" @click="viewAllJobs">
        查看全部工种 <text class="view-all-arrow">›</text>
      </view>
    </view>

    <!-- 浮动客服 -->
    <view class="float-service" @click="navigateTo('service-chat')">
      <text class="service-icon">◉</text>
      <text class="service-label">客服</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      searchText: "",
      allJobs: [
        "电子厂普工",
        "五金厂CNC操作工",
        "注塑厂注塑工",
        "快递分拣打包工",
        "快递搬运装卸工",
        "电商分拣打包工",
        "电商手工活",
        "餐饮服务员",
        "餐饮服务员、洗碗工",
        "厨师/厨师助理",
        "酒水促销员",
        "营业员/收银员",
        "超市理货员",
        "冷库分拣员",
        "装配工",
        "包装工",
        "质检员",
        "仓管员",
        "叉车司机",
        "货运司机",
        "网约车司机",
        "保洁员",
        "保安",
        "物业维修工",
        "汽修学徒",
        "美容美发助理",
        "服装/家纺缝纫工",
        "电子维修工",
        "电焊工",
        "氩弧焊/气保焊",
        "厨师/面点师",
        "前台接待",
        "电话客服",
        "售后客服",
      ],
      hotJobs: [
        "电子厂普工",
        "五金厂CNC操作工",
        "注塑厂注塑工",
        "快递分拣打包工",
        "快递搬运装卸工",
        "电商分拣打包工",
        "电商手工活",
        "餐饮服务员",
      ],
    };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
  },
  computed: {
    filteredJobs() {
      if (!this.searchText) return this.hotJobs;
      return this.allJobs.filter((j) => j.includes(this.searchText));
    },
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    filterJobs() {
      // 搜索逻辑已通过computed实现
    },
    selectJob(jobName) {
      this.searchText = jobName;
      // 原型交互：选择工种后进入发布招工信息页，并把工种带入下一步
      uni.navigateTo({
        url: `/pages/boss/publish-info?job=${encodeURIComponent(jobName)}`,
      });
    },
    viewAllJobs() {
      uni.navigateTo({ url: "/pages/boss/all-jobs" });
    },
    navigateTo(page) {
      uni.navigateTo({ url: `/pages/boss/${page}` });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  box-sizing: border-box;
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
  color: #333;
}

.content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  display: block;
}

.page-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
  display: block;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 24px;
  padding: 12px 18px;
  margin-bottom: 24px;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
}

.search-box input::placeholder {
  color: #999;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
}

.section-icon {
  color: #ff6b35;
  font-size: 14px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.hot-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.hot-item {
  background: #f8f8f8;
  border-radius: 10px;
  padding: 16px;
  text-align: center;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.hot-item:active {
  background: #fff3ed;
  color: #ff6b35;
}

.view-all {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #ff6b35;
}

.view-all-arrow {
  font-size: 12px;
  margin-left: 3px;
}

.float-service {
  position: absolute;
  right: 16px;
  bottom: 60px;
  width: 56px;
  height: 56px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.service-icon {
  font-size: 18px;
  color: #ff6b35;
}

.service-label {
  font-size: 10px;
  color: #ff6b35;
  margin-top: 2px;
}
</style>
