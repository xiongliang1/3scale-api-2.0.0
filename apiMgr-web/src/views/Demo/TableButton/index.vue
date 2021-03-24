<!--
 * @Description: 
 * @Author: gaohongmei
 * @Date: 2020-09-14 15:17:49
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-18 00:30:37
 * @FilePath: /vue-com/src/views/TableButton/index.vue
-->
<template>
  <div>
    <a-button
      :icon="data.icon"
      :type="data.type || 'primary'"
      :size="size"
      @click="btClick()"
    >
      {{ $t(data.name.slice(6).toUpperCase()) }}
    </a-button>
  </div>
</template>
<script>
export default {
  props: {
    data: Object,
    size: String,
    record: Object,
    pageName: String,
    comKey: String
  },
  data() {
    return {};
  },
  methods: {
    btClick() {
      alert(JSON.stringify(this.record));
    }
  }
};
</script>
