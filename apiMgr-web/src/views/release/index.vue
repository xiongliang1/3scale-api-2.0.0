<template>
    <div class="release">
        <header class="clearfix">
            <a-radio-group v-model="radio" @change="pagination.current=1;pagination.pageSize=10;pagination1.current=1;pagination1.pageSize=10;getData()">
                <a-radio-button value="0">
                    待审批({{pagination.total}})
                </a-radio-button>
                <a-radio-button value="1">
                    已审批({{pagination1.total}})
                </a-radio-button>
            </a-radio-group>
            <div class="search">
                <a-select style="width: 120px" v-model="select">
                    <a-select-option value="1">
                        API名称
                    </a-select-option>
<!--                    <a-select-option value="2">-->
<!--                        创建时间-->
<!--                    </a-select-option>-->
                </a-select>
                <a-input v-model="name" v-show="select==='1'"/>
<!--                <a-range-picker v-show="select==='2'"-->
<!--                                :placeholder="['开始时间','结束时间']"-->
<!--                                :show-time="{-->
<!--              defaultValue: [moment('00:00:00', 'HH:mm:ss'), moment('11:59:59', 'HH:mm:ss')],-->
<!--            }"-->
<!--                                format="YYYY-MM-DD HH:mm:ss"-->
<!--                                @change="switchTime"-->
<!--                />-->
                &nbsp;&nbsp;
                <a-button type="primary" @click="getData">
                    查询
                </a-button>
                &nbsp;&nbsp;
                <a-button @click="name=null;getData()">重置</a-button>
            </div>
        </header>
        <div class="body" v-if="radio=='0'">
            <a-table
                    :columns="columns"
                    :data-source="list"
                    :pagination="pagination"
                    :loading="loading"
                    size="small"
                    @change="handleTableChange"
            >
<!--                <div slot="name" slot-scope="text,record" class="name"-->
<!--                     @click="$router.push({path:'/releaseDetails',query:{id:record.id}})">-->
<!--                    {{text}}-->
<!--                </div>-->
                <div slot="operation" slot-scope="text,record">
                    <a-button type="link" @click="show(record)" v-show="record.processInstID">
                        查看流程
                    </a-button>
                </div>
            </a-table>
        </div>
        <div class="body" v-if="radio=='1'">
            <a-table
                    :columns="columns1"
                    :data-source="list1"
                    :pagination="pagination1"
                    :loading="loading"
                    size="small"
                    @change="handleTableChange1"
            >
                <div slot="status" slot-scope="text,record">
                    <a-tag v-show="record.status" color="green" v-if="record.status===2">
                        {{text | status}}
                    </a-tag>
                    <a-tag v-show="record.status" color="red" v-if="record.status===3">
                        {{text | status}}
                    </a-tag>
                </div>
                <div slot="updateTime" slot-scope="text,record">
                    {{text||record.createTime}}
                </div>
            </a-table>
        </div>
    </div>
</template>

<script>
    import request from "@/utils/request";
    import moment from "moment"

    const columns = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName'
        },
        {
            title: '所属系统',
            dataIndex: 'apiSystemName',
            key: 'apiSystemName'
        },
        {
            title: '申请人',
            key: 'creator',
            // sorter: true,
            dataIndex: 'creator'
        },
        {
            title: '申请时间',
            key: 'createTime',
            // sorter: true,
            dataIndex: 'createTime'
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'}
        }
    ];
    const columns1 = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName'
        },
        {
            title: '所属系统',
            dataIndex: 'apiSystemName',
            key: 'apiSystemName'
        },
        {
            title: '申请人',
            key: 'creator',
            // sorter: true,
            dataIndex: 'creator'
        },
        {
            title: '申请时间',
            key: 'createTime',
            // sorter: true,
            dataIndex: 'createTime'
        },
        {
            title: '审批状态',
            key: 'status',
            dataIndex: 'status',
            scopedSlots: {customRender: 'status'},
            // filters: [
            //     {text: '通过', value: '0'},
            //     {text: '拒绝', value: '1'},
            // ],
            // filterMultiple: false
        },
        {
            title: '审批时间',
            key: 'updateTime',
            // sorter: true,
            dataIndex: 'updateTime',
            scopedSlots: {customRender: 'updateTime'},
        }
    ];
    export default {
        data() {
            return {
                radio: '0',
                select: '1',
                timer: ["", ""],
                name: null,
                data: [{name: '测试数据'}],
                list: [],
                list1: [],
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
                pagination1: {//分页配置
                    total: 0,
                    current: 1,
                    defaultCurrent: 1,
                    showSizeChanger: true,
                    defaultPageSize: 10,
                    pageSize: 10,
                    pageSizeOptions: ['10', '20', '30', '40'],
                    showQuickJumper: true
                },
                columns,
                columns1,
                loading: false,
                searchData: {
                    total: 0,
                    current: 1,
                    defaultCurrent: 1,
                    showSizeChanger: true,
                    defaultPageSize: 10,
                    pageSize: 10,
                    pageSizeOptions: ['10', '20', '30', '40'],
                    showQuickJumper: true,
                },
                url: null,
                visible: false
            };
        },
        filters:{
           status(num){
               // if (num===1){
               //     return "待审批"
               // }else if (num===4){
               //     return "审批通过"
               // }else if (num===5||num===6){
               //     return "审批拒绝"
               // }else {
               //     return ""
               // }
               if (num===0){
                   return "取消审批"
               }else if (num===1){
                   return "审批中"
               }else if (num===2){
                   return "审批通过"
               }else if (num===3){
                   return "审批拒绝"
               }
           }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getData();
        },
        methods: {
            moment,
            switchTime(date, dateString) {//时间插件
                this.timer = dateString;
            },
            getData() {
                request(this.url + "/processRecord/api/applyList?page="+this.pagination.current+"&size="+this.pagination.pageSize+"&name=" + this.name + "&isApproved=0", {
                    method: "GET"
                }).then(res => {
                    this.list = res.content;
                    this.pagination.total = res.totalElements;
                })
                request(this.url + "/processRecord/api/applyList?page="+this.pagination1.current+"&size="+this.pagination1.pageSize+"&name=" + this.name + "&isApproved=1", {
                    method: "GET"
                }).then(res => {
                    this.list1 = res.content;
                    this.pagination1.total = res.totalElements;
                })
            },
            handleTableChange(pagination, filters, sorter) {
               if (this.pagination.pageSize===pagination.pageSize){
                   this.pagination.current = pagination.current;
                   this.getData()
               }else{
                   this.pagination.current=1;
                   this.pagination.pageSize=pagination.pageSize;
                   this.getData();
               }
            },
            handleTableChange1(pagination, filters, sorter) {
                if (this.pagination1.pageSize===pagination.pageSize){
                    this.pagination1.current = pagination.current;
                    this.getData()
                }else{
                    this.pagination1.current=1;
                    this.pagination1.pageSize=pagination.pageSize;
                    this.getData();
                }
            },
            show(item) {
                window.vm.$router.push({
                  path:"/gateway/flowChart",
                  query:{id:item.processInstID}
                })
            }
        },
        activated() {
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
                    if (window.vm.$route.name==='release'){
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.pagination1.current=1;
                        this.pagination1.pageSize=10;
                        this.getData()
                    }
                }
            }
        },
    };
</script>
<style scoped lang="less">
    .release {
        header {
            padding: 16px 16px 0;
            .ant-radio-group {
                float: left;
            }

            .search {
                float: right;

                .ant-input {
                    width: 200px;
                }
            }
        }

        .body {
            background-color: #ffffff;
            padding: 16px;
            min-height: calc(~"100vh - 163px");

            .name {
                cursor: pointer;
                color: #00aaa6;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .name:hover {
                text-decoration: underline;
            }
        }
    }
</style>
