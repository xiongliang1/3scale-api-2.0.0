<!--
 * @Description: 
 * @Author: gaohongmei
 * @Date: 2020-09-14 15:32:00
 * @LastEditors: gaohongmei
 * @LastEditTime: 2020-09-14 15:42:18
 * @FilePath: /vue-component/src/views/CustomButton/TestModal.vue
-->
<template>
  <div>
    <a-modal
      title="测试表单"
      :visible="showTest"
      @cancel="showTest = false"
      @ok="confirm"
    >
      <div>
        <span>测试数据：</span>
        <a-input v-model="testValue"></a-input>
      </div>
    </a-modal>
  </div>
</template>
<script>
export default {
  data() {
    return {
      showTest: false,
      testValue: ""
    };
  },
  methods: {
    confirm() {
      console.log("测试数据==", this.testValue)
    },
    show() {
      this.showTest = true;
    }
  }
};
</script>
