<template>
  <div class="developerCenter clearfix">
    <div class="nav">
      <div><img src="../assets/dev-center-icon.png"><span>个人中心</span></div>
      <ul>
        <li>
          <router-link to="/principal/developerCenter/mySubscription" replace>
            我的订阅
          </router-link>
        </li>
        <li>
          <router-link to="/principal/developerCenter/myApply" replace>
            我的申请
          </router-link>
        </li>
        <!--<li>
          <router-link to="/principal/developerCenter/myCollection" replace>
            我的收藏
          </router-link>
        </li>-->
        <li>
          <router-link to="/principal/developerCenter/logs" replace>
            日志审计
          </router-link>
        </li>
        <li>
          <router-link to="/principal/developerCenter/dashboard" replace>
            接口统计
          </router-link>
        </li>
      </ul>
    </div>
    <router-view class="box" />
  </div>
</template>

<script>
  export default {
    name: "developerCenter"
  }
</script>

<style lang="less">
  .developerCenter {
    height: calc(~"100vh - 94px");
    width: 1200px;
    margin: 0 auto;
    padding-top: 16px;
    .nav{
      float: left;
      width: 148px;
      height: 95%;
      background-color: #ffffff;
      border-radius: 2px;
      div{
        height: 58px;
        line-height: 58px;
        img{
          margin-left: 10px;
          margin-right: 6px;
          position: relative;
          top: -2px;
        }
      }
      ul{
        li{
          height: 40px;
          line-height: 40px;
          cursor: pointer;
          text-indent: 24px;
          a{
            color: #4A4A4A;
            display: block;
            width: 100%;
            height: 100%;
          }
        }
        .router-link-active{
          background-color: #E6FFFB;
          color: #00AAA6;
          border-right: 2px solid #00AAA6;
        }
      }
    }
    .box{
      float: right;
      width: 1046px;
      height: 95%;
      background-color: #ffffff;
      border-radius: 2px;
      overflow: auto;
    }
  }
</style>
