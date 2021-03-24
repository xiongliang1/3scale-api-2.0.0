<template>
    <div class="apiDetails">
        <header>
            <a-button icon="left" @click="goBack">返回</a-button>
            <span>当前页面：API详情</span>
        </header>
        <div class="port clearfix">
            <div :style="{background:(!image?'linear-gradient(180deg, rgb(243, 246, 249) 0%, rgb(225, 232, 237) 100%)':'')}">
                <a-icon v-show="!image" type="api"/>
                <img :src="image" v-if="image" style="width: 100%;height: 100%;">
            </div>

            <div>
                <p>{{info.name}}
                    <span>{{info.publishApiGroupDto.categoryOneName}}-{{info.publishApiGroupDto.categoryTwoName}}</span></p>
                <a-tooltip placement="top">
                    <template slot="title">
                        <span>{{info.description||"暂无"}}</span>
                    </template>
                    <p>{{info.description||"暂无"}}</p>
                </a-tooltip>
            </div>
            <div>
                <a-button type="primary" icon="form" v-if="info.status!==2" v-show="$route.query.type==0"
                          @click="alter">
                    修改
                </a-button>

                <a-tooltip>
                    <template slot="title">
                        当前API正在订阅审批中，无法修改
                    </template>
                    <a-button type="primary" icon="form" disabled  v-if="info.status===2">
                        修改
                    </a-button>
                </a-tooltip>
            </div>
        </div>
        <!--    tab切换-->
        <div class="portDetails">
            <ul class="tab">
                <li :class="{'active':!index}" @click="index=0">接口详情</li>
                <li :class="{'active':index===1}" @click="index=1" v-show="!($route.query.type-0)">接口调试</li>
                <li :class="{'active':index===2}" @click="index=2;getEcharts()">接口监控</li>
                <li :class="{'active':index===3}" @click="index=3" v-show="!($route.query.type-0)">
                    已订阅({{paginationSubscription.total}})
                </li>
                <li :class="{'active':index===4}" @click="index=4" v-show="!($route.query.type-0)">发布历史</li>
                <!--        <li :class="{'active':index===5}" @click="index=5">评论互动({{commentTotal}})</li>-->
            </ul>
            <!--    接口详情-->
            <div class="details" v-if="!index">
                <div class="info">
                    <div class="clearfix">
                        <p><span>创建时间：</span><span>{{info.createTime}}</span></p>
                        <p><span>所属分组：</span><span>{{info.publishApiGroupDto.name}}</span></p>
                        <p><span>发布环境：</span><span>{{info.partition?"外网":"内网"}}</span></p>
                        <p><span>是否需要审批：</span><span>{{info.needSubscribe?"是":"否"}}</span></p>
                    </div>
                    <div class="clearfix">
                        <p><span>是否鉴权：</span><span>{{info.needAuth?"是":"否"}}</span></p>
                        <p><span>是否记录日志：</span><span>{{info.needLogging?"是":"否"}}</span></p>
                        <p><span>Host：</span><span>{{info.hostHeader}}</span></p>
                        <p><span>URL前缀：</span><span>{{info.url}}</span></p>
                    </div>
                    <div class="clearfix">
                        <p><span>协议：</span><span>{{info.accessProtocol}}</span></p>
                        <p style="width: 50%;"><span>关联后端服务：</span><span>{{info.host}}</span></p>
                        <p><span>超过时间(S)：</span><span>{{info.timeout}}</span></p>
                    </div>
                </div>
                <div class="listInfo" v-if="show">
                    <p class="title">路由规则</p>
                    <a-table
                            :columns="columnsRoute"
                            :data-source="info.apiMappingRuleDtos"
                            class="apiDetails_tabel"
                            bordered
                            size="small"
                            :pagination="{hideOnSinglePage:true}"
                            :rowKey="row=>row.index"
                    >
                    </a-table>
                </div>
                <div class="listInfo">
                    <p class="title"><span style="cursor: pointer" @click="flag=!flag"><a-icon :type="flag?'down':'right'" />高级配置</span></p>
                    <div v-show="flag">
                        <div style="margin-left: 53px">
                            <p><span>接口类型：</span>{{info.accessProType}}</p>
                        </div>
                        <div style="margin-left: 35px">
                            <p><span style="display:inline-block;">SecretToken：</span>{{info.secretToken}}</p>
                        </div>
                        <div v-show="ipWhiteList.length" style="overflow: hidden;margin-left: 67px;margin-top: 20px">
                            <span style="float: left">IP白名单：</span>
                            <a-textarea v-model="ipWhiteList.toString()" style="height: 1.5cm;width: 72%"></a-textarea>
                        </div>
                        <div v-show="ipBlackList.length" style="overflow: hidden;margin-left: 67px;margin-top: 20px">
                            <span style="float: left">IP黑名单：</span>
                            <a-textarea v-model="ipBlackList.toString()" style="height: 1.5cm;width: 72%"></a-textarea>
                        </div>
                        <div v-if="info.requestHeader" style="overflow: hidden;margin-left: 20px;margin-top: 28px">
                            <span v-if="info.requestHeader.length" style="float: left">requestHeader：</span>
                            <a-table
                                    v-if="info.requestHeader.length"
                                    style="width: 70%;float: left"
                                    :columns="columnsRequestHeader"
                                    :data-source="info['requestHeader']"
                                    class="apiDetails_tabel"
                                    bordered
                                    size="small"
                                    :pagination="{hideOnSinglePage:true}"
                                    :rowKey="row=>row.index"
                            ></a-table>
                        </div>
                    </div>
                </div>
                <div class="listInfo" style="padding-bottom: 0">
                    <p class="titles">定义API请求
                        <a-radio-group v-model="type" style="margin-left: 10px">
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
                <div v-show="type==='a'">
                    <div class="listInfo" v-show="routerList>1">
                        <p class="title">API请求参数</p>
                        <ul class="switch clearfix">
                            <li v-for="(item,index) in routerList" :key="index" @click="active=index;getApiParameter()"
                                :class="{'active':active===index}">{{item.httpMethod}}<span>{{item.pattern|text}}</span></li>
                        </ul>
                    </div>
                    <div class="box" v-if="requestParamsData.length||requestBodyData[0].flag||responseBodyData[0].flag">
                        <div class="listInfo" v-show="requestParamsData.length">
                            <p class="title">请求参数</p>
                            <a-table
                                    :columns="columnsRequestParams"
                                    :data-source="requestParamsData"
                                    class="apiDetails_tabel"
                                    bordered
                                    size="small"
                                    :pagination="{hideOnSinglePage:true}"
                                    :rowKey="row=>row.index"
                            ></a-table>
                        </div>
                        <div class="listInfo" v-show="requestBodyData[0].flag">
                            <p class="title">请求body</p>
                            <a-table
                                    :columns="columnsRequestBody"
                                    :data-source="requestBodyData"
                                    class="apiDetails_tabel"
                                    bordered
                                    size="small"
                                    :pagination="{hideOnSinglePage:true}"
                                    :rowKey="row=>row.index"
                            ></a-table>
                        </div>
                        <div class="listInfo" v-show="responseBodyData[0].flag">
                            <p class="title">输出参数</p>
                            <a-table
                                    :columns="columnsRequestBody"
                                    :data-source="responseBodyData"
                                    class="apiDetails_tabel"
                                    bordered
                                    size="small"
                                    :pagination="{hideOnSinglePage:true}"
                                    :rowKey="row=>row.index"
                            ></a-table>
                        </div>
                    </div>
                </div>
                <div v-show="type==='b'">
                    <ul class="preview">
                        <li v-for="item in info.attFiles" :key="item.id" v-show="item.fileName.indexOf('.jar')<0">
                            <span @click="fileClick(item)">
                                <a-icon type="link"/>
                                {{item.fileName}}
                            </span>
                        </li>
                    </ul>
                </div>
                <div v-show="type==='c'">
                    <ul class="preview">
                        <li v-for="item in info.attFiles" :key="item.id" v-show="item.fileName.indexOf('.jar')>-1">
                            <span @click="fileClick(item)">
                                <a-icon type="link"/>
                                {{item.fileName}}
                            </span>
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
                            size="small"
                            :pagination="{hideOnSinglePage:true}"
                            :rowKey="row=>row.index"
                    ></a-table>
                </div>
            </div>
            <!--      接口测试-->
            <div class="portTest clearfix" v-if="index===1">
                <div class="requestHeader">
                    <p class="titles">调试 API</p>
                    <div class="debugging">
                        <a-select :default-value="linkType[0]" placeholder="请选择" v-model="typePort" @change="switchType">
                            <a-select-option v-for="item in linkType" :key="item">
                                {{item}}
                            </a-select-option>
                        </a-select>
                        <a-select v-model="linkPost" style="width: 55.5%">
                            <a-select-option v-for="item in urlPath" :key="item">
                                <div>
                                    <a-tooltip placement="top">
                                        <template slot="title">
                                            <span>{{item}}</span>
                                        </template>
                                        <div style="overflow: hidden;text-overflow:ellipsis;white-space: nowrap">{{item}}</div>
                                    </a-tooltip>
                                </div>
                            </a-select-option>
                        </a-select>
                        <a-button type="primary" @click="send">
                            发送
                        </a-button>
                    </div>
                    <div class="control" v-if="info.needAuth">
                        <p class="titles">访问控制</p>
                        <ul>
                            <li v-show="this.APP_KEY.userKey">user_key：{{this.APP_KEY.userKey}}</li>
                            <li v-show="this.APP_KEY.authAppId">appId：{{this.APP_KEY.authAppId}}</li>
                            <li v-show="this.APP_KEY.authAppKey">appKey：{{this.APP_KEY.authAppKey}}</li>
                        </ul>
                    </div>
                    <div class="headers">
                        <p class="titles">请求头参数 Headers</p>
                        <ul class="parameter" v-show="headers.length">
                            <li v-for="(item,index) in headers" :key="index">
                                <a-input placeholder="请输入参数名" v-model="item.param"></a-input>
                                <a-input placeholder="请输入参数名" v-model="item.value"></a-input>
                                <a-icon type="delete" @click="deleteHeaders(index)"/>
                            </li>
                        </ul>
                        <div class="add" @click="addHeaders">
                            <a-icon type="plus-circle"/>
                            添加参数
                        </div>
                    </div>
                    <div class="bodys">
                        <p class="titles">请求体 Body
                            <a-radio-group name="radioGroup" v-model="radio_type">
                                <a-radio :value="1">
                                    表单
                                </a-radio>
                                <a-radio :value="2">
                                    XML格式
                                </a-radio>
                                <a-radio :value="3">
                                    JSON格式
                                </a-radio>
                            </a-radio-group>
                        </p>
                        <div class="form">
                            <ul class="list" v-if="radio_type===1" v-show="bodys.length">
                                <li v-for="(item,index) in bodys" :key="index">
                                    <a-input placeholder="请输入参数名" v-model="item.key"></a-input>
                                    <a-input placeholder="请输入参数名" v-model="item.value"></a-input>
                                    <a-icon type="delete" @click="deleteBodys(index)"/>
                                </li>
                            </ul>
                            <a-textarea placeholder="XML格式" v-if="radio_type===2" v-model="xml"></a-textarea>
                            <a-textarea placeholder="JSON格式" v-if="radio_type===3" v-model="json"></a-textarea>
                        </div>
                        <div class="add" @click="addBodys">
                            <a-icon type="plus-circle"/>
                            添加参数
                        </div>
                    </div>
                </div>
                <div class="requestBody">
                    <p class="titles">返回结果</p>
                    <p class="timer">耗时：<span>{{time}}</span>ms</p>
                    <p class="results">响应结果 <span>Result</span></p>
                    <a-textarea v-model="portReturnValue"></a-textarea>
                    <a-spin v-show="loading" style="position: relative;top: -160px;left: 260px" />
                </div>
            </div>
            <!--      接口监控-->
            <div class="portMonitoring clearfix" v-show="index===2">
        <span>
          <a-select default-value="1" class="select" @change="handleChange">
          <a-select-option value="1">
            访问次数
          </a-select-option>
          <a-select-option value="2">
            接口响应
          </a-select-option>
          <a-select-option value="3">
            接口网络
          </a-select-option>
        </a-select>
        </span>
                <div v-show="monitoringType!=='2'">
                    订阅系统：
                    <a-select style="width: 120px;margin-right:32px" v-model="systemId" placeholder="所有系统">
                        <a-select-option v-for="(item,index) in systemList" :key="item.appId" :value="index">
                            {{item.systemName}}
                        </a-select-option>
                    </a-select>
                </div>
                <div class="timer">
                    <span>时间范围：</span>
                    <a-radio-group v-model="value" @change="timeScope(0)">
                        <a-radio-button value="a">
                            近1小时
                        </a-radio-button>
                        <a-radio-button value="b">
                            近6小时
                        </a-radio-button>
                        <a-radio-button value="c">
                            近1天
                        </a-radio-button>
                    </a-radio-group>
                </div>
                <a-range-picker
                        class="datePicker"
                        @change="onChange"
                        :placeholder="['开始日期', '结束日期']"
                        :show-time="{
              defaultValue: [moment('00:00:00', 'HH:mm:ss'), moment('11:59:59', 'HH:mm:ss')],
            }"
                        format="YYYY-MM-DD HH:mm:ss"
                        v-model="timer"
                />
                <div>
                    横坐标单位：
                    <a-select class="select" v-model="timeType">
                        <a-select-option value="1">
                            小时
                        </a-select-option>
                        <a-select-option value="2">
                            天
                        </a-select-option>
                        <a-select-option value="3">
                            月
                        </a-select-option>
                    </a-select>
                </div>
                <span style="margin-right: 32px"><a-button @click="reset">重置</a-button></span>
                <span><a-button type="primary" @click="getEcharts">查询</a-button></span>
                <br>
                <div style="width: 100%">
                    <div class="empty" v-show="empty">{{empty}}</div>
                    <div class="chart" v-show="!empty"></div>
                </div>
            </div>
            <!--      已订阅-->
            <div class="portSubscription" v-if="index===3">
                <a-table
                        :columns="columnSubscription"
                        :data-source="dataSubscription"
                        bordered
                        size="small"
                        @change="handleChangeSubscription"
                        :pagination="paginationSubscription"
                        :rowKey="row=>row.index"
                ></a-table>
            </div>
            <!--      发布历史-->
            <div class="portHistory" v-if="index===4">
                <a-table
                        :columns="columns2"
                        :data-source="historyList"
                        bordered
                        size="small"
                        :rowKey="row=>row.id"
                        :pagination="paginationHistory"
                        @change="handleChangeHistory"
                ></a-table>
            </div>
        </div>
    </div>
</template>

<script>
    import moment from "moment";
    import echarts from "echarts";
    import request from "../../utils/request";
    import {columnsRequestHeader,columnsResponsetHeader} from "./methods/data"
    let id;
    let type;
    let partition;
    export default {
        name: "apiDetails",
        data() {
            return {
                index: 0,
                columns: [
                    {
                        title: '序号',
                        dataIndex: 'index',
                        key: 'index'
                    },
                    {
                        title: 'IP',
                        dataIndex: 'ip',
                        key: 'ip',
                    }
                ],
                columnsRoute: [
                    {
                        title: '请求方法',
                        dataIndex: 'httpMethod',
                        key: 'httpMethod',
                        width:100
                    },
                    {
                        title: '访问路径',
                        dataIndex: 'pattern',
                        key: 'pattern',
                        width: 150,
                        ellipsis: true
                    },
                    {
                        title: '接口地址',
                        dataIndex: 'url',
                        key: 'url',
                    }
                ],
                columnsRequestParams: [
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
                columnsRequestBody: [
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
                columnSubscription: [
                    {
                        title: '订阅系统',
                        dataIndex: 'system',
                        key: 'system',
                        ellipsis: true
                        /*filters: [
                          {
                            text: 'London',
                            value: 'London',
                          },
                          {
                            text: 'New York',
                            value: 'New York'
                          }
                        ],
                        filterMultiple: false*/
                    },
                    {
                        title: '订阅人',
                        dataIndex: 'user',
                        key: 'user',
                        /*filters: [
                          {
                            text: 'London',
                            value: 'London',
                          },
                          {
                            text: 'New York',
                            value: 'New York'
                          }
                        ],
                        filterMultiple: false*/
                    },
                    {
                        title: '订阅时间',
                        dataIndex: 'createTime',
                        key: 'createTime',
                        sorter: true,
                        width: 200
                    }
                ],
                dataSubscription: [],
                paginationSubscription: {
                    total: 0,
                    current: 1,
                    pageSize: 10
                },
                searchSubscription: {
                    system: null,
                    user: null,
                    time: null
                },
                columns2: [
                    {
                        title: '更新时间',
                        dataIndex: 'createTime',
                        key: 'createTime',
                        width: 166,
                        sorter: true
                    },
                    {
                        title: '操作人',
                        dataIndex: 'creator',
                        key: 'creator',
                    },
                    {
                        title: '版本',
                        dataIndex: 'version',
                        key: 'version',
                    }
                ],
                score: null,//一级评论评分
                value: 'a',
                typePort: null,//接口调试，接口类型
                linkPost: null,//接口链接
                headers: [],//请求头参数
                radio_type: 3,//请求体参数类型
                bodys: [],//请求体参数
                commentsList: [],//评论列表
                commentTotal: 0,//评论总条数
                text: null,//一级评论的内容
                current: 1,//评论当前页数
                APP_KEY: null,//接口调用
                historyList: [],//历史列表
                paginationHistory:{
                    total: 0,
                    current: 1,
                    pageSize: 5,
                    showTotal:((total) => {
                        return `共 ${total} 条`;
                    }),
                },
                searchDataHistory:{
                    current:1,
                    sort:["d","createTime"]
                },
                info: {
                    publishApiGroupDto: {},
                    attFiles:[],
                    requestHeader:[]
                },
                proxyList: [],
                routerList: [],
                active: 0,
                requestParamsData: [],
                requestBodyData: [{}],
                responseBodyData: [{}],
                ipWhiteList: [],//白名单
                ipBlackList:[],//黑名单
                systemList: [],//订阅系统列表
                systemId: undefined,//订阅系统ID
                timer: null,//时间插件时间
                timeStamp: null,//时间范围
                monitoringType: "1",//访问类型
                portReturnValue: null,//返回结果
                empty: null,
                timeType: "1",
                urlPath: [],
                linkType:[],
                requestUrlAll:[],
                xml:null,
                json:null,
                time:0,
                charts:null,
                image:null,
                url:null,
                loading:false,
                type:'a',
                show:false,
                columnsRequestHeader,
                columnsResponsetHeader,
                flag:false
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
        methods: {
            moment,
            handleChange(data) {
                this.monitoringType = data;
            },
            onChange(data, dateString) {
                if (dateString[0]) {
                    this.value = undefined;
                    this.timer = dateString;
                } else {
                    this.value = 'a';
                    this.timer = null;
                    this.timeType = "1";
                }
                this.timeScope(1)
            },
            addHeaders() {//接口调试-------请求头参数添加
                this.headers.push({param: null, value: null})
            },
            deleteHeaders(index) {//接口调试-------请求头参数删除
                this.headers.splice(index, 1)
            },
            addBodys() {//接口调试-------请求体参添加
                this.bodys.push({key: null, value: null})
            },
            deleteBodys(index) {//接口调试-------请求体参数删除
                this.bodys.splice(index, 1)
            },
            getAppIdAndKeyParam() {//接口调试----获取appId和appKey
                if (!this.$route.query.id){
                    return
                }
                request(this.url+"/debugging/" + this.$route.query.id + "/getUserKey/" + this.$route.query.partition,{
                    method:"GET"
                }).then(res=>{
                    this.APP_KEY = res;
                    this.getPortUrl()
                })
            },
            getData() {//已订阅
                if (!this.$route.query.id){
                    return
                }
                request(this.url+"/publishApi/findApiSubscribeSystem/" + this.$route.query.id,{
                    method: "POST",
                    body:{
                        page: this.paginationSubscription.current,
                        size: this.paginationSubscription.pageSize,
                        system: this.searchSubscription.system,
                        user: this.searchSubscription.user,
                        sort: this.searchSubscription.time === "ascend" ? "a" : "b"
                    }
                }).then(res => {
                    this.dataSubscription = res.content;
                    this.dataSubscription.forEach((item, index) => {
                        item.index = index
                    })
                    this.paginationSubscription.total = res.totalElements;
                })
            },
            handleChangeSubscription(pagination, filters, sorter) {
                let json = {
                    system: filters.system ? filters.system[0] : null,
                    user: filters.user ? filters.user[0] : null,
                    time: sorter.order
                }
                if (
                    this.searchSubscription.system === json.system &&
                    this.searchSubscription.user === json.user &&
                    this.searchSubscription.time === json.time
                ) {
                    this.paginationSubscription.current = pagination.current;
                    this.getData()
                } else {
                    this.paginationSubscription.current = 1;
                    this.searchSubscription = {
                        system: json.system,
                        user: json.user,
                        time: json.time
                    };
                    this.getData();
                }
            },
            publishHistory() {//发布历史
                if (!this.$route.query.id){
                    return
                }
                request(this.url+"/publishApi/findApiPublishInfos/" + this.$route.query.id,{
                    method:"POST",
                    body:{
                        pageNum: this.searchDataHistory.current,
                        pageSize: 5,
                        sort:this.searchDataHistory.sort
                    }
                }).then(res=>{
                    this.historyList = res.content;
                    this.paginationHistory.total=res.totalElements
                })
            },
            getApiDetails() {//api详情
                if (!this.$route.query.id){
                    return
                }
                request(this.url+"/publishApi/" + this.$route.query.id,{
                    method:"GET"
                }).then(res=>{
                    res.apiMappingRuleDtos.forEach((item,index)=>{
                        item.index=index
                    })
                    this.info = res;
                    this.info.requestHeader?this.info.requestHeader.forEach((item,index)=>{
                        item.index=index
                        if (item.valueType==="liquid"){
                           item.value=item.value.replace("{{","").replace("}}","")
                        }
                    }):null
                    this.info.responseHeader?this.info.responseHeader.forEach((item,index)=>{
                        item.index=index
                        if (item.valueType==="liquid"){
                            item.value=item.value.replace("{{","").replace("}}","")
                        }
                    }):null
                    for (let i = 0; i < this.info.apiMappingRuleDtos.length; i++) {
                        if (this.info.apiMappingRuleDtos[i].httpMethod === "OPTIONS") {
                            continue
                        }
                        this.routerList.push(this.info.apiMappingRuleDtos[i])
                    }
                    this.ipWhiteList= this.info.ipWhiteList?this.info.ipWhiteList:[];
                    this.ipBlackList= this.info.ipBlackList?this.info.ipBlackList:[];
                    this.getApiParameter()
                    this.getProxyData(res.proxy);
                    this.showImage(res.picFiles[0]);
                    if (this.info.needAuth){
                        this.getAppIdAndKeyParam();
                    }else{
                        this.getPortUrl()
                    }
                })
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
                            required: item.required ?"必填": "选填",
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
                }else{
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
            showImage(info){
                if(!info){
                    return
                }
                request(this.url+"/publishApi/showApiDocFile/"+info.id,{
                    method:"GET"
                }).then(res=>{
                    this.image=res
                })
            },
            getSystemList() {
                if (!this.$route.query.id){
                    return
                }
                request(this.url+"/analytics/getSubscribeSystems/" + this.$route.query.id,{
                    method:"GET"
                }).then(res=>{
                    if (res) {
                        this.systemList = res;
                        this.systemList.unshift({systemName: "所有系统", appId: 0})
                        this.systemId = 0;
                    }
                })
            },
            reset() {
                this.systemId = undefined;
                this.value = "a";
                this.timeType = '1';
                this.timer = null
            },
            getEcharts() {
                let json = {
                    start: this.timer ? this.timer[0] : null,
                    end: this.timer ? this.timer[1] : null
                }
                if (this.monitoringType === "1") {//访问次数
                    request(this.url+"/analytics/apiTrafficStatistics",{
                        method:"POST",
                        body:{
                            apiId: this.$route.query.id - 0,
                            appId:this.systemList.length?this.systemList[this.systemId].appId:0,
                            statType: this.systemId ? 2 : 1,
                            granularity: this.timeType === "1" ? 4 : (this.timeType === "2" ? 5 : 6),
                            timeQuery: this.timer ? json : this.timeStamp
                        }
                    }).then(res=>{
                        if (!res.period){
                            this.empty = "暂无数据";
                            if (this.charts){
                                this.charts.clear();
                            }
                            return
                        }
                        if (res.period) {
                            this.empty = ""
                        }
                        let timeStart = Date.parse(res.period.since.replace("T", " ").replace("Z", "").replace("+08:00", ""));
                        let timeEnd = Date.parse(res.period.until.replace("T", " ").replace("Z", "").replace("+08:00", ""));
                        let time = []
                        if (res.values.length === 2) {
                            time[0] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart);
                            time[1] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeEnd);
                        } else {
                            let num = (timeEnd - timeStart) / res.values.length;
                            res.values.forEach((item, index) => {
                                time.push(echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', timeStart + num * index))
                            })
                        }
                        this.chart(time, [res.values])
                    })
                } else if (this.monitoringType === "2") {//接口异常
                    let requestCode2xx=()=>{
                        return new Promise((resolve, reject) => {
                            request(this.url+"/analytics/apiTrafficStatistics",{
                                method:"POST",
                                body:{
                                    apiId: this.$route.query.id - 0,
                                    statType: 3,
                                    granularity: this.timeType === "1" ? 4 : (this.timeType === "2" ? 5 : 6),
                                    timeQuery: this.timer ? json : this.timeStamp
                                }
                            }).then(res => {
                                resolve(res)
                            }).catch(e=>{
                                reject(e)
                            })
                        })
                    }
                    let requestCode4xx=()=>{
                        return new Promise((resolve, reject) => {
                            request(this.url+"/analytics/apiTrafficStatistics",{
                                method:"POST",
                                body:{
                                    apiId: this.$route.query.id - 0,
                                    statType: 4,
                                    granularity: this.timeType === "1" ? 4 : (this.timeType === "2" ? 5 : 6),
                                    timeQuery: this.timer ? json : this.timeStamp
                                }
                            }).then(res => {
                                resolve(res)
                            }).catch(e=>{
                                reject(e)
                            })
                        })
                    }
                    let requestCode5xx=()=>{
                        return new Promise((resolve, reject) => {
                            request(this.url+"/analytics/apiTrafficStatistics",{
                                method:"POST",
                                body:{
                                    apiId: this.$route.query.id - 0,
                                    statType: 5,
                                    granularity: this.timeType === "1" ? 4 : (this.timeType === "2" ? 5 : 6),
                                    timeQuery: this.timer ? json : this.timeStamp
                                }
                            }).then(res => {
                                resolve(res)
                            }).catch(e=>{
                                reject(e)
                            })
                        })
                    }
                    Promise.all([requestCode2xx(),requestCode4xx(),requestCode5xx()]).then(res=>{
                        let time = res[0].period || res[1].period || res[2].period || null;
                        if (time) {
                            this.empty = "";
                            let timeStart = Date.parse(time.since.replace("T", " ").replace("Z", "").replace("+08:00", ""));
                            let timeEnd = Date.parse(time.until.replace("T", " ").replace("Z", "").replace("+08:00", ""));
                            let times = [];
                            if (res[0].values){
                                if (res[0].values.length === 2) {
                                    res[0] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart);
                                    res[1] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeEnd);
                                } else {
                                    let num = (timeEnd - timeStart) / res[0].values.length;
                                    res[0].values.forEach((item, index) => {
                                        times.push(echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart + num * index))
                                    })
                                }
                            }else if (res[1].values){
                                if (res[1].values.length === 2) {
                                    res[1] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart);
                                    res[2] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeEnd);
                                } else {
                                    let num = (timeEnd - timeStart) / res[1].values.length;
                                    res[1].values.forEach((item, index) => {
                                        times.push(echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart + num * index))
                                    })
                                }
                            }else {
                                if (res[2].values.length === 2) {
                                    res[2] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart);
                                    res[3] = echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeEnd);
                                } else {
                                    let num = (timeEnd - timeStart) / res[2].values.length;
                                    res[2].values.forEach((item, index) => {
                                        times.push(echarts.format.formatTime('yyyy-MM-dd\nhh:mm:ss', timeStart + num * index))
                                    })
                                }
                            }
                            this.chart(times, [res[0].values, res[1].values, res[2].values],0)
                        } else {
                            this.empty = "暂无数据"
                        }
                    })
                }else{
                    request(this.url + "/analytics/apiResponseStatistics", {
                        method: "POST",
                        body: {
                            apiId: this.$route.query.id,
                            appId:this.systemList.length?this.systemList[this.systemId].applicationId:0,
                            statType: this.systemId ? 7 : 6,
                            granularity: this.timeType === "1" ? 4 : (this.timeType === "2" ? 5 : 6),
                            timeQuery: this.timer ? json : this.timeStamp
                        }
                    }).then(res => {
                        if (res){
                            this.empty="";
                            let time = [];
                            let value = [];
                            res.values.forEach(item => {
                                if (item.showX) {
                                    time.push(item.date)
                                } else {
                                    time.push("")
                                }
                                let arr = [];
                                for (let key in item.map) {
                                    arr.push(item.map[key])
                                }
                                value.push(arr);
                            })
                            this.chart(time, value,3)
                        }else{
                            this.empty = "暂无数据";
                        }
                    })
                }
            },
            chart(time, value,num) {
                function filters(index,num){
                    if (num===0){
                        if (!index){
                            return {
                                text:"2XX",
                                color:"green"
                            }
                        }else if (index===1){
                            return {
                                text:"4XX",
                                color:"#f2711c"
                            }
                        }else {
                            return {
                                text:"5XX",
                                color:"red"
                            }
                        }
                    }else {
                        return{
                            text:"访问量",
                            color:"blue"
                        }
                    }
                }
                let charts = echarts.init(document.querySelector(".chart"))
                let arr = [];
                let str="";
                if (num){
                    str="ms"
                    let json={
                        name: '响应时间',
                        type: 'line',
                        stack: '响应时间',
                        data:[],
                        color: "blue",
                        areaStyle: {}
                    }
                    value.forEach(item=>{
                        json.data.push(item[0]||0)
                    })
                    arr.push(json)
                }else {
                    for (let i=0;i<value.length;i++){
                        let json={
                            name: filters(i,num).text,
                            type: 'line',
                            stack: '总量',
                            data: (value[i] && value[i].length)?value[i]:[],
                            color: filters(i,num).color,
                            areaStyle: {}
                        }
                        arr.push(json);
                    }
                }
                charts.setOption({
                    tooltip: {
                        trigger: 'axis'
                    },
                    legend: {
                        data: ['p50', 'p75', 'p90', 'p95', 'p99']
                    },
                    grid: {
                        left: '3%',
                        right: '4%',
                        bottom: '3%',
                        containLabel: true
                    },
                    xAxis: {
                        type: 'category',
                        boundaryGap: false,
                        data: time
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: {
                            formatter: '{value} '+str
                        }
                    },
                    series: arr
                }, true);
            },
            timeScope(num) {
                let myDate = Date.parse(new Date());
                if (!num) {
                    this.timer = null;
                }
                if (this.value === "a") {
                    this.timeStamp = {
                        start: echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', (myDate - 60 * 60 * 1000)),
                        end: echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', myDate)
                    }
                } else if (this.value === "b") {
                    this.timeStamp = {
                        start: echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', (myDate - 6 * 60 * 60 * 1000)),
                        end: echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', myDate)
                    }
                } else {
                    this.timeStamp = {
                        start: echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', (myDate - 24 * 60 * 60 * 1000)),
                        end: echarts.format.formatTime('yyyy-MM-dd hh:mm:ss', myDate)
                    }
                }
            },
            send() {
                let form={};
                this.bodys.forEach(item=>{
                    form[item.key]=item.value
                })
                let time=0;
                let timer=setInterval(()=>{
                    time++;
                },1)
                this.loading=true;
                request(this.url+"/debugging/" + this.$route.query.id,{
                    method:"POST",
                    body:{
                        appId:this.APP_KEY?this.APP_KEY.authAppId:null,
                        appKey:this.APP_KEY?this.APP_KEY.authAppKey:null,
                        authType: this.info.needAuth ? 'auth' : 'noauth',
                        backendVersion: 1,
                        header: this.headers,
                        parameterContentType: this.radio_type === 1 ? "form" : (this.radio_type === 2 ? "xml" : "json"),
                        parameterMap: this.radio_type === 1?form:null,
                        parameterStr: this.radio_type===2?this.xml:(this.radio_type===3?(this.json || '{}'):null),
                        path: this.linkPost,
                        type: this.typePort,
                        userKey: this.APP_KEY?this.APP_KEY.userKey:null
                    }
                }).then(res=>{
                    this.loading=false;
                    clearInterval(timer);
                    this.time=time;
                    this.portReturnValue = res?JSON.stringify(res):null
                })
            },
            getPortUrl() {
                if (!this.$route.query.id){
                    return
                }
                let str;
                if (this.info.needAuth){
                    if (this.APP_KEY.userKey){
                        str='?user_key='+ ( this.$route.query.userKey || this.APP_KEY.userKey )
                    }else{
                        str='?app_id='+this.APP_KEY.authAppId+'&app_key='+this.APP_KEY.authAppKey
                    }
                }
                request(`${this.url}/debugging/${this.$route.query.id}/getPath/${this.$route.query.partition}`,{
                    method:"GET"
                }).then(res=>{
                    this.requestUrlAll=res;
                    res.forEach((item,index)=> {
                        for (let key in item){
                            if (!this.linkType.includes(key)){
                                this.linkType.push(key);
                            }
                            if (this.linkType[0]===key){
                                this.urlPath.push(item[key]);
                            }
                            this.info.apiMappingRuleDtos[index].url=item[key]+(str||"")
                        }
                    })
                    this.show=true;
                    this.linkPost=this.urlPath[0]
                    this.typePort=this.linkType[0]
                })
            },
            switchType(value){
                this.urlPath=[];
                this.requestUrlAll.forEach(item=>{
                    for (let key in item){
                        if (value===key){
                            this.urlPath.push(item[key])
                        }
                    }
                })
                this.linkPost=this.urlPath[0]
            },
            handleChangeHistory(pagination, filters, sorter){
                let json={
                    current:pagination.current,
                    sort:sorter.order?(sorter.order==='ascend'?['a','createTime']:['d','createTime']):['d','createTime']
                }
                if (json.current===this.searchDataHistory.current){
                    this.searchDataHistory.sort=json.sort;
                    this.searchDataHistory.current=1;
                    this.paginationHistory.current=1;
                    this.publishHistory();
                }else {
                    this.searchDataHistory.current=json.current;
                    this.paginationHistory.current=json.current;
                    this.publishHistory();
                }
            },
            fileClick(item){
                let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway;
                if (item.fileName.substring(item.fileName.length-3,item.fileName.length)==="jar"){
                    window.open("/apimgr/api/v1/tenant/tenant_id_1/project/"+data[1].id+"/"+data[0].key+"/publishApi/downloadApiDocFile/"+item.id)
                }else{
                    let originUrl = window.location.origin+"/apimgr/api/v1/tenant/tenant_id_1/project/"+data[1].id+"/"+data[0].key+"/publishApi/downloadApiDocFile/"+item.id;
                    let previewUrl = originUrl + '?fullfilename='+item.fileName;
                    window.open(window.location.protocol+"//kkfile-hisense-apigateway-test.devapps.hisense.com/onlinePreview?url="+encodeURIComponent(previewUrl));
                }
            },
            goBack(){
                if (this.$multiTab){
                    this.$multiTab.closeCurrentPage();
                }
                setTimeout(()=>{
                    if (!(this.$route.query.type-0)){
                        window.vm.$router.push({
                            path:"/gateway/apiList/apiListCreated"
                        })
                    }else{
                        window.vm.$router.push({
                            path:"/gateway/apiList/apiListsubscribe"
                        })
                    }
                })
            },
            alter(){
                let id=this.$route.query.id;
                let url=window.location.pathname+window.location.search
                window.vm.$multiTab.close(url);
                let bool=this.$route.query.bool
                setTimeout(()=>{
                   window.vm.$router.push({path:'/gateway/apiListalterApi',query:{text:'修改API',id:id,bool:bool}})
                },10)
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getData();
            this.publishHistory();
            this.getApiDetails();
            this.getSystemList();
            this.timeScope();
            //this.getCommentsContent();\
            id=this.$route.query.id;
            type=this.$route.query.type;
            partition=this.$route.query.partition;
        },
        mounted() {
            window.onresize = () => {
                if (window.vm.$route.name!=="apiDetails"){
                    return
                }
                if (this.charts){
                    this.charts.resize()
                }
            }
        },
        destroyed(){
            window.onresize=null;
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
    .apiDetails {
        height: 100%;
        overflow-y: auto;

        header {
            padding: 20px 0 0 24px;
            > span {
                font-size: 14px;
                margin-left: 16px;
            }
        }

        .port {
            padding: 24px 32px 0;
            background: #ffffff;

            > div:nth-child(1) {
                width: 64px;
                height: 64px;
                float: left;
                border-radius: 8px;

                i {
                    font-size: 32px;
                    color: #AFB5C1;
                    line-height: 64px;
                    position: relative;
                    left: 50%;
                    transform: translateX(-50%);
                }
            }

            > div:nth-child(2) {
                float: left;
                width: 83%;
                padding-left: 32px;

                p:nth-child(1) {
                    font-size: 18px;
                    color: #000000;

                    span {
                        padding: 0 4px;
                        height: 22px;
                        background: rgb(252, 252, 252);
                        border-radius: 4px;
                        border: 1px solid rgba(145, 213, 255, 1);
                        font-size: 12px;
                        color: #108EE9;
                        display: inline-block;
                        line-height: 20px;
                        text-align: center;
                    }
                }

                p:nth-child(2) {
                    font-size: 14px;
                    color: rgba(0, 0, 0, 0.35);
                    margin-top: 12px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    display: inline-block;
                    max-width: 100%;
                }
            }
        }

        .portDetails {
            background: #ffffff;
            min-height: calc(~"100vh - 270px");

            .tab {
                height: 46px;
                border-bottom: 1px solid rgba(232, 232, 232, 1);
                padding-left: 32px;
                overflow: hidden;
                margin-bottom: 0;
                > li {
                    float: left;
                    line-height: 46px;
                    width: 88px;
                    text-align: center;
                    color: rgba(0, 0, 0, 0.65);
                    font-size: 14px;
                    position: relative;
                    top: -2px;
                    cursor: pointer;
                }

                .active {
                    color: #00AAA6;
                    border-bottom: 2px solid #00AAA6;
                }
            }

            .details {
                padding: 20px 32px;

                .info {
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
                    }

                    > div:last-child {
                        > p:nth-child(1) {
                            width: 66.6%;
                        }
                    }
                }

                .interface {
                    padding: 32px;

                    p {
                        padding-bottom: 10px;

                        > span:nth-child(1) {
                            color: rgba(0, 0, 0, 0.35);
                        }

                        > span:nth-child(2) {
                            color: rgba(0, 0, 0, 0.65);
                        }
                    }
                }

                .listInfo {
                    padding-bottom: 24px;

                    .title {
                        font-size: 16px;
                        color: #000000;
                        padding-bottom: 18px;
                        line-height: 1;
                        margin-bottom: 0;
                    }

                    .switch {
                        li {
                            float: left;
                            width: 188px;
                            height: 32px;
                            margin-right: 8px;
                            font-size: 12px;
                            background-color: #EEF3F2;
                            color: #8DB6B5;
                            line-height: 32px;
                            padding: 0 8px;
                            border-radius: 1px;
                            cursor: pointer;
                        }

                        .active {
                            background-color: #00AAA6;
                            color: #ffffff;
                        }

                        span {
                            padding-left: 20px;
                        }
                    }
                }
                .preview{
                   li{
                       padding-bottom: 6px;
                       span{
                           cursor: pointer;
                           color: #108EE9;
                           i{
                               margin-right: 4px;
                           }
                       }
                   }
                }
                .box {
                    box-shadow: 0 1px 12px 0 rgba(0, 0, 0, 0.15);
                    margin-bottom: 32px;
                    padding: 14px 24px;
                }
            }

            .portMonitoring {
                padding: 32px;

                > * {
                    float: left;
                    padding-bottom: 16px;
                }

                .select {
                    margin-right: 32px;
                    width: 102px;
                }

                .timer {
                    padding-right: 32px;
                }

                .datePicker {
                    padding-right: 32px;
                }

                .chart {
                    width: 100%;
                    height: 350px;
                    background: #ffffff;
                }

                .empty {
                    text-align: center;
                    width: 100%;
                    font-size: 18px;
                    position: relative;
                    top: 160px;
                }
            }

            .portSubscription, .portHistory {
                padding: 32px;
            }

            .portInteractive {
                padding: 0 32px 32px;

                .textarea {
                    padding-top: 24px;

                    textarea {
                        height: 64px;
                        border: 1px solid #D9D9D9;
                        display: block;
                        width: 100%;
                        outline: none;
                        padding: 6px;
                    }

                    > div {
                        padding-top: 16px;
                        float: right;

                        > ul {
                            padding-right: 16px;
                        }
                    }
                }

                .comments {
                    > li:nth-child(n):not(:last-child) {
                        border-bottom: 1px solid #E8E8E8;
                    }

                    li {
                        padding-top: 24px;
                        padding-bottom: 26px;

                        > div:nth-child(1) {
                            float: left;
                            width: 48px;
                            height: 48px;
                            margin-right: 24px;

                            > img {
                                display: block;
                                width: 100%;
                                height: 100%;
                                border-radius: 50%;
                            }
                        }

                        > div:nth-child(2) {
                            float: left;
                            width: 85%;

                            p:nth-child(1) {
                                color: #000000;
                                font-size: 14px;
                                line-height: 20px;
                            }

                            p:nth-child(2) {
                                line-height: 30px;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                                display: inline-block;
                                max-width: 100%;
                                cursor: pointer;
                            }

                            p:nth-child(3) {
                                span {
                                    font-size: 12px;
                                    color: rgba(0, 0, 0, 0.35);
                                }
                            }
                        }

                        > div:nth-child(3) {
                            float: right;

                            p:nth-child(1) {
                                font-size: 12px;
                                color: rgba(0, 0, 0, 0.35);
                                line-height: 20px;
                                text-align: right;
                            }

                            p:nth-child(2) {
                                padding-top: 35px;

                                i, > img {
                                    margin-left: 26px;
                                    cursor: pointer;
                                }

                                i:nth-child(1), i:nth-child(3) {
                                    font-size: 18px;
                                    color: rgba(0, 0, 0, 0.25);
                                    position: relative;
                                    top: 2px;
                                }

                                > span {
                                    font-size: 14px;
                                    color: rgba(0, 0, 0, 0.35);
                                    position: relative;
                                    top: 2px;
                                    margin-left: 2px;
                                }
                            }
                        }

                        .reply {
                            width: 100%;
                            float: left;
                            margin-top: 26px;

                            button {
                                float: right;
                                margin-top: 16px;
                            }
                        }

                        .comment {
                            width: 100%;
                            float: left;

                            li {
                                margin-top: 16px;
                                padding-right: 18px;
                                background: #F7F7F7;
                                padding-left: 20px;

                                > div:nth-child(1) {
                                    float: left;
                                    width: 48px;
                                    height: 48px;
                                    margin-right: 24px;

                                    > img {
                                        display: block;
                                        width: 100%;
                                        height: 100%;
                                        border-radius: 50%;
                                    }
                                }

                                > div:nth-child(2) {
                                    float: left;

                                    p:nth-child(1) {
                                        color: #000000;
                                        font-size: 14px;
                                        line-height: 20px;
                                    }

                                    p:nth-child(2) {
                                        line-height: 30px;
                                    }

                                    p:nth-child(3) {
                                        span {
                                            font-size: 12px;
                                            color: rgba(0, 0, 0, 0.35);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                .ant-pagination {
                    text-align: right;
                }

                .ant-empty {
                    padding-top: 19%;
                }
            }

            .portTest {
                padding-bottom: 20px;

                .requestHeader, .requestBody {
                    float: left;
                    width: 50%;
                    height: 97%;
                    margin-top: 1%;
                    padding-left: 16px;

                    .titles {
                        font-size: 16px;
                        color: #666666;
                        padding-bottom: 16px;
                    }
                }

                .requestHeader {
                    border-right: 1px solid #E8E8E8;

                    .debugging {
                        margin-bottom: 16px;

                        > * {
                            margin-right: 10px;
                        }

                        .ant-select {
                            width: 120px;
                        }
                    }

                    .control {
                        border-top: 1px solid #E8E8E8;
                        padding-top: 14px;
                        width: 95%;

                        ul {
                            font-size: 12px;

                            li {
                                padding-bottom: 12px;
                            }
                        }
                    }

                    .headers {
                        border-top: 1px solid #E8E8E8;
                        padding-top: 14px;
                        width: 95%;

                        .parameter {
                            border: 1px solid #E8E8E8;
                            padding: 10px;

                            li {
                                padding-bottom: 8px;
                                margin-bottom: 8px;

                                .ant-input {
                                    width: 46%;
                                }

                                i {
                                    font-size: 16px;
                                    color: #aeadad;
                                    margin-left: 8px;
                                }
                            }

                            li:last-child {
                                border-bottom: none;
                                padding-bottom: 0;
                                margin-bottom: 0;
                            }
                        }

                        .add {
                            color: #00AAA6;
                            font-size: 12px;
                            margin-top: 6px;
                            display: inline-block;
                            cursor: pointer;

                            i {
                                margin-right: 6px;
                            }
                        }
                    }

                    .bodys {
                        width: 95%;
                        border-top: 1px solid #E8E8E8;
                        padding-top: 14px;
                        margin-top: 14px;

                        .ant-radio-group {
                            margin-left: 16px;
                        }

                        .form {
                            .list {
                                padding: 10px;
                                border: 1px solid #E8E8E8;

                                li {
                                    padding-bottom: 8px;
                                    margin-bottom: 8px;

                                    .ant-input {
                                        width: 46%;
                                    }

                                    i {
                                        font-size: 16px;
                                        color: #aeadad;
                                        margin-left: 6px;
                                    }
                                }

                                li:last-child {
                                    padding-bottom: 0;
                                    margin-bottom: 0;
                                }
                            }

                            teatarea {
                                height: 300px;
                            }
                        }

                        .add {
                            color: #00AAA6;
                            font-size: 12px;
                            margin-top: 6px;
                            display: inline-block;
                            cursor: pointer;

                            i {
                                margin-right: 6px;
                            }
                        }
                    }
                }

                .requestBody {
                    .timer {
                        font-size: 12px;

                        span {
                            color: #00AAA6;
                        }
                    }

                    .results {
                        padding-top: 10px;
                        color: #666666;

                        span {
                            color: #000000;
                        }
                    }

                    textarea {
                        width: 95%;
                        margin-top: 10px;
                        height: 290px;
                        display: block;
                    }
                }
            }
        }
    }

    .apiDetails::-webkit-scrollbar {
        display: none;
    }
</style>
<style>
    ul,li{
        list-style: none;
        padding: 0;
    }
    /*.listInfo .apiDetails_tabel {*/
    /*  width: 93.5%;*/
    /*  margin: 0 auto;*/
    /*}*/

    /*.ant-radio-group label span:nth-child(2) {*/
    /*  font-size: 12px;*/
    /*  padding: 0 4px;*/
    /*}*/
</style>
