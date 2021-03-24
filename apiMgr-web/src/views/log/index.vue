<template>
    <div class="log">
        <header>
            <div>
                <a-radio-group v-model.trim="value" @change="getData">
                    <a-radio-button value="0">
                        全部
                    </a-radio-button>
                    <a-radio-button value="1">
                        请求成功
                    </a-radio-button>
                    <a-radio-button value="2">
                        请求失败
                    </a-radio-button>
                </a-radio-group>
            </div>
            <a-row>
                <a-col :span="3">
                    <a-input v-model="requestBody" placeholder="请输入请求体"/>
                </a-col>
                <a-col :span="3">
                    <a-input v-model="responseBody" placeholder="请输入返回参数"/>
                </a-col>
                <a-col :span="3">
                    <a-input v-model="name" placeholder="请输入API名称"/>
                </a-col>
                <a-col :span="10">
                    <a-range-picker
                            :placeholder="['开始时间','结束时间']"
                            :show-time="{
              defaultValue: [moment('00:00:00', 'HH:mm:ss'), moment('11:59:59', 'HH:mm:ss')],
            }"
                            format="YYYY-MM-DD HH:mm:ss"
                            @change="switchTime"
                            :allowClear="false"
                            :default-value="timer"
                    />
                </a-col>
                <a-col :span="5">
                    <a-button type="primary" @click="getData()">
                        查询
                    </a-button>
                    &nbsp;&nbsp;
                    <a-button @click="name=null;requestBody=null;responseBody=null;getData()">重置</a-button>
                    &nbsp;&nbsp;
                    <a-button type="primary" @click="exports">导出</a-button>
                </a-col>
            </a-row>
        </header>

        <div class="body">
            <a-table
                    :columns="columns"
                    :data-source="data"
                    :pagination="pagination"
                    :rowKey="row=>row.index"
                    size="small"
                    @change="handleTableChange"
                    :loading="loading"
            >
                <div slot="requestParam" slot-scope="text,record">
                    <a-icon type="appstore" @click="show(0,record)" />
                </div>
                <div slot="responseBody" slot-scope="text,record">
                    <a-icon type="appstore" @click="show(1,record)" />
                </div>
                <div slot="httpStatusCode" slot-scope="text">
                   <span style="width: 4px;height: 4px;display: inline-block;background: #87d068;border-radius: 5px;position: relative;top: -2px" v-show="text[0]==='2'||text[0]==='3'"></span>
                   <span style="width: 4px;height: 4px;display: inline-block;background: #f50;border-radius: 5px;position: relative;top: -2px" v-show="text[0]==='4'||text[0]==='5'"></span>
                    {{text}}
                </div>
                <div slot="responseTime" slot-scope="text">
                    {{text}}
                </div>
                <div slot="operation" slot-scope="text,record" class="operation">
                    <!--          <span style="cursor: pointer" @click="modification">修改</span>-->
                    <span @click="resend(record)" style="cursor: pointer">重发</span>
                </div>
            </a-table>
        </div>

        <a-modal
                title="修改"
                :visible="visible"
                width="546px"
                @cancel="handleCancel"
        >
            <div>
                <span>输入参数：</span>
                <a-input v-model="parameter" style="width: 85%" />
            </div>
            <template slot="footer">
                <a-button @click="handleCancel">
                    取消
                </a-button>
                <a-button @click="save">
                    保存
                </a-button>
                <a-button type="primary" @click="handleOk">
                    保存并重发
                </a-button>
            </template>
        </a-modal>

        <a-modal
                :title="text"
                :visible="response"
                @cancel="response=false"
                @ok="response=false"
        >
            <div style="max-height: 400px;overflow-y: auto">
                <p v-show="text==='输入参数'" style="margin: 8px 0;color: #333333;font-size: 18px">请求头</p>
                <div v-show="text==='输入参数'">
                    {{content.requestHeader||"空"}}
                </div>
                <p v-show="text==='输入参数'" style="margin: 8px 0;color: #333333;font-size: 18px">请求体</p>
                <div v-show="text==='输入参数'">{{content.requestBody||"空"}}</div>
                <p v-show="text==='输出参数'" style="margin: 8px 0;color: #333333;font-size: 18px">返回参数</p>
                <div v-show="text==='输出参数'">{{content.responseBody||"空"}}</div>
            </div>
        </a-modal>
    </div>
</template>

<script>
    import request from "../../utils/request";
    import axios from "axios";
    const columns = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName'
        },
        {
            title: '请求时间',
            dataIndex: 'requestTime',
            key: 'requestTime',
            sorter: true,
            width: 160
        },
        {
            title: '请求方式',
            dataIndex: 'httpMethod',
            key: 'httpMethod',
            width: 90
        },
        {
            title: '路由',
            dataIndex: 'httpPattern',
            key: 'httpPattern',
            ellipsis: true
        },
        {
            title: '调用系统',
            dataIndex: 'callSystem',
            key: 'callSystem'
        },
        {
            title: '接口系统',
            dataIndex: 'apiSystem',
            key: 'apiSystem'
        },
        {
            title: '输入参数',
            dataIndex: 'requestParam',
            key: 'requestParam',
            scopedSlots: {customRender: 'requestParam'}
        },
        {
            title: '输出参数',
            dataIndex: 'responseBody',
            key: 'responseBody',
            ellipsis: true,
            scopedSlots: {customRender: 'responseBody'}
        },
        {
            title: '请求状态',
            dataIndex: 'httpStatusCode',
            key: 'httpStatusCode',
            scopedSlots: {customRender: 'httpStatusCode'}
        },
        {
            title: '请求耗时(ms)',
            dataIndex: 'responseTime',
            key: 'responseTime',
            scopedSlots: {customRender: 'responseTime'},
            width: 110
        },
        {
            title: 'IP地址',
            dataIndex: 'ipList',
            key: 'ipList'
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'}
        }
    ];
    import moment from "moment"
    import Cookies from "js-cookie";
    export default {
        name: "index",
        data(){
            return{
                value:"0",
                select:'1',
                name:null,
                requestBody:null,
                responseBody:null,
                visible:false,
                parameter:null,
                columns,
                pagination:{//分页信息
                    total:0,
                    current:1,
                    defaultCurrent:1,
                    showSizeChanger:true,
                    defaultPageSize:10,
                    pageSize:10,
                    pageSizeOptions:['10', '20', '30', '40'],
                    showQuickJumper:true,
                    showTotal:((total) => {
                        return `共 ${total} 条`;
                    }),
                },
                data:[],
                searchData:{
                    current: 1,
                    pageSize:10,
                    sort:null
                },
                loading:false,
                timer:["",""],
                url:null,
                response:false,
                content:{},
                text:"输入参数"
            }
        },
        methods:{
            moment,
            switchTime(date, dateString) {//时间插件
                this.timer = dateString;
            },
            resend(item){//重发
                let that=this;
                this.$confirm({
                    title: '确定要重新发起该请求吗？',
                    okText: '确定',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk(){
                        request(that.url+"/apiInvokeLog/recall",{
                            method: "POST",
                            body: {
                                hashId:item.hashId,
                                requestTime:item.requestTime+"",
                                apiId:item.apiId
                            }
                        })
                    }
                });
            },
            handleOk(){//确认
                this.visible=false;
            },
            handleCancel(){//取消
                this.visible=false;
            },
            modification(){//修改
                this.visible=true;
            },
            save(){//保存
                this.visible=false;
            },
            getData(){
                let timer={
                    start:moment(this.timer[0]).format("YYYY-MM-DD HH:mm:ss"),
                    end:moment(this.timer[1]).format("YYYY-MM-DD HH:mm:ss")
                }
                this.loading=true;
                request(this.url+"/apiInvokeLog/queryForPage",{
                    method:"POST",
                    body:{
                        apiName:this.name,
                        pageNum:this.searchData.current,
                        pageSize:this.searchData.pageSize,
                        statType:this.value-0,
                        timeQuery:timer,
                        sort:this.searchData.sort==="ascend"?["a","startTime"]:["d","startTime"],
                        requestBody:this.requestBody || null,
                        responseBody:this.responseBody || null
                    }
                }).then(res=>{
                    this.loading=false;
                    if (!res){
                        this.data=[];
                        this.pagination.total=0;
                        return
                    }
                    this.data=res.content;
                    this.data.forEach((item,index)=>{
                        item.index=index
                    })
                    this.pagination.total=res.totalElements;
                })
            },
            exports(){
                let timer={
                    start:moment(this.timer[0]).format("YYYY-MM-DD HH:mm:ss"),
                    end:moment(this.timer[1]).format("YYYY-MM-DD HH:mm:ss")
                }
                axios({
                    url: this.url+"/apiInvokeLog/download",
                    method: "post",
                    headers: {
                        'Content-Type': 'application/json',
                        "Access-controt-allow-0rigin":"*",
                        token: this.$ls.get("Access-Token"),
                        Authorization: `Bearer ${this.$ls.get("Access-Token")}`,
                        "current-id": Cookies.get("current-id")
                    },
                    data:{
                        apiName:this.name,
                        pageNum:this.searchData.current,
                        pageSize:this.pagination.total,
                        statType:this.value-0,
                        timeQuery:timer,
                        sort:this.searchData.sort==="ascend"?["a","startTime"]:["d","startTime"]
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
            },
            handleTableChange(pagination, filters, sorter){
                let json={
                    current:pagination.current,
                    pageSize:pagination.pageSize,
                    sort:sorter.order
                }
                if (this.searchData.current!==json.current){
                    this.pagination.current=json.current;
                    this.searchData.current=json.current;
                    this.getData();
                }else {
                    this.searchData.pageSize=json.pageSize;
                    this.searchData.sort=json.sort;
                    this.searchData.current=1;
                    this.pagination.current=1;
                    this.getData();
                }
            },
            show(num,item){
                this.content={};
                if (!num){
                    console.log(item);
                    this.content["requestHeader"]=item.requestHeader?JSON.stringify(item.requestHeader):""
                    this.content["requestBody"]=item.requestBody?JSON.stringify(item.requestBody):""
                    this.text="输入参数"
                    this.response=true;
                }else {
                    console.log(item);
                    this.content["responseBody"]=item.responseBody?JSON.stringify(item.responseBody):""
                    this.text="输出参数"
                    this.response=true;
                }
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            let time=[];
            time[0]=moment(Date.parse(new Date())-30*60*60*1000).format('YYYY-MM-DD HH:mm:ss');
            time[1]=moment(Date.parse(new Date())).format('YYYY-MM-DD HH:mm:ss');
            this.timer=[moment(time[0],'YYYY-MM-DD HH:mm:ss'), moment(time[1],'YYYY-MM-DD HH:mm:ss')];
            this.getData()
        },
        activated() {
          console.log("log：进入activated")
          this.getData()
        },
        watch:{
            "$store.state.appNowCategory":{
                deep:true,
                handler:function (oldValue,newValue) {
                    if (JSON.stringify(oldValue)===JSON.stringify(newValue)){
                        return;
                    }
                    this.$multiTab.closeAll();
                    return;
                    this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${this.$store.state.appNowCategory.gateway[1].id}/${this.$store.state.appNowCategory.gateway[0].key}`
                    if (window.vm.$route.name==='log'){
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.getData();
                    }
                }
            }
        },
    }
</script>

<style scoped lang="less">
    .log {
        header {
            padding: 16px 16px 0;
            overflow: hidden;
            >div:nth-child(1){
                >button{
                    margin-top: 10px;
                }
                .ant-input {
                    width: 150px;
                    margin-top: 10px;
                }
            }
            .search {
                float: right;

                .ant-input {
                    width: 150px;
                }

                >button{
                    float: right;
                    margin-top: 20px;
                    margin-right: 10px;
                }
            }
            .ant-row{
                margin-top: 10px;
                >.ant-col{
                    padding-right: 10px;
                    .ant-calendar-picker{
                        width: 100%!important;
                    }
                }
            }
        }

        .body {
            padding: 16px;
            background-color: #ffffff;
            min-height: calc(~"100vh - 163px");

            .operation {
                span {
                    color: #00aaa6;
                    margin-right: 6px;
                }
            }
        }
    }
</style>
