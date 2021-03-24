<template>
    <div class="grouping">
        <header class="clearfix">
            <div>
                <a-button type="primary" icon="plus" @click="text='新建分组';categoryRouter=false;form.one = undefined;form.two = undefined;form.note = null;form.name = null;form.system = undefined;categoryOneChildren = [];systemList = [];visible=true;">
                    新建分组
                </a-button>
            </div>
            <div class="search">
                <a-select style="width: 120px" v-model="select">
                    <a-select-option value="1">
                        组名称
                    </a-select-option>
                    <a-select-option value="2">
                        创建时间
                    </a-select-option>
                </a-select>
                <a-input v-model.trim="name" v-show="select==='1'"/>
                <a-range-picker v-show="select==='2'"
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

        <div class="body">
            <a-table
                    :columns="columns"
                    :data-source="list"
                    :rowKey="row=>row.id"
                    size="small"
                    :pagination="pagination"
                    @change="handleTableChange"
                    :loading="loading">
                <div slot="apiNum" slot-scope="text,record" class="apiName" @click="jump(record)">
                    {{text}}
                </div>
                <div slot="createTime" slot-scope="text,record">
                    {{record.createTime}}
                </div>
                <div slot="operation" slot-scope="text,record" style="color:#00aaa6;">
                    <span style="cursor: pointer;margin-right: 4px" v-show="!record.apiNum"
                          @click="modify(record)">修改</span>
                    <span style="color:#00aaa67a;cursor: not-allowed;margin-right: 4px" v-show="record.apiNum">修改</span>
                    <span style="cursor: pointer" v-show="!record.apiNum" @click="del(record.id)">删除</span>
                    <span style="color:#00aaa67a;cursor: not-allowed;" v-show="record.apiNum">删除</span>
                </div>
            </a-table>
        </div>

        <a-modal :title="text" :visible="visible" :confirm-loading="confirmLoading"
                 okText="确认" cancelText="取消" width="600px" @ok="handleSubmit" @cancel="handleCancel"
                 wrapClassName="groupModal">
            <a-form-model :model="form" ref="form" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }" :rules="rules">
                <a-form-model-item label="组名称" prop="name">
                    <a-input placeholder="请输入" v-model="form.name"/>
                </a-form-model-item>
                <a-form-model-item label="描述" prop="note">
                    <a-textarea placeholder="请输入" v-model="form.note"/>
                </a-form-model-item>
                <a-form-model-item label="所属类别" class="category" prop="two" ref="two" :autoLink="false">
                    <a-select placeholder="请选择" style="width: 220px;margin-right:20px" v-model="form.one"
                              @change="handleChange">
                        <a-select-option v-for="item in categoryOne" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                    <a-select placeholder="请选择" style="width: 220px;" v-model="form.two" @change="handleChangeTwo">
                        <a-select-option v-for="item in categoryOneChildren" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                    <div class="ant-form-explain" style="color: #f5222d" v-show="categoryRouter">请选择二级类目</div>
                </a-form-model-item>
                <a-form-model-item label="所属系统" prop="system">
                    <a-select placeholder="请选择" v-model="form.system">
                        <a-select-option v-for="item in systemList" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal v-model="success" :title="null" :footer="null" :keyboard="false" :closable="false"
                 wrapClassName="createdSuccGroup">
            <p>
                <svg class="icon"
                     style="width: 1em; height: 1em;vertical-align: middle;fill: currentColor;overflow: hidden;"
                     viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1965">
                    <path d="M512 0C228.266667 0 0 228.266667 0 512c0 283.733333 228.266667 512 512 512 283.733333 0 512-228.266667 512-512C1024 228.266667 795.733333 0 512 0zM832 384 492.8 723.2C469.333333 746.666667 426.666667 746.666667 403.2 723.2L192 512c0 0-32-32 0-64s64 0 64 0l192 192 320-320c0 0 32-32 64 0S832 384 832 384z"
                          p-id="1966"></path>
                </svg>
                <span>分组{{text==="新建分组"?'创建':'修改'}}成功！是否继续创建API？</span>
            </p>
            <div>
                <a-button @click="success=false">返回分组列表</a-button>
                &nbsp;&nbsp;
                <a-button type="primary" @click="continues()">
                    继续
                </a-button>
            </div>
        </a-modal>
    </div>
</template>

<script>
    import moment from "moment";
    import request from "../../utils/request";
    let one = [];
    let two = [];
    const columns = [
        {
            title: '组名称',
            dataIndex: 'name',
            key: 'name',
            ellipsis: true
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            ellipsis: true
        },
        {
            title: '一级类别',
            dataIndex: 'categoryOneName',
            key: 'categoryOneName',
            filters: one,
        },
        {
            title: '二级类别',
            dataIndex: 'categoryTwoName',
            key: 'categoryTwoName',
            filters: two,
        },
        {
            title: 'api数量',
            dataIndex: 'apiNum',
            key: 'apiNum',
            scopedSlots: {customRender: 'apiNum'},
            width: 85
        },
        {
            title: '创建人',
            dataIndex: 'creator',
            key: 'creator',
            ellipsis: true
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            scopedSlots: {customRender: 'createTime'},
            width: 180,
            sorter: true
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'}
        }
    ];
    export default {
        name: "index",
        data() {
            return {
                columns,
                select: '1',
                name: null,
                visible: false,
                confirmLoading: false,
                loading: false,
                text: '新建分组',
                form: {//新建分组的数据
                    name: null,//组名称
                    note: null,//描述
                    one: undefined,//一级类目
                    two: undefined,//二级类目
                    system: undefined//所属系统
                },
                rules: {
                    name: [
                        {required: true, message: '请输入组名称', trigger: 'blur', whitespace: true},
                        {min: 1, max: 60, message: '请不要输入超过60个字符', trigger: 'blur'},
                        {
                            pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9-\u4e00-\u9fa5]+$/,
                            message: '请正确填入组名称',
                            trigger: ['change', 'blur']
                        }
                    ],
                    note: [
                        {min: 0, max: 300, message: '请不要输入超过300个字符', trigger: 'blur'},
                    ],
                    two: [
                        {validator:(rule, value, callback)=> {
                                if (!this.form.one&&!this.form.two){
                                    callback(new Error('请选择一级类目和二级类目'))
                                }
                                callback();
                         },trigger: 'change'}
                    ],
                    system: [
                        {required: true, message: '请选择所属系统', trigger: 'blur'},
                    ]
                },
                timer: ["", ""],
                list: [],//列表数据
                pageNum: 1,
                pageSize: 10,
                categoryOne: [],//一级类目
                categoryTwo: [],//二级类目
                categoryOneChildren: [],//一级类目下的二级类目
                systemList: [],//一级类目和二级类目的所属系统列表
                that: this,//改变this指向
                id: null,//列表的ID
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
                searchData: {//表头查询信息，用于判断是否携带查询条件切换分页
                    categoryOneName: [],
                    categoryTwoName: [],
                    time: undefined,
                    pageSize: 10
                },
                success: false,
                categoryRouter: false,
                url: null
            }
        },
        methods: {
            moment,
            switchTime(date, dateString) {//时间插件
                this.timer = dateString;
            },
            handleCancel() {//新建分组取消
                this.categoryRouter=false;
                this.form.one = undefined;
                this.form.two = undefined;
                this.form.note = null;
                this.name = null;
                this.system = undefined;
                this.categoryOneChildren = [];
                this.systemList = [];
                this.$refs.form.resetFields()
                this.visible = false;
            },
            handleSubmit() {//新建or修改分组确认
                this.$refs.form.validate(valid => {
                    if (valid) {
                        this.categoryRouter=false;
                        if (this.text==="新建分组"){
                            request(this.url+"/publishApiGroup/createPublishApiGroup",{
                                method:"POST",
                                body:{
                                    categoryOne: this.form.one,
                                    categoryTwo: this.form.two,
                                    name: this.form.name,
                                    description: this.form.note,
                                    system: this.form.system
                                }
                            }).then(res=>{
                                if ((typeof res==="boolean")&&res) {
                                    this.form.one = undefined;
                                    this.$refs.form.resetFields()
                                    this.visible = false;
                                    this.success = true;
                                    this.getData();
                                }
                            })
                        }else{
                            request(this.url+"/publishApiGroup/updatePublishApiGroup/"+this.id,{
                                method:"POST",
                                body:{
                                    categoryOne: this.form.one,
                                    categoryTwo: this.form.two,
                                    name: this.form.name,
                                    description: this.form.note,
                                    system: this.form.system
                                }
                            }).then(res => {
                                if ((typeof res==="boolean")&&res) {
                                    this.form.one = undefined;
                                    this.$refs.form.resetFields()
                                    this.visible = false;
                                    this.success = true;
                                    this.getData();
                                }
                            })
                        }
                    }else{
                        if (this.form.one&&!this.form.two){
                            this.categoryRouter=true;
                        }
                    }
                })
            },
            del(id) {
                let that = this;
                this.$confirm({
                    title: '确认要删除该api分组吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk() {
                        request(that.url+ "/publishApiGroup/deletePublishApiGroup/" + id,{
                            method:"DELETE"
                        }).then(res=>{
                            if (res) {
                                that.pagination.current = 1;
                                that.getData();
                            }
                        })
                    }
                });
            },
            getData() {
                let time = {
                    start: this.timer[0],
                    end: this.timer[1]
                }
                this.loading = true;
                request(this.url+'/publishApiGroup/searchPublishApiGroup',{
                    method:"POST",
                    body:{
                        categoryOne: this.searchData.categoryOneName.length ? this.searchData.categoryOneName : null,
                        categoryTwo: this.searchData.categoryTwoName.length ? this.searchData.categoryTwoName : null,
                        name: this.name || null,
                        pageNum: this.pagination.current,
                        pageSize: this.pagination.pageSize,
                        timeQuery: this.timer[0] ? time : null,
                        sort: this.searchData.time === "descend" ? ["a", "createTime"] : ["d", "createTime"]
                    }
                }).then(res => {
                    this.loading = false;
                    this.list = res.content;
                    this.pagination.total = res.totalElements;
                })
            },
            category() {//查询全部的一级类目，查询全部的二级类目，第一个一级类目下的二级类目
                let categoryOne = () => {
                    return new Promise((resolve) => {
                        request(this.url+"/dataItems/categoryOne/findDataItems",{
                            method:"GET"
                        }).then(res=>{
                            resolve(res)
                        })
                    })
                }

                let categoryTwo = () => {
                    return new Promise((resolve, reject) => {
                        request(this.url+ "/dataItems/categoryTwo/findDataItems",{
                            method:"GET"
                        }).then(res=>{
                            resolve(res)
                        })
                    })
                }

                categoryOne().then(res => {//查询一级类目和二级类目
                    this.categoryOne = res;
                    res.forEach(item => {
                        one.push({
                            text: item.itemName,
                            value: item.id + ""
                        })
                    })
                }).finally(() => {
                    categoryTwo().then(res => {
                        this.categoryTwo = res;
                        res.forEach(item => {
                            two.push({
                                text: item.itemName,
                                value: item.id + ""
                            })
                        })
                    })
                })
            },
            handleChange(value) {//切换一级类目
                this.form.two = undefined;
                this.categoryTwo = [];
                this.form.system = undefined;
                this.systemList = [];
                request(this.url+"/dataItems/categoryTwo/findDataItemsByParentId/" + value,{
                    method:"GET"
                }).then(res=>{
                    this.categoryOneChildren = res;
                })
                try {
                    this.$refs.two.onFieldChange()
                }catch (e) {

                }
            },
            handleChangeTwo(value) {//查询所属系统
                request(`${this.url}/dataItems/system/findSystemDataItems?categoryOne=${this.form.one}&categoryTwo=${this.form.two}`,{
                    method:"GET"
                }).then(res => {
                    this.systemList = res;
                })
                try {
                    this.$refs.two.onFieldChange();
                }catch (e) {

                }
            },
            modify(item) {//修改
                this.text = '编辑分组';
                this.id = item.id
                this.form = {
                    name: item.name,
                    note: item.description,
                    one: item.categoryOne,
                    two: item.categoryTwo,
                    system: item.system
                }
                request(this.url+"/dataItems/categoryTwo/findDataItemsByParentId/" + item.categoryOne,{
                    method:"GET"
                }).then(res=>{
                    this.categoryOneChildren = res;
                })
                this.handleChangeTwo();
                this.visible = true;
            },
            onreset() {//重置
                this.name = null;
                this.timer = ["", ""];
                this.select = "1";
                this.getData();
            },
            handleTableChange(pagination, filters, sorter) {//表格change事件
                let json = {
                    categoryOneName: filters.categoryOneName ? filters.categoryOneName.map(Number) : [],
                    categoryTwoName: filters.categoryTwoName ? filters.categoryTwoName.map(Number) : [],
                    time: sorter.order,
                    pageSize: pagination.pageSize
                }
                if (
                    this.searchData.pageSize === json.pageSize &&
                    JSON.stringify(this.searchData.categoryOneName) === JSON.stringify(json.categoryOneName) &&
                    JSON.stringify(this.searchData.categoryTwoName) === JSON.stringify(json.categoryTwoName) &&
                    this.searchData.time === json.time
                ) {
                    this.pagination.current = pagination.current;
                    this.getData();
                } else {
                    this.pagination.current = 1;
                    this.pagination.pageSize = json.pageSize;
                    this.searchData = {
                        categoryOneName: json.categoryOneName,
                        categoryTwoName: json.categoryTwoName,
                        time: json.time,
                        pageSize: json.pageSize
                    }
                    this.getData()
                }
            },
            jump(record) {
                if (!record.apiNum) {
                    return
                }
                sessionStorage.setItem("group",record.id)
                window.vm.$router.push({
                    path:"/gateway/apiList/apiListCreated"
                })
            },
            continues(){
                this.success=false;
                window.vm.$router.push({
                    path:"/gateway/apiList/apiListCreated"
                })
            }
        },
        activated(){
            this.getData();
        },
        mounted() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.category()
            this.getData();
        },
        destroyed() {
            one = [];
            two = [];
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
                    if (window.vm.$route.name==="groupManage"){
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.getData()
                    }
                }
            }
        },
    }
</script>

<style lang="less" scoped>
    .grouping {
        header {
            width: 100%;
            padding:16px 16px 0;
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

        .body {
            min-height: calc(~"100vh - 163px");
            background-color: #ffffff;
            padding: 16px;

            .apiName {
                cursor: pointer;
                color: #00aaa6;
            }

            .apiName:hover {
                text-decoration: underline;
            }
        }
    }
</style>
<style>
    .createdSuccGroup .ant-modal-body p:nth-child(1) {
        font-size: 16px;
        color: rgba(0, 0, 0, 0.85);
    }

    .createdSuccGroup .ant-modal-body p:nth-child(1) svg {
        color: #52C41A;
        margin-right: 12px;
        position: relative;
        top: -2px;
    }

    .createdSuccGroup .ant-modal-body div {
        text-align: right;
        padding-top: 14px;
    }

    .groupModal .category {
        position: relative;
        /*margin-bottom: 0;*/
    }

    .groupModal .category::before {
        content: "*";
        color: #f5222d;
        position: absolute;
        top: 12px;
        left: 15px;
    }
</style>
