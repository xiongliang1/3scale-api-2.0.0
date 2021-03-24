/*
 * @Description: vue-cli配置文件
 * @Author: gaorubin
 * @Date: 2020-08-28 15:37:33
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-01 10:33:28
 * @FilePath: /vue-com/vue.config.js
 */
const path = require("path");
function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = {
  lintOnSave: false,
  productionSourceMap:false,
  chainWebpack: config => {
    config.resolve.alias.set("@", resolve("src"));
    config.module
        .rule("images")
        .use("url-loader")
        .loader("url-loader")
        .tap((options)=>Object.assign(options,{limit:true}))
  },
  css: {
    extract: false, // css提取独立文件
    loaderOptions: {
      less: {
        modifyVars: {
          "primary-color": "#00AAA6",
          "font-size-base": "14px",
          "font-family": "Microsoft YaHei"
        },
        javascriptEnabled: true
      }
    }
  },
  devServer: {
    port: 8082,
    proxy: {
      "/api/v1/tenant": {
        target:
            "http://hip-web-hisense-apigateway-test.devapps.hisense.com",
        changeOrigin: true
      },
      "/api/v1/isDeveloper": {
        target:
            "http://hip-web-hisense-apigateway-test.devapps.hisense.com",
        changeOrigin: true
      },
      "/api/v1/userSystemInfos": {
        target:
            "http://hip-web-hisense-apigateway-test.devapps.hisense.com",
        changeOrigin: true
      },
      "/api/v1/editUserSystemInfos": {
        target:
            "http://hip-web-hisense-apigateway-test.devapps.hisense.com",
        changeOrigin: true
      },
      "/api/groupInfos": {
        target: "http://message-hip-message-test.devapps.hisense.com/",
        changeOrigin: true,
        pathRewrite: {
          '^/message': '' //重写,
        },
      },
      "/fuse": {
        target: "http://hip-web-hisense-apigateway-test.devapps.hisense.com/",
        changeOrigin: true
      },
      "/api/hip-flowable":{
        target: "http://hip-web-hisense-apigateway-test.devapps.hisense.com",
        changeOrigin: true
      },
    }
  }
};
