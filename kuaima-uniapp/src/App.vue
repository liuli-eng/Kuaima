<script setup>
// uni-app 应用根组件。页面级逻辑位于 src/pages。
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
