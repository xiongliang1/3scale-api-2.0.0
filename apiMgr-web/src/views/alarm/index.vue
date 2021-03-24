<template>
    <div class="alarm clearfix">
        <header class="clearfix">
            <div>
                <a-button type="primary" icon="plus" @click="text='新建告警策略';visible=true">
                    创建告警策略
                </a-button>
            </div>
            <div class="search">
                <a-select style="width: 120px" v-model="select">
                    <a-select-option value="1">
                        策略名称
                    </a-select-option>
                    <a-select-option value="2">
                        创建时间
                    </a-select-option>
                </a-select><a-input v-model.trim="name" placeholder="请输入" v-show="select==='1'"/><a-range-picker v-show="select==='2'"
                                                                                                                 :placeholder="['开始时间','结束时间']"
                                                                                                                 :show-time="{
              defaultValue: [moment('00:00:00', 'HH:mm:ss'), moment('11:59:59', 'HH:mm:ss')],
            }"
                                                                                                                 format="YYYY-MM-DD HH:mm:ss"
                                                                                                                 @change="switchTime"
            />
                &nbsp;&nbsp;
                <a-button type="primary" @click="getData">
                    查询
                </a-button>
                &nbsp;&nbsp;
                <a-button @click="onreset">重置</a-button>
            </div>
        </header>
        <div class="box">
            <a-table
                    :columns="tableHeader"
                    :data-source="tableList"
                    :rowKey="row=>row.id"
                    :loading="loading"
                    size="small"
                    @change="tableHandleChange"
                    :pagination="pagination"
            >
                <div slot="status" slot-scope="text">
                    {{text|status}}
                </div>
                <div slot="msgSendTypes" slot-scope="text">
                    {{text|type}}
                </div>
                <div slot="enable" slot-scope="text">{{text?"开启":"关闭"}}</div>
                <div slot="operation" slot-scope="text,record">
                    <a-button type="link" style="padding-left: 0;" @click="editor(record.id)">
                        编辑
                    </a-button>
                    <a-button type="link" style="padding-left: 0;" @click="id=record.id;linkApiLeft();linkApiRight()">
                        关联API
                    </a-button>
                    <a-button type="link" style="padding-left: 0;" @click="open(record)">
                        {{record.enable?"关闭":"开启"}}
                    </a-button>
                    <a-button type="link" style="padding-left: 0;" @click="del(record)" v-show="!record.bindApis">
                        删除
                    </a-button>
                    <a-tooltip>
                        <template slot="title">
                            删除策略前请解绑已关联API
                        </template>
                        <a-button type="link" style="padding-left: 0;" v-show="record.bindApis" disabled>
                            删除
                        </a-button>
                    </a-tooltip>
                </div>
            </a-table>
        </div>
        <a-modal
                :title="text"
                :visible="visible"
                :confirm-loading="confirmLoading"
                @ok="alarmHandleOk"
                @cancel="handleCancel(0)"
                width="800px"
                okText="确认"
                cancelText="取消"
                class="alarm_roules"
        >
            <a-form-model :model="form" ref="ruleForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }" :rules="rules">
                <a-form-model-item label="告警名称：" prop="name">
                    <a-input v-model="form.name" placeholder="请输入"/>
                </a-form-model-item>
                <a-form-model-item label="告警发送间隔：" prop="time">
                    <a-input v-model="form.time" addon-after="分钟" placeholder="请输入"/>
                </a-form-model-item>
                <a-form-model-item label="告警接收人：">
                    <div class="searchSystem">
                        <div class="sysyemCon">
                            <div class="checkoutSystems" v-show="checkoutSystem.length">
                                <a-tag closable @close="delSystem(index)" color="#00AAA6" v-for="(item,index) in checkoutSystem" :key="item.uid">
                                    {{item.cn}}
                                </a-tag>
                            </div>
                            <a-input-search placeholder="请输入" v-model.trim="systemName" :loading="searchLoading" enter-button="搜索" @search="onSearch" />
                        </div>
                        <div class="systemList">
                            <ul v-show="systemList.length">
                                <li v-for="(item,index) in systemList" :key="item.id" v-show="systemList.length" @click="check(item,index)">{{item.cn}}</li>
                            </ul>
                        </div>
                    </div>
                    <a-tooltip>
                        <template slot="title">
                            不指定告警接收人时，默认为项目负责人
                        </template>
                        <a-icon style="margin-left: 10px" type="question-circle"/>
                    </a-tooltip>
                </a-form-model-item>
                <a-form-model-item label="告警方式：" prop="msgSendTypes">
                    <a-checkbox-group v-model="form.msgSendTypes">
                        <a-checkbox :value="0">
                            短信
                        </a-checkbox>
                        <a-checkbox :value="1">
                            邮箱
                        </a-checkbox>
                        <!--<a-checkbox :value="2">
                          信鸿公众号
                        </a-checkbox>
                        <a-checkbox :value="3">
                          微信公众号
                        </a-checkbox>-->
                    </a-checkbox-group>
                </a-form-model-item>
                <a-form-model-item label="告警规则：" prop="routers">
                    <ul class="tabel_header">
                        <li style="width: 28%;padding: 0 10px">规则名称</li>
                        <li style="width: 28%;padding: 0 10px">告警级别</li>
                        <li style="width: 28%;padding: 0 4px">规则</li>
                        <li style="width: 16%">操作</li>
                    </ul>
                    <ul class="tabel_body">
                        <li>
                            <div style="width: 28%;padding: 0 10px">
                                <a-select placeholder="请选择" v-model="form.routers[0].triggerType" disabled>
                                    <a-select-option :value="1">
                                        响应超时告警
                                    </a-select-option>
                                    <a-select-option :value="0">
                                        状态码告警
                                    </a-select-option>
                                </a-select>
                            </div>
                            <div style="width: 28%;padding: 0 10px">
                                <a-select placeholder="请选择" v-model="form.routers[0].alertLevel">
                                    <a-select-option value="1">
                                        主要告警
                                    </a-select-option>
                                    <a-select-option value="0">
                                        次要告警
                                    </a-select-option>
                                </a-select>
                            </div>
                            <div style="width: 28%;padding: 0 10px">
                                <a-input v-model.trim="form.routers[0].responseTime" addon-after="s" placeholder="超时时间"/>
                            </div>
                            <div style="width: 16%">
                                <a-switch checked-children="开" un-checked-children="关" v-model="form.routers[0].bool"
                                          style="margin-left: 10px"/>
                            </div>
                        </li>
                        <li>
                            <div style="width: 28%;padding: 0 10px">
                                <a-select placeholder="请选择" v-model="form.routers[1].triggerType" disabled>
                                    <a-select-option :value="1">
                                        响应超时告警
                                    </a-select-option>
                                    <a-select-option :value="0">
                                        调用异常告警
                                    </a-select-option>
                                </a-select>
                            </div>
                            <div style="width: 28%;padding: 0 10px">
                                <a-select placeholder="请选择" v-model="form.routers[1].alertLevel">
                                    <a-select-option value="1">
                                        主要告警
                                    </a-select-option>
                                    <a-select-option value="0">
                                        次要告警
                                    </a-select-option>
                                </a-select>
                            </div>
                            <div style="width: 28%;padding: 0 10px">
                                <a-input disabled default-value="调用异常"/>
                            </div>
                            <div style="width: 16%">
                                <a-switch checked-children="开" un-checked-children="关" v-model="form.routers[1].bool"
                                          style="margin-left: 10px"/>
                            </div>
                        </li>
                    </ul>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal
                title="关联API"
                :visible="associated"
                @ok="handleOk"
                @cancel="handleCancel"
                okText="确认"
                cancelText="取消"
                width="1010px"
                wrapClassName="transfer"
        >
            <div class="transfer_right">
                <p class="title">未关联列表</p>
                <a-input-search placeholder="请输入搜索内容" v-if="associated" @search="onSearchRight"/>
                <a-table
                        :columns="columns"
                        :data-source="dataRight"
                        :pagination="paginationRight"
                        :loading="loadingRight"
                        size="small"
                        @change="handleTableChangeRight"
                        :rowKey="row=>row.id"
                        :row-selection="{ selectedRowKeys: selectedRowKeysRight, onChange: onSelectChangeRight }"
                        v-if="associated"
                >
                    <div slot="partition" slot-scope="text">
                        {{text|partition}}
                    </div>
                </a-table>
            </div>

            <div class="transfer_center">

                <a-button type="primary" icon="left" size="small" @click="transLateRight" :disabled="!selectedRowKeysLeft.length"></a-button>
                <br>
                <a-button type="primary" icon="right" size="small" @click="transLateList" :disabled="!selectedRowKeysRight.length"></a-button>
            </div>
            <div class="transfer_left">
                <p class="title">关联列表</p>
                <a-input-search placeholder="请输入搜索内容" v-if="associated" @search="onSearchLeft"/>
                <a-table
                        :columns="columns"
                        :data-source="dataLeft"
                        :pagination="paginationLeft"
                        :loading="loadingLeft"
                        size="small"
                        @change="handleTableChangeLeft"
                        :rowKey="row=>row.id"
                        :row-selection="{ selectedRowKeys: selectedRowKeysLeft, onChange: onSelectChangeLeft }"
                        v-if="associated"
                >
                    <div slot="partition" slot-scope="text">
                        {{text|partition}}
                    </div>
                </a-table>
            </div>

        </a-modal>
    </div>
</template>

<script>
    import moment from "moment";
    import request from "../../utils/request";
    const columns = [
        {
            title: 'API名称',
            dataIndex: 'name',
            key: 'name'
        },
        {
            title: '环境',
            dataIndex: 'partition',
            key: 'partition',
            filters: [
                {text: "内网", value: '0'},
                {text: "外网", value: '1'}
            ],
            filterMultiple: false,
            scopedSlots: {customRender: 'partition'},
        },
        {
            title: '所属分组',
            dataIndex: 'groupName',
            key: 'groupName',
            ellipsis: true,
            filters: []
        }
    ];
    const tableHeader = [
        {
            title: '策略名称',
            dataIndex: 'name',
            key: 'name'
        },
        {
            title: "告警方式",
            dataIndex: "msgSendTypes",
            key: "msgSendTypes",
            scopedSlots: {customRender: 'msgSendTypes'},
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            sorter: true,
            width: 180
        },
        {
            title: '状态',
            dataIndex: 'enable',
            key: 'enable',
            filters: [
                {text: '开启', value: '1'},
                {text: '关闭', value: '0'},
            ],
            filterMultiple: false,
            scopedSlots: {customRender: 'enable'},
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'},
        }
    ]
    export default {
        name: "index",
        data() {
            return {
                moment,
                tableHeader,
                tableList: [],
                flag: false,
                columns,
                visible: false,
                confirmLoading: false,
                associated: false,//关联API
                id: null,
                dataLeft: [],
                dataRight: [],
                pagination: {//分页信息
                    total: 0,
                    current: 1,
                    defaultCurrent: 1,
                    showSizeChanger: true,
                    defaultPageSize: 10,
                    pageSize: 10,
                    pageSizeOptions: ['10', '20', '30', '40'],
                    showQuickJumper: true,
                    showTotal:((total) => {
                        return `共 ${total} 条`;
                    }),
                },
                paginationLeft: {
                    current: 1,
                    total: 0,
                    hideOnSinglePage: true
                },
                paginationRight: {
                    current: 1,
                    total: 0,
                    hideOnSinglePage: true
                },
                loading: false,
                loadingLeft: false,
                loadingRight: false,
                selectedRowKeysLeft: [],
                selectedRowKeysRight: [],
                person: undefined,
                text: "新建告警策略",
                select: "1",
                timer: ["", ""],
                name: null,
                bindApiIds: [],//默认已关联列表
                unBindApiIds: [],//默认未关联列表
                form: {
                    name: null,//告警名称
                    person: undefined,//告警接收人
                    time: null,//告警发送间隔
                    msgSendTypes: [],//告警方式
                    routers: [
                        {
                            triggerType: 1,
                            alertLevel: undefined,
                            responseTime: null,
                            bool: true
                        },
                        {
                            triggerType: 0,
                            alertLevel: undefined,
                            bool: true
                        }
                    ]
                },
                rules: {
                    name: [
                        {required: true, message: '请填写告警名称', trigger: 'blur'},
                        {pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9-\u4e00-\u9fa5]+$/, message: '请正确填入告警名称', trigger: ['change', 'blur']}
                    ],
                    time: [
                        {required: true, message: '请填写告警发送间隔', trigger: 'blur'},
                        {pattern: /^\d+$/, message: '只支持整数', trigger: ['blur', 'change']}
                    ],
                    msgSendTypes: [
                        {required: true, message: '请选择告警方式', trigger: 'blur'}
                    ],
                    routers: [
                        {required: true, message: '请填写', trigger: 'blur'},
                        {
                            validator: function (rule, value, callback) {
                                if (!value[0].bool && !value[1].bool) {
                                    callback(new Error('请选择一项告警规则'));
                                } else if (value[0].bool && !value[1].bool) {
                                    if (!value[0].alertLevel) {
                                        callback(new Error('请选择响应超时的告警级别'));
                                    }
                                    let reg = /^\d+$/;
                                    if (!value[0].responseTime) {
                                        callback(new Error('请正确驶入响应超时的超时时间'))
                                    }
                                } else if (!value[0].bool && value[1].bool) {
                                    if (!value[1].alertLevel) {
                                        callback(new Error('请选择调用异常报告的告警级别'));
                                    }
                                } else if (value[0].bool && value[1].bool) {
                                    if (!value[0].alertLevel) {
                                        callback(new Error('请选择响应超时的告警级别'));
                                    }
                                    let reg = /^\d+$/;
                                    if (!value[0].responseTime) {
                                        callback(new Error('请正确驶入响应超时的超时时间'))
                                    }
                                    if (!value[1].alertLevel) {
                                        callback(new Error('请选择调用异常报告的告警级别'));
                                    }
                                    if (value[0].alertLevel === value[1].alertLevel) {
                                        callback(new Error('两个告警规则不允许同样的告警等级'));
                                    }
                                }
                                callback();
                            }, trigger: 'change'
                        }
                    ]
                },
                fetching: false,
                selectList: [],
                searchData: {
                    status: null,
                    time: null,
                    pageSize: 10
                },
                searchLeftName:null,
                searchRightName:null,
                groupLeft:null,
                groupRight:null,
                partitionLeft:null,
                partitionRight:null,
                searchLoading:false,
                systemList:[],
                checkoutSystem:[],
                systemName: null,
                url:null
            }
        },
        components: {
            VNodes: {
                functional: true,
                render: (h, ctx) => {
                    return ctx.props.vnodes
                }
            }
        },
        filters: {
            status(num) {
                if (!num) {
                    return "删除"
                } else {
                    return "正常"
                }
            },
            type(arr) {
                let array = [];
                arr.forEach(item => {
                    if (!item) {
                        array.push("短信")
                    } else if (item === 1) {
                        array.push("邮箱")
                    } else if (item === 2) {
                        array.push("信鸿公众号")
                    } else if (item === 3) {
                        array.push("微信公众号")
                    }
                })
                return array.join(",")
            },
            partition(num) {
                if (!num) {
                    return "内网"
                } else {
                    return "外网"
                }
            }
        },
        methods: {
            handleOk() {
                let data=[];
                data=data.concat(this.dataLeft,this.dataRight);
                let arr=[];
                for (const value of this.bindApiIds) {
                    for (let i=0;i<data.length;i++){
                        if (value===data[i].id&&!data[i].needLoging){
                            arr.push(data[i].name)
                            break
                        }
                    }
                }
                if (arr.length){
                    this.$notification.warn({
                        message: '通知',
                        description: `请打开【${arr.join("，")}】日志记录开关，否则无法接收到告警信息。`,
                    });
                }
                request(this.url+"/alertPolicy/bindToPublishApi/" + this.id,{
                    method:"POST",
                    body:{
                      bindApiIds: this.bindApiIds,
                      unBindApiIds: this.unBindApiIds
                    }
                }).then(res=>{
                   if ((typeof res==="boolean")&&res){
                       this.id = null;
                       this.selectedRowKeysLeft = [];
                       this.selectedRowKeysRight = [];
                       this.dataLeft=[];
                       this.dataRight=[];
                       this.searchLeftName=null;
                       this.searchRightName=null;
                       this.bindApiIds=[];
                       this.unBindApiIds=[];
                       this.associated = false;
                       this.getData();
                   }
                })
            },
            handleCancel(num) {
                this.visible = false;
                this.associated = false;
                this.id = null;
                this.selectedRowKeysLeft = [];
                this.selectedRowKeysRight = [];
                this.dataLeft=[];
                this.dataRight=[];
                this.searchLeftName=null;
                this.searchRightName=null;
                this.bindApiIds=[];
                this.unBindApiIds=[];
                if (!num){
                    this.$refs.ruleForm.resetFields();
                    this.form.person=undefined;
                    this.checkoutSystem=[];
                    this.form.name=null;
                    this.form.time=null;
                    this.form.msgSendTypes=[];
                    this.form.routers= [
                        {
                            triggerType: 1,
                            alertLevel: undefined,
                            responseTime: null,
                            bool: true
                        },
                        {
                            triggerType: 0,
                            alertLevel: undefined,
                            bool: true
                        }
                    ]
                }
            },
            onreset() {//重置
                this.name = null;
                this.timer = ["", ""];
                this.select = "1";
                this.getData();
            },
            switchTime(date, dateString) {//时间插件
                this.timer = dateString;
            },
            handleTableChangeLeft(pagination, filters, sorter) {
                this.linkApiLeft((filters.groupName?filters.groupName.map(Number):null),(filters.partition?filters.partition.map(Number):null))
            },
            handleTableChangeRight(pagination, filters, sorter) {
                this.linkApiRight((filters.groupName?filters.groupName.map(Number):null),(filters.partition?filters.partition.map(Number):null))
            },
            onSearchLeft(value) {
                this.searchLeftName=value;
                this.linkApiLeft()
            },
            onSearchRight(value){
                this.searchRightName=value;
                this.linkApiRight();
            },
            onSelectChangeLeft(selectedRowKeys) {
                this.selectedRowKeysLeft = selectedRowKeys;
            },
            onSelectChangeRight(selectedRowKeys) {
                this.selectedRowKeysRight = selectedRowKeys;
            },
            alarmHandleOk() {
                this.$refs.ruleForm.validate(valid => {
                    if (!valid) {
                        return
                    }
                    let arr = [];
                    if (this.form.routers[0].bool) {
                        arr.push({
                            triggerType: this.form.routers[0].triggerType,
                            alertLevel: this.form.routers[0].alertLevel - 0,
                            responseTime: this.form.routers[0].responseTime - 0
                        })
                    }
                    if (this.form.routers[1].bool) {
                        arr.push({
                            triggerType: this.form.routers[1].triggerType,
                            alertLevel: this.form.routers[1].alertLevel - 0
                        })
                    }
                    let person=[];
                    this.checkoutSystem.forEach(item=>{
                        person.push(item.uid);
                    })
                    console.log(person)
                    if (!this.id) {
                        request(this.url+"/alertPolicy/addPolicy",{
                            method:"POST",
                            body:{
                                name: this.form.name,
                                msgReceivers: person,
                                msgSendInterval: this.form.time - 0,
                                msgSendTypes: this.form.msgSendTypes,
                                triggerMethods: arr
                            }
                        }).then(res=>{
                            if((typeof res==="boolean")&&res){
                                this.visible = false;
                                this.$refs.ruleForm.resetFields();
                                this.checkoutSystem=[];
                                this.getData();
                            }
                        })
                    } else {
                        request(this.url+"/alertPolicy/updateAlertPolicy/"+this.id,{
                            method:"POST",
                            body:{
                                name: this.form.name,
                                msgReceivers: person,
                                msgSendInterval: this.form.time - 0,
                                msgSendTypes: this.form.msgSendTypes,
                                triggerMethods: arr
                            }
                        }).then(res=>{
                            if ((typeof res==="boolean")&&res){
                                this.visible = false;
                                this.$refs.ruleForm.resetFields();
                                this.checkoutSystem=[];
                                this.getData();
                            }
                        })
                    }
                })
            },
            getData() {
                let time = {
                    start: this.timer[0],
                    end: this.timer[1]
                }
                this.loading = true;
                request(this.url+"/alertPolicy/findAlertPoliciesByPage",{
                    method:"POST",
                    body:{
                        page: this.pagination.current,
                        size: this.searchData.pageSize,
                        name: this.name,
                        statusList: this.searchData.status ? [this.searchData.status-0] : null,
                        sort: this.searchData.time?(this.searchData.time==="ascend"?["a","createTime"]:["d","createTime"]):null,
                        timeQuery: this.timer[0] ? time : null
                    }
                }).then(res=>{
                    this.loading = false;
                    this.tableList = res.content;
                    this.pagination.total = res.totalElements;
                })
            },
            open(item) {
                let that = this;
                this.$warning({
                    title: item.enable ? "关闭策略" : "开启策略",
                    okText: '确认',
                    maskClosable: true,
                    onOk() {
                        request(`${that.url}/alertPolicy/enableAlertPolicy/${item.id}?enable=${!item.enable}`,{
                            method:"PUT"
                        }).then(res=>{
                            that.getData();
                        })
                    }
                });
            },
            del(item) {
                let that = this;
                this.$warning({
                    title: "请确认删除当前策略",
                    okText: '确认',
                    maskClosable: true,
                    onOk() {
                        request(that.url+"/alertPolicy/deleteAlertPolicies",{
                            method:"POST",
                            body:{
                                deleteIds: [item.id]
                            }
                        }).then(res => {
                            that.getData();
                        })
                    }
                })
            },
            editor(id) {
                this.text = "编辑告警策略";
                this.id = id;
                this.form.routers[0].bool = false;
                this.form.routers[1].bool = false;
                request(this.url+"/alertPolicy/" + id,{
                    method:"GET"
                }).then(res => {
                    this.form.name = res.name;
                    this.checkoutSystem = (function (list) {
                        if (!list){
                            return []
                        }
                        let arr=[];
                        list.forEach(item=>{
                            arr.push({cn:item,uid:item})
                        })
                        return arr
                    })(res.msgReceivers);
                    this.form.person = res.msgReceivers||undefined;
                    this.form.time = res.msgSendInterval;
                    this.form.msgSendTypes = res.msgSendTypes;
                    res.triggerMethods.forEach(item => {
                        if (item.triggerType) {
                            this.form.routers[0].bool = true;
                            this.form.routers[0].alertLevel = item.alertLevel + "";
                            this.form.routers[0].responseTime = item.responseTime + "";
                        } else {
                            this.form.routers[1].alertLevel = item.alertLevel + "";
                            this.form.routers[1].bool = true;
                        }
                    })
                    this.visible = true;
                })
            },
            linkApiLeft(group,partition) {
                this.associated = true;
                this.loadingLeft = true;
                request(this.url+"/alertPolicy/findBindUnBindApiList/" + this.id,{
                    method:"POST",
                    body:{
                        bind: true,
                        name:this.searchLeftName||null,
                        groupIds:group||null,
                        partitions:partition||null
                    }
                }).then(res => {
                    this.loadingLeft=false;
                    this.dataLeft = res;
                    this.paginationLeft.total = res.length;
                    this.paginationLeft.pageSize = res.length;
                    res.forEach(item => {
                        this.bindApiIds.push(item.id);
                    })
                })
            },
            linkApiRight(group,partition) {
                this.loadingRight=true;
                request(this.url+"/alertPolicy/findBindUnBindApiList/" + this.id,{
                    method:"POST",
                    body:{
                        bind: false,
                        name:this.searchRightName||null,
                        groupIds:group||null,
                        partitions:partition||null
                    }
                }).then(res=>{
                    this.loadingRight=false;
                    this.dataRight = res;
                    this.paginationRight.total = res.length;
                    this.paginationRight.pageSize = res.length;
                    res.forEach(item => {
                        this.unBindApiIds.push(item.id);
                    })
                })
            },
            getGroupList() {//获得全部的分组列表数据
                request(this.url+"/publishApiGroup/findPublishApiGroup",{
                    method:"GET"
                }).then(res=>{
                    res.forEach(item => {
                        this.columns[2].filters.push({
                            text: item.name,
                            value: item.id + ""
                        })
                    })
                })
            },
            transLateList() {
                this.paginationLeft.total += this.selectedRowKeysRight.length;
                this.paginationLeft.pageSize += this.selectedRowKeysRight.length;
                this.selectedRowKeysRight.forEach(item => {
                    this.bindApiIds.push(item);
                    this.unBindApiIds.splice(this.bindApiIds.indexOf(item),1)
                    for (let i = 0; i < this.dataRight.length; i++) {
                        if (this.dataRight[i].id === item) {
                            this.dataLeft.push(this.dataRight[i])
                            this.dataRight.splice(i, 1);
                            break
                        }
                    }
                })
                this.selectedRowKeysRight = [];
            },
            transLateRight() {
                this.paginationRight.total += this.selectedRowKeysLeft.length;
                this.paginationRight.pageSize += this.selectedRowKeysLeft.length;
                this.selectedRowKeysLeft.forEach(item => {
                    this.unBindApiIds.push(item);
                    this.bindApiIds.splice(this.bindApiIds.indexOf(item),1)
                    for (let i = 0; i < this.dataLeft.length; i++) {
                        if (this.dataLeft[i].id === item) {
                            this.dataRight.push(this.dataLeft[i])
                            this.dataLeft.splice(i, 1);
                            break
                        }
                    }
                })
                this.selectedRowKeysLeft = [];
            },
            fetchUser(value) {
                if (!value) {
                    return
                }
                this.fetching = true;
                this.selectList = [];
                /*this.axios.get("http://" + window.location.host + "/api/groupInfos/getUserFromLdap/" + value).then(res => {
                    this.fetching = false;
                    this.selectList = res;
                })*/
                request( "/message/api/groupInfos/getUserFromLdap/" + value,{
                    method:"GET"
                }).then(res=>{
                    this.fetching = false;
                    this.selectList = res;
                })
            },
            handleChange(value) {
                this.form.person = value
            },
            tableHandleChange(pagination, filters, sorter) {
                let json = {
                    status: filters.enable ?filters.enable[0]:null,
                    time: sorter.order || null,
                    pageSize: pagination.pageSize
                }
                if (
                    this.searchData.pageSize === json.pageSize&&
                    this.searchData.time === json.time&&
                    this.searchData.status === json.status
                ){
                    this.pagination.current = pagination.current;
                    this.getData();
                }else {
                    this.pagination.current = 1;
                    this.pagination.pageSize = json.pageSize;
                    this.searchData={
                        pageSize: json.pageSize,
                        time: json.time,
                        status: json.status
                    };
                    this.getData();
                }
            },
            onSearch(){
                if (!this.systemName){
                    this.$message.error("请输入要搜索的内容")
                    return
                }
                this.searchLoading=true;
                this.axios.get("/message/api/groupInfos/getUserFromLdap/" + this.systemName).then(res=>{
                    this.searchLoading=false;
                    this.systemList=res;
                    this.systemName=""
                })
            },
            check(item,index){
                for (let i=0;i<this.checkoutSystem.length;i++){
                    if (this.checkoutSystem[i].cn===item.cn){
                        return
                    }
                }
                this.checkoutSystem.push(item);
                this.systemList[index].active=true;
                this.systemList=[];
            },
            delSystem(index){
                this.checkoutSystem.splice(index,1)
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getData();
            this.getGroupList();
        },
        activated() {
          this.getData();
          this.getGroupList()
        },
        beforeDestroy() {
            this.columns[2].filters = [];
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
                    if (window.vm.$route.name==='alarm'){
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.getData();
                        this.getGroupList();
                    }
                }
            }
        },
    }
</script>

<style scoped lang="less">
    .alarm {
        header {
            padding: 16px 16px 0;
            > div:nth-child(1) {
                float: left;
            }

            .search {
                float: right;

                .ant-input {
                    width: 200px;
                }
            }
        }

        .box {
            background-color: #ffffff;
            padding: 16px;
            height: calc(~"100vh - 163px");
            overflow-y: auto;
        }
    }
</style>
<style>
    ul,li{
        padding: 0;
        list-style: none;
    }
    .alarm_roules .tabel_header {
        width: 100%;
        height: 46px;
        margin-top: 8px;
        background: rgba(0, 0, 0, 0.02);
        border: 1px solid #E8E8E8;
    }

    .alarm_roules .tabel_header li {
        float: left;
        line-height: 46px;
        text-indent: 6px;
        color: rgba(0, 0, 0, 0.85);
    }

    .alarm_roules .tabel_body li {
        height: 46px;
        border: 1px solid #E8E8E8;
        margin-top: -1px;
    }

    .alarm_roules .searchSystem {
        width: 95%;
        float: left;
    }
    .alarm_roules .searchSystem .systemList{
        position: relative;
    }
    .alarm_roules .searchSystem .systemList>ul{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        max-height: 300px;
        overflow-y: auto;
        background-color: #ffffff;
        z-index: 10;
        text-indent: 10px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        cursor: pointer;
        line-height: 30px;
        font-size: 14px;
    }
    /*.alarm_roules .searchSystem .sysyemCon input{*/
    /*    float: left;*/
    /*    width: 80%;*/
    /*}*/
    /*.alarm_roules .searchSystem .sysyemCon button{*/
    /*    float: left;*/
    /*    margin-left: 4px;*/
    /*}*/
    .alarm_roules .searchSystem .systemList li:hover{
        color: #00AAA6;
    }
    .alarm_roules .tabel_body li > div {
        float: left;
        height: 100%;
    }

    .transfer .transfer_left {
        float: right;
        width: 47%;
        border: 1px solid #D9D9D9;
        border-radius: 4px;
    }

    .transfer .transfer_right {
        float: left;
        width: 47%;
        border: 1px solid #D9D9D9;
        border-radius: 4px;
    }

    .transfer .ant-table-wrapper {
        height: 214px;
        overflow-y: auto;
    }

    .transfer .transfer_left .title, .transfer .transfer_right .title {
        font-size: 14px;
        line-height: 40px;
        color: rgba(0, 0, 0, 0.85);
        padding-left: 17px;
    }

    .transfer .transfer_left .ant-input-search, .transfer .transfer_right .ant-input-search {
        width: 95%;
        display: block;
        margin: 6px auto 12px;
    }

    .transfer .ant-table-thead > tr > th, .transfer .ant-table-tbody > tr > td {
        padding: 12px 16px;
    }

    .transfer .transfer_center {
        float: left;
        width: 6%;
        text-align: center;
        padding-top: 160px;
    }

    .transfer .transfer_center button:nth-child(3) {
        margin-top: 4px;
    }

    .transfer .ant-modal-body:after {
        content: "";
        display: block;
        clear: both;
    }
    .checkoutSystems>span>i>svg{
        color: #ffffff;
    }
</style>


