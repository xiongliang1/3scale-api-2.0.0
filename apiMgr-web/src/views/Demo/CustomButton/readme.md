<!--
 * @Description: 
 * @Author: gaohongmei
 * @Date: 2020-09-17 13:32:37
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-18 00:32:05
 * @FilePath: /vue-com/src/views/CustomButton/readme.md
-->

# 表格操作栏自定义按钮组件


## 接收的props参数

| 参数名称     | 参数类型 | 参数说明                                                              |
| ------------ | -------- | --------------------------------------------------------------------- |
| data         | Object   | 上传文件 页面设计器配置自定义按钮的信息 type按钮类型 isSelected是否需要勾选表格数据|
| size         | String   | 页面设计器配置的公共按钮尺寸                                                    |
| selectedRows | Array   | 当前表格的勾选数据                        |
| pageName     | String  | 当前页面的名称                           |
| comKey       | String  | 当前表格组件的key值                       |

提示：
一、若想调用表格模板中查询接口可用
this.$store.dispatch({
  type:`${this.pageName}/${this.comKey}/fetch`
})
二、打包自定义按钮组件时需要用 自定义时的按钮名称进行打包
"build:button": "vue-cli-service build --target lib --name handleTest123 src/views/CustomButton/index.vue",

## 使用示例

```vue
<template>
  <div>
    <a-button
      :type="data.type || 'primary'"
      :size="size"
      @click="btClick()"
      :disabled="
        (data.isSelected === 'any' && !selectedRows.length) ||
          (data.isSelected === 'one' && selectedRows.length !== 1)
      "
    >
      {{ $t(data.name.slice(6).toUpperCase()) }}
    </a-button>
    <test-modal ref="testModal"></test-modal>
  </div>
</template>
<script>
import TestModal from "./TestModal";
export default {
  props: {
    data: Object,
    size: String,
    selectedRows: Array,
    pageName: String,
    comKey: String
  },
  components: { TestModal },
  data() {
    return {};
  },
  methods: {
    btClick() {
      this.$refs.testModal.show();
    }
  }
};
</script>
```