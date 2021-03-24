<template>
  <div>
    <a-button type="primary" @click="btClick()">
      历史查询
    </a-button>
  </div>
</template>
<script>
  import axios from "axios";
  import Cookies from "js-cookie";
export default {
  props: {
    data: Object,
    size: String,
    selectedRows: Array,
    pageName: String,
    comKey: String
  },
  data() {
    return {};
  },
  methods: {
    btClick() {
      let that=this;
      this.$warning({
        title: '提示',
        content: '确认下载历史查询的数据吗？',
        closable:true,
        maskClosable:true,
        onOk(){
          axios({
            url:"/api/hip-flowable/api/v1/process/download?pageNum=1&pageSize=10",
            method: "get",
            headers: {
              'Content-Type': 'application/json',
              "Access-controt-allow-0rigin":"*",
              token: that.$ls.get("Access-Token"),
              Authorization: `Bearer ${that.$ls.get("Access-Token")}`,
              "current-id": Cookies.get("current-id")
            },
            responseType: "blob"
          }).then(res=>{
            const content = res.data //后台返回二进制数据
            const blob = new Blob([content])
            const fileName = '日志文件.xlsx'
            if ('download' in document.createElement('a')) { // 非IE下载
              const elink = document.createElement('a')
              elink.download = fileName
              elink.style.display = 'none'
              elink.href = URL.createObjectURL(blob)
              document.body.appendChild(elink)
              elink.click()
              URL.revokeObjectURL(elink.href) // 释放URL 对象
              document.body.removeChild(elink)
            } else { // IE10+下载
              navigator.msSaveBlob(blob, fileName)
            }
          })
        }
      });
    }
  }
};
</script>
