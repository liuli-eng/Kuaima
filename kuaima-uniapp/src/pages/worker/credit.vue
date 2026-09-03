<template>
  <view class="page"
    ><AppNavBar title="信用分" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="score"
        ><text class="number">{{ score }}</text
        ><text>当前信用分</text
        ><text class="tip">保持诚信履约，可接更多优质岗位</text></view
      ><view class="card"
        ><text class="title">信用分明细</text
        ><view v-for="item in details" :key="item.id" class="row"
          ><view
            ><text>{{ item.title }}</text
            ><text class="time">{{ item.time }}</text></view
          ><text :class="item.value > 0 ? 'plus' : 'minus'"
            >{{ item.value > 0 ? "+" : "" }}{{ item.value }}</text
          ></view
        ></view
      ><view class="card rules"
        ><text class="title">信用分规则</text
        ><text
          >初始信用分为 100
          分；按时到岗、完成订单可提升分数，爽约、迟到等行为可能扣分，具体以平台规则为准。</text
        ></view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { request } from "@/api/http";
const score = ref(100);
const details = ref([
  { id: 1, title: "完成订单", time: "08月30日", value: 2 },
  { id: 2, title: "按时到岗", time: "08月28日", value: 1 },
]);
onMounted(async () => {
  try {
    const r = await request({ url: "/worker/credit" });
    if (r) {
      score.value = r.score ?? score.value;
      details.value = r.details || details.value;
    }
  } catch (_) {}
});
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
}
.score {
  margin: 24rpx;
  padding: 40rpx;
  text-align: center;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ff8a45, #ffbd62);
  color: #fff;
}
.number {
  display: block;
  font-size: 80rpx;
  font-weight: 800;
}
.score text:nth-child(2) {
  font-size: 25rpx;
}
.tip {
  display: block;
  margin-top: 20rpx;
  font-size: 22rpx;
}
.card {
  margin: 22rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
}
.title {
  display: block;
  font-size: 29rpx;
  font-weight: 700;
  margin-bottom: 20rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f1f1;
  font-size: 25rpx;
}
.time {
  display: block;
  color: #aaa;
  font-size: 21rpx;
  margin-top: 6rpx;
}
.plus {
  color: #52c41a;
  font-size: 30rpx;
}
.minus {
  color: #ff4757;
  font-size: 30rpx;
}
.rules text:last-child {
  display: block;
  color: #777;
  font-size: 23rpx;
  line-height: 1.8;
}
</style>
