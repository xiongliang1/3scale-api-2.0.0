<template>
    <div class="subscribe">
        <header class="clearfix">
<!--            <a-button @click="btnCancel">取消订阅</a-button>-->
<!--            <a-button @click="getData">刷新</a-button>-->
            <!--      <span class="title"><a-icon type="info-circle"/>你一共订阅了 {{pagination.total}} 个API，截止到加载页面事合计调用了 345 次</span>-->
            <div class="search">
                <span style="padding-right: 4px">API名称</span>
                <a-input v-model.trim="name" v-show="select==='1'"/>
                &nbsp;&nbsp;
                <a-button type="primary" @click="getData">
                    查询
                </a-button>
                &nbsp;&nbsp;
                <a-button @click="name=null;getData()">重置</a-button>
            </div>
        </header>
        <div class="body">
            <a-table
                    :columns="columns"
                    :data-source="data"
                    :pagination="pagination"
                    :rowKey="row=>row.id"
                    size="small"
                    @change="handleChange"
            >
                <div slot="operation" slot-scope="text,record" class="operation">
                    <span @click="jump(record)">详情</span>&nbsp;&nbsp;
                    <span @click="cancel(record.id)">取消订阅</span>
                </div>
            </a-table>
        </div>
    </div>
</template>

<script>
    import moment from "moment"
    import request from "../../utils/request.js"
    let categoryOne = [];
    let categoryTwo = [];
    const columns = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName',
            width: 180
        },
        {
            title: '一级类别',
            dataIndex: 'categoryOneName',
            key: 'categoryOneName',
            filters:categoryOne
        },
        {
            title: '二级类别',
            dataIndex: 'categoryTwoName',
            key: 'categoryTwoName',
            filters: categoryTwo
        },
        {
            title: '发布环境',
            dataIndex: 'partition',
            key: 'partition',
            scopedSlots: {customRender: 'partition'},
            filters: [
                { text: '内网', value: '0' },
                { text: '外网', value: '1' },
            ],
            filterMultiple: false
        },
        {
            title: '最新发布时间',
            dataIndex: 'lastestPublishTime',
            key: 'lastestPublishTime',
            sorter: true,
            width: 180
        },
        {
            title: '调用次数',
            dataIndex: 'calledCount',
            key: 'calledCount',
            width: 80
        },
        {
            title: '最近调用时间',
            dataIndex: 'lastestCalledTime',
            key: 'lastestCalledTime',
            sorter: true,
            width: 160
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'},
            width: 130
        }
    ];
    export default {
        name: "subscribe",
        data() {
            return {
                select: '1',
                name: null,
                list:[],
                columns,
                data:[],
                pagination: {//分页配置
                    total: 0,
                    current:1,
                    defaultCurrent:1,
                    showSizeChanger:true,
                    defaultPageSize:10,
                    pageSize:10,
                    pageSizeOptions:['10', '20', '30', '40'],
                    showQuickJumper:true,
                    hideOnSinglePage:false,
                    showTotal:((total) => {
                        return `共 ${total} 条`;
                    }),
                },
                selectedRowKeys:[],
                searchData:{
                    pageSize:10,
                    categoryOne:[],
                    categoryTwo:[],
                    partition:null,
                    updateTime:null,
                    callTime:null
                },
                url: null
            }
        },
        methods: {
            moment,
            category(){//获取一级类别和二级类别的数据
                request(this.url+"/dataItems/categoryOne/findDataItems",{
                    method:"GET"
                }).then(res=>{
                    res.forEach(item=>{
                        categoryOne.push({
                            text:item.itemName,
                            value:item.id+""
                        })
                    })
                })
                request(this.url+"/dataItems/categoryTwo/findDataItems",{
                    method:"GET"
                }).then(res=>{
                    res.forEach(item=>{
                        categoryTwo.push({
                            text:item.itemName,
                            value:item.id+""
                        })
                    })
                })
            },
            cancel(id){
                let that=this;
                this.$confirm({
                    title: '提示',
                    content: '确认取消订阅该条API吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk:()=>{
                        request(this.url+"/subscribedApi/unSubscribeApi",{
                            method:"POST",
                            body:{
                                ids:[id]
                            }
                        }).then(res=>{
                            this.getData();
                        })
                    }
                });
            },
            onSelectChange(selectedRowKeys) {
                this.selectedRowKeys = selectedRowKeys;
            },
            getData(){
                request(this.url+"/subscribedApi",{
                    method:"POST",
                    body:{
                        page:this.pagination.current,
                        size:this.searchData.pageSize,
                        name:this.name||null,
                        categoryOnes:this.searchData.categoryOne.length?this.searchData.categoryOne:null,
                        categoryTwos:this.searchData.categoryTwo.length?this.searchData.categoryTwo:null,
                        partitions:this.searchData.partition?[this.searchData.partition]:null,
                        publishTimeSort:this.searchData.updateTime?(this.searchData.updateTime==="ascend"?"a":"d"):null,
                        callTimeSort:this.searchData.callTime?(this.searchData.callTime==="ascend"?"a":"d"):null
                    }
                }).then(res=>{
                    console.log(res)
                    this.data=res.content;
                    this.pagination.total=res.totalElements;
                })
            },
            btnCancel(){
                if (!this.selectedRowKeys.length){
                    return
                }
                let that=this;
                this.$confirm({
                    title: '提示',
                    content: '确认取消订阅选中的API吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk:()=>{
                        request(this.url+"/subscribedApi/unSubscribeApi",{
                            method:"POST",
                            body:{
                                ids:that.selectedRowKeys
                            }
                        }).then(res=>{
                            that.getData();
                        })
                    }
                });
            },
            handleChange(pagination, filters, sorter){
                let json={
                    pageSize:pagination.pageSize,
                    categoryOne:filters.categoryOneName?filters.categoryOneName.map(Number):[],
                    categoryTwo:filters.categoryTwoName?filters.categoryTwoName.map(Number):[],
                    partition:filters.partition?filters.partition[0]:null,
                    updateTime:sorter.columnKey==="lastestPublishTime"&&sorter.order?sorter.order:null,
                    callTime:sorter.columnKey==="lastestCalledTime"&&sorter.order?sorter.order:null
                }
                if (
                    this.searchData.pageSize === json.pageSize &&
                    JSON.stringify(this.searchData.categoryOne)===JSON.stringify(json.categoryOne)&&
                    JSON.stringify(this.searchData.categoryTwo)===JSON.stringify(json.categoryTwo)&&
                    this.searchData.partition===json.partition&&
                    this.searchData.updateTime===json.updateTime&&
                    this.searchData.callTime===json.callTime
                ){
                    this.pagination.current = pagination.current;
                    this.getData();
                }else {
                    this.pagination.current = 1;
                    this.pagination.pageSize = json.pageSize;
                    this.searchData={
                        pageSize: json.pageSize,
                        categoryOne: json.categoryOne,
                        categoryTwo: json.categoryTwo,
                        partition: json.partition,
                        updateTime: json.updateTime,
                        callTime: json.callTime
                    }
                    this.getData();
                }
            },
            jump(record){
                window.vm.$router.push({
                    path:'/gateway/apiDetails',
                    query:{type:1,id:record.apiId,partition:record.partition==='内网'?0:1,bool:1,userKey:record.userKey}
                });
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getData();
            this.categoryOne=[];
            this.categoryTwo=[];
            this.category();
        },
        beforeDestroy() {
            categoryOne=[];
            categoryTwo=[];
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
                    if (window.vm.$route.name==='apiListsubscribe'){
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
    .subscribe {
        header {
            padding: 16px 16px 0;
            > button {
                float: left;
                margin-right: 8px;
            }

            .title {
                float: left;
                width: 34%;
                height: 32px;
                line-height: 32px;
                background: rgba(230, 247, 255, 1);
                border: 1px solid rgba(145, 213, 255, 1);
                color: rgba(0, 0, 0, 0.65);
                padding-left: 1.5%;
                overflow: hidden;
                text-overflow:ellipsis;
                white-space: nowrap;
                i {
                    padding-right: 8px;
                    color: #1890FF;
                }
            }

            .search {
                float: right;

                .ant-input {
                    width: 200px;
                }
            }
        }
        .body{
            background-color: #ffffff;
            padding: 16px;
            min-height: calc(~"100vh - 163px");
            .operation{
                color: #00AAA6;
                cursor: pointer;
            }
        }
    }
</style>
