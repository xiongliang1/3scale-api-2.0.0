<!--
 * @Description: 
 * @Author: gaohongmei
 * @Date: 2020-09-14 15:17:49
 * @LastEditors: gaohongmei
 * @LastEditTime: 2020-09-17 13:39:09
 * @FilePath: /vue-component/src/views/CustomButton/index.vue
-->
<template>
  <div>
    <a-button
      :icon="data.icon"
      :type="data.type || 'primary'"
      :size="size"
      @click="btClick()"
      :disabled="
        (data.isSelected === 'any' && !selectedRows.length) ||
          (data.isSelected === 'one' && selectedRows.length !== 1)
      "
    >
      {{ $t(data.name.slice(6).toUpperCase()) }}
    </a-button>
    <test-modal ref="testModal"></test-modal>
  </div>
</template>
<script>
import TestModal from "./TestModal";
export default {
  props: {
    data: Object,
    size: String,
    selectedRows: Array,
    pageName: String,
    comKey: String
  },
  components: { TestModal },
  data() {
    return {};
  },
  methods: {
    btClick() {
      this.$refs.testModal.show();
    }
  }
};
</script>
