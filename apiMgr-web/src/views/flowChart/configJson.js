/*
 * @Description: 组件配置描述json
 * @Author: gaorubin
 * @Date: 2020-09-03 16:08:36
 * @LastEditors: gaorubin
 * @LastEditTime: 2020-09-21 16:25:05
 * @FilePath: /vue-com/src/views/Demo/index/configJson.js
 */
const comJson = {
  json: [
    {
      title: "分组标题1",
      type: "collapse",
      coms: [
        {
          type: "input",
          col: "width",
          label: "表格宽度",
          props: {
            "addon-after": "px" // 后置标签
          }
        }
      ]
    },
    {
      title: "分组标题2",
      type: "transfer",
      coms: [
        {
          type: "input",
          col: "height",
          label: "表格高度",
          props: {
            "addon-after": "px" // 后置标签
          }
        }
      ]
    },
  ],
  props: {
    width: '100',
    height: '200',
  },
  dynamic: {
    width: {
      height: {
        type: "hide",
        value: 1000
      }
    },
  }
};
export default comJson;
