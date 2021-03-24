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
      this.url=`/bpm/default/flow/processGraph/processGraph.html?processInstID=${this.$route.query.id}&tenantID=HXJT`
    }
  }
</script>

<style lang="less">
  .flowChart {
    iframe{
      width: 100%;
      height: 85vh;
    }
  }
</style>
