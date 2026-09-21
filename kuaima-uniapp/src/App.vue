<script setup>
// 避免 onLaunch / onShow 重复触发扫码入口跳转
let __lastScanCode = null;
let __lastScanTs = 0;

/**
 * 处理微信扫一扫（扫普通二维码打开小程序 / 扫小程序码）的入口参数。
 * 规则在 mp.weixin.qq.com → 开发管理 → 扫普通二维码打开小程序 中配置：
 *   规则 URL 包含 kuaima.com/invite ，启动参数把 query.code 透传。
 * 小程序码 scene 也可映射到 code 参数。
 */
function handleScanEntry(options) {
  if (!options) return;
  const query = options.query || {};
  // 兼容 onLaunch 的 options 和 onShow 的 options（后者 query 可能在 options 顶层）
  const code = query.code || options.code || "";
  if (!code) return;

  // 防重复：同 code + 10 秒内不重复跳转
  const now = Date.now();
  if (code === __lastScanCode && now - __lastScanTs < 10000) return;
  __lastScanCode = code;
  __lastScanTs = now;

  uni.navigateTo({
    url: `/pages/worker/enterprise-apply?code=${encodeURIComponent(code)}`,
    fail: () => uni.reLaunch({
      url: `/pages/worker/enterprise-apply?code=${encodeURIComponent(code)}`,
    }),
  });
}

// 全局生命周期（uni-app Vue3 在 App.vue 中直接调用即可）
// #ifdef APP-PLUS || MP-WEIXIN || H5
onLaunch((options) => {
  handleScanEntry(options);
});
onShow((options) => {
  handleScanEntry(options);
});
// #endif
</script>

<style>
page {
  background-color: #F3F4F6 !important;
}

/*
 * 统一所有业务页面的画布底色。大量历史页面在全屏根容器上单独声明了
 * 米黄或浅灰背景，因此只设置 page 不足以生效；这里仅覆盖页面第一层
 * 根节点，不影响卡片、提示条、按钮和业务强调区域。
 */
page > view:first-child {
  background-color: #F3F4F6 !important;
}

/* H5 构建产物中的页面根节点。 */
uni-page-body {
  background-color: #F3F4F6 !important;
}

uni-page-body > view:first-child {
  background-color: #F3F4F6 !important;
}

/* 原型中的时间、信号、电量属于设备装饰，正式页面统一交给系统渲染。 */
.status-bar {
  display: flex !important;
  visibility: hidden !important;
}

/* 右上角的省略号/圆点胶囊是原型设备装饰，不属于业务 UI。微信原生胶囊不受此规则影响。 */
.nav-icons {
  display: none !important;
}

</style>
