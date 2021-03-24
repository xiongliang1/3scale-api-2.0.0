<template>
    <div class="operation">
        <header class="clearfix">
            <div>
                <a-input placeholder="请输入操作人" style="width: 200px" v-model.trim="name"></a-input>&nbsp;&nbsp;
                <a-input placeholder="请输入apiId" style="width: 200px" v-model.trim="apiId"></a-input>&nbsp;&nbsp;
                <a-range-picker
                        :placeholder="['开始时间','结束时间']"
                        :show-time="{
              defaultValue: [moment('00:00:00', 'HH:mm:ss'), moment('11:59:59', 'HH:mm:ss')],
            }"
                        format="YYYY-MM-DD HH:mm:ss"
                        @change="switchTime"
                        v-model="timer"
                />&nbsp;&nbsp;
                <a-button type="primary" @click="pagination.current=1;pagination.pageSize=10;getData()">&nbsp;&nbsp;
                    查询
                </a-button>&nbsp;&nbsp;
                <a-button @click="clear">
                    重置
                </a-button>
            </div>
        </header>
        <a-table :columns="columns" :data-source="data" size="small" :rowKey="row=>row.id" :loading="loading" :pagination="pagination" @change="handleTableChange">
            <span slot="type" slot-scope="text">{{ text | type }}</span>
        </a-table>
    </div>
</template>

<script>
    import request from "@/utils/request";
    import moment from "moment"

    const columns = [
        {
            title: '操作',
            dataIndex: 'apiName',
            key: 'apiName',
            width: 300
        },
        {
            title: '操作类型',
            dataIndex: 'type',
            key: 'type',
            scopedSlots: {customRender: 'type'},
        },
        {
            title: 'apiId',
            dataIndex: 'apiId',
            key: 'apiId'
        },
        {
            title: '操作人',
            dataIndex: 'creator',
            key: 'creator'
        },
        {
            title: '操作时间',
            dataIndex: 'createTime',
            key: 'createTime'
        }
    ];
    export default {
        data() {
            return {
                name: null,
                apiId: null,
                timer: ["", ""],
                url: null,
                data: [],
                columns,
                loading: false,
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
            };
        },
        filters: {
            type(num) {
                if (!num) {
                    return "创建"
                } else if (num === 1) {
                    return "发布"
                } else if (num === 2) {
                    return "修改"
                } else if (num === 3) {
                    return "删除"
                } else if (num === 4) {
                    return "下线"
                } else if (num === 5) {
                    return "取消订阅"
                } else if (num === 6) {
                    return "一键发生产"
                } else {
                    return "其他"
                }
            }
        },
        methods: {
            moment,
            switchTime(date, dateString) {//时间插件
                this.timer = dateString;
            },
            clear() {
                this.name = null;
                this.apiId = null;
                this.timer = ["", ""];
                this.pagination.current=1;
                this.pagination.pageSize=10;
                this.getData();
            },
            getData() {
                let timer = {
                    start: this.timer[0],
                    end: this.timer[1]
                }
                this.loading = true;
                request(this.url + "/operationApi/operation", {
                    method: "POST",
                    body: {
                        timeQuery: timer.start[0] ? timer : null,
                        creator: this.name || null,
                        apiId: this.apiId || null,
                        pageNum: this.pagination.current,
                        pageSize: this.pagination.pageSize,
                        sort: [
                            "d",
                            "createTime"
                        ]
                    }
                }).then(res => {
                    this.loading = false;
                    this.data = res.content;
                    this.pagination.total=res.totalElements;
                })
            },
            handleTableChange(pagination){
                this.pagination.current=pagination.current;
                this.pagination.pageSize=pagination.pageSize;
                this.getData();
            }
        },
        created() {
            let data = JSON.parse(localStorage.getItem("appNowCategory")).gateway;
            this.url = `/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`;
            this.getData();
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
                    if (window.vm.$route.name==='operation'){
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.getData();
                    }
                }
            }
        },
    };
</script>
<style lang="less" scoped>
    .operation {
        header {
            > div {
                float: right;
                margin: 16px;
            }
        }

        .ant-table-wrapper {
            padding: 0 16px 16px;
        }
    }
</style>
