<template>
  <div class="myCollection">
    <a-empty :description="false" description="开发中" />
  </div>
</template>

<script>
  export default {
    name: "myCollection"
  }
</script>

<style lang="less">
  .myCollection {
    position: relative;
     .ant-empty{
       position: absolute;
       top: 50%;
       left: 50%;
       transform: translate(-50%,-50%);
     }
  }
</style>
