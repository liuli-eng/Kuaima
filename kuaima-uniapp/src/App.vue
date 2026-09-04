<script setup>
// uni-app 应用根组件。页面级逻辑位于 src/pages。
</script>

<style>
page {
  background: #f3f4f6;
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
