<!--
 * @Description: 
 * @Author: gaohongmei
 * @Date: 2020-09-17 13:32:37
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-18 00:32:14
 * @FilePath: /vue-com/src/views/TableButton/readme.md
-->

# 表格内部的操作栏自定义按钮组件


## 接收的props参数

| 参数名称     | 参数类型 | 参数说明                                                              |
| ------------ | -------- | --------------------------------------------------------------------- |
| data         | Object   | 上传文件 页面设计器配置自定义按钮的信息 type按钮类型 |
| size         | String   | 页面设计器配置的公共按钮尺寸                                                    |
| record       | Object   | 当前表格这一行的数据                        |
| pageName     | String  | 当前页面的名称                           |
| comKey       | String  | 当前表格组件的key值                       |

提示：
一、若想调用表格模板中查询接口可用
this.$store.dispatch({
  type:`${this.pageName}/${this.comKey}/fetch`
})
二、打包自定义按钮组件时需要用 自定义时的按钮名称进行打包
例："build:tableBtn": "vue-cli-service build --target lib --name handleAbc src/views/TableButton/index.vue",

## 使用示例

```vue
<template>
  <div>
    <a-button
      :type="data.type || 'primary'"
      :size="size"
      @click="btClick()"
    >
      {{ $t(data.name.slice(6).toUpperCase()) }}
    </a-button>
  </div>
</template>
<script>
export default {
  props: {
    data: Object,
    size: String,
    record: Object,
    pageName: String,
    comKey: String
  },
  data() {
    return {};
  },
  methods: {
    btClick() {
      alert(JSON.stringify(this.record));
    }
  }
};
</script>
```