/*
 * @Description: vue入口js文件
 * @Author: gaorubin
 * @Date: 2020-08-28 14:03:15
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-02 15:47:57
 * @FilePath: /vue-com/src/main.js
 */
import Vue from "vue";
import Antd from "ant-design-vue";
import Storage from "vue-ls";
import App from "./App.vue";
import router from "./router";
import "ant-design-vue/dist/antd.less";

Vue.config.productionTip = false;

Vue.use(Antd);
Vue.use(Storage, {
  namespace: "pro__",
  name: "ls",
  storage: "local"
});

window.vm = new Vue({
  router,
  render: h => h(App)
}).$mount("#app");
