import axios from 'axios';
import { notification } from 'ant-design-vue'
axios.defaults.baseURL ='/api/v1/'+(sessionStorage.getItem("enviroment")||"prodution");
import Cookie from "js-cookie";
//请求拦截器
axios.interceptors.request.use(config => {
  config.headers = {
    'Content-Type': 'application/json',
  }
  const token = Cookie.get('token');
  //配置token
  if(token){
    config.headers.Authorization='Bearer '+token;
  }
  return config
}, error => {
  return Promise.reject(error)
})

//响应拦截器即异常处理
axios.interceptors.response.use(response => {
  // 根据后端接口code执行操作
  /*if(response.data.code==="1") {//处理共有的操作
    notification.error({
      message:'通知',
      description:response.data.data.msg,
      duration:2
    })
  }*/
  return response.data
}, error => {
  if(error && error.response) {
    switch(error.response.status) {
      case 400:
        notification.error({
          message:'通知',
          description:'错误请求',
          duration:2
        })
        break;
      case 401:
        if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
          window.location="/api/v1/login/ssologin"
        }else{
          window.location="http://"+window.location.host+"/api/v1/login/ssologin"
        }
        break;
      case 403:
        notification.error({
          message:'通知',
          description:'拒绝访问',
          duration:2
        })
        break;
      case 404:
        notification.error({
          message:'通知',
          description:'请求错误,未找到该资源',
          duration:2
        })
        break;
      case 405:
        notification.error({
          message:'通知',
          description:'请求方法未允许',
          duration:2
        })
        break;
      case 408:
        notification.error({
          message:'通知',
          description:'请求超时',
          duration:2
        })
        break;
      case 500:
        notification.error({
          message:'通知',
          description:'服务器端出错',
          duration:2
        })
        break;
      case 501:
        notification.error({
          message:'通知',
          description:'网络未实现',
          duration:2
        })
        break;
      case 502:
        notification.error({
          message:'通知',
          description:'网络错误',
          duration:2
        })
        break;
      case 503:
        notification.error({
          message:'通知',
          description:'服务不可用',
          duration:2
        })
        break;
      case 504:
        notification.error({
          message:'通知',
          description:'网络超时',
          duration:2
        })
        break;
      case 505:
        notification.error({
          message:'通知',
          description:'http版本不支持该请求',
          duration:2
        })
        break;
      default:
        notification.error({
          message:'通知',
          description:`连接错误${err.response.status}`,
          duration:2
        })
    }
  } else {
    notification.error({
      message:'通知',
      description:'连接到服务器失败',
      duration:2
    })
  }
  return Promise.resolve(error.response)
})

export default {
  // get请求
  get (url, param = {}) {
    return new Promise((resolve, reject) => {
      axios.get(url,{params:param})
        .then(res => {
          resolve(res)
        }, err => {
          reject(err)
        })
    })
  },
  // post请求
  post (url, param = {}) {
    return new Promise((resolve, reject) => {
      axios.post(
        url,
        param
      ).then(res => {
        resolve(res)
      }, err => {
        reject(err)
      })
    })
  },
  // put请求
  put (url, param = {}) {
    return new Promise((resolve, reject) => {
      axios.put(url, param)
        .then(response => {
          resolve(response)
        }, err => {
          reject(err)
        })
    })
  },
  // delete
  delete (url,param = {}){
    return new Promise((resolve, reject) => {
      axios.delete(url, param)
        .then(response => {
          resolve(response)
        }, err => {
          reject(err)
        })
    })
  }
}
