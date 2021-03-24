<template>
    <div class="logs">
        <header class="clearfix">
            <a-radio-group v-model="value" @change="getData()">
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
            &nbsp;&nbsp;
            <a-button type="primary" @click="exports">导出</a-button>
            <a-input-search placeholder="请输入返回参数" style="width: 200px;margin-left: 10px" v-model="responseBody" @search="getData"/>
            <a-input-search placeholder="请输入请求体" style="width: 200px;margin-left: 10px" v-model="requestBody" @search="getData"/>
            <a-input-search placeholder="请输入你要查询的服务" style="width: 200px;margin-left: 10px" v-model="apiName" @search="getData"/>
            <a-range-picker
                    :default-value="timer"
                    format="YYYY-MM-DD HH:mm:ss"
                    style="float: right;width: 320px;margin-top: 10px"
                    :allowClear="false"
                    @change="switchTime"
            />
        </header>
        <div class="body">
            <a-table
                    :columns="columns"
                    :data-source="data"
                    :pagination="{total:0,hideOnSinglePage:true,pageSize:pagination.pageSize}"
                    :loading="loading"
                    @change="handleTableChange"
                    size="small"
            >
                <div slot="requestParam" slot-scope="text,record">
                    <a-icon type="appstore" @click="show(0,record)"/>
                </div>
                <div slot="responseBody" slot-scope="text,record">
                    <a-icon type="appstore" @click="show(1,record)"/>
                </div>
                <div slot="httpStatusCode" slot-scope="text">
                    <span style="width: 4px;height: 4px;display: inline-block;background: #87d068;border-radius: 5px;position: relative;top: -2px" v-show="text[0]==='2'||text[0]==='3'"></span>
                    <span style="width: 4px;height: 4px;display: inline-block;background: #f50;border-radius: 5px;position: relative;top: -2px" v-show="text[0]==='4'||text[0]==='5'"></span>
                    {{text}}
                </div>
                <div slot="operation" slot-scope="text,record" class="operation">
                    <span @click="resend(record)" style="cursor: pointer;color: #00AAA6">重发</span>
                </div>
            </a-table>
        </div>
        <a-pagination
                v-model="pagination.current"
                :total="pagination.total"
                @change="onChange"
                @showSizeChange="onShowSizeChange"
                show-size-changer
                show-quick-jumper
                show-less-items
                :show-total="total => `共${total}条`"
                size="small"/>

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
    import moment from "moment"

    const columns = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName',
            width: 180
        },
        {
            title: '请求时间',
            dataIndex: 'requestTime',
            key: 'requestTime',
            // sorter: true,
            width: 180
        },
        {
            title: '请求方式',
            dataIndex: 'httpMethod',
            key: 'httpMethod'
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
            key: 'apiSystem',
            ellipsis: true
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
            key: 'responseTime'
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
    export default {
        name: "logs",
        data() {
            return {
                moment,
                value: '0',
                columns,
                data: [],
                loading: false,
                apiMame: null,
                pagination: {//分页信息
                    total: 0,
                    current: 1,
                    defaultCurrent: 1,
                    showSizeChanger: true,
                    defaultPageSize: 10,
                    pageSize: 10,
                    pageSizeOptions: ['10', '20', '30', '40'],
                    showQuickJumper: true
                },
                timer: ["", ""],
                sorter: ["d", "startTime"],
                response:false,
                content:{},
                text:"输入参数",
                apiName:null,
                requestBody:null,
                responseBody:null,
            }
        },
        methods: {
            resend(item) {//重发
                let that = this;
                this.$confirm({
                    title: '确定要重新发起该请求吗？',
                    okText: '确定',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk() {
                        that.axios.post("/apiInvokeLog/recall", {
                            hashId: item.hashId,
                            requestTime: item.requestTime + "",
                            apiId: item.apiId
                        }).then(res => {
                            if (res.code === "1") {
                                that.$notification.warning({
                                    message: '通知',
                                    description: res.msg
                                })
                            } else {
                                that.$notification.success({
                                    message: '通知',
                                    description: res.msg
                                })
                            }
                        })
                    }
                });
            },
            getData() {
                let time = {
                    end: moment(this.timer[1]).format("YYYY-MM-DD HH:mm:ss"),
                    start: moment(this.timer[0]).format("YYYY-MM-DD HH:mm:ss")
                }
                this.loading = true;
                this.axios.post("/apiInvokeLog/queryForPage", {
                    apiName: this.apiName || null,
                    pageNum: this.pagination.current,
                    pageSize: this.pagination.pageSize,
                    sort: this.sorter,
                    statType: this.value - 0,
                    timeQuery: time,
                    requestBody:this.requestBody || null,
                    responseBody:this.responseBody || null
                }).then(res => {
                    this.loading = false;
                    if (res.code === "1") {
                        this.pagination.total = 0;
                        this.data = [];
                    } else {
                        this.data = res.data.content;
                        this.data.forEach((item, index) => {
                            item.index = index
                        })
                        this.pagination.total = res.data.totalElements;
                    }
                })
            },
            switchTime(date, dateString) {
                this.timer = dateString;
                this.getData();
            },
            onChange(val) {
                this.pagination.current = val;
                this.getData();
            },
            handleTableChange(pagination, filters, sorter) {
                if (sorter.order === "ascend") {
                    this.sorter = ["a", "startTime"];
                } else {
                    this.sorter = ["d", "startTime"];
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
            },
            onShowSizeChange(current, pageSize){
                this.pagination.current=1;
                this.pagination.pageSize=pageSize;
                this.getData();
            },
            exports(){
                let timer={
                    start:moment(this.timer[0]).format("YYYY-MM-DD HH:mm:ss"),
                    end:moment(this.timer[1]).format("YYYY-MM-DD HH:mm:ss")
                }
                this.axios({
                    url: "/apiInvokeLog/download",
                    method: "post",
                    data:{
                        apiName:this.name,
                        pageNum:1,
                        pageSize:this.pagination.total || 1,
                        statType:this.value-0,
                        timeQuery:timer,
                        sort:this.sorter
                    },
                    responseType: "blob"
                }).then(res=>{
                    let blob = new Blob([res]);
                    let link= document.createElement('a');
                    link.download = '日志.xlsx';
                    link.href = URL.createObjectURL(blob);
                    link.click();
                })
            },
        },
        created() {
            let time = [];
            if (!this.$route.query.apiName){
                time[0] = moment(Date.parse(new Date()) - 30 * 60 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss');
                time[1] = moment(Date.parse(new Date())).format('YYYY-MM-DD HH:mm:ss');
                this.timer = [moment(time[0], 'YYYY-MM-DD HH:mm:ss'), moment(time[1], 'YYYY-MM-DD HH:mm:ss')];
                this.getData();
            }else {
                this.apiName=this.$route.query.apiName;
                this.value="2";
                time[0] = moment(Date.parse(new Date())).format('YYYY-MM-DD')+" 00:00:00";
                time[1] = moment(Date.parse(new Date())).format('YYYY-MM-DD HH:mm:ss');
                this.timer = [moment(time[0], 'YYYY-MM-DD HH:mm:ss'), moment(time[1], 'YYYY-MM-DD HH:mm:ss')];
                this.getData();
            }
        }
    }
</script>

<style lang="less">
    .logs {
        padding: 14px;
        overflow-y: auto;
        header {
            > span:nth-child(1) {
                float: left;
            }

            .ant-input-search {
                float: right;
            }
        }

        .body {
            margin-top: 20px;
            overflow-x: auto;
            .ant-table-wrapper {
                width: 1350px;
            }
        }

        .ant-pagination {
            text-align: right;
            margin: 20px;
        }
    }
</style>
