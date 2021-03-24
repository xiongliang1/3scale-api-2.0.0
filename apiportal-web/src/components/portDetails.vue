<template>
  <div class="portDetails">
    <header>
      <a-button icon="left" @click="$router.go(-1)">返回</a-button>
      <span>首页 / API市场 </span>
    </header>
    <div class="port clearfix">
      <div>
        <img :src="image">
      </div>
      <div>
        <p>{{info.name}}
          <span>{{info.publishApiGroupDto.categoryOneName||""}}-{{info.publishApiGroupDto.categoryTwoName||""}}</span>
          <a-tag v-for="item in info.sysyemNames" :key="item">
            {{item}}
          </a-tag>
        </p>
        <p>{{info.description}}</p>
        <a-button type="primary" @click="immediately">
          立即订阅
        </a-button>
      </div>
    </div>
    <div class="details">
      <div class="info">
        <div class="clearfix">
          <p><span>更新时间：</span><span>{{info.updateTime}}</span></p>
          <p><span>发布环境：</span><span>{{info.partition?"外网":"内网"}}</span></p>
          <p><span>所属系统：</span><span>{{info.publishApiGroupDto.systemName}}</span></p>
          <p><span>接口类型：</span><span>{{info.accessProType}}</span></p>
          <p v-if="!info.partition"><span>创建者：</span><span>{{info.creator}}</span></p>
        </div>
      </div>
      <div class="listInfo" style="padding-bottom: 0">
        <p class="title">定义API请求
          <a-radio-group v-model="value" style="margin-left: 10px">
          <a-radio-button value="a">
            在线配置
          </a-radio-button>
          <a-radio-button value="b">
            API文档
          </a-radio-button>
          <a-radio-button value="c">
            SDK上传
          </a-radio-button>
        </a-radio-group>
        </p>
      </div>
      <div v-show="value==='a'">
        <div class="listInfo" v-show="routerList.length>1" v-if="info.sysyemNames.length">
          <p class="title">API请求参数</p>
          <ul class="routerList clearfix">
            <li v-for="(item,index) in routerList" :key="index" @click="active=index;getApiParameter()"
                :class="{'active':active===index}">{{item.httpMethod}} <span>{{item.pattern|text}}</span></li>
          </ul>
        </div>
        <div class="box" v-show="routerList.length" v-if="requestParamsData.length||requestBodyData[0].flag||responseBodyData[0].flag">
          <div class="listInfo" v-show="requestParamsData.length">
            <p class="title">请求参数</p>
            <a-table
                    :columns="requestParamsColumns"
                    :data-source="requestParamsData"
                    class="apiDetails_tabel"
                    bordered
                    :pagination="{hideOnSinglePage:true}"
                    :rowKey="row=>row.index"
            >
              <div slot="required" slot-scope="text">
                {{text?"必填":"选填"}}
              </div>
            </a-table>
          </div>
          <div class="listInfo" v-show="requestBodyData[0].flag">
            <p class="title">请求body</p>
            <a-table
                    :columns="requestBodyColumns"
                    :data-source="requestBodyData"
                    class="apiDetails_tabel"
                    bordered
                    :pagination="{hideOnSinglePage:true}"
                    :rowKey="row=>row.index"
            ></a-table>
          </div>
          <div class="listInfo" v-show="responseBodyData[0].flag">
            <p class="title">输出参数</p>
            <a-table
                    :columns="requestBodyColumns"
                    :data-source="responseBodyData"
                    class="apiDetails_tabel"
                    bordered
                    :pagination="{hideOnSinglePage:true}"
                    :rowKey="row=>row.index"
            ></a-table>
          </div>
        </div>
      </div>
      <div v-show="value==='b'" style="margin-bottom: 10px;">
        <ul class="preview" v-show="info.sysyemNames.length">
          <li style="padding-bottom: 6px;color:#108ee9" v-for="item in info.attFiles" :key="item.id" @click="fileClick(item)" v-show="item.fileName.indexOf('.jar')<0">
            <a-icon type="link" style="margin-right: 4px;"/>
            {{item.fileName}}
          </li>
        </ul>
      </div>
      <div v-show="value==='c'" style="margin-bottom: 10px;">
        <ul class="preview" v-show="info.sysyemNames.length">
          <li style="padding-bottom: 6px;color:#108ee9" v-for="item in info.attFiles" :key="item.id" @click="fileClick(item)" v-show="item.fileName.indexOf('.jar')>-1">
            <a-icon type="link" style="margin-right: 4px;"/>
            {{item.fileName}}
          </li>
        </ul>
      </div>
      <div class="listInfo">
        <p class="title">返回码</p>
        <a-table
          :columns="returnCodeColumns"
          :data-source="proxyList"
          class="apiDetails_tabel"
          bordered
          :pagination="{hideOnSinglePage:true}"
          :rowKey="row=>row.index"
        ></a-table>
      </div>
    </div>

    <a-modal
      title="订阅确认"
      :visible="subscription"
      :confirm-loading="confirmLoading"
      okText="提交"
      @ok="handleOk"
      @cancel="handleCancel"
      width="1000px"
      dialogClass="subscription_modal"
    >
      <div class="title">订阅系统</div>
      <div>
        <span>你希望给哪个系统订阅接口？</span>
        <a-config-provider>
          <template v-if="!newCategory.length" #renderEmpty>
            <a-empty>
              <span slot="description">暂无数据，如需新增系统，点击通知管理员按钮 </span>
              <a-button type="primary" @click="inform" :loading="loading">
                通知管理员
              </a-button>
            </a-empty>
          </template>
          <!--<a-select style="width: 763px" v-model="subscriptionSystem" placeholder="请选择" mode="multiple" show-search v-if="subscription" @popupScroll="handlePopupScroll" @blur="scrollPage=1;newCategory=systems.slice(0, 80)" @search="handleSearch">
            <a-select-option v-for="(item,index) in newCategory" :key="item.itemName" :value="item.itemName" :disabled="item.disabled">
              {{item.itemName}}
            </a-select-option>
          </a-select>-->
          <a-select
                  mode="multiple"
                  placeholder="请选择"
                  :value="selectedItems"
                  style="width: 763px"
                  @change="handleChange"
                  :filter-option="filterOption"
          >
            <a-select-option v-for="item in filteredOptions" :key="item.id" :value="item.id">
              {{ item.itemName }}
            </a-select-option>
          </a-select>
        </a-config-provider>

        <a-textarea v-model="desc" style="height: 200px;margin-top: 20px" placeholder="请输入补充说明"></a-textarea>
      </div>
    </a-modal>
  </div>
</template>

<script>
  export default {
    name: "portDetails",
    data() {
      return {
        data: [],
        routerRulesColumns: [
          {
            title: '请求方法',
            dataIndex: 'type',
            key: 'type',
            width:100
          },
          {
            title: '访问路径',
            dataIndex: 'pattern',
            key: 'pattern',
            width:200
          },
          {
            title: '接口地址',
            dataIndex: 'url',
            key: 'url',
            scopedSlots: {customRender: 'url'},
          }
        ],
        returnCodeColumns: [
          {
            title: '类型',
            dataIndex: 'type',
            key: 'type'
          },
          {
            title: 'response code',
            dataIndex: 'responseCode',
            key: 'responseCode',
          },
          {
            title: 'content type',
            dataIndex: 'contentType',
            key: 'contentType'
          },
          {
            title: 'response body',
            dataIndex: 'responseBody',
            key: 'responseBody'
          }
        ],
        requestParamsColumns: [
          {
            title: "参数名",
            dataIndex: "name",
            key: "name"
          },
          {
            title: "参数位置",
            dataIndex: "localation",
            key: "localation"
          },
          {
            title: "类型",
            dataIndex: "type",
            key: "type"
          },
          {
            title: "必填",
            dataIndex: "required",
            key: "required",
            scopedSlots: {customRender: 'required'},
          },
          {
            title: "示例",
            dataIndex: "defaultValue",
            key: "defaultValue"
          },
          {
            title: "描述",
            dataIndex: "value",
            key: "value"
          }
        ],
        requestParamsData: [],
        requestBodyColumns: [
          {
            title: "参数名",
            dataIndex: "name",
            key: "name"
          },
          {
            title: "类型",
            dataIndex: "type",
            key: "type"
          },
          {
            title: "示例",
            dataIndex: "defaultValue",
            key: 'defaultValue'
          },
          {
            title: "描述",
            dataIndex: "value",
            key: "value"
          }
        ],
        requestBodyData: [{}],
        responseBodyData: [{}],
        info: {
          publishApiGroupDto: {},
          attFiles:[],
          sysyemNames:[]
        },
        subscription: false,
        confirmLoading: false,
        subscriptionSystem: undefined,
        proxyList: [],
        routers: [],
        routerList: [],
        active: 0,
        systems: JSON.parse(sessionStorage.getItem("systemList")),
        desc: undefined,
        image:require("../assets/api-cover-default-green.png"),
        routerTable:[],
        value:"a",
        searchValue: null,
        searchSystem:[],
        newCategory: [],
        loading:false,
        selectedItems:[]
      }
    },
    filters: {
      text(str) {
        if (str < 17) {
          return str
        } else {
          return "..." + str.substring(0, 17)
        }
      }
    },
    computed: {
      filteredOptions() {
        return this.systems.filter(o => !this.selectedItems.includes(o.itemName));
      },
    },
    methods: {
      immediately() {
        this.subscription = true;
      },
      handleOk() {
        if (!this.selectedItems.length) {
          this.$message.error("请选择需要订阅的系统")
          return
        }
        /*let arr=[];
        for (let i=0;i<this.systems.length;i++){
          for (let j=0;j<this.subscriptionSystem.length;j++){
            if (this.systems[i].itemName===this.subscriptionSystem[j]){
              arr.push(this.systems[i].id)
              break
            }
          }
        }*/
        this.confirmLoading=true;
        this.axios.post("/applications/subscribeApi", [{
          id: this.$route.query.id + "",
          system: this.selectedItems,
          description: this.desc || null
        }]).then(res => {
          this.confirmLoading=false;
          if (res.code === "0" || res.code === "2") {
            this.$notification.success({
              message: '通知',
              description: res.msg,
              duration: 3
            });
            this.$router.push({path: "/principal/developerCenter/myApply"})
          }else{
            this.$notification.error({
              message:'通知',
              description:res.msg,
              duration:2
            })
          }
        }).catch(err=>{
          this.confirmLoading=false;
        })
      },
      handleCancel() {
        this.subscription = false;
        this.subscriptionSystem = undefined;
      },
      getData() {
        this.axios.get("/publishApi/" + this.$route.query.id).then(res => {
          this.info = res.data;
          this.getPortUrl()
          this.info.sysyemNames=[];
          let data=JSON.parse(sessionStorage.getItem("systemList"));
          if (res.data.subscribeSystem){
            for (let i=0;i<res.data.subscribeSystem.length;i++){
              for (let j=0;j<data.length;j++){
                if (res.data.subscribeSystem[i]===data[j].id){
                  this.info.sysyemNames.push(data[j].itemName);
                  break;
                }
              }
            }
          }
          for (let i=0;i<this.systems.length;i++){
            if (this.info.subscribeSystem.includes(this.systems[i].id)){
              this.systems[i].disabled=true;
            }else{
              this.systems[i].disabled=false;
            }
          }
          this.getProxyData(res.data.proxy);
          if (!this.info.apiMappingRuleDtos) {
            return
          }
          for (let i = 0; i < this.info.apiMappingRuleDtos.length; i++) {
            if (this.info.apiMappingRuleDtos[i].httpMethod === "OPTIONS") {
              continue
            }
            this.routerList.push(this.info.apiMappingRuleDtos[i])
          }
          this.getApiParameter();
          if (res.data.picFiles.length){
            this.showImage(this.info.picFiles[0].id)
          }else if (res.data.secretLevel==="低"){
            this.image=require("../assets/api-cover-default-green.png");
          }else if (res.data.secretLevel==="高"){
            this.image=require("../assets/api-cover-default-red.png")
          }
        })
      },
      getProxyData(proxy) {
        if (!proxy) {
          return
        }
        this.proxyList = [{}, {}, {}, {}];
        this.proxyList[0].type = "鉴权失败";
        this.proxyList[0].responseCode = proxy.errorStatusAuthFailed;
        this.proxyList[0].contentType = proxy.errorHeadersAuthFailed;
        this.proxyList[0].responseBody = proxy.errorAuthMissing;
        this.proxyList[0].index = 0;
        this.proxyList[1].type = "鉴权信息丢失";
        this.proxyList[1].responseCode = proxy.errorStatusAuthMissing;
        this.proxyList[1].contentType = proxy.errorHeadersAuthMissing;
        this.proxyList[1].responseBody = proxy.errorAuthMissing;
        this.proxyList[1].index = 1;
        this.proxyList[2].type = "未匹配到路由";
        this.proxyList[2].responseCode = proxy.errorStatusNoMatch;
        this.proxyList[2].contentType = proxy.errorHeadersNoMatch;
        this.proxyList[2].responseBody = proxy.errorNoMatch;
        this.proxyList[2].index = 2;
        this.proxyList[3].type = "限流";
        this.proxyList[3].responseCode = proxy.errorStatusLimitsExceeded;
        this.proxyList[3].contentType = proxy.errorHeadersLimitsExceeded;
        this.proxyList[3].responseBody = proxy.errorLimitsExceeded;
        this.proxyList[3].index = 3;
      },
      getApiParameter() {
        if (!this.info.apiMappingRuleDtos) {
          return
        }
        this.requestParamsData = [];
        this.requestBodyData = [];
        this.responseBodyData = [];
        if (this.routerList[this.active || 0].requestParams){
          this.routerList[this.active || 0].requestParams.forEach((item, index) => {
            this.requestParamsData.push({
              name: item.name || "-",
              localation: item.paramType || "-",
              type: item.dataType || "-",
              required: (typeof item.required === "boolean") ? item.required : "-",
              defaultValue: item.defaultValue || "-",
              value: item.value || "-",
              index: index
            })
          })
        }
        if (this.routerList[this.active || 0].requestBody) {
          let flag;
          if (this.routerList[this.active || 0].requestBody.defaultValue===null&&this.routerList[this.active || 0].requestBody.name===null&&this.routerList[this.active || 0].requestBody.object===null&&this.routerList[this.active || 0].requestBody.paramType===null&&this.routerList[this.active || 0].requestBody.required===false&&this.routerList[this.active || 0].requestBody.value===null){
            flag=false;
          }else {
            flag=true;
          }
          let requestBody = {
            name: "requestBody",
            type: this.routerList[this.active || 0].requestBody.dataType || "-",
            defaultValue: this.routerList[this.active || 0].requestBody.defaultValue || "-",
            value: this.routerList[this.active || 0].requestBody.description || "-",
            index: "0",
            flag:flag
          }
          if (this.routerList[this.active || 0].requestBody.object) {
            requestBody.children = [];
            this.routerList[this.active || 0].requestBody.object.forEach((item, index) => {
              let json = {
                name: item.name || "-",
                type: item.dataType || "-",
                defaultValue: item.defaultValue || "-",
                value: item.description || "-",
                index: "0" + index
              }
              if (item.object) {
                json.children = [];
                item.object.forEach((it, num) => {
                  let jsonOne = {
                    name: it.name || "-",
                    type: it.dataType || "-",
                    defaultValue: it.defaultValue || "-",
                    value: it.description || "-",
                    index: "0" + index + num
                  }
                  if (it.object) {
                    jsonOne.children = [];
                    it.object.forEach((i, n) => {
                      let jsonTwo = {
                        name: i.name || "-",
                        type: i.dataType || "-",
                        defaultValue: i.defaultValue || "-",
                        value: i.description || "-",
                        index: "0" + index + num + n
                      }
                      jsonOne.children.push(jsonTwo)
                    })
                  }
                  json.children.push(jsonOne)
                })
              }
              requestBody.children.push(json);
            })
          }
          this.requestBodyData.push(requestBody)
        }else {
          this.requestBodyData = [{}];
        }
        let flag;
        if (this.routerList[this.active || 0].responseBody){
          if (this.routerList[this.active || 0].responseBody.defaultValue===null&&this.routerList[this.active || 0].responseBody.object===null&&this.routerList[this.active || 0].responseBody.description===null){
            flag=false;
          }else {
            flag=true;
          }
        }
        let responseBody = {
          name: "responseBody",
          type: this.routerList[this.active || 0].responseBody?(this.routerList[this.active || 0].responseBody.dataType || "-"):"-",
          defaultValue: this.routerList[this.active || 0].responseBody?(this.routerList[this.active || 0].responseBody.defaultValue || "-"):"-",
          value: this.routerList[this.active || 0].responseBody?(this.routerList[this.active || 0].responseBody.description || "-"):"-",
          index: 0,
          flag:flag
        }
        if (this.routerList[this.active || 0].responseBody&&this.routerList[this.active || 0].responseBody.object) {
          responseBody.children = [];
          this.routerList[this.active || 0].responseBody.object.forEach((item, index) => {
            let json = {
              name: item.name || "-",
              type: item.dataType || "-",
              defaultValue: item.defaultValue || "-",
              value: item.description || "-",
              index: "0" + index
            }
            if (item.object) {
              json.children = [];
              item.object.forEach((it, num) => {
                let jsonOne = {
                  name: it.name || "-",
                  type: it.dataType || "-",
                  defaultValue: it.defaultValue || "-",
                  value: it.description || "-",
                  index: "0" + index + num
                }
                if (it.object) {
                  jsonOne.children = [];
                  it.object.forEach((i, n) => {
                    let jsonTwo = {
                      name: i.name || "-",
                      type: i.dataType || "-",
                      defaultValue: i.defaultValue || "-",
                      value: i.description || "-",
                      index: "0" + index + num + n
                    }
                    jsonOne.children.push(jsonTwo)
                  })
                }
                json.children.push(jsonOne)
              })
            }
            responseBody.children.push(json);
          })
        }
        this.responseBodyData.push(responseBody)
      },
      showImage(id){
        this.axios.get("/publishApi/showApiDocFile/"+id).then(res=>{
          this.image=res.data
        })
      },
      fileClick(item){
        if (window.location.host!=="portal-web-hisense-apigateway-test.devapps.hisense.com"&&window.location.host!=="localhost:8080"){
          if (item.fileName.substring(item.fileName.length-3,item.fileName.length)==="jar"){
            window.open("/api/v1/"+(sessionStorage.getItem("enviroment")||"staging")+"/publishApi/downloadApiDocFile/"+item.id)
          }else{
            let originUrl = window.location.origin+"/api/v1/"+(sessionStorage.getItem("enviroment")||"staging")+"/publishApi/downloadApiDocFile/"+item.id;
            let previewUrl = originUrl + '?fullfilename='+item.fileName;
            window.open(window.location.protocol+"//kkfileview-hisense-hip-prd.prdapp.hisense.com/onlinePreview?url="+encodeURIComponent(previewUrl));
          }
        }else{
          if (item.fileName.substring(item.fileName.length-3,item.fileName.length)==="jar"){
            window.open(window.location.origin+"/api/v1/"+(sessionStorage.getItem("enviroment")||"staging")+"/publishApi/downloadApiDocFile/"+item.id)
          }else{
            let originUrl = window.location.origin+"/api/v1/"+(sessionStorage.getItem("enviroment")||"staging")+"/publishApi/downloadApiDocFile/"+item.id;
            let previewUrl = originUrl + '?fullfilename='+item.fileName;
            window.open(window.location.protocol+"//kkfile-hisense-apigateway-test.devapps.hisense.com/onlinePreview?url="+encodeURIComponent(previewUrl));
          }
        }
      },
      getPortUrl() {
        this.axios.get(`debugging/${this.$route.query.id}/getPath/${this.info.partition}`).then(res => {
          let arr=[];
          res.data.forEach((item,index)=> {
            for (let key in item){
              arr.push({
                type:key,
                url:this.info.sysyemNames.length?item[key]:"",
                pattern:this.info.apiMappingRuleDtos[index].pattern,
                index:index
              })
            }
          })
          this.routerTable=arr;
        })
      },
      handlePopupScroll(e) {
        if (this.searchValue){
          return;
        }
        const {target} = e;
        // scrollHeight：代表包括当前不可见部分的元素的高度
        // scrollTop：代表当有滚动条时滚动条向下滚动的距离，也就是元素顶部被遮住的高度
        // clientHeight：包括padding但不包括border、水平滚动条、margin的元素的高度
        const rmHeight = target.scrollHeight - target.scrollTop;
        const clHeight = target.clientHeight;
        if (rmHeight - clHeight <= 10) {
          if (this.systems.length === this.newCategory.length) {
            return
          }
          this.scrollPage++;
        }
      },
      handleSearch(val) {
        if (!val){
          this.newCategory = this.systems.slice(0, 80);
          return
        }
        let timer=setTimeout(()=>{
          this.scrollPage=1;
          this.searchValue=val;
          this.newCategory=[]
          this.systems.forEach(item=>{
            if (item.itemName.indexOf(val)>-1){
              this.newCategory.push(item)
            }
          })
          clearTimeout(timer)
        },300)
      },
      inform(){
        this.loading=true;
        let name=document.querySelector(".ant-select-search__field").value;
        this.axios.get(`/dataItems/getSystem/sendEmail?systemName=${name}&status=1`).then(res=>{
          this.loading=false;
          if (res.code==="0"){
            this.$notification.success({
              message:'通知',
              description:res.msg,
              duration:2
            })
          }
        })
      },
      filterOption(input, option) {
        return (
                option.componentOptions.children[0].text
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
        )
      },
      handleChange(selectedItems) {
        this.selectedItems = selectedItems;
      },
    },
    created() {
      this.newCategory = this.systems.slice(0, 80);
      this.getData();
    },
    watch: {
      scrollPage(newValue, oldValue) {
        if (newValue > 1) {
          this.newCategory.push(...this.systems.slice(oldValue * 80, newValue * 80));
        } else {
          this.newCategory = this.systems.slice(0, 80);
        }
      }
    }
  }
</script>

<style lang="less">
  .portDetails {
    width: 1200px;
    margin: 0 auto;

    header {
      padding-top: 16px;

      > span {
        font-size: 14px;
        margin-left: 16px;
      }
    }

    .port {
      margin: 16px auto;
      padding: 24px 32px;
      background: #ffffff;

      > div:nth-child(1) {
        float: left;
        width: 64px;
        height: 64px;
        border-radius: 8px;
        i {
          font-size: 32px;
          color: #AFB5C1;
          line-height: 64px;
          position: relative;
          left: 50%;
          transform: translateX(-50%);
        }
        img{
          width: 100%;
          height: 100%;
        }
      }

      > div:nth-child(2) {
        float: left;
        width: 88%;
        padding-left: 32px;

        p:nth-child(1) {
          font-size: 18px;
          color: #000000;

          span:nth-child(1) {
            height: 22px;
            background: rgba(230, 247, 255, 1);
            border-radius: 4px;
            border: 1px solid rgba(145, 213, 255, 1);
            font-size: 12px;
            color: #108EE9;
            display: inline-block;
            line-height: 22px;
            text-align: center;
            margin-left: 8px;
            padding: 0 6px;
          }
        }

        p:nth-child(2) {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.35);
          padding-top: 12px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        > button:nth-child(3) {
          width: 148px;
          margin-top: 16px;
        }

        > a:nth-child(4) {
          color: rgba(0, 0, 0, 0.35);
          padding-left: 24px;
          cursor: text;

          span {
            color: #00AAA6;
          }
        }
      }
    }

    .details {
      padding: 26px;
      background: #ffffff;

      .info {
        padding-bottom: 30px;

        > div {
          padding-bottom: 10px;

          > p {
            float: left;
            width: 25%;

            > span:nth-child(1) {
              color: rgba(0, 0, 0, 0.35);
            }

            > span:nth-child(2) {
              color: rgba(0, 0, 0, 0.65);
            }
          }

          > .url {
            span {
              float: left;
            }

            span:nth-child(2) {
              width: 83%;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
      }

      .listInfo {
        .title {
          font-size: 16px;
          color: #000000;
          padding-bottom: 18px;
          line-height: 1;
          font-weight: 600;
        }

        .routerList {
          padding-bottom: 16px;

          li {
            cursor: pointer;
            width: 188px;
            height: 32px;
            float: left;
            background: #EEF3F2;
            color: #8DB6B5;
            line-height: 32px;
            padding: 0 8px;
            overflow: hidden;
            margin-right: 10px;

            span {
              margin-left: 14px;
            }
          }

          .active {
            background-color: #00AAA6;
            color: #ffffff;
          }
        }

        .preview{
          li{
            display: inline-block;
            margin-right: 20px;
            margin-bottom: 10px;
            cursor: pointer;
          }
        }
      }

      .listInfo:not(:last-child) {
        padding-bottom: 32px;
      }

      .box {
        box-shadow: 0 1px 12px 0 rgba(0, 0, 0, 0.15);
        padding: 24px;
        margin-bottom: 24px;
        border-radius: 4px;
      }
    }
  }
</style>
<style>
  .subscription_modal .title {
    color: #000000;
    font-size: 16px;
  }

  .subscription_modal .table {
    width: 952px;
    margin: 10px auto 0;
    max-height: 300px;
    overflow-y: auto;
  }

  .subscription_modal .table .head {
    height: 46px;
    line-height: 46px;
    background: rgba(0, 0, 0, 0.02);
  }

  .subscription_modal .table .head li {
    float: left;
  }

  .subscription_modal .table .head li:nth-child(1) {
    width: 7%;
    text-indent: 16px;
  }

  .subscription_modal .table .head li:nth-child(2) {
    width: 23%;
  }

  .subscription_modal .table .head li:nth-child(3) {
    width: 70%;
  }

  .subscription_modal .table .body span {
    float: left;
  }

  .subscription_modal .table .body span:nth-child(1) {
    line-height: 46px;
    text-indent: 16px;
    width: 7%;
  }

  .subscription_modal .table .body span:nth-child(2) {
    width: 23%;
    line-height: 46px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .subscription_modal .table .body span:nth-child(3) {
    width: 70%;
  }

  .subscription_modal .table .body span:nth-child(3) textarea {
    width: 95%;
    height: 16px;
    margin-top: 8px;
  }
</style>
