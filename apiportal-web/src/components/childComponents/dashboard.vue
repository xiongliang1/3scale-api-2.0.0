<template>
    <div class="dashboard">
        <div class="header clearfix">
            <a-input-search placeholder="请输入你要查询的服务" style="width: 300px" v-model.trim="apiName" @search="getData"/>
        </div>
        <a-table
                :columns="columns"
                :data-source="list"
                :pagination="{hideOnSinglePage:true,pageSize:Infinity}"
                size="small"
                bordered
                :rowKey="row=>row.index"
                :loading="loading"
        >
            <div slot="apiInvokeFailDayount" slot-scope="text,record" class="error" @click="handJump(record)">
                {{text}}
            </div>
        </a-table>
        <a-pagination
                :show-total="total => `共${total}条`"
                size="small"
                v-model="current"
                :total="total"
                @change="onChange"
                @showSizeChange="onShowSizeChange"
                show-size-changer
                show-quick-jumper
                show-less-items/>
    </div>
</template>

<script>
    const columns = [
        {
            title: 'API名称',
            dataIndex: 'apiName'
        },
        {
            title: '订阅系统',
            dataIndex: 'appSystem'
        },
        {
            title: '发布系统',
            dataIndex: 'apiSystem'
        },
        {
            title: '今日调用量',
            dataIndex: 'apiInvokeDayCount'
        },
        {
            title: '今日成功数',
            dataIndex: 'apiInvokeSuccessDayount',
            scopedSlots: {customRender: 'apiInvokeSuccessDayount'}
        },
        {
            title: '今日失败数',
            dataIndex: 'apiInvokeFailDayount',
            scopedSlots: {customRender: 'apiInvokeFailDayount'}
        },
        {
            title: '总调用量',
            dataIndex: 'apiInvokeTotalCount'
        }
    ];
    export default {
        name: "dashboard",
        data() {
            return {
                columns,
                list: [],
                loading: false,
                apiName: null,
                current: 1,
                pageSize: 10,
                total: 0
            }
        },
        created() {
            this.getData()
        },
        methods: {
            handJump(record) {
                if (record.apiInvokeFailDayount) {
                    this.$router.push({path: "/principal/developerCenter/logs", query: {apiName: record.apiName}})
                }
            },
            getData() {
                this.loading=true;
                this.axios.post("/dashboard/apiMarket/overview",{
                    apiName:this.apiName,
                    page:this.current,
                    size:this.pageSize
                }).then(res => {
                    this.loading=false;
                    this.list = res.data.content;
                    this.total = res.data.totalElements;
                    this.loading = false;
                })
            },
            onChange(val) {
                this.current = val;
                this.getData();
            },
            onShowSizeChange(current, pageSize) {
                this.current = 1;
                this.pageSize = pageSize;
                this.getData();
            }
        }
    }
</script>
<style lang="less">
    .dashboard {
        .header {
            text-align: right;
            padding: 14px 24px;
        }

        .error {
            color: red;
            cursor: pointer;

            &:hover {
                text-decoration: underline;
            }
        }

        .ant-pagination {
            text-align: right;
            margin: 20px;
        }
    }
</style>
