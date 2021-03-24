<template>
  <div class="main">
    <div class="banner">
      <p>海信开放平台「焕」新上线</p>
      <p>· 新功能&nbsp;&nbsp;&nbsp; · 新设计&nbsp;&nbsp;&nbsp; · 新体验</p>
      <div class="search">
        <a-select v-model="value">
          <a-select-option value="全部">
            全部
          </a-select-option>
          <a-select-option v-for="item in category" :value="item.id" :key="item.id">
            {{item.itemName}}
          </a-select-option>
        </a-select>
        <a-input v-model="text" @pressEnter="goList" placeholder="请输入想要查询的内容"/>
        <a-button class="search_btn" type="primary" @click="goList">查询</a-button>
      </div>
      <div class="backdrop"></div>
    </div>
    <div class="postServe">
      <div class="title">多领域全场景接口服务 <span @click="$router.push({path:'/principal/list'})">查看全部 <a-icon type="right"/></span></div>
      <div class="category">
        <ul class="nav">
          <li :class="{'active':index===0}" @click="index=0;getData()">
            <p>热门推荐
              <a-icon type="right"/>
            </p>
            <p>为你推荐热门服务</p>
          </li>
          <li :class="{'active':index===201}" @click="index=201;getCategoryOne()">
            <p>供应链<a-icon type="right"/></p>
            <p>供应链领域相关接口服务</p>
          </li>
          <li :class="{'active':index===202}" @click="index=202;getCategoryOne()">
            <p>营销<a-icon type="right"/></p>
            <p>营销领域相关接口服务</p>
          </li>
          <li :class="{'active':index===205}" @click="index=205;getCategoryOne()">
            <p>财务<a-icon type="right"/></p>
            <p>财务领域相关接口服务</p>
          </li>
          <li :class="{'active':index===203}" @click="index=203;getCategoryOne()">
            <p>办公共享<a-icon type="right"/></p>
            <p>办公共享领域相关接口服务</p>
          </li>
          <li :class="{'active':index===206}" @click="index=206;getCategoryOne()">
            <p>大数据
              <a-icon type="right"/>
            </p>
            <p>大数据领域相关接口服务</p>
          </li>
          <li :class="{'active':index===207}" @click="index=207;getCategoryOne()">
            <p>技术开发
              <a-icon type="right"/>
            </p>
            <p>技术开发领域相关接口服务</p>
          </li>
          <li @click="$router.push({path:'/principal/list'})">
            <p>更多
              <a-icon type="right"/>
            </p>
            <p>更多领域相关接口服务</p>
          </li>
        </ul>
        <div class="box">
          <p class="clearfix" v-for="(item,i) in list" :key="i" @click="goApiDetails(item)">
            <a-tooltip>
              <template slot="title">
                {{item.name||item.apiName}}
              </template>
              <span>{{(item.name||item.apiName) | filtersText}}</span>
            </a-tooltip>
            <a-tooltip>
              <template slot="title">
                {{item.system||item.systemName}}
              </template>
              <a-tag color="orange">
                [{{item.system||item.systemName}}]
              </a-tag>
            </a-tooltip>

            <a-tooltip>
              <template slot="title">
                {{item.note||item.description}}
              </template>
              <a>{{item.note||item.description}}</a>
            </a-tooltip>
          </p>
          <a-empty style="margin-top: 200px;" v-show="!list.length" />
          <div class="more" @click.stop="more">查看更多 <a-icon type="right"/></div>
        </div>
      </div>
    </div>
    <div class="exhibition">
      <div class="title">全面开放助力实现开发加速</div>
      <ul>
        <li>
          <p>快速检索</p>
          <p>对现有服务和接口进行了多层级分类梳理，方便你快速通过层级关系或搜索查询功能找到你需要的服务和接口。</p>
        </li>
        <li>
          <p>服务订购</p>
          <p>我们提供多种能力给开发者，你可以快速订购需要的接口服务进行统一管理，同时我们也提供了统计服务，帮助开发者实现更多可能。</p>
        </li>
        <li>
          <p>安全管控</p>
          <p>提供API安全管控机制，让接口调用更可靠，安全。</p>
        </li>
      </ul>
    </div>
    <div class="apiPort" v-show="false">
      <div class="title">丰富API接口，高质量高性能</div>
      <p class="text">运行你的请求  $ curl -v https://echo-api.3scale.net</p>
      <div class="request">
        <div>
          <p>请求</p>
          <div>
            <p>> GET / HTTP/1.1</p>
            <p>> User-Agent: curl/7.27.0</p>
            <p>> Host: https://echo-api.3scale.net/echo</p>
            <p>> Accept: *\*</p>
            <p>></p>
          </div>
        </div>
        <div>
          <p>返回</p>
          <div>
            <p>< HTTP/1.1 200 OK</p>
            <p>< Content-Type: text/plain; charset=utf-8</p>
            <p>> Host: https://echo-api.3scale.net/echo</p>
            <p>< Connection: close</p>
            <p>echo</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Cookie from "js-cookie"
  export default {
    name: '',
    data() {
      return {
        value: "全部",
        text: null,
        index:0,
        category:JSON.parse(sessionStorage.getItem("category")),
        list:[],
      }
    },
    filters:{
      filtersText(text){
        if (text.length<28){
          return text
        }else {
          let str=text.slice(0,27);
          return str+"..."
        }
      }
    },
    methods:{
      getData(){
        this.list=[];
        let timer=setInterval(()=>{
          let token=Cookie.get('token');
          if (!token){
            return
          }
          this.axios.get("/dataItems/hotApi").then(res=>{
            if (res.code==="0"){
              this.list=res.data;
            }
          })
          clearInterval(timer)
        },10)
      },
      getCategoryOne(){
        this.list=[];
        this.axios.get(`/publishApi/findApiInfosByCategoryOne?cateGoryOneId=${this.index}`).then(res=>{
          if (res.code==="0"){
            res.data.forEach((item,index)=>{
              if (index<16){
                this.list.push({
                  id:item.apiId,
                  system:item.systemName,
                  name:item.apiName,
                  note:item.description,
                })
              }
            })
          }
        })
      },
      goList(){
        this.$router.push({path:'/principal/list',query:{categoryOne:(typeof this.value==='number'?this.value:''),apiName:this.text}});
        sessionStorage.setItem("status",true)
      },
      more(){
        if (!this.index){
          this.$router.push({path:'/principal/list',query:{sort:'update'}});
          sessionStorage.setItem("status",true)
        }else{
          this.$router.push({path:'/principal/list',query:{categoryOne:this.index}});
          sessionStorage.setItem("status",true)
        }
      },
      goApiDetails(item){
        this.$router.push({path:'/principal/portDetails',query:{id:item.apiId||item.id}})
      }
    },
    created() {
      let url = window.location.href;
      if (url.indexOf("?") !== -1) {
        url = url.replace(/(\?|#)[^'"]*/, '');
        window.history.pushState({}, 0, url);
      }
      this.getData()
    }
  }
</script>
<style lang="less">
  .main {
    background: #F6F9FC;

    .banner {
      height: 304px;
      background-color: #060929;
      padding-top: 42px;
      background-image: url("../assets/banner.png");
      background-repeat: no-repeat;
      background-size: 110% 100%;

      > p {
        text-align: center;
        color: #ffffff;
      }

      > p:nth-child(1) {
        font-size: 24px;
        line-height: 45px;
      }

      > p:nth-child(2) {
        padding-top: 17px;
        font-size: 18px;
        line-height: 28px;
      }

      .search {
        width: 700px;
        height: 38px;
        margin: 44px auto;
        background-color: #ffffff;
        border-radius: 4px;

        .ant-select {
          width: 120px;
          height: 100%;
          position: relative;
          float: left;

          .ant-select-selection, .ant-select-selection__rendered {
            height: 100%;
            border-top-right-radius: 0;
            border-bottom-right-radius: 0;
            border: none;
          }

          .ant-select-selection-selected-value {
            padding-top: 6px;
            text-align: center;
            width: 80%;
          }
        }

        .ant-select::before {
          content: "";
          position: absolute;
          right: -4px;
          width: 1px;
          height: 16px;
          top: 30%;
          background-color: #D8D8D8;
          z-index: 1;
        }

        > .ant-input {
          width: 418px;
          height: 100%;
          margin-left: 18px;
          border: none;
        }

        .search_btn {
          float: right;
          width: 118px;
          height: 100%;
          border-top-left-radius: 0;
          border-bottom-left-radius: 0;
        }
      }
    }

    .title {
      text-align: center;
      font-size: 26px;

      span {
        display: inline-block;
        width: 82px;
        height: 22px;
        border-radius: 16px;
        border: 1px solid rgba(218, 226, 241, 1);
        font-size: 12px;
        line-height: 20px;
        color: rgb(96, 101, 125);
        cursor: pointer;
        position: relative;
        top: -2px;
      }
    }

    .postServe {
      padding-top: 32px;
      background-color: #F6F9FC;

      .category {
        width: 1200px;
        height: 594px;
        margin: 28px auto 0;
        padding: 8px 0;

        .nav {
          background-image: url("../assets/category.png");
          background-size: 100% 100%;
          width: 260px;
          height: 100%;
          float: left;

          li {
            height: calc(~"100%/8");
            text-indent: 24px;
            border-left: 4px solid transparent;
            cursor: pointer;
            p:nth-child(1){
              font-size: 16px;
              font-weight: bold;
              line-height: 40px;
              i{
                text-indent: 8px;
                font-size: 12px;
                color: #929292!important;
              }
            }
            p:nth-child(2){
              color: #929292;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }

          .active{
            background-color: #ffffff;
            border-left: 4px solid #00AAA6;
            p:nth-child(1){
              color: #00AAA6;
            }
          }
        }
        .box{
          width: 940px;
          height: 100%;
          float: right;
          background-color: #ffffff;
          position: relative;
          >p{
            float: left;
            width: 25%;
            height: 23%;
            padding: 10px 16px 20px;
            border-right: 1px solid #eeeeee;
            border-bottom: 1px solid #eeeeee;
            cursor: pointer;
            position: relative;
            span:nth-child(1){
              margin-right: 6px;
              margin-bottom: 4px;
              display: block;
            }
            a{
              display: block;
              max-width: 90%;
              color: #989898;
              overflow: hidden;
              text-overflow:ellipsis;
              white-space: nowrap;
              font-size: 12px;
              padding-top: 6px;
              position: absolute;
              bottom: 10px;
              left: 16px;
            }
          }
          .list{
            height: 464px;
            li{
              float: left;
              width: 304px;
              padding-left: 32px;
              .head{
                padding-top: 32px;
                color: #333333;
                font-weight: 600;
                line-height: 24px;
              }
              .apiInfo{
                padding-top: 16px;
                p:nth-child(1){
                  color: #333333;
                  font-size: 14px;
                  span:nth-child(1){
                    max-width: 60%;
                    overflow: hidden;
                    text-overflow:ellipsis;
                    white-space: nowrap;
                    cursor: pointer;
                    float: left;
                    margin-right: 6px;
                  }
                  span:nth-child(2){
                    max-width: 40%;
                    overflow: hidden;
                    text-overflow:ellipsis;
                    white-space: nowrap;
                    cursor: pointer;
                  }
                }
                p:nth-child(2){
                  font-size: 12px;
                  color: #999999;
                  overflow: hidden;
                  text-overflow:ellipsis;
                  white-space: nowrap;
                }
              }
            }
          }
          .more{
            padding-right: 32px;
            color: #999999;
            cursor: pointer;
            position: absolute;
            bottom: 10px;
            right: 0;
            z-index: 10;
          }
        }
      }
    }
    .exhibition{
      padding-top: 32px;
      .title{
        padding-bottom: 28px;
      }
      ul{
        width: 1200px;
        display: flex;
        justify-content: space-between;
        height: 388px;
        margin: 0 auto;
        li{
          height: 100%;
          width: 388px;
          background-size: cover;
          p:nth-child(1){
            text-align: center;
            padding-top: 200px;
            font-size: 24px;
            color: rgba(0, 0, 0, 0.85);
          }
          p:nth-child(2){
            width: 324px;
            margin: 24px auto 0;
          }
        }
        li:nth-child(1){
          background-image: url("../assets/development1.png");
        }
        li:nth-child(2){
          background-image: url("../assets/development2.png");
        }
        li:nth-child(3){
          background-image: url("../assets/development3.png");
        }
      }
    }
    .apiPort{
      padding: 72px 0;
      margin: 0 auto;
      background-color: #ffffff;
      position: relative;
      .text{
        padding: 16px 0 24px 0;
        text-align: center;
        color: #999999;
      }
      .request{
        width: 1200px;
        margin: 0 auto;
        display: flex;
        justify-content: space-between;
        >div{
          >p:nth-child(1){
            color: rgba(0, 0, 0, 0.85);
            font-size: 16px;
            padding-bottom: 12px;
          }
          >div{
            width: 584px;
            height: 152px;
            background: #F6F6F6;
            padding: 16px 24px;
            color: rgba(0, 0, 0, 0.85);
          }
        }
      }
    }
    .apiPort:after{
      content: "";
      position: absolute;
      width: 100%;
      height: 98px;
      background-color: #ffffff;
      top: 440px;
    }
    .apiPort:before{
      content: "";
      position: absolute;
      width: 100%;
      height: 58px;
      top: -58px;
      background-color: #ffffff;
    }
  }
</style>
