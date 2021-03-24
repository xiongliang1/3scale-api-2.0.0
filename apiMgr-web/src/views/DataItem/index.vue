<template>
    <div class="dataItems">
        <header>
            <div class="clearfix">
                <a-input addon-before="类目名称：" v-model.trim="name"/>
                <a-input addon-before="类目编码：" v-model.trim="code"/>
                <div>
                    <a-button type="primary" @click="getData()">
                        查询
                    </a-button>
                    &nbsp;&nbsp;
                    <a-button @click="onReset()">
                        重置
                    </a-button>
                </div>
            </div>
            <div>
                <a-button type="primary" @click="createDataItem(0)">
                    新增
                </a-button>
            </div>
        </header>
        <div class="body">
            <a-table
                    :columns="columns"
                    :data-source="data"
                    size="small"
                    :pagination="{hideOnSinglePage:true,total:0,pageSize:2000}"
                    :rowKey="row=>row.id"
                    :loading="loading"
            >
                <div slot="action" slot-scope="text,record">
                    <a-button type="link" size="small" style="padding-left: 0;" @click="createDataItem(1,record)"
                              :disabled="record.groupKey==='system'">
                        新增
                    </a-button>
                    <a-button type="link" size="small" @click="alter(record)">
                        修改
                    </a-button>
                </div>
            </a-table>
        </div>

        <a-modal
                :title="text"
                :visible="visible"
                :confirm-loading="confirmLoading"
                @ok="handleOk"
                @cancel="handleCancel"
        >
            <a-form-model
                    ref="ruleForm"
                    :model="form"
                    :rules="rules"
                    :label-col="labelCol"
                    :wrapper-col="wrapperCol"
            >
                <a-form-model-item label="类目名称" prop="itemName">
                    <a-input v-model="form.itemName"/>
                </a-form-model-item>
                <a-form-model-item label="类目编码" prop="itemKey">
                    <a-input v-model="form.itemKey"/>
                </a-form-model-item>
            </a-form-model>
        </a-modal>
    </div>
</template>

<script>
    import request from "@/utils/request";

    const columns = [
        {title: '类目', dataIndex: 'groupName', key: 'groupName', width: '25%'},
        {title: '类目编码', dataIndex: 'itemKey', key: 'itemKey', width: '25%'},
        {title: '类目名称', dataIndex: 'itemName', key: 'itemName', width: '25%'},
        {title: '操作', dataIndex: '', key: 'x', scopedSlots: {customRender: 'action'}, width: '25%'}
    ];
    export default {
        data() {
            return {
                url: null,
                data: [],
                columns,
                loading: false,
                name: "",
                code: "",
                text: "",
                visible: false,
                confirmLoading: false,
                labelCol: {span: 6},
                wrapperCol: {span: 14},
                form: {
                    itemKey: null,
                    itemName: null,
                    groupName:null,
                    groupKey:null,
                    parentId:null
                },
                rules: {
                    itemKey: [
                        { required: true, message: '请输入类目名称', trigger: 'blur' }
                    ],
                    itemName: [
                        { required: true, message: '请输入类目编码', trigger: 'blur' }
                    ],
                },
                id:null
            };
        },
        created() {
            this.url = "/apimgr/api/v1/tenant/tenantId/project/1/"+JSON.parse(localStorage.getItem("appNowCategory")).gateway[0].key+"/dataItems/";
            this.getData();
        },
        methods: {
            getData() {
                this.loading = true;
                request(this.url + "searchDataItems?groupName=" + this.name + "&itemKey=" + this.code, {
                    method: "GET"
                }).then(res => {
                    this.loading = false;
                    let str = JSON.stringify(res);
                    this.data = JSON.parse(str.split("dataItemList").join("children"));
                })
            },
            onReset() {
                this.name = "";
                this.code = "";
                this.getData();
            },
            createDataItem(num, item) {
                this.visible = true;
                this.id=null;
                this.form.itemKey=null;
                this.form.itemName=null;
                this.form.parentId=item?item.id:0;
                if (!item) {
                    this.text = "新增一级类目";
                    this.form.groupName="一级类目";
                    this.form.groupKey="categoryOne";
                } else if (item.groupKey === "categoryOne") {
                    this.text = "新增二级类目"
                    this.form.groupName="二级类目";
                    this.form.groupKey="categoryTwo";
                } else if (item.groupKey === "categoryTwo") {
                    this.text = "新增所属系统";
                    this.form.groupName="所属系统";
                    this.form.groupKey="system";
                }
            },
            handleOk() {
                let url=this.id?("updateDataItem/"+this.id):"createDataItem";
                this.$refs.ruleForm.validate(valid => {
                    if (valid) {
                        request(this.url+url,{
                            method:"POST",
                            body:this.form
                        }).then(res=>{
                            if ((typeof res==="boolean")&&res){
                                this.handleCancel();
                                this.getData();
                            }
                        })
                    }
                });
            },
            handleCancel() {
                this.visible = false;
                try {
                    this.$refs.ruleForm.resetFields();
                }catch (e) {

                }
            },
            alter(item) {
                this.id=item.id;
                this.text="修改"+item.groupName
                this.form={
                    itemKey: item.itemKey,
                    itemName: item.itemName,
                    groupName:item.groupName,
                    groupKey:item.groupKey,
                    parentId:item.parentId
                }
                this.visible=true;
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
                    this.url="/apimgr/api/v1/tenant/tenantId/project/1/"+newVal.gateway[0].key+"/dataItems/";
                    if (window.vm.$route.name==='dataItem'&&(oldVal.gateway[0].key!==newVal.gateway[0].key)){
                        this.getData();
                    }
                }
            }
        },
    };
</script>
<style lang="less" scoped>
    .dataItems {
        header {
            padding: 24px 24px 0;

            .ant-input-group-wrapper {
                width: 400px;
                margin-right: 30px;
            }

            > div {
                margin-bottom: 20px;

                > div {
                    float: right;
                }
            }
        }

        .body {
            padding: 0 24px 24px;
        }
    }
</style>
