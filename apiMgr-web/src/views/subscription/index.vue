<template>
    <div class="subscription">
        <header>
            <a-radio-group v-model="radio" @change="selectedRowKeys=[];pagination.current=1;pagination.pageSize=10;getData()">
                <a-radio-button value="a">
                    待审批
                </a-radio-button>
                <a-radio-button value="b">
                    已审批
                </a-radio-button>
            </a-radio-group>
<!--            <a-button type="primary" @click="decision(1)" v-show="radio==='a'">通过</a-button>-->
<!--            <a-button @click="decision(0)" v-show="radio==='a'">拒绝</a-button>-->
<!--            <a-button type="primary" v-show="radio==='b'" @click="batch(1)">批量启用</a-button>-->
<!--            <a-button v-show="radio==='b'" @click="batch(0)">批量禁用</a-button>-->
            <div class="search">
                <a-select style="width: 120px" v-model="select">
                    <a-select-option value="1">
                        API名称
                    </a-select-option>
                    <a-select-option value="2">
                        申请时间
                    </a-select-option>
                </a-select><a-input v-model.trim="name" v-show="select==='1'"/><a-range-picker v-if="select==='2'"
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
                <a-button @click="reset">重置</a-button>
            </div>
        </header>
        <div class="body" v-show="radio==='a'">
            <a-table :columns="columns"
                     :data-source="list"
                     :pagination="pagination"
                     :loading="loading"
                     size="small"
                     :row-key="record => record.id"
                     :show-total="total => `共${pagination.total}条`"
                     @change="handleTableChange"
            >
                <div slot="status" slot-scope="text">
                    {{text | status}}
                </div>
                <div slot="operation" slot-scope="text,record" class="operation">
                    <span @click="view(record.id)">查看详情</span>
<!--                    <span @click="through(record.id)">通过</span><span @click="reject(record.id)">拒绝</span>-->
                </div>
            </a-table>
        </div>

        <div class="body" v-show="radio==='b'">
            <a-table :columns="columns1"
                     :data-source="list"
                     :pagination="pagination"
                     :loading="loading"
                     size="small"
                     :row-key="record => record.id"
                     @change="handleTableChange"
            >
                <div slot="status" slot-scope="text">
                    {{text | status}}
                </div>
                <div slot="state" slot-scope="text">
                    {{text?(text==="suspended"?"禁用":"启用"):""}}
                </div>
                <div slot="instructions" slot-scope="text">
                    <a-tooltip>
                        <template slot="title">
                            {{text}}
                        </template>
                        <a-icon type="file-text" class="ico" />
                    </a-tooltip>
                </div>
                <div slot="operation" slot-scope="text,record" class="operation">
                    <span @click="view(record.id)">查看详情</span>
<!--                    <span @click="enable(record.id)" v-show="record.state==='suspended'">启用</span>-->
<!--                    <span @click="disable(record.id)" v-show="record.state==='live'">禁用</span>-->
                </div>
            </a-table>
        </div>

        <a-modal
                :title="'审批'+(text?'通过':'拒绝')"
                :visible="visible"
                @ok="handleOk"
                @cancel="handleCancel"
                :confirm-loading="confirmLoading"
                cancelText="取消"
                okText="确认"
                wrapClassName="affirm"
        >
            <p>
                <a-icon type="exclamation-circle"/>
                <span>确定「{{text?'通过':'拒绝'}}」该订阅申请吗？</span>
            </p>
            <p>你进行补充说明，该说明将会呈现给订阅人：</p>
            <a-textarea plaeholder="请输入" v-model.trim="value"></a-textarea>
        </a-modal>
    </div>
</template>

<script>
    const columns = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName',
            width:150,
            ellipsis: true,
        },
        {
            title: '申请时间',
            dataIndex: 'createTime',
            key: 'createTime',
            sorter: true,
            width: 160
        },
        {
            title: '订阅系统',
            dataIndex: 'appSystemName',
            key: 'appSystemName',
            filters: [],
            ellipsis: true
        },
        {
            title: '订阅人',
            dataIndex: 'creator',
            key: 'creator',
            filters: []
        },
        // {
        //     title: '审批状态',
        //     dataIndex: 'status',
        //     key: 'status',
        //     scopedSlots: {customRender: 'status'}
        // },
        {
            title: '操作',
            dataIndex: 'operation',
            scopedSlots: {customRender: 'operation'}
        },
    ];


    const columns1 = [
        {
            title: 'API名称',
            dataIndex: 'apiName',
            key: 'apiName',
            width:180,
        },
        {
            title: '申请时间',
            dataIndex: 'createTime',
            key: 'createTime',
            sorter: true,
            width: 160
        },
        {
            title: '订阅系统',
            dataIndex: 'appSystemName',
            key: 'appSystemName',
            filters: [],
            ellipsis: true
        },
        {
            title: '订阅人',
            dataIndex: 'creator',
            key: 'creator',
            filters: []
        },
        // {
        //     title: '审批状态',
        //     dataIndex: 'status',
        //     key: 'status',
        //     filters: [
        //         { text: '通过', value: '2' },
        //         { text: '拒绝', value: '3' },
        //     ],
        //     scopedSlots: {customRender: 'status'}
        // },
        {
            title: '审批时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            sorter: true,
            width: 160
        },
        {
            title: '审批人',
            dataIndex: 'updater',
            key: 'updater',
            filters: []
        },
        // {
        //     title: '状态',
        //     dataIndex: 'state',
        //     key: 'state',
        //     filters: [
        //         { text: '启用', value: '1' },
        //         { text: '禁用', value: '0' },
        //     ],
        //     filterMultiple: false,
        //     scopedSlots: {customRender: 'state'}
        // },
        {
            title: '补充说明',
            dataIndex: 'remark',
            key: 'remark',
            scopedSlots: {customRender: 'instructions'}
        },
        {
            title: '操作',
            dataIndex: 'operation',
            scopedSlots: {customRender: 'operation'}
        }
    ];
    import moment from "moment"
    import request from "../../utils/request";
    export default {
        data(){
            return {
                columns,
                columns1,
                radio: 'a',
                select: '1',
                timer: ["", ""],
                name: null,
                list: [],
                visible: false,
                confirmLoading: false,
                text: null,
                loading: false,
                id:null,
                value:null,
                selectedRowKeys: [],
                pagination: {//分页配置
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
                searchData:{
                    a:{
                        system:[],
                        person:[],
                        time:undefined,
                        pageSize: 10
                    },
                    b:{
                        system:[],
                        person:[],
                        status:[],
                        review:[],
                        state:undefined,
                        time:undefined,
                        examine:undefined,
                        pageSize: 10
                    }
                },
                url:null
            }
        },
        filters: {
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
        methods: {
            moment,
            switchTime(date, dateString) {//时间插件
                this.timer = dateString;
            },
            view(id) {//查看详情
                // this.$router.push({path: '/subscriptionDetails', query: {id: id}})
                window.vm.$router.push({
                    path:"/gateway/subscriptionDetails",
                    query:{
                        id: id
                    }
                })
            },
            getData() {
                this.loading = true;
                if (this.radio === "a") {
                    request(this.url+"/processRecord/application/applyList",{
                        method:"POST",
                        body:{
                            page: this.pagination.current,
                            size: this.searchData.a.pageSize,
                            apiName: this.name||null,
                            apiSystem: this.searchData.a.system||null,
                            createIds: this.searchData.a.person||null,
                            startDate: this.timer[0]||null,
                            endDate: this.timer[1]||null,
                            sort:this.searchData.a.time==="descend"?["asc","createTime"]:["desc","createTime"]
                        }
                    }).then(res => {
                        this.loading = false;
                        this.list = res.content;
                        this.pagination.total = res.totalElements;
                    })
                } else if (this.radio === "b") {
                    request(this.url+"/applications/approvalComplete",{
                        method:"POST",
                        body: {
                            page: this.pagination.current,
                            size: this.searchData.b.pageSize,
                            apiName: this.name||null,
                            apiSystem: this.searchData.b.system,
                            createIds: this.searchData.b.person,
                            startDate: this.timer[0]||null,
                            endDate: this.timer[1]||null,
                            sort:this.filterTime(this.searchData.b.time,this.searchData.b.examine),
                            updateIds:this.searchData.b.review,
                            status:this.searchData.b.status,
                            state:this.state(this.searchData.b.state)
                        }
                    }).then(res=>{
                        this.loading = false;
                        this.list = res.content;
                        this.pagination.total = res.totalElements;
                    })
                }
            },
            filterTime(time,examine){
                if (!time&&!examine){
                    return ["desc","createTime"]
                }else if (!time&&examine){
                    if (examine==='descend'){
                        return ["asc","updateTime"]
                    }else{
                        return ["desc","updateTime"]
                    }
                }else if (time&&!examine){
                    if (time==='descend'){
                        return ["asc","createTime"]
                    }else{
                        return ["desc","createTime"]
                    }
                }
            },
            state(num){
                if (typeof num==='undefined'){
                    return null
                }else{
                    return num-0?["suspended"]:["live"]
                }
            },
            through(id){//通过
                this.visible=true;
                this.id=id;
                this.text=1;
            },
            reject(id){//拒绝
                this.visible=true;
                this.id=id;
                this.text=0;
            },
            handleOk(){
                let arr=[];
                if (this.id){
                    arr.push({
                        id:this.id,
                        remark:this.value,
                        status:this.text
                    })
                }else{
                    this.selectedRowKeys.forEach(item=>{
                        arr.push({
                            id:item,
                            remark:this.value,
                            status:this.text
                        })
                    })
                }
                request(this.url+"/processRecord/application/approveList",{
                    method:"POST",
                    body:arr
                }).then(res=>{
                  this.visible = false;
                  this.value=null;
                  this.getData();
                })
            },
            handleCancel() {
                this.visible = false;
                this.value=null;
                this.id = null;
                this.text = null;
            },
            onSelectChange(selectedRowKeys) {
                this.selectedRowKeys = selectedRowKeys;
            },
            disable(id){//禁用
                request(this.url+"/applications/suspendList",{
                    method:"PUT",
                    body:[id]
                }).then(res=>{
                   this.visible = false;
                   this.value=null;
                   this.getData();
                })
            },
            enable(id){//启用
                request(this.url+"/applications/resumeList",{
                    method:"PUT",
                    body:[id]
                }).then(res=>{
                    this.visible = false;
                    this.value=null;
                    this.getData();
                })
            },
            handleTableChange(pagination, filters, sorter){
                console.log(filters)
                if (this.radio==="a"){
                    let json={
                        system:filters.appSystemName?filters.appSystemName.map(Number):[],
                        person:filters.createName?filters.createName.map(Number):[],
                        time:sorter.order,
                        pageSize: pagination.pageSize
                    };
                    if(
                        this.searchData.a.pageSize===json.pageSize&&
                        JSON.stringify(this.searchData.a.system)===JSON.stringify(json.system)&&
                        JSON.stringify(this.searchData.a.person)===JSON.stringify(json.person)&&
                        this.searchData.a.time===json.time
                    ){
                        this.pagination.current=pagination.current;
                        this.getData();
                    }else {
                        this.pagination.current=pagination.current;
                        this.searchData.a={
                            system:json.system,
                            person:json.person,
                            time:json.time,
                            pageSize: json.pageSize
                        }
                        this.getData();
                    }
                }else if (this.radio==="b"){
                    let json={
                        system:filters.appSystemName?filters.appSystemName.map(Number):[],
                        person:filters.createName?filters.createName.map(Number):[],
                        status:filters.status?filters.status.map(Number):[],
                        review:filters.updateName?filters.updateName.map(Number):[],
                        time:sorter.columnKey==="createTime"?sorter.order:undefined,
                        examine:sorter.columnKey==="updateTime"?sorter.order:undefined,
                        pageSize: pagination.pageSize,
                        // state:filters.state[0]
                    };
                    if (
                        this.searchData.b.pageSize===json.pageSize&&
                        JSON.stringify(this.system)===JSON.stringify(json.system)&&
                        JSON.stringify(this.person)===JSON.stringify(json.person)&&
                        JSON.stringify(this.status)===JSON.stringify(json.status)&&
                        JSON.stringify(this.review)===JSON.stringify(json.review)&&
                        this.searchData.b.time===json.time&&
                        this.searchData.b.examine===json.examine
                        // this.searchData.b.state===json.state
                    ){
                        this.pagination.current=pagination.current;
                        this.getData();
                    }else {
                        this.pagination.current=pagination.current;
                        this.searchData.b={
                            system:json.system,
                            person:json.person,
                            status:json.status,
                            review:json.review,
                            time:json.time,
                            examine:json.examine,
                            pageSize: json.pageSize,
                            // state: json.state
                        }
                        this.getData();
                    }
                }
            },
            decision(num){
                if (!this.selectedRowKeys.length){
                    this.$message.error("请勾选API")
                    return
                }
                this.visible=true;
                this.text=num;
            },
            batch(num){
                let url;
                if (num){
                    url='/applications/resumeList'
                }else {
                    url='/applications/suspendList'
                }
                this.axios.put(url,this.selectedRowKeys).then(res=>{
                    if(res.code==="1"){
                        this.$notification.success({
                            message: '通知',
                            description: num?"审批成功":"审批拒绝"
                        });
                        this.selectedRowKeys=[];
                        this.getData();
                    }
                })
                request(this.url+url,{
                    method:"PUT",
                    body:this.selectedRowKeys
                }).then(res=>{
                    if(res.code==="0"){
                        this.$notification.success({
                            message: '通知',
                            description: num?"审批成功":"审批拒绝"
                        });
                        this.selectedRowKeys=[];
                        this.getData();
                    }
                })
            },
            person(){
                request(this.url+"/processRecord/application/subscribers",{
                    method:"GET"
                }).then(res=>{
                    res.createUser.forEach(item=>{
                        if (!item){
                            return
                        }
                        this.columns[3].filters.push({
                            text: item.name,
                            value: item.id+""
                        })
                        this.columns1[3].filters.push({
                            text: item.name,
                            value: item.id+""
                        })
                    })
                    res.updateUser.forEach(item=>{
                        if (!item){
                            return
                        }
                        this.columns1[6].filters.push({
                            text: item.name,
                            value: item.id+""
                        })
                    })
                })
                request(this.url+"/dataItems/system/findDataItems",{
                    method:"GET"
                }).then(res=>{
                    res.forEach(item=>{
                        this.columns[2].filters.push({
                            text: item.itemName,
                            value: item.id+""
                        })
                        this.columns1[2].filters.push({
                            text: item.itemName,
                            value: item.id+""
                        })
                    })
                })
            },
            reset(){
                this.name=null;
                this.select="1";
                this.timer=["",""];
                this.getData();
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getData()
            this.person();
        },
        activated() {
            this.getData()
            this.person();
        },
        beforeDestroy() {
            this.columns[2].filters=[];
            this.columns1[2].filters=[];
            this.columns[3].filters=[];
            this.columns1[3].filters=[];
            this.columns1[6].filters=[];
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
                    if (window.vm.$route.name==='subscription'){
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.getData()
                    }
                }
            }
        },
    }
</script>

<style scoped lang="less">
    .subscription {
        header{
            padding: 16px 16px 0;
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
            .operation{
                span{
                    color: #00AAA6;
                    margin-right: 6px;
                    cursor: pointer;
                }
            }
            .ico:hover{
                color: #00aaa6;
            }
        }
    }
</style>
<style>
    .affirm .ant-modal-body p:nth-child(1) {
        font-size: 16px;
        color: #000000;
    }

    .affirm .ant-modal-body p:nth-child(1) i {
        background-color: #FAAD14;
        color: #ffffff;
        border-radius: 50%;
    }

    .affirm .ant-modal-body p:nth-child(2) {
        font-size: 12px;
        padding: 8px 0;
    }

    .affirm .ant-modal-body textarea {
        height: 100px;
    }
</style>

