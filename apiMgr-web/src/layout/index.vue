<!--
 * @Description: 
 * @Author: gaorubin
 * @Date: 2020-08-05 14:08:28
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-02 13:08:15
 * @FilePath: /vue-com/src/layout/index.vue
-->
<template>
  <div>
    这里是新的布局
    <router-view :key="$route.path" />
  </div>
</template>

<script>
export default {
  data() {
    return {};
  }
};
</script>
