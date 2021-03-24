<template>
    <div class="flowCenterDetails">
        <type1 :info="info" v-if="info.type===6||info.type===7"></type1>
        <!-- -----------------------------------------API发布和订阅------------------------------------------------>
        <type2 :info="info" v-if="info.type===0||info.type===1"></type2>
        <!--  ----------------------------------- 服务申请和应用订阅----------------------------------------------------->
        <type3 :info="info" v-if="info.type===3"></type3>
        <!--  ---------------------------------------短信申请-------------------------------------------------->
        <type4 :info="info" v-if="info.type===4"></type4>
        <!--  ---------------------------------------信鸿公众号-------------------------------------------------->
        <type5 :info="info" v-if="info.type===5"></type5>
        <!--  ---------------------------------------信鸿待办-------------------------------------------------->
        <info :info="info" v-show="$route.query.page&&$route.query.page!=='queryPersonWorkItemsWithBizInfo'&&(info.type===3 || info.type===4 || info.type===5)"></info>
        <!--  ---------------------------------------审批信息-------------------------------------------------->
        <p class="title">审批记录</p>
        <div class="table">
            <div>
                <span>当前节点审批人：<a-tag style="margin-bottom: 6px" v-for="item in person" :key="item.name">{{item.name}}</a-tag></span>
                <span v-show="info.currentState==='完成'">流程已结束</span>
                <span v-show="info.currentState==='终止'">流程异常终止</span>
            </div>
            <a-table
                    :columns="columns"
                    :data-source="data"
                    :loading="loading"
                    :rowKey="row=>row.activityInstID"
                    :pagination="pagination"
                    @change="handleTableChange"
            ></a-table>
        </div>
        <!--  ---------------------------------------审批记录-------------------------------------------------->
        <div class="operation"
             v-show="info.isCurrentHandle===1">
            <a-radio-group name="radioGroup" v-model="type">
                <a-radio :value="1">
                    通过
                </a-radio>
                <a-radio :value="0">
                    驳回
                </a-radio>
            </a-radio-group>
            <div style="margin-top: 10px">
                <a-form-model ref="form" :model="form" :label-col="labelCol" :wrapper-col="wrapperCol" :rules="rules">
                    <a-form-model-item label="驳回节点：" prop="node" v-if="!type&&nodeList.length">
                        <a-select placeholder="请选择" v-model="form.node">
                            <a-select-option :value="item.id" v-for="item in nodeList" :key="item.id">
                                {{item.name}}
                            </a-select-option>
                        </a-select>
                    </a-form-model-item>
                    <a-form-model-item label="短信平台账号：" prop="account" v-if="info.type===3&&type">
                        <a-input v-model.trim="form.account"/>
                    </a-form-model-item>
                    <a-form-model-item label="短信平台密码：" prop="password" v-if="info.type===3&&type">
                        <a-input-password v-model.trim="form.password"/>
                    </a-form-model-item>
                    <a-form-model-item label="短信鉴权：" prop="smsAuth" v-if="info.type===3&&type">
                        <a-select v-model="form.smsAuth">
                            <a-select-option value="1">
                                明文鉴权
                            </a-select-option>
                            <a-select-option value="2">
                                加密鉴权
                            </a-select-option>
                        </a-select>
                    </a-form-model-item>
                    <a-form-model-item label="EID：" prop="eid" v-if="info.type===4&&type">
                        <a-input v-model.trim="form.eid"/>
                    </a-form-model-item>
                    <a-form-model-item label="公众号ID：" prop="officialAccounts" v-if="info.type===4&&type">
                        <a-input v-model.trim="form.officialAccounts"/>
                    </a-form-model-item>
                    <a-form-model-item label="公众号秘钥：" prop="secretKey" v-if="info.type===4&&type">
                        <a-input v-model.trim="form.secretKey"/>
                    </a-form-model-item>
                    <a-form-model-item label="EID：" prop="xhEid" v-if="info.type===5&&type">
                        <a-input v-model.trim="form.xhEid"/>
                    </a-form-model-item>
                    <a-form-model-item label="appId：" prop="appId" v-if="info.type===5&&type">
                        <a-input v-model.trim="form.appId"/>
                    </a-form-model-item>
                    <a-form-model-item label="APPsecret：" prop="APPsecret" v-if="info.type===5&&type">
                        <a-input v-model.trim="form.APPsecret"/>
                    </a-form-model-item>
                    <a-form-model-item label="图标链接：" prop="imageUrl" v-if="info.type===5&&type">
                        <a-input v-model.trim="form.imageUrl"/>
                    </a-form-model-item>
                    <a-form-model-item label="审批意见：">
                        <a-textarea v-model.trim="msg" placeholder="请输入"/>
                    </a-form-model-item>
                </a-form-model>
            </div>
            <a-button type="primary" @click="onsubmit" class="submit" :loading="btnLoading">
                确认
            </a-button>
        </div>
    </div>
</template>

<script>
    import request from "@/utils/request";
    import type1 from "./components/type1.vue";
    import type2 from "./components/type2.vue";
    import type3 from "./components/type3.vue";
    import type4 from "./components/type4.vue";
    import type5 from "./components/type5.vue";
    import info from "./components/info.vue";

    const columns = [
        {
            title: '工作项名称',
            dataIndex: 'activityInstName',
            key: 'activityInstName'
        },
        {
            title: '工作项处理时间',
            dataIndex: 'createTime',
            key: 'createTime'
        },
        {
            title: '工作项处理人',
            dataIndex: 'userName',
            key: 'userName'
        },
        {
            title: '操作',
            dataIndex: 'extend3',
            key: 'extend3'
        },
        {
            title: '审批意见',
            dataIndex: 'content',
            key: 'content'
        }
    ]
    export default {
        data() {
            return {
                url: "/api/hip-flowable/api/v1/process/",
                info: {
                    items: [],
                    data: {}
                },
                labelCol: {span: 8},
                wrapperCol: {span: 14},
                form: {
                    node: undefined,
                    smsAuth: '1'
                },
                rules: {
                    node: [
                        {required: true, message: '请选择驳回节点', trigger: 'change'}
                    ],
                    account: [
                        {required: true, message: '请输入短信平台账号', trigger: 'change'}
                    ],
                    password: [
                        {required: true, message: '请输入短信平台密码', trigger: 'change'}
                    ],
                    eid: [
                        {required: true, message: '请输入EID', trigger: 'change'}
                    ],
                    officialAccounts: [
                        {required: true, message: '请输入公众号ID', trigger: 'change'}
                    ],
                    secretKey: [
                        {required: true, message: '请输入公众号秘钥', trigger: 'change'}
                    ],
                    xhEid: [
                        {required: true, message: '请输入EID', trigger: 'change'}
                    ],
                    appId: [
                        {required: true, message: '请输入appId', trigger: 'change'}
                    ],
                    APPsecret: [
                        {required: true, message: '请输入APPsecret', trigger: 'change'}
                    ],
                    smsAuth: [
                        {required: true, message: '请选择鉴权方式', trigger: 'change'}
                    ],
                    imageUrl:[
                        {required: true, message: '输入图片地址', trigger: 'change'}
                    ]
                },
                data: [],
                columns,
                loading: false,
                pagination: {//分页配置
                    total: 0,
                    current: 1,
                    defaultCurrent: 1,
                    showSizeChanger: true,
                    defaultPageSize: 10,
                    pageSize: 10,
                    pageSizeOptions: ['10', '20', '30', '40'],
                    showQuickJumper: true
                },
                pageNum: 1,
                pageSize: 10,
                type: 1,
                nodeList: [],
                btnLoading: false,
                msg: "",
                person:[]
            }
        },
        components: {
            type1,
            type2,
            type3,
            type4,
            type5,
            info
        },
        created() {
            this.getData();
            this.recordList();
        },
        methods: {
            getData() {
                // 服务申请订单审批流程	com.hisense.bpm.osms.serviceApply
                // 应用订阅订单审批流程	com.hisense.bpm.osms.appSubscription
                // 短信账号申请流程	com.hisense.bpm.message.channelAccount
                // 信鸿账号申请流程	com.hisense.bpm.message.msgchannelAccount
                // 三合一账号申请流程	com.hisense.bpm.message.portalchannelAccount
                // 订阅API审批流程	com.hisense.bpm.hip.subscribeApi
                // 发布API审批流程	com.hisense.bpm.hip.publishApi
                if (!this.$route.query.processInstID){
                    return
                }
                request(this.url + "getApiBasicInfo?processId=" + this.$route.query.processInstID + "&currentActInstID=" + this.$route.query.activityInstID, {
                    method: "GET"
                }).then(res => {
                    this.info = res;
                    if (this.info.processDefNameEn === "com.hisense.bpm.osms.serviceApply") {
                        this.info.type = 0;
                    } else if (this.info.processDefNameEn === "com.hisense.bpm.osms.appSubscription") {
                        this.info.type = 1;
                    } else if (this.info.processDefNameEn === "com.hisense.bpm.message.channelAccount") {
                        this.info.type = 3;
                        if (res.lastestProcessData){
                            this.$set(this.form,"account",JSON.parse(res.lastestProcessData).smsAccount);
                            this.$set(this.form,"password",window.atob(JSON.parse(res.lastestProcessData).smsPwd));
                        }
                    } else if (this.info.processDefNameEn === "com.hisense.bpm.message.msgchannelAccount") {
                        this.info.type = 4;
                        if (res.lastestProcessData){
                            this.$set(this.form,"eid",JSON.parse(res.lastestProcessData).pubEid);
                            this.$set(this.form,"officialAccounts",JSON.parse(res.lastestProcessData).pubId);
                            this.$set(this.form,"secretKey",window.atob(JSON.parse(res.lastestProcessData).pubSecret));
                        }
                    } else if (this.info.processDefNameEn === "com.hisense.bpm.message.portalchannelAccount") {
                        this.info.type = 5;
                        if (res.lastestProcessData){
                            this.$set(this.form,"xhEid",JSON.parse(res.lastestProcessData).todoEid);
                            this.$set(this.form,"appId",JSON.parse(res.lastestProcessData).appId);
                            this.$set(this.form,"APPsecret",window.atob(JSON.parse(res.lastestProcessData).appSecret));
                            this.$set(this.form,"imageUrl",JSON.parse(res.lastestProcessData).imageUrl);
                        }
                    } else if (this.info.processDefNameEn === "com.hisense.bpm.hip.subscribeApi") {
                        this.info.type = 6;
                    } else if (this.info.processDefNameEn === "com.hisense.bpm.hip.publishApi") {
                        this.info.type = 7;
                    }
                    if (res.isCurrentHandle===1) {
                        request(this.url + "getPreviousActivities", {
                            method: "POST",
                            body: {
                                currentActInstID: this.$route.query.activityInstID
                            }
                        }).then(response => {
                            this.nodeList = response.activityDefines || [];
                        })
                    }
                })
                request(this.url+"getCurrentHandlePerson/" + this.$route.query.processInstID,{
                    method: "GET"
                }).then(res=>{
                     this.person=res;
                })
            },
            recordList() {
                if (!this.$route.query.processInstID){
                    return
                }
                this.loading = true;
                request(this.url + "queryOptMsgsByProcessInstID?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize + "&processInstID=" + this.$route.query.processInstID, {
                    method: "GET"
                }).then(res => {
                    this.loading = false;
                    this.data = res.optMessages;
                    this.pagination.total = res.pageCond.count;
                })
            },
            handleTableChange(pagination, filters, sorter) {
                if (pagination.current === this.pageNum) {
                    this.pagination.current = 1;
                    this.pageSize = pagination.pageSize;
                    this.recordList();
                } else if (pagination.pageSize === this.pageSize) {
                    this.pagination.current = pagination.pageNum;
                    this.pageNum = pagination.pageNum;
                    this.recordList();
                }
            },
            onsubmit() {
                if (!this.type) {
                    this.$refs.form.validate(valid => {
                        if (!valid) {
                            return
                        }
                        this.btnLoading = true;
                        request(this.url + "backActivity", {
                            method: "POST",
                            body: {
                                currentActInstID: this.$route.query.activityInstID,
                                destActDefID: this.form.node,
                                processInstID: this.$route.query.processInstID,
                                activityInstName: this.$route.query.activityInstName || null,
                                msg: this.msg || null,
                                dataValue:JSON.stringify({
                                    smsAccount: this.form.account,
                                    smsPwd: window.btoa(this.form.password),
                                    pubEid: this.form.eid,
                                    pubId: this.form.officialAccounts,
                                    pubSecret: window.btoa(this.form.secretKey),
                                    imageUrl:this.form.imageUrl,
                                    todoEid: this.form.xhEid,
                                    appId: this.form.appId,
                                    appSecret: window.btoa(this.form.APPsecret),
                                    smsAuth:this.form.smsAuth
                                })
                            }
                        }).then(res => {
                            window.vm.$store.dispatch({
                                type: "queryPersonWorkItemsWithBizInfo/1603854216000/fetch",
                                payload: {}
                            })
                            window.vm.$store.dispatch({
                                type: "queryPersonFinishedWorkItemsWithBizInfo/1603864588000/fetch",
                                payload: {}
                            })
                            window.vm.$store.dispatch({
                                type: "queryPersonStartProcessInstWithBizInfo/1603789338000/fetch",
                                payload: {}
                            })
                            window.vm.$multiTab.close(window.location.pathname + window.location.search);
                            this.loading = false;
                            setTimeout(() => {
                                window.vm.$router.push({
                                    path: "/flowcenter/queryPersonWorkItemsWithBizInfo"
                                })
                            }, 50)
                        })
                    })
                } else {
                    this.$refs.form.validate(valid => {
                        if (!valid) {
                            return
                        }
                        this.btnLoading = true;
                        request(this.url + "finishWorkItem", {
                            method: "POST",
                            body: {
                                workItemID: this.$route.query.workItemID,
                                processInstID: this.$route.query.processInstID,
                                msg: this.msg || null,
                                dataValue:JSON.stringify({
                                    smsAccount: this.form.account,
                                    smsPwd: window.btoa(this.form.password),
                                    pubEid: this.form.eid,
                                    pubId: this.form.officialAccounts,
                                    pubSecret: window.btoa(this.form.secretKey),
                                    imageUrl:this.form.imageUrl,
                                    todoEid: this.form.xhEid,
                                    appId: this.form.appId,
                                    appSecret: window.btoa(this.form.APPsecret),
                                    smsAuth:this.form.smsAuth
                                })
                            }
                        }).then(res => {
                            window.vm.$store.dispatch({
                                type: "queryPersonWorkItemsWithBizInfo/1603854216000/fetch",
                                payload: {}
                            })
                            window.vm.$store.dispatch({
                                type: "queryPersonFinishedWorkItemsWithBizInfo/1603864588000/fetch",
                                payload: {}
                            })
                            window.vm.$store.dispatch({
                                type: "queryPersonStartProcessInstWithBizInfo/1603789338000/fetch",
                                payload: {}
                            })
                            window.vm.$multiTab.close(window.location.pathname + window.location.search);
                            this.loading = false;
                            setTimeout(() => {
                                window.vm.$router.push({
                                    path: "/flowcenter/queryPersonWorkItemsWithBizInfo"
                                })
                            }, 50)
                        })
                    })
                }
            }
        }
    };
</script>
<style lang="less" scoped>
    ul, li {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .flowCenterDetails {
        width: 1000px;
        margin: 0 auto;
        padding: 20px 0;

        .ellipsis {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap
        }

        .title {
            font-size: 16px;
            color: #333333;
            margin-top: 30px;
            position: relative;
            padding-left: 12px;
        }

        .title::before {
            content: "";
            position: absolute;
            top: 5%;
            left: 0;
            width: 4px;
            height: 90%;
            background: #00aaa6;
        }

        .line {
            margin-top: 10px;
            border: none;
            border-top: 1px solid #cccccc;
            transform: scaleY(0.5);
        }

        .apiInfo {
            li {
                float: left;
                width: 25%;
                margin-bottom: 10px;
            }
        }

        .operation {
            margin: 20px auto 0;
            width: 500px;

            .ant-radio-group {
                margin-left: 187px;
            }

            .nodeList {
                padding-top: 15px;
                text-align: center;
            }

            .submit {
                display: block;
                margin: 15px auto 0;
            }
        }
    }
</style>
