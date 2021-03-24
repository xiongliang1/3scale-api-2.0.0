/*
 * @Description: 公共请求方法
 * @Author: gaorubin
 * @Date: 2020-09-01 08:44:05
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-11 10:24:44
 * @FilePath: /vue-com/src/utils/request.js
 */
import axios from "axios";

async function request(url, options) {
  // 创建 axios 实例
  const service = axios.create({
    baseURL: "", // api base_url
    timeout: 6000 // 请求超时时间
  });
  // 请求拦截
  service.interceptors.request.use(config => {
    config.data = config.body;
    config.headers={"Authorization":"Bearer 1fa4bfc6-9e08-40f8-9597-0613217557eb"}
    return config;
  });

  // 返回拦截
  service.interceptors.response.use(response => {
    return response.data.data;
  });
  if (window.vm && window.vm.$request) {
    return window.vm.$request(url, options);
  } else {
    return service(url, options);
  }
}

export default request;
