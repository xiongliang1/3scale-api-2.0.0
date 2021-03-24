<template>
    <div class="subscriptionDetails">
        <header>
            <a-button icon="left" @click="goBack">返回</a-button>
            <span>当前页面：订阅详情</span>
        </header>
        <ul class="body">
            <li>
                <p class="title">API基本信息</p>
                <ol class="clearfix">
                    <li>
                        <span>API名称：</span>
                        <span>
              <a-tooltip>
                <template slot="title">
                  {{data.apiName}}
                </template>
                {{data.apiName}}
              </a-tooltip>
            </span>
                    </li>
                    <li>
                        <span>API所属分组：</span><span>{{data.groupName}}</span>
                    </li>
                    <li>
                        <span>一级类目：</span><span>{{data.categoryOneName}}</span>
                    </li>
                    <li>
                        <span>二级类目：</span><span>{{data.categoryTwoName}}</span>
                    </li>
                    <li>
                        <span>所属系统：</span><span>{{data.apiSystemName}}</span>
                    </li>
                </ol>
            </li>
            <li>
                <p class="title">API订阅信息</p>
                <ol class="clearfix">
                    <li>
                        <span>订阅人：</span><span>{{data.creator}}</span>
                    </li>
                    <li>
                        <span>订阅系统：</span><span>{{data.appSystemName}}</span>
                    </li>
                    <li>
                        <span>订阅时间：</span><span>{{data.createTime}}</span>
                    </li>
                </ol>
            </li>
            <li>
                <p class="title">API审批</p>
                <ol class="clearfix">
                    <li>
                        <span>审批人：</span><span>{{data.updater||"无"}}</span>
                    </li>
                    <li>
                        <span>审批时间：</span><span>{{data.updateTime||"无"}}</span>
                    </li>
                    <li>
                        <span>审批状态：</span><span>{{data.status|status}}</span>
                    </li>
                    <li style="width: 100%">
                        <span>审批备注：</span>
                        <span>
                 <a-textarea disabled style="margin:10px 0;height:70px" :value="data.remark" default-value="暂无" />
              </span>
                    </li>
                </ol>
            </li>
        </ul>
    </div>
</template>

<script>
    import request from "../../utils/request";
    let id;
    export default {
        name: "subscriptionDetails",
        data(){
            return{
                data:{},
                // url:'/apimgr/api/v1/tenant/tenant_id_1/project/PID_TEST1/'+(sessionStorage.getItem("enviroment") || "staging")
            }
        },
        filters:{
            status(num){
                if (num===0){
                    return "通过"
                }else if (num===1){
                    return "待审批"
                }else if (num===2){
                    return "通过"
                }else if (num===3){
                    return "拒绝"
                }
            }
        },
        methods:{
            getData(){
                if (!this.$route.query.id){
                    return
                }
                if (this.$route.query.type==="a"){
                    request(this.url+"/processRecord/application/"+this.$route.query.id,{
                        method:"GET"
                    }).then(res=>{
                        this.data=res
                    })
                }else{
                    request(this.url+"/applications/approvalComplete/"+this.$route.query.id,{
                        method:"GET"
                    }).then(res=>{
                        this.data=res
                    })
                }
            },
            goBack(){
                if (this.$multiTab){
                    this.$multiTab.closeCurrentPage();
                }
                setTimeout(()=>{
                    window.vm.$router.push({
                        path:"/gateway/subscription"
                    })
                })
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getData();
            id=this.$route.query.id;
        },
        watch:{
            "$store.state.appNowCategory":{
                deep: true,
                handler:function (oldValue,newValue) {
                    if (JSON.stringify(oldValue)===JSON.stringify(newValue)){
                        return;
                    }
                    this.$multiTab.closeAll();
                }
            }
        }
    }
</script>

<style scoped lang="less">
    ul,li{
        list-style: none;
    }
    .subscriptionDetails {
        header {
            > span {
                font-size: 14px;
                margin-left: 16px;
            }
        }

        .body {
            margin-top: 16px;
            background: #ffffff;
            height: calc(~"100% - 64px");
            width: 60%;

            > li {
                width: 95%;
                border-bottom: 1px solid #E8E8E8;
                padding-bottom: 16px;
                margin: 0 auto;

                .title {
                    font-size: 16px;
                    color: #000000;
                    padding-top: 16px;
                }

                ol {
                    li {
                        width: 50%;
                        float: left;
                        margin-top: 10px;

                        >span:nth-child(1) {
                            float: left;
                            width: 100px;
                            color: #333333;
                        }

                        >span:nth-child(2) span{
                            max-width: calc(~"100% - 100px");
                            float: left;
                            overflow:hidden;
                            text-overflow:ellipsis;
                            white-space:nowrap;
                            cursor: pointer;
                        }
                    }
                }
            }

        }
    }
</style>
