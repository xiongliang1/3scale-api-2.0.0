/*
 * @Description: 路由文件
 * @Author: gaorubin
 * @Date: 2020-08-05 14:02:37
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-02 11:41:12
 * @FilePath: /vue-com/src/router/index.js
 */
import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;
