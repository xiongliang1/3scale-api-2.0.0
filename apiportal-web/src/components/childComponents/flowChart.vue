<template>
  <div class="flowChart">
    <iframe :src="url" :frameborder="0"></iframe>
  </div>
</template>

<script>
  export default {
    name: "flowChart",
    data(){
      return{
        url:null
      }
    },
    created() {
      if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
         this.url=`/bpm/default/flow/processGraph/processGraph.html?processInstID=${this.$route.query.id}&tenantID=HXJT`
      }else{
         this.url=`http://10.19.34.78:8080/default/flow/processGraph/processGraph.html?processInstID=${this.$route.query.id}&tenantID=HXJT`
      }
    }
  }
</script>

<style lang="less">
  .flowChart {
     iframe{
       width: 100%;
       height: 100%;
     }
  }
</style>
