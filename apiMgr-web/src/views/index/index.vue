<template>
    <div class="homePage_pree">
        <div v-if="flag" class="dashboard">
            <a-tabs default-active-key="1" :tabBarGutter="0" class="dash_tab" @change="initData">
                <a-tab-pane key="1" tab="全局概览" class="dash_tab_item">
                    <a-spin :spinning="dashLoading_1">
                        <a-row class="count_list" type="flex">
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiProjectCount}}</span>
                                    </template>
                                    <div class="num">{{topBoard.apiProjectCount}}</div>
                                </a-tooltip>
                                <div class="name">系统数量</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiUseDayCount}}</span>
                                    </template>
                                    <div class="num">{{topBoard.apiUseDayCount}}</div>
                                </a-tooltip>
                                <div class="name">今日调用量</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <div class="num">
                    <span style="color:#00BF17;">
                      <a-tooltip placement="bottom" class="ellipisis">
                        <template slot="title">
                          <span>{{topBoard.apiUseSuccessDayount}}</span>
                        </template>
                        {{topBoard.apiUseSuccessDayount}}
                      </a-tooltip>
                    </span>/
                                        <span style="color:#E35757">
                      <a-tooltip placement="bottom" class="ellipisis">
                        <template slot="title">
                          <span>{{topBoard.apiUseFailDayount}}</span>
                        </template>
                        {{topBoard.apiUseFailDayount}}
                      </a-tooltip>
                    </span>
                                    </div>
                                </a-tooltip>
                                <div class="name">今日调用成功/失败</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiCount}}</span>
                                    </template>
                                    <div class="num hover" @click="editShow('累计发布API',1)">{{topBoard.apiCount}}</div>
                                </a-tooltip>
                                <div class="name">累计发布API</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiSubscribedCount}}</span>
                                    </template>
                                    <div
                                            class="num hover"
                                            @click="editShow('累计被订阅',2)"
                                    >{{topBoard.apiSubscribedCount}}
                                    </div>
                                </a-tooltip>
                                <div class="name">累计被订阅</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiUseTotalCount}}</span>
                                    </template>
                                    <div class="num">{{topBoard.apiUseTotalCount}}</div>
                                </a-tooltip>
                                <div class="name">累计调用</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{releaseTotal}}</span>
                                    </template>
                                    <div
                                            class="num hover"
                                            style="color: #00AAA6 "
                                            @click="routerPush"
                                    >{{releaseTotal}}
                                    </div>
                                </a-tooltip>
                                <div class="name">待处理申请</div>
                            </a-col>
                        </a-row>
                    </a-spin>
                    <a-row class="echarts_list" :gutter="12">
                        <a-col :span="18" class="chart_area">
                            <a-spin :spinning="dashLoading_2">
                                <a-row class="echarts_area" :gutter="12">
                                    <a-col :span="12" class="echarts_area_item">
                                        <div class="title">API订阅数量TOP5</div>
                                        <div id="apiTop" :style="{width: '100%', height: '198px'}"></div>
                                    </a-col>
                                    <a-col :span="12" class="echarts_area_item">
                                        <div class="title">API订阅数量BOTTOM5</div>
                                        <div id="apiBottom" :style="{width: '100%', height: '198px'}"></div>
                                    </a-col>
                                    <a-col :span="12" class="echarts_area_item">
                                        <div class="title">API累计调用量TOP5</div>
                                        <div id="apiVisitTop" :style="{width: '100%', height: '198px'}"></div>
                                    </a-col>
                                    <a-col :span="12" class="echarts_area_item">
                                        <div class="title">API今日调用量TOP5</div>
                                        <div id="apiVisitBottom" :style="{width: '100%', height: '198px'}"></div>
                                    </a-col>
                                </a-row>
                            </a-spin>
                        </a-col>
                        <a-col :span="6" class="list_area">
                            <div class="title">
                                TOP5项目
                                <a-radio-group
                                        class="title_icon"
                                        v-model="number"
                                        @change="(e)=>{initList(e.target.value)}"
                                        style="float:right"
                                        size="small"
                                >
                                    <a-radio-button value="1">API发布数</a-radio-button>
                                    <a-radio-button value="2">API订阅数</a-radio-button>
                                </a-radio-group>
                            </div>

                            <a-spin :spinning="dashLoading_3">
                                <div class="con">
                                    <div v-for="(item,key) in liList" class="con_item" :key="key">
                                        <div class="name ellipisis">
                                            <a-tooltip placement="bottom">
                                                <template slot="title">{{item.systemName}}</template>
                                                {{item.systemName}}
                                            </a-tooltip>
                                        </div>
                                        <a-row class="echarts_area" :gutter="12">
                                            <a-col :span="12" class="ellipisis">
                                                <span class="sub_head">{{dashListIndex==1?"发布API : ":"被订阅API : "}}</span>
                                                <span>
                          <a-tooltip placement="bottom">
                            <template slot="title">{{item.apiCount}}</template>
                            {{item.apiCount}}
                          </a-tooltip>
                        </span>
                                            </a-col>
                                            <a-col :span="12" class="ellipisis">
                                                <span class="sub_head">占比 :</span>
                                                <span>
                          <a-tooltip placement="bottom">
                            <template slot="title">{{item.percentage}}</template>
                            {{item.percentage}}
                          </a-tooltip>
                        </span>
                                            </a-col>
                                        </a-row>
                                    </div>
                                </div>
                            </a-spin>
                        </a-col>
                    </a-row>
                </a-tab-pane>
                <a-tab-pane key="2" tab="系统概览" class="dash_tab_item" force-render>
                    <a-spin :spinning="dashLoading_1">
                        <a-row class="count_list" type="flex">
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiUseDayCount}}</span>
                                    </template>
                                    <div class="num">{{topBoard.apiUseDayCount}}</div>
                                </a-tooltip>
                                <div class="name">今日调用量</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <div class="num">
                    <span style="color:#00BF17;">
                      <a-tooltip placement="bottom" class="ellipisis">
                        <template slot="title">
                          <span>{{topBoard.apiUseSuccessDayount}}</span>
                        </template>
                        {{topBoard.apiUseSuccessDayount}}
                      </a-tooltip>
                    </span>/
                                        <span style="color:#E35757">
                      <a-tooltip placement="bottom" class="ellipisis">
                        <template slot="title">
                          <span>{{topBoard.apiUseFailDayount}}</span>
                        </template>
                        {{topBoard.apiUseFailDayount}}
                      </a-tooltip>
                    </span>
                                    </div>
                                </a-tooltip>
                                <div class="name">今日调用成功/失败</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiUseTotalCount}}</span>
                                    </template>
                                    <div class="num">{{topBoard.apiUseTotalCount}}</div>
                                </a-tooltip>
                                <div class="name">累计调用量</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiCount}}</span>
                                    </template>
                                    <div class="num hover" @click="editShow('累计发布API',1)">{{topBoard.apiCount}}</div>
                                </a-tooltip>
                                <div class="name">发布API</div>
                            </a-col>
                            <a-col style="flex: 1">
                                <a-tooltip placement="bottom">
                                    <template slot="title">
                                        <span>{{topBoard.apiSubscribedCount}}</span>
                                    </template>
                                    <div
                                            class="num hover"
                                            @click="editShow('累计被订阅',4)"
                                    >{{topBoard.apiSubscribedCount}}
                                    </div>
                                </a-tooltip>
                                <div class="name">累计被订阅</div>
                            </a-col>
                        </a-row>
                    </a-spin>
                    <a-row class="echarts_area" :gutter="12">
                        <a-spin :spinning="dashLoading_2">
                            <a-col :span="12" class="echarts_area_item">
                                <div class="title">API订阅数量TOP5</div>
                                <div id="apiTop_one" :style="{width: '100%', height: '198px'}"></div>
                            </a-col>
                            <a-col :span="12" class="echarts_area_item">
                                <div class="title">API订阅数量BOTTOM5</div>
                                <div id="apiBottom_one" :style="{width: '100%', height: '198px'}"></div>
                            </a-col>
                            <a-col :span="12" class="echarts_area_item">
                                <div class="title">API累计调用量TOP5</div>
                                <div id="apiVisitTop_one" :style="{width: '100%', height: '198px'}"></div>
                            </a-col>
                            <a-col :span="12" class="echarts_area_item">
                                <div class="title">API今日调用量TOP5</div>
                                <div id="apiVisitBottom_one" :style="{width: '100%', height: '198px'}"></div>
                            </a-col>
                        </a-spin>
                    </a-row>
                </a-tab-pane>

                <a-button
                        slot="tabBarExtraContent"
                        type="primary"
                        icon="plus"
                        class="dash_button"
                        :style="{marginRight:'10px'}"
                        @click="text='添加系统';visible=true"
                >添加系统
                </a-button>
                <a-button
                        slot="tabBarExtraContent"
                        @click="edit"
                        :loading="loading"
                        type="primary"
                        icon="edit"
                        v-show="isDeveloper"
                        :style="{marginRight:'10px'}"
                        class="dash_button"
                        v-if="getSystemList.length"
                >编辑系统
                </a-button>
            </a-tabs>
        </div>
        <div v-if="!flag">
            <img class="img" src="../../assets/homePage.png"/>
            <p class="title">你好，欢迎使用服务管理中心</p>
            <a-button type="primary" icon="plus" class="btn" @click="text='添加系统';visible=true">点击快速添加系统</a-button>
        </div>
        <a-modal
                :title="text"
                :visible="visible"
                :confirm-loading="confirmLoading"
                @ok="handleOk"
                @cancel="handleCancel"
                class="system_roules"
        >
            <a-form-model
                    :model="form"
                    ref="form"
                    :label-col="{ span: 6 }"
                    :wrapper-col="{ span: 18 }"
                    :rules="rules"
            >
                <a-form-model-item label="系统名称：" prop="name">
                    <div class="checkoutSystem" v-if="text==='添加系统'">
                        <a-tag v-show="systemForm" color="#fff" closable @close="delSystemItem"><span
                                style="color:#00aaa6;">{{systemForm}}</span></a-tag>
                    </div>
                    <div class="checkoutSystem" v-else>
                        <a-tag v-show="systemForm" color="#fff" @close="delSystemItem"><span style="color:#00aaa6;">{{systemForm}}</span>
                        </a-tag>
                    </div>
                    <div class="searchSystem">
                        <div class="sysyemCon">
                            <a-input-search
                                    placeholder="请输入系统名称"
                                    enter-button
                                    :loading="searchLoading"
                                    v-model.trim="systemName"
                                    style="width: 70%"
                                    @search="onSearch(0)"
                                    @pressEnter="onSearch(0)"
                                    @blur="blur"
                                    v-show="text==='添加系统'"
                            />
                        </div>
                        <div class="systemList">
                            <ul v-if="noData">
                                <li
                                        v-for="(item,index) in systemList"
                                        :key="item.id"
                                        v-show="systemList.length"
                                        @click="checkSystem(item,index)"
                                >{{item.name}}
                                </li>
                                <div v-if="systemList.length===0"
                                     style="padding: 15px 0;box-sizing: border-box;position: relative;">
                                    <a-empty>
                                        <a-icon type="close" @click="noData=false" style="position: absolute;right: 15px;top: 15px"/>
                                        <span slot="description">暂无数据，如需新增系统，点击通知管理员按钮</span>
                                        <a-button @click="inForm" type="primary" :loading="loadingTo">通知管理员</a-button>
                                    </a-empty>
                                </div>
                            </ul>
                        </div>
                    </div>
                </a-form-model-item>
                <a-form-model-item label="系统负责人：" prop="person">
                    <div class="searchSystem">
                        <div class="sysyemCon">
                            <div class="checkoutSystem" v-show="form.person">
                                <a-tag
                                        closable
                                        @close="delSystem(index)"
                                        color="#ffffff"
                                        v-for="(item,index) in form.person"
                                        :key="item.uid"
                                >
                                    <span style="color:#00aaa6;">{{item.cn}}</span>
                                </a-tag>
                            </div>
                            <a-input-search
                                    placeholder="请输入系统负责人名字拼音"
                                    :loading="searchLoading1"
                                    enter-button
                                    style="width: 70%"
                                    v-model.trim="seearchName"
                                    @search="onSearch(1)"
                                    @pressEnter="onSearch(1)"
                                    @blur="blur"
                            />
                        </div>
                        <div class="systemList">
                            <ul v-show="personList.length">
                                <li
                                        v-for="(item,index) in personList"
                                        :key="index"
                                        v-show="personList.length"
                                        @click="check(item,index)"
                                >{{item.cn}}
                                </li>
                            </ul>
                        </div>
                    </div>
                </a-form-model-item>
                <a-form-model-item label="开发人员：" prop="person1">
                    <div class="searchSystem">
                        <div class="sysyemCon">
                            <div class="checkoutSystem" v-show="form.person1">
                                <a-tag
                                        closable
                                        @close="delSystem1(index)"
                                        color="#ffffff"
                                        v-for="(item,index) in form.person1"
                                        :key="item.uid"
                                >
                                    <span style="color:#00aaa6;">{{item.cn}}</span>
                                </a-tag>
                            </div>
                            <a-input-search
                                    placeholder="请输入开发人员名字拼音"
                                    style="width: 70%"
                                    enter-button
                                    :loading="searchLoading2"
                                    v-model.trim="seearchName1"
                                    @search="onSearch(2)"
                                    @pressEnter="onSearch(2)"
                                    @onBlur="blur"
                            />
                        </div>
                        <div class="systemList">
                            <ul v-show="personList1.length">
                                <li
                                        v-for="(item,index) in personList1"
                                        :key="index"
                                        v-show="personList1.length"
                                        @click="check1(item,index)"
                                >{{item.cn}}
                                </li>
                            </ul>
                        </div>
                    </div>
                </a-form-model-item>
                <a-form-model-item label="租户管理员：" prop="person2">
                    <div class="searchSystem">
                        <div class="sysyemCon">
                            <div class="checkoutSystem" v-show="form.person2">
                                <a-tag
                                        closable
                                        @close="delSystem2(index)"
                                        color="#ffffff"
                                        v-for="(item,index) in form.person2"
                                        :key="item.uid"
                                >
                                    <span style="color:#00AAA6;">{{item.cn}}</span>
                                </a-tag>
                            </div>
                            <a-input-search
                                    placeholder="请输入租户管理员名字拼音"
                                    style="width: 70%"
                                    enter-button
                                    :loading="searchLoading3"
                                    v-model.trim="seearchName2"
                                    @search="onSearch(3)"
                                    @pressEnter="onSearch(3)"
                                    @onBlur="blur"
                            />
                        </div>
                        <div class="systemList">
                            <ul v-show="personList2.length">
                                <li
                                        v-for="(item,index) in personList2"
                                        :key="index"
                                        v-show="personList2.length"
                                        @click="check2(item,index)"
                                >{{item.cn}}
                                </li>
                            </ul>
                        </div>
                    </div>
                </a-form-model-item>
            </a-form-model>
        </a-modal>
        <div>
            <a-modal
                    width="900px"
                    :title="edits_title"
                    :visible="edits_show"
                    :footer="null"
                    @cancel="edits_show=false"
            >
                <a-table :columns="tableColumns" :data-source="tableData" :pagination="false" size="small"></a-table>
            </a-modal>
        </div>
    </div>
</template>

<script>
    import request from "@/utils/request";
    import echarts from "echarts";

    export default {
        data() {
            return {
                visible: false,
                edits_show: false,
                edits_title: "",
                confirmLoading: false,
                dashLoading_1: false,
                dashLoading_2: false,
                dashLoading_3: false,
                dashListIndex: 1,
                topBoard: {},
                form: {
                    name: undefined,
                    person: [],
                    person1: [],
                    person2: []
                },
                rules: {
                    person: [
                        {required: true, message: "请选择系统负责人", trigger: "blur"}
                    ],
                    name: [{required: true, message: "请选择系统", trigger: "blur"}],
                    person1: [
                        {required: true, message: "请选择开发人员", trigger: "blur"}
                    ],
                    person2: [
                        {required: true, message: "请选择租户管理员", trigger: "blur"}
                    ]
                },
                selectList: [],
                systemList: [],
                timer: null,
                seearchName: null,
                seearchName1: null,
                seearchName2: null,
                checkoutSystem: [],
                searchLoading: false,
                searchLoading1: false,
                searchLoading2: false,
                searchLoading3: false,
                personList: [],
                personList1: [],
                personList2: [],
                systemName: null,
                systemInfo: null,
                systemForm: null,
                url: null,
                url2: null,
                url3: null,
                url4: null,
                loading: false,
                text: "添加系统",
                list: [],
                searchList: [],

                liList: [],
                // APi名称、发布系统、接口路由，后端地址，创建人
                // API名称，发布系统，一对多：订阅系统列表，订阅人
                tableColumns: [],
                tableColumns_1: [
                    {
                        title: "APi名称",
                        dataIndex: "apiName",
                        ellipsis: true
                    },
                    {
                        title: "发布系统",
                        dataIndex: "apiSystem",
                        ellipsis: true
                    },
                    {
                        title: "接口路由",
                        dataIndex: "pattern",
                        ellipsis: true
                    },
                    {
                        title: "后端地址",
                        dataIndex: "host",
                        ellipsis: true
                    },

                    {
                        title: "创建人",
                        dataIndex: "creator",
                        ellipsis: true
                    }
                ],
                tableColumns_2: [
                    {
                        title: "APi名称",
                        dataIndex: "apiName",
                        ellipsis: true
                    },
                    {
                        title: "发布系统",
                        dataIndex: "apiSystem",
                        ellipsis: true
                    },
                    {
                        title: "订阅系统列表",
                        dataIndex: "appSystem",
                        ellipsis: true
                    },
                    {
                        title: "订阅人",
                        dataIndex: "subscriber",
                        ellipsis: true
                    }
                ],
                tableData: [],
                isDeveloper: false, //是否显示添加和编辑按钮   false不显示  和接口相反
                initDataIndex: 0,
                getSystemList: [],
                releaseTotal: 0,
                userSystemList: [],
                flag: false,
                number: "1",
                noData: false,
                loadingTo: false
            };
        },
        mounted() {
            // this.initDataIndex=1;
            // return;
            // this.apiTop();
            // this.apiBottom();
            // this.apiVisitTop();
            // this.apiVisitBottom();
            // this.apiTop("_one");
            // this.apiBottom("_one");
            // this.apiVisitTop("_one");
            if (window.vm.$route.name !== "gateway_indexPage") {
                return
            }
            this.initData(1);
        },
        methods: {
            initList(e) {
                let url = "";
                if (e == 1) {
                    url = "/dashboard/topPublishApiCountProjects";
                } else {
                    url = "/dashboard/topSubscribeApiSystem";
                }
                this.dashLoading_3 = true;
                this.dashListIndex = e;
                request(`/apimgr/api/v1/tenant/tenant_id_1/project/undefined/${this.$store.state.appNowCategory.gateway[0].key}` + url, {
                    method: "GET"
                }).then(res => {
                    this.liList = res;
                    this.dashLoading_3 = false;
                });
            },
            initData(key) {
                this.number = "1";
                this.getReleaseTotal();
                if (key == 1) {
                    this.initList(1);
                }
                let url = "";
                if (key == 1) {
                    url = `/apimgr/api/v1/tenant/tenant_id_1/project/undefined/${this.$store.state.appNowCategory.gateway[0].key}`;
                } else {
                    url = this.url;
                }
                this.initDataIndex = key;
                this.dashLoading_1 = true;
                this.topBoard = {};
                request(url + `/dashboard/releaseStatistics`, {
                    method: "GET"
                }).then(res => {
                    this.topBoard = res;

                    this.dashLoading_1 = false;
                });

                this.dashLoading_2 = true;
                request(url + `/dashboard/topApiBarChart`, {
                    method: "GET"
                }).then(res => {
                    if (key == 1) {
                        this.apiTop(res);
                        this.apiBottom(res);
                        this.apiVisitTop(res);
                        this.apiVisitBottom(res);
                    } else {
                        this.apiTop(res, "_one");
                        this.apiBottom(res, "_one");
                        this.apiVisitTop(res, "_one");
                        this.apiVisitBottom(res, "_one");
                    }
                    this.dashLoading_2 = false;
                });
            },
            apiTop(e, name) {
                // 基于准备好的dom，初始化echarts实例
                let myChart = echarts.init(
                    document.getElementById(`apiTop${name || ""}`)
                );
                // 绘制图表
                myChart.setOption({
                    tooltip: {
                        trigger: "item",
                        position: "bottom",
                        formatter: function (val) {
                            let reg = new RegExp("\\S{1,16}", "g");
                            let ma = val.name.match(reg);
                            return val.marker + ma.join("<br/>");
                        }
                    },
                    grid: {
                        top: "5%",
                        bottom: "15%",
                        // containLabel: true,
                        x: 120
                    },
                    xAxis: {
                        axisLabel: {
                            textStyle: {
                                color: "#8c8c8c"
                            },
                            formatter: function (value) {
                                if (value >= 1000000) {
                                    return `${value / 1000000}M`
                                }
                                console.log(value)
                                if (value >= 10000) {
                                    return `${value / 10000}W`
                                }
                                return `${value}`
                            }
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                    },
                    yAxis: {
                        axisLabel: {
                            formatter: function (val) {
                                let str = "";

                                if (val.length > 8) {
                                    str = val.substring(0, 8) + "...";
                                } else {
                                    str = val;
                                }
                                return str;
                            },
                            textStyle: {
                                color: "#8c8c8c"
                            }
                        },

                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                        data: e.subscribeApiTop || []
                    },
                    series: [
                        {
                            type: "bar",
                            label: {
                                show: true,
                                position: "insideLeft",
                                color: '#333333'
                            },
                            barWidth: 15,
                            itemStyle: {
                                color: "#e8684a"
                            },
                            data: e.subscribeApiTopCount || []
                        }
                    ]
                });
            },
            apiBottom(e, name) {
                // 基于准备好的dom，初始化echarts实例
                let myChart = echarts.init(
                    document.getElementById(`apiBottom${name || ""}`)
                );
                // 绘制图表
                myChart.setOption({
                    tooltip: {
                        trigger: "item",
                        position: "bottom",
                        formatter: function (val) {
                            let reg = new RegExp("\\S{1,16}", "g");
                            let ma = val.name.match(reg);
                            return val.marker + ma.join("<br/>");
                        }
                    },
                    grid: {
                        top: "5%",
                        bottom: "15%",
                        // containLabel: true
                        x: 120
                    },
                    xAxis: {
                        axisLabel: {
                            textStyle: {
                                color: "#8c8c8c"
                            },
                            formatter: function (value) {
                                if (value >= 1000000) {
                                    return `${value / 1000000}M`
                                }
                                console.log(value);
                                if (value >= 10000) {
                                    return `${value / 10000}W`
                                }
                                return `${value}`
                            }
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        }
                    },
                    yAxis: {
                        axisLabel: {
                            formatter: function (val) {
                                if (val.length > 8) {
                                    return val.substring(0, 8) + "...";
                                } else {
                                    return val;
                                }
                            },
                            textStyle: {
                                color: "#8c8c8c"
                            }
                        },

                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                        data: e.subscribeApiBottom
                    },
                    series: [
                        {
                            type: "bar",

                            barWidth: 15,
                            label: {
                                show: true,
                                position: "insideLeft",
                                color: '#333333'
                            },
                            itemStyle: {
                                color: "#42CE78"
                            },
                            data: e.subscribeApiBottomCount
                        }
                    ]
                });
            },
            apiVisitTop(e, name) {
                // 基于准备好的dom，初始化echarts实例
                let myChart = echarts.init(
                    document.getElementById(`apiVisitTop${name || ""}`)
                );
                // 绘制图表
                myChart.setOption({
                    tooltip: {
                        trigger: "item",
                        position: "bottom",
                        formatter: function (val) {
                            let reg = new RegExp("\\S{1,16}", "g");
                            let ma = val.name.match(reg);
                            return val.marker + ma.join("<br/>");
                        }
                    },
                    grid: {
                        top: "5%",
                        bottom: "15%",
                        // containLabel: true
                        x: 120
                    },
                    xAxis: {
                        axisLabel: {
                            textStyle: {
                                color: "#8c8c8c"
                            },
                            formatter: function (value) {
                                if (value >= 1000000) {
                                    return `${value / 1000000}M`
                                }
                                console.log(value);
                                if (value >= 10000) {
                                    return `${value / 10000}W`
                                }
                                return `${value}`
                            }
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        }
                    },
                    yAxis: {
                        axisLabel: {
                            formatter: function (val) {
                                if (val.length > 8) {
                                    return val.substring(0, 8) + "...";
                                } else {
                                    return val;
                                }
                            },
                            textStyle: {
                                color: "#8c8c8c"
                            }
                        },

                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                        data: e.totalInvokeApi
                    },
                    series: [
                        {
                            type: "bar",

                            barWidth: 15,
                            label: {
                                show: true,
                                position: "insideLeft",
                                color: '#333333'
                            },
                            itemStyle: {
                                color: "#7E70CA"
                            },
                            data: e.totalInvokeApiCount
                        }
                    ]
                });
            },
            apiVisitBottom(e, name) {
                // 基于准备好的dom，初始化echarts实例
                let myChart = echarts.init(
                    document.getElementById(`apiVisitBottom${name || ""}`)
                );
                // 绘制图表
                myChart.setOption({
                    tooltip: {
                        trigger: "item",
                        position: "bottom",
                        formatter: function (val) {
                            let reg = new RegExp("\\S{1,16}", "g");
                            let ma = val.name.match(reg);
                            return val.marker + ma.join("<br/>");
                        }
                    },
                    grid: {
                        top: "5%",
                        bottom: "15%",
                        // containLabel: true
                        x: 120
                    },
                    xAxis: {
                        axisLabel: {
                            textStyle: {
                                color: "#8c8c8c"
                            },
                            formatter: function (value) {
                                if (value >= 1000000) {
                                    return `${value / 1000000}M`
                                }
                                console.log(value);
                                if (value >= 10000) {
                                    return `${value / 10000}W`
                                }
                                return `${value}`
                            }
                        },
                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        }
                    },
                    yAxis: {
                        axisLabel: {
                            formatter: function (val) {
                                if (val.length > 8) {
                                    return val.substring(0, 8) + "...";
                                } else {
                                    return val;
                                }
                            },
                            textStyle: {
                                color: "#8c8c8c"
                            }
                        },

                        axisTick: {
                            show: false
                        },
                        axisLine: {
                            show: false
                        },
                        data: e.todayInvokeApi
                    },
                    series: [
                        {
                            type: "bar",
                            label: {
                                show: true,
                                position: "insideLeft",
                                color: '#333333'
                            },
                            itemStyle: {
                                color: "#7E70CA"
                            },
                            barWidth: 15,
                            data: e.todayInvokeApiCount
                        }
                    ]
                });
            },
            handleOk() {
                this.$refs.form.validate(valid => {
                    if (!valid) {
                        return;
                    }
                    let arr = [];
                    this.form.person.forEach(item => {
                        arr.push(item.uid);
                    });
                    let arr1 = [];
                    this.form.person1.forEach(item => {
                        arr1.push(item.uid);
                    });
                    let arr2 = [];
                    this.form.person2.forEach(item => {
                        arr2.push(item.uid);
                    });
                    this.confirmLoading = true;
                    if (this.text === "添加系统") {
                        request(this.url + "/createSystem", {
                            method: "POST",
                            body: {
                                apiAdminName: arr.join(","),
                                id: this.form.name,
                                apiDevName: arr1.join(","),
                                apiTenantName: arr2.join(","),
                                name: this.systemForm
                            }
                        })
                            .then(res => {
                                this.confirmLoading = false;
                                if (typeof res === "boolean" && res) {
                                    this.handleCancel();
                                    window.vm.$emit("fetchData");
                                }
                            })
                            .catch(e => {
                                this.confirmLoading = false;
                            });
                    } else {
                        request(this.url3, {
                            method: "POST",
                            body: {
                                apiAdminName: arr.join(","),
                                id: this.form.name,
                                apiDevName: arr1.join(","),
                                apiTenantName: arr2.join(","),
                                name: this.systemForm
                            }
                        })
                            .then(res => {
                                this.confirmLoading = false;
                                if (typeof res === "boolean" && res) {
                                    this.handleCancel();
                                    window.vm.$emit("fetchData");
                                }
                            })
                            .catch(e => {
                                this.confirmLoading = false;
                            });
                    }
                });
            },
            handleCancel() {
                this.$refs.form.clearValidate();
                this.form.name = undefined;
                this.form.person = [];
                this.form.person1 = [];
                this.form.person2 = [];
                this.visible = false;
                this.systemForm = null;
                this.seearchName = null;
                this.seearchName1 = null;
                this.seearchName2 = null;
                this.systemName = null;
                this.personList = [];
                this.personList1 = [];
                this.personList2 = [];
                this.systemList = [];
            },
            onSearch(num) {
                if (!num) {
                    this.searchLoading = true;
                    if (this.text === "添加系统") {
                        request(
                            this.url + "/systemInfos?systemName=" + this.systemName || null,
                            {
                                method: "GET"
                            }
                        ).then(res => {
                            this.searchLoading = false;
                            this.noData = true;
                            if (!res.length) {

                            } else {
                                this.systemName = null;
                            }
                            this.systemList = res;
                        });
                    } else {
                        this.systemList = [];
                        if (this.systemName) {
                            for (let i = 0; i < this.searchList.length; i++) {
                                if (this.searchList[i].name.indexOf(this.systemName) > -1) {
                                    this.systemList.push(this.searchList[i]);
                                }
                            }
                        } else {
                            this.systemList = this.searchList;
                        }
                        this.searchLoading = false;
                    }
                } else if (num === 1) {
                    if (!this.seearchName) {
                        this.$message.error("请输入要搜索的内容");
                        return;
                    }
                    this.searchLoading1 = true;
                    request("/message/api/groupInfos/getUserFromLdap/" + this.seearchName, {
                        method: "GET"
                    }).then(res => {
                        this.searchLoading1 = false;
                        if (!res.length) {
                            this.noData = true;
                        } else {
                            this.seearchName = null;
                        }
                        this.searchLoading = false;
                        this.personList = res;
                    });
                } else if (num === 2) {
                    if (!this.seearchName1) {
                        this.$message.error("请输入要搜索的内容");
                        return;
                    }
                    this.searchLoading2 = true;
                    request(
                        "/message/api/groupInfos/getUserFromLdap/" + this.seearchName1,
                        {
                            method: "GET"
                        }
                    ).then(res => {
                        this.searchLoading2 = false;
                        if (!res.length) {
                            this.$notification.warn({
                                message: "通知",
                                description: "暂无数据"
                            });
                        } else {
                            this.seearchName1 = null;
                        }
                        this.searchLoading = false;
                        this.personList1 = res;
                    });
                } else if (num === 3) {
                    if (!this.seearchName2) {
                        this.$message.error("请输入要搜索的内容");
                        return;
                    }
                    this.searchLoading3 = true;
                    request(
                        "/message/api/groupInfos/getUserFromLdap/" + this.seearchName2,
                        {
                            method: "GET"
                        }
                    ).then(res => {
                        this.searchLoading3 = false;
                        if (!res.length) {
                            this.$notification.warn({
                                message: "通知",
                                description: "暂无数据"
                            });
                        } else {
                            this.seearchName2 = null;
                        }
                        this.searchLoading = false;
                        this.personList2 = res;
                    });
                }
            },
            delSystem(index) {
                this.form.person.splice(index, 1);
            },
            delSystem1(index) {
                this.form.person1.splice(index, 1);
            },
            delSystem2(index) {
                this.form.person2.splice(index, 1);
            },
            check(item, index) {
                for (let i = 0; i < this.form.person.length; i++) {
                    if (this.form.person[i].cn === item.cn) {
                        return;
                    }
                }
                this.form.person.push(item);
                this.personList[index].active = true;
                this.personList = [];
            },
            check1(item, index) {
                for (let i = 0; i < this.form.person1.length; i++) {
                    if (this.form.person1[i].cn === item.cn) {
                        return;
                    }
                }
                this.form.person1.push(item);
                this.personList1[index].active = true;
                this.personList1 = [];
            },
            check2(item, index) {
                for (let i = 0; i < this.form.person2.length; i++) {
                    if (this.form.person2[i].cn === item.cn) {
                        return;
                    }
                }
                this.form.person2.push(item);
                this.personList2[index].active = true;
                this.personList2 = [];
            },
            checkSystem(item) {
                this.noData=false;
                if (this.text === "添加系统") {
                    this.form.name = item.id;
                    this.systemForm = item.name;
                    this.systemList = [];
                } else {
                    this.form.name = item.id;
                    this.systemForm = item.name;
                    this.systemList = [];
                    for (let i = 0; i < this.list.length; i++) {
                        if (this.list[i].id === this.form.name) {
                            this.form.person = this.list[i].adminNames;
                            this.form.person1 = this.list[i].devNames;
                            this.form.person2 = this.list[i].tenantNames;
                            break;
                        }
                    }
                }
            },
            edit() {
                this.text = "编辑系统";
                let id = JSON.parse(localStorage.getItem("appNowCategory")).gateway[1].id;
                for (let i = 0; i < this.userSystemList.length; i++) {
                    if (this.userSystemList[i].id === id) {
                        this.systemForm = this.userSystemList[i].name;
                        this.form.name = this.userSystemList[i].id;
                        this.form.person = this.userSystemList[i].adminNames;
                        this.form.person1 = this.userSystemList[i].devNames;
                        this.form.person2 = this.userSystemList[i].tenantNames;
                        this.visible = true;
                        break;
                    }
                }
            },
            delSystemItem() {
                this.form.name = undefined;
                this.systemForm = null;
            },
            blur() {
                setTimeout(() => {
                    this.systemList = [];
                    this.personList = [];
                    this.personList1 = [];
                    this.personList2 = [];
                    this.noData=false;
                }, 200);
            },
            editShow(name, key) {
                if (key == 1) {
                    this.tableColumns = this.tableColumns_1;
                    this.tableData = this.topBoard.apiDetails;
                } else if (key == 2) {
                    this.tableColumns = this.tableColumns_2;
                    this.tableData = this.topBoard.apiSubscribedVOs;
                } else if (key == 3) {
                    this.tableColumns = this.tableColumns_1;
                    this.tableData = this.topBoard.apiDetails;
                } else if (key == 4) {
                    this.tableColumns = this.tableColumns_2;
                    this.tableData = this.topBoard.apiSubscribedVOs;
                }
                this.edits_title = name;
                this.edits_show = true;
            },
            routerPush() {
                window.vm.$router.push({
                    path: "/flowcenter/queryPersonWorkItemsWithBizInfo"
                });
            },
            limits(id) {
                this.isDeveloper = false;
                if (!this.getSystemList.length) {
                    request(this.url4, {
                        method: "GET"
                    }).then(res => {
                        this.getSystemList = res;
                        for (let i = 0; i < res.length; i++) {
                            if (res[i].id === id) {
                                this.isDeveloper = !res[i].developer;
                                break
                            }
                        }
                    });
                } else {
                    for (let i = 0; i < this.getSystemList.length; i++) {
                        if (this.getSystemList[i].id === id) {
                            this.isDeveloper = !this.getSystemList[i].developer;
                            break
                        }
                    }
                }
            },
            getReleaseTotal() {//待处理申请
                request("/api/hip-flowable/api/v1/process/queryPersonWorkItemsWithBizInfo?pageNum=1&pageSize=1", {
                    method: "GET"
                }).then(res => {
                    this.releaseTotal = res.total;
                })
            },
            inForm() {
                this.loadingTo=true;
                request("/apimgr/api/v1/createSystem/sendEmail/type/0?systemName="+this.systemName, {
                    methods: "GET"
                }).then(()=> {
                    this.loadingTo=false;
                })
            }
        },
        activated() {
            this.number = "1"
            this.userSystemList = [];
            this.url = `/apimgr/api/v1/tenant/tenant_id_1/project/${this.$store.state.appNowCategory.gateway[1].id}/${this.$store.state.appNowCategory.gateway[0].key}`;
            this.url2 = `/apimgr/api/v1/userSystemInfos/${this.$store.state.appNowCategory.gateway[0].key}`;
            this.url3 = `/apimgr/api/v1/editUserSystemInfos/${this.$store.state.appNowCategory.gateway[0].key}`;
            this.url4 = `/apimgr/api/v1/isDeveloper/${this.$store.state.appNowCategory.gateway[0].key}`;
            this.initData(this.initDataIndex);
            if (this.$store.state.appNowCategory.gateway[1]) {
                this.flag = true
            } else {
                this.flag = false
            }
            this.limits(this.$store.state.appNowCategory.gateway[1].id);
            request(this.url2, {
                method: "GET"
            }).then(res => {
                this.userSystemList = res;
            })
        },
        created() {
            // return;
            if (window.vm.$route.name !== "gateway_indexPage" && process.env.NODE_ENV !== "development") {
                return
            }
            this.getReleaseTotal();
            if (localStorage.getItem("appNowCategory")) {
                let data = JSON.parse(localStorage.getItem("appNowCategory")).gateway;
                if (data[1]) {
                    this.flag = true
                } else {
                    this.flag = false
                }
                this.url = `/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`;
                this.url2 = `/apimgr/api/v1/userSystemInfos/${data[0].key}`;
                this.url3 = `/apimgr/api/v1/editUserSystemInfos/${data[0].key}`;
                this.url4 = `/apimgr/api/v1/isDeveloper/${data[0].key}`;
                this.limits(data[1].id);
            } else {
                this.url = "/apimgr/api/v1/tenant/tenant_id_1/project/3/staging";
                this.url2 = `/apimgr/api/v1/userSystemInfos/staging`;
                this.url3 = `/apimgr/api/v1/editUserSystemInfos/staging`;
                this.url4 = `/apimgr/api/v1/isDeveloper/staging`;
                this.limits(3);
            }
            request(this.url2, {
                method: "GET"
            }).then(res => {
                this.userSystemList = res
            })
        },
        watch: {
            "$store.state.appNowCategory": {
                deep: true,
                handler: function (oldValue, newValue) {
                    if (JSON.stringify(oldValue) === JSON.stringify(newValue)) {
                        return;
                    }
                    if (window.vm.$route.name !== "gateway_indexPage") {
                        return
                    }
                    this.userSystemList = [];
                    this.url = `/apimgr/api/v1/tenant/tenant_id_1/project/${this.$store.state.appNowCategory.gateway[1].id}/${this.$store.state.appNowCategory.gateway[0].key}`;
                    this.url2 = `/apimgr/api/v1/userSystemInfos/${this.$store.state.appNowCategory.gateway[0].key}`;
                    this.url3 = `/apimgr/api/v1/editUserSystemInfos/${this.$store.state.appNowCategory.gateway[0].key}`;
                    this.url4 = `/apimgr/api/v1/isDeveloper/${this.$store.state.appNowCategory.gateway[0].key}`;
                    this.initData(this.initDataIndex);
                    if (this.$store.state.appNowCategory.gateway[1]) {
                        this.flag = true
                    } else {
                        this.flag = false
                    }
                    if (newValue) {
                        if (oldValue.gateway[0].key !== newValue.gateway[0].key) {
                            this.getSystemList = [];
                        }
                        this.limits(this.$store.state.appNowCategory.gateway[1].id);
                    }
                    request(this.url2, {
                        method: "GET"
                    }).then(res => {
                        this.userSystemList = res;
                    })
                },
                immediate: true
            }
        }
    };
</script>
<style lang="less" scoped>
    .dashboard {
        width: 100%;
        height: 100%;
    }

    .ellipisis {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .dashboard .dash_button {
        background: #00aaa6;
        padding: 0 8px;
        border-radius: 2px;
        width: 108px;
    }

    .dashboard .dash_tab_item {
        background: #f0f2f5;
    }

    .dashboard .count_list {
        text-align: center;
        margin-bottom: 12px;
        background: #fff;
        height: 122px;

        .num {
            line-height: 40px;
            font-size: 28px;
            font-weight: 500;
            color: rgba(0, 0, 0, 0.85);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            margin: 24px 0 8px 0;
        }

        .num.hover {
            cursor: pointer;
        }

        .num.hover:hover {
            text-decoration: underline #00aaa6;
            color: #00aaa6;
        }

        .name {
            line-height: 20px;
            font-size: 14px;
            font-weight: 400;
            color: rgba(0, 0, 0, 0.35);
        }
    }

    .dashboard .dash_tab .title {
        height: 46px;
        font-size: 14px;
        line-height: 46px;
        text-align: left;
        font-weight: 400;
        color: rgba(0, 0, 0, 0.85);
        border-bottom: 1px solid #f0f0f0;
        padding-left: 16px;
        box-sizing: border-box;
        margin: 0;
    }

    .dashboard .dash_tab .title:after {
        content: ".";
        display: block;
        height: 0;
        clear: both;
        visibility: hidden;
    }

    .dashboard .dash_tab .title_icon {
        margin: 11px;
    }

    .dashboard .dash_tab .echarts_area_item {
        margin-bottom: 12px;
    }

    .dashboard .dash_tab .echarts_area_item > div {
        background: #fff;
    }

    .list_area > div {
        background: #fff;
    }

    .dashboard .dash_tab .list_area .title {
        padding-left: 10px;
    }

    .list_area .con {
        padding: 12px 16px;
        padding-bottom: 22px;
        box-sizing: border-box;
        height: 454px;
    }

    .list_area .con .con_item {
        height: 84px;
    }

    .list_area .con .con_item {
        color: rgba(0, 0, 0, 0.85);
        font-size: 14px;
        box-shadow: 0 1px 0 0 #f0f0f0;
    }

    .list_area .con .con_item .name {
        margin-bottom: 4px;
        padding-top: 16px;
        box-sizing: border-box;
    }

    .list_area .con .con_item .sub_head {
        color: rgba(0, 0, 0, 0.35);
    }
</style>
<style lang="less">
    .homePage_pree {
        background: #ffffff;
        height: calc(100vh - 120px);

        .img {
            display: block;
            margin: 60px auto 0;
        }

        .title {
            text-align: center;
            font-size: 26px;
            color: #00aaa6;
            margin: 30px auto;
        }

        .btn {
            display: block;
            margin: 18px auto;
        }

        .btn:nth-child(4) {
            width: 164px;
        }
    }
</style>
<style>
    ul,
    li {
        padding: 0;
        list-style: none;
        margin: 0;
    }

    .system_roules .searchSystem .systemList {
        position: relative;
    }

    .system_roules .searchSystem .systemList > ul {
        position: absolute;
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

    .homePage_pree .dash_tab {
        font-family: PingFangSC-Regular, PingFang SC, "Microsoft YaHei";
    }

    .homePage_pree .dash_tab .ant-tabs-tab {
        padding: 0 !important;
        margin: 12px;
        margin-left: 24px;
        line-height: 22px;
    }

    .sysyemCon > .checkoutSystem > span {
        margin-right: 0;
        padding-right: 0;
    }

    .checkoutSystem > span > i > svg {
        color: #00aaa6;
    }
</style>

