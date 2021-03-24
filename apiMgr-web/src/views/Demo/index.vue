<!--
 * @Description: 组件打包入口示例文件
 * @Author: gaorubin
 * @Date: 2020-08-28 14:23:09
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-22 09:13:54
 * @FilePath: /vue-com/src/views/Demo/index/index.vue
-->
<template>
  <div>
    <a-button type="primary" @click="handleClick">
      {{ myData }}
    </a-button>
    <a-button type="primary" @click="handleRequest" style="margin-left: 10px">
      触发请求
    </a-button>
  </div>
</template>

<script>
import request from "@/utils/request";
export default {
  props: {
    data: Object, // 组件配置属性对象
    comKey: Number, // 组件的唯一key, 用于组件间交互唯一身份标识
  },
  data() {
    return {
      myData: "触发事件"
    };
  },
  mounted() {
  },
  methods: {
    handleClick() {
      window.vm.$emit("handleClick", "传参");
    },
    handleRequest() {
      request("/api/system/cardUserRed/listByUserId", {
        method: "POST",
        body: {
          param: 1
        }
      });
    }
  }
};
</script>
<style lang="less" scoped></style>
