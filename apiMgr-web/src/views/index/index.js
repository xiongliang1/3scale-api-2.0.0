/*
 * @Description: 组件开发入口文件
 * @Author: gaorubin
 * @Date: 2020-09-03 15:59:20
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-21 16:25:30
 * @FilePath: /vue-com/src/views/Demo/index/index.js
 */
import Com from "./index.vue";
import configJson from "./configJson"

export default {
  myCom: Com, // 属性名为需要导出的组件名
  configJson, // 组件属性描述json
};
