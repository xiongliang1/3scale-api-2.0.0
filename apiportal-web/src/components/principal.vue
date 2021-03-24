<template>
  <div class="project">
    <header id="header" class="clearfix">
      <router-link to="/principal/main">
        <div class="logo"><img src="./../assets/logo.png">API市场</div>
      </router-link>
      <ul class="nav">
        <li class="server">
          <router-link to="/principal/list">
            <div>功能与服务</div>
          </router-link>
          <ul class="list" v-show="show">
            <li class="clearfix" v-for="item in category" :key="item.id">
              <span>
                <a-tooltip>
                  <template slot="title">
                    {{item.itemName}}
                  </template>
                  {{item.itemName}}
                </a-tooltip>
              </span>
              <span v-for="i in item.dataItemList" @click="goList(item,i)">{{i.itemName}}</span>
            </li>
          </ul>
        </li>
        <li>
          <router-link to="/principal/developerCenter/mySubscription">
            <div>个人中心</div>
          </router-link>
        </li>
        <li>
          <a-select v-model="enviroment" @change="change">
            <a-select-option value="staging">
              测试环境
            </a-select-option>
            <a-select-option value="prodution">
              生产环境
            </a-select-option>
          </a-select>
        </li>
        <li class="login" v-show="!username">
          <a-button type="primary" shape="round" @click="login">登录</a-button>
        </li>
        <li class="control-panel">
          <div @click="handleJump">控制台</div>
        </li>
        <li class="control-panel">
          <a-select placeholder="老门户入口" style="width: 130px">
            <a-select-option @click="jump(0)" value="staging">
              内网门户
            </a-select-option>
            <a-select-option @click="jump(1)" value="prodution">
              外网门户
            </a-select-option>
          </a-select>
        </li>
        <li class="user" v-show="username">
          <a-dropdown>
            <a class="ant-dropdown-link">
              <a-icon type="user"/>
              {{username}}
              <a-icon type="down"/>
            </a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a @click="layout()">退出</a>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </li>
      </ul>
    </header>
    <router-view v-if="username" />
    <footer id="footer">
      Copyright 2021 Hisense Group Holdings Co., Ltd. All Rights Reserved. 海信集团控股股份有限公司版权所有
    </footer>
  </div>
</template>

<script>
  import Cookie from "js-cookie";
  export default {
    name: "indexs",
    data(){
      return{
        category:null,
        flag:false,
        show:true,
        code:null,
        username:null,
        enviroment:"prodution"
      }
    },
    created() {
      if (sessionStorage.getItem("enviroment")){
        this.enviroment=sessionStorage.getItem("enviroment")
      }
      let str=window.location.search;
      if (str){
        this.code=window.location.search.substring(1).split("&")[0].split("=")[1];
        this.axios.post(window.location.protocol+"//"+window.location.hostname+"/api/v1/login/loginNew",{
          code:this.code
        }).then(res=>{
          Cookie.set('userName',res.data.userName);
          if (typeof window._paq !== 'undefined' ) {
            _paq.push(['setUserId', res.data.userName]);
          }
          this.username=Cookie.get('userName');
          Cookie.set('token',res.data.token);
          let url = window.location.href;
          if (url.indexOf("?") !== -1) {
            url = url.replace(/(\?|#)[^'"]*/, '');
            window.history.pushState({}, 0, url);
          }
          this.getData();
          this.systemList();
        })
      }else{
        let token=Cookie.get("token");
        if (!token){
          if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
            window.location="/api/v1/login/ssologin"
          }else{
            window.location="http://"+window.location.host+"/api/v1/login/ssologin"
          }
        }
        if (Cookie.get('userName')){
          this.username=Cookie.get("userName");
          this.getData();
          this.systemList();
        }
      }
    },
    methods:{
      getData(){
        let findAllDataItems0=()=>{
          return new Promise((resolve, reject)=>{
            this.axios.get("/dataItems/findAllDataItems",{
              params:{
                partition:0
              }
            }).then(res=>{
              resolve(res)
            }).catch(e=>{
              reject(e)
            })
          })
        }
        let findAllDataItems1=()=>{
          return new Promise((resolve, reject)=>{
            this.axios.get("/dataItems/findAllDataItems",{
              params:{
                partition:1
              }
            }).then(res=>{
              resolve(res)
            }).catch(e=>{
              reject(e)
            })
          })
        }
        this.axios.get("/dataItems/searchDataItems").then(res=>{
          this.flag=true;
          this.category=res.data
          sessionStorage.setItem("category",JSON.stringify(res.data));
          Promise.all([findAllDataItems0(),findAllDataItems1()]).then(response=>{
            let data={
              "all":[],
              "0":[],
              "1":[]
            }
            let arr=[];
            let arr1=[];
            let arr2=[];
            for (let key in response[0].topOne){
              if (response[0].topOne[key].length){
                arr1.push(key)
              }
            }
            for (let key in response[1].topOne){
              if (response[1].topOne[key].length){
                arr2.push(key)
              }
            }
            arr1=arr1.map(Number);
            arr2=arr2.map(Number);
            arr=arr1.concat(arr2);
            let arr3=[];
            for (let i=0;i<res.data.length;i++){
              if (res.data[i].dataItemList){
                for (let j=0;j<res.data[i].dataItemList.length;j++){
                  let json={...res.data[i]};
                  if (arr.includes(res.data[i].dataItemList[j].id)){
                    json.dataItemList[j].hidden=false;
                    if (!arr3.includes(json.id)){
                      arr3.push(json.id);
                      data['all'].push(json);
                    }
                  }
                }
              }
            }
            arr3=[];
            for (let i=0;i<res.data.length;i++){
              if (res.data[i].dataItemList){
                for (let j=0;j<res.data[i].dataItemList.length;j++){
                  let json={...res.data[i]};
                  if (arr1.includes(res.data[i].dataItemList[j].id)){
                    json.dataItemList[j].hidden=false;
                    if (!arr3.includes(json.id)){
                      arr3.push(json.id);
                      data['0'].push(json);
                    }
                  }
                }
              }
            }
            arr3=[];
            for (let i=0;i<res.data.length;i++){
              if (res.data[i].dataItemList){
                for (let j=0;j<res.data[i].dataItemList.length;j++){
                  let json={...res.data[i]};
                  if (arr2.includes(res.data[i].dataItemList[j].id)){
                    json.dataItemList[j].hidden=false;
                    if (!arr3.includes(json.id)){
                      arr3.push(json.id);
                      data['1'].push(json);
                    }
                  }
                }
              }
            }
            sessionStorage.setItem("categoryTwoAndApi",JSON.stringify(data))
          })
        })
      },
      goList(item,i){
        this.$router.push({path:'/principal/list',query:{categoryOne:item.id,categoryTwo:i.id}});
        sessionStorage.setItem("status",true)
      },
      systemList(){
        this.axios.get("/dataItems/getAllSystems").then(res=>{
          sessionStorage.setItem("systemList",JSON.stringify(res.data))
        })
      },
      getPath(){
        if(this.$route.path==="/principal/main"){
          this.show=true;
        }else {
          this.show=false;
        }
      },
      login(){
        if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
          window.location="/api/v1/login/ssologin"
        }else{
          window.location="http://"+window.location.host+"/api/v1/login/ssologin"
        }
      },
      layout(){
        Cookie.remove('token');
        Cookie.remove('userName');
        if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
          window.location="https://sso.hisense.com/logout?service=sso.hisense.com&redirect_url=http://"+location.host
        }else{
          window.location="https://sso.hisense.com/logout?service=sso.hisense.com&redirect_url=http://"+location.host
        }
      },
      change(value){
        sessionStorage.setItem("enviroment",value);
        setTimeout(()=>{
          this.$router.go(0)
        })
      },
      handleJump(){
        if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
          window.open("https://hip.hisense.com")
        }else{
          window.open("http://hip-web-hisense-apigateway-test.devapps.hisense.com/gateway/indexPage")
        }
      },
      jump(num){
        if (this.enviroment==="staging"){
          if (!num){
             window.open("https://api-inner-openapi.devapps.hisense.com")
          }else {
            window.open("https://api-outer-openapi.devapps.hisense.com")
          }
        }else {
          if (!num){
            window.open("https://apis.prdapp.hisense.com")
          }else {
            window.open("https://openapi.hisense.com")
          }
        }
      }
    },
    watch:{
      "$route":"getPath"
    },
  }
</script>

<style lang="less">
  .project {
    #header {
      background-color: #060929;
      height: 46px;
      line-height: 46px;
      padding: 0 32px 0 28px;

      .logo {
        float: left;
        color: #ffffff;
        cursor: pointer;

        img {
          width: 112px;
          height: 18px;
          position: relative;
          top: -4px;
          margin-right: 10px;
        }
      }

      .nav {
        float: right;

        > li {
          float: left;
          color: #ffffff;
          height: 46px;
          cursor: pointer;
          a{
            color: #ffffff;
          }
          .router-link-active{
            color: #00AAA6;
          }
        }

        > li:nth-child(-n+3) {
          width: 86px;
          text-align: center;
          margin-right: 24px;
        }

        > li:nth-child(-n+3):hover > div {
          color: #38C2BB;
        }

        > li.login {
          margin-left: 10px;

          button {
            padding: 0 22px;
          }
        }

        > li.user {
          margin-left: 34px;
          a {
            color: #ffffff;
            height: 100%;
            display: block;
          }
        }

        > li.control-panel{
          margin-left: 34px;
        }

        > li.server {
          position: relative;

          .list {
            position: absolute;
            width: 600px;
            left: -256px;
            padding: 32px 32px 15px;
            text-align: left;
            line-height: 1;
            display: none;
            background-color: #ffffff;
            z-index: 3;
            border-radius: 4px;
            max-height: 500px;
            overflow-y: auto;
            border: 1px solid #cccccc;

            li span:nth-child(1) {
              float: left;
              width: 76px;
              font-size: 15px;
              color: #000000;
              font-weight: 600;
              overflow: hidden;
              text-overflow:ellipsis;
              white-space: nowrap;
              cursor: pointer;
              padding-bottom: 16px;
            }

            li span:nth-child(n+2) {
              float: left;
              color: #4A4A4A;
              padding-left: 24px;
              cursor: pointer;
              padding-bottom: 16px;
            }

            li span:nth-child(n+2):hover {
              color: #00AAA6;
            }
          }
        }

        > li.server:hover {
          .list {
            display: block;
          }
        }
      }
    }

    #footer {
      color: #999999;
      width: 1200px;
      height: 48px;
      margin: 0 auto;
      line-height: 48px;
      border-top: 1px solid #EAEAF3;
      text-align: center;
      font-size: 12px;
    }
  }
</style>
