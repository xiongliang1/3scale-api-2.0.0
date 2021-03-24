# vue-component

**Pangea 组件开发脚手架，支持UI展示组件及应用整体布局组件的开发及打包**



## 工程启动

```bash
// 依赖安装
yarn install

// 启动
yarn run start
```



## 组件开发

### 入口文件

~~~bash
// UI展示组件入口文件
src/views/index.vue

// 应用整体布局组件入口文件
src/layout/index.vue
~~~

### 组件开发说明

详情请查看Pangae官方组件开发规范文档
https://confluence.hisense.com/pages/viewpage.action?pageId=36263154



## 组件打包

~~~
// UI展示组件打包命令
npm run build

// 应用整体布局组件打包命令
npm run build:layout
~~~

通过执行对应的组件打包命令，会在工程根目录dist文件夹生成myCom.umd.min.js或myLayout.umd.min.js文件，即为需要上传到Pangea开发框架的组件文件。