// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import 'babel-polyfill';
import Antd from 'ant-design-vue';
import axios from "axios";
import "./assets/theme.less"
import './utils/ant-design-vue'//修改antd默认主题颜色
import "./utils/request"
Vue.config.productionTip = false
Vue.use(Antd)
Vue.prototype.axios=axios;
import promise from 'es6-promise'
promise.polyfill()
/* eslint-disable no-new */
import fun from "./utils/filtersRouter"
router.beforeEach((to,from,next)=>{
  if (typeof window._paq !== 'undefined' ) {
    _paq.push(['setCustomUrl', to.path]);
    _paq.push(['setDocumentTitle', fun(to.path)]);
    _paq.push(['trackPageView']);
  }
  if (to.path==="/"){
    next({path:'/principal/main'})
  }else {
    next()
  }
})

new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
