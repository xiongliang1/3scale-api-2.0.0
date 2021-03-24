<template>
  <div id="app">
    <a-config-provider :locale="locale">
      <router-view />
    </a-config-provider>
  </div>
</template>

<script>
  import zhCN from 'ant-design-vue/lib/locale-provider/zh_CN';
  import Cookie from "js-cookie";
  export default {
    name: 'App',
    data(){
      return{
        locale: zhCN
      }
    },
    created() {
      if (Cookie.get('userName')){
        if (typeof window._paq !== 'undefined' ) {
          _paq.push(['setUserId', Cookie.get('userName')]);
        }
      }
    }
  }
</script>

<style lang="less">
  #app {
    height: 100%;
    overflow-y: auto;
    background-color: #F6F9FC;
  }
</style>
