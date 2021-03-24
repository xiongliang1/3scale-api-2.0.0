<template>
    <div class="flowcenter-systemConfiguration">
        <a-form layout="inline" :form="searchForm" @submit="handleSubmit" :label-col="{span:8}"
                :wrapper-col="{span:16}">
            <a-form-item label="系统名称">
                <a-input v-model.trim="searchForm.name" placeholder="请输入"/>
            </a-form-item>
            <a-form-item label="系统uuid">
                <a-input v-model.trim="searchForm.uuid" placeholder="请输入"/>
            </a-form-item>
            <a-form-item label="租户ID">
                <a-input v-model.trim="searchForm.tenantid" placeholder="请输入"/>
            </a-form-item>
            <a-form-item label="流程定义前缀">
                <a-input v-model.trim="searchForm.process" placeholder="请输入"/>
            </a-form-item>
            <a-form-item label="" class="search">
                <a-button type="primary" html-type="submit">查询</a-button>
                <a-button type="primary" @click="formReset">重置</a-button>
            </a-form-item>
        </a-form>

        <div class="container clearfix">
            <div class="container-tree">
                <a-tree :load-data="onLoadData" :tree-data="treeData" @select="select" :blockNode="true"
                        :showLine="true" v-if="flag" :defaultExpandedKeys="['0']"></a-tree>
            </div>
            <div class="container-box">
                <div class="container-header">
                    <h3 v-if="level===1">系统：</h3>
                    <h3 v-if="level===2">流程：</h3>
                    <h3 v-if="level===3">节点：</h3>
                    <a-button type="primary" class="add" @click="add" v-show="level<4">新增</a-button>
                </div>
                <a-table :columns="columns" :data-source="treeDataTable" size="small" :row-key="record => record.id"
                         :pagination="{hideOnSinglePage:true}" :loading="loading" v-show="level===1">
                    <div slot="status" slot-scope="text">
                        {{text | status}}
                    </div>
                    <div slot="operation" slot-scope="text,record">
                        <a-button size="small" type="primary" @click="modificationDelails(record)">修改</a-button>&nbsp;&nbsp;
                        <a-button size="small" type="primary" @click="delSystem(record)">删除</a-button>
                    </div>
                </a-table>
                <a-table :columns="columsProcess" :data-source="processData" size="small" :row-key="record => record.id"
                         :pagination="{hideOnSinglePage:true}" v-show="level===2">
                    <div slot="closeStatus" slot-scope="text,record">
                        {{text | closeStatus}}
                    </div>
                    <div slot="operation" slot-scope="text,record">
                        <a-button size="small" type="primary" @click="modificationProcess(record)">修改</a-button>&nbsp;&nbsp;
                        <a-button size="small" type="primary" @click="delProcess(record)">删除</a-button>
                    </div>
                </a-table>
                <a-table :columns="columsNode" :data-source="nodeData" size="small" :row-key="record => record.id"
                         :pagination="{hideOnSinglePage:true}" :loading="loading" v-show="level===3">
                    <div slot="operation" slot-scope="text,record">
                        <a-button size="small" type="primary" @click="modificationNode(record)">修改</a-button>&nbsp;&nbsp;
                        <a-button size="small" type="primary" @click="delNode(record)">删除</a-button>
                    </div>
                </a-table>
                <div class="nodeDelails" v-show="level===4">
                    <h5>节点详细信息：</h5>
                   <p>中文名称：{{nodeDetails.processNodeNameCn}}</p>
                   <p>英文名称：{{nodeDetails.processNodeNameEn}}</p>
                   <p>创建人：{{nodeDetails.creator}}</p>
                   <p>创建时间：{{nodeDetails.createTime}}</p>
                   <p>修改人：{{nodeDetails.updator}}</p>
                   <p>修改时间：{{nodeDetails.updateTime}}</p>
                </div>
            </div>
        </div>

        <a-modal
                :title="system['title']"
                :visible="system['visible']"
                :confirm-loading="system['confirmLoading']"
                okText="确认"
                cancelText="取消"
                :maskClosable="true"
                @ok="systemHandleOk"
                @cancel="systemHandleCancel"
        >
            <a-form-model :model="systemForm" ref="systemRuleForm" :rules="systemRules" :label-col="{span:6}"
                          :wrapper-col="{span:18}">
                <a-form-model-item label="系统名称" prop="name">
                    <a-input v-model.trim="systemForm.name" placeholder="请输入"></a-input>
                </a-form-model-item>
                <a-form-model-item label="系统uuid" prop="uuid">
                    <a-input v-model.trim="systemForm.uuid" placeholder="请输入"></a-input>
                </a-form-model-item>
                <a-form-model-item label="租户ID" prop="tenantid">
                    <a-input v-model.trim="systemForm.tenantid" placeholder="请输入"></a-input>
                </a-form-model-item>
                <a-form-model-item label="流程定义前缀" prop="process">
                    <a-input v-model.trim="systemForm.process" placeholder="请输入"></a-input>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal
                :title="process['title']"
                :visible="process['visible']"
                :confirm-loading="process['confirmLoading']"
                okText="确认"
                cancelText="取消"
                :maskClosable="true"
                @ok="processHandleOk"
                @cancel="processHandleCancel"
        >
            <a-form-model :model="processForm" ref="processRuleForm" :rules="processRules" :label-col="{span:6}"
                          :wrapper-col="{span:18}">
                <a-form-model-item label="中文名称" prop="nameCn">
                    <a-input v-model.trim="processForm.nameCn" placeholder="请输入"></a-input>
                </a-form-model-item>
                <a-form-model-item label="英文名称" prop="nameEn">
                    <a-input v-model.trim="processForm.nameEn" placeholder="请输入"></a-input>
                </a-form-model-item>
                <a-form-model-item label="驳回处理标识" prop="status">
                    <a-select v-model="processForm.status" placeholder="请选择">
                        <a-select-option :value="1">
                            终止
                        </a-select-option>
                        <a-select-option :value="0">
                            不终止
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
                <a-form-model-item label="接口路径" prop="path">
                    <a-input v-model.trim="processForm.path" placeholder="请输入">
                        <a-tooltip slot="suffix" title="获取流程基本信息接口">
                            <a-icon type="info-circle" style="color: rgba(0,0,0,.45)"/>
                        </a-tooltip>
                    </a-input>
                </a-form-model-item>
                <a-form-model-item label="url" prop="url">
                    <a-input v-model.trim="processForm.url" placeholder="请输入">
                        <a-tooltip slot="suffix" title="流程处理结束返回业务系统数据接口url">
                            <a-icon type="info-circle" style="color: rgba(0,0,0,.45)"/>
                        </a-tooltip>
                    </a-input>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal
                :title="node['title']"
                :visible="node['visible']"
                :confirm-loading="node['confirmLoading']"
                okText="确认"
                cancelText="取消"
                :maskClosable="true"
                @ok="nodeHandleOk"
                @cancel="nodeHandleCancel"
        >
            <a-form-model
                    :model="nodeForm" ref="nodeRuleForm" :rules="nodeRules" :label-col="{span:6}"
                    :wrapper-col="{span:18}">
                <a-form-model-item label="中文名称" prop="nameCn">
                    <a-input v-model.trim="nodeForm.nameCn" placeholder="请输入"></a-input>
                </a-form-model-item>
                <a-form-model-item label="英文名称" prop="nameEn">
                    <a-input v-model.trim="nodeForm.nameEn" placeholder="请输入"></a-input>
                </a-form-model-item>
            </a-form-model>
        </a-modal>
    </div>
</template>

<script>
    import request from "@/utils/request";

    const columns = [
        {
            title: '名称',
            dataIndex: 'catalogName',
            key: 'catalogName',
            width: 120
        },
        {
            title: 'UUID',
            dataIndex: 'catalogUuid',
            key: 'catalogUuid',
            width: 120
        },
        {
            title: '租户ID',
            dataIndex: 'tenantid',
            key: 'tenantid',
            width: 120
        },
        {
            title: '流程定义前缀',
            dataIndex: 'processDefNamePre',
            key: 'processDefNamePre',
            width: 120
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 120
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 120
        },
        {
            title: '创建人',
            dataIndex: 'creator',
            key: 'creator',
            width: 120
        },
        {
            title: '更新人',
            dataIndex: 'updator',
            key: 'updator',
            width: 120
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'},
            width: 140
        }
    ];
    const columsProcess = [
        {
            title: '中文名称',
            dataIndex: 'processNameCn',
            key: 'processNameCn',
            width: 120
        },
        {
            title: '英文名称',
            dataIndex: 'processNameEn',
            key: 'processNameEn',
            width: 120
        },
        {
            title: '驳回处理标识',
            dataIndex: 'closeStatus',
            key: 'closeStatus',
            scopedSlots: {customRender: 'closeStatus'},
            width: 120
        },
        {
            title: '基本信息接口',
            dataIndex: 'basicInfoApiPath',
            key: 'basicInfoApiPath',
            scopedSlots: {customRender: 'basicInfoApiPath'},
            width: 120
        },
        {
            title: '流程回调结束接口',
            dataIndex: 'processResultHandleUrl',
            key: 'processResultHandleUrl',
            scopedSlots: {customRender: 'processResultHandleUrl'},
            width: 120
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 120
        },
        {
            title: '创建人',
            dataIndex: 'creator',
            key: 'creator',
            width: 120
        },
        {
            title: '修改时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 120
        },
        {
            title: '修改人',
            dataIndex: 'updator',
            key: 'updator',
            width: 120
        },
        {
            title: '操作',
            scopedSlots: {customRender: 'operation'},
            width: 130
        }
    ];
    const columsNode = [
        {
            title: '中文名称',
            dataIndex: 'processNodeNameCn',
            key: 'processNodeNameCn',
            width: 150
        },
        {
            title: '英文名称',
            dataIndex: 'processNodeNameEn',
            key: 'processNodeNameEn',
            width: 150
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 150
        },
        {
            title: '创建人',
            dataIndex: 'creator',
            key: 'creator',
            width: 150
        },
        {
            title: '修改时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 150
        },
        {
            title: '修改人',
            dataIndex: 'updator',
            key: 'updator',
            width: 150
        },
        {
            title: '操作',
            dataIndex: 'operation',
            key: 'operation',
            scopedSlots: {customRender: 'operation'},
            width:120
        }
    ];
    export default {
        name: "systemConfiguration",
        data() {
            return {
                searchForm: {
                    uuid: null,
                    name: null,
                    tenantid: null,
                    process: null
                },
                flag: true,
                treeData: [
                    { title: '系统名称', key: '0',level:0 }
                ],
                processData: [],
                treeDataTable: [],
                nodeData: [],
                columns,
                columsProcess,
                columsNode,
                loading: false,
                level: 1,
                nodeId:null,
                system: {
                    visible: false,
                    confirmLoading: false,
                    title: "新增系统"
                },
                systemForm: {
                    name: null,
                    uuid: null,
                    tenantid: null,
                    process: null,
                    id: null
                },
                systemRules: {
                    name: [
                        {required: true, message: '请输入系统名称', trigger: 'blur'}
                    ],
                    uuid: [
                        {required: true, message: '请输入uuid', trigger: 'blur'}
                    ],
                    tenantid: [
                        {required: true, message: '请输入租户id', trigger: 'blur'}
                    ],
                    process: [
                        {required: true, message: '请输入流程定义前缀', trigger: 'blur'}
                    ]
                },
                process: {
                    visible: false,
                    confirmLoading: false,
                    title: "新增流程"
                },
                processForm: {
                    nameEn: null,
                    nameCn: null,
                    status: undefined,
                    path: null,
                    url: null,
                    systemId: null,
                    id: null
                },
                processRules: {
                    nameEn: [
                        {required: true, message: '请输入流程英文名称', trigger: 'blur'}
                    ],
                    nameCn: [
                        {required: true, message: '请输入流程英文名称', trigger: 'blur'}
                    ],
                    status: [
                        {required: true, message: '请选择驳回处理标识', trigger: 'blur'}
                    ],
                    path: [
                        {required: true, message: '请输入获取流程基本信息接口', trigger: 'blur'}
                    ],
                    url: [
                        {required: true, message: '请输入流程处理结束返回业务系统数据接口url', trigger: 'blur'}
                    ]
                },
                node:{
                    visible: false,
                    confirmLoading: false,
                    title: "新增节点"
                },
                nodeForm:{
                    nameCn:null,
                    nameEn:null
                },
                nodeRules:{
                    nameCn: [
                        {required: true, message: '请输入中文名称', trigger: 'blur'}
                    ],
                    nameEn: [
                        {required: true, message: '请输入英文名称', trigger: 'blur'}
                    ],
                },
                nodeDetails:{

                }
            }
        },
        filters: {
            status(num) {
                if (!num) {
                    return "不可用"
                } else {
                    return "正常"
                }
            },
            closeStatus(num) {
                if (!num) {
                    return "不终止"
                } else {
                    return "终止"
                }
            }
        },
        methods: {
            handleSubmit(e) {
                e.preventDefault();
                this.getParentTree();
            },
            onLoadData(treeNode) {
                return new Promise(resolve => {
                    if (treeNode.dataRef.children) {
                        resolve();
                        return;
                    }
                    if (!treeNode.dataRef.level){
                        let treeData=JSON.parse(JSON.stringify(this.treeDataTable));
                        treeData.forEach(item => {
                            item.title = item.catalogName;
                            item.key = item.id;
                            item.level = 1;
                        })
                        this.treeData[0].children = treeData;
                        this.treeData = [...this.treeData];
                        resolve();
                    }else if (treeNode.dataRef.level === 1) {
                        request("/api/hip-flowable/api/v1/flowProcess/getFlowProcess", {
                            method: "POST",
                            body: {
                                catalogId: treeNode.dataRef.key
                            }
                        }).then(res => {
                            if (res.length) {
                                res.forEach((item, index, array) => {
                                    array[index].key = (treeNode.dataRef.level + 1) + "-" + array[index].id;
                                    array[index].title = array[index].processNameCn;
                                    array[index].level = 2;
                                })
                                treeNode.dataRef.children = res;
                                this.treeData = [...this.treeData];
                            }
                            resolve();
                        })
                    } else if (treeNode.dataRef.level === 2) {
                        request("/api/hip-flowable/api/v1/node/getFlowProcessNode", {
                            method: "POST",
                            body: {
                                flowProcessId: treeNode.dataRef.id
                            }
                        }).then(res => {
                            if (res.length) {
                                 res.forEach((item,index,array)=>{
                                     res.forEach((item, index, array) => {
                                         array[index].key = (treeNode.dataRef.level + 1) + "-" + array[index].id;
                                         array[index].title = array[index].processNodeNameCn;
                                         array[index].isLeaf = true;
                                         array[index].level = 3;
                                     })
                                     treeNode.dataRef.children = res;
                                     this.treeData = [...this.treeData];
                                 })
                            }
                            resolve()
                        })
                    }
                })
            },
            getParentTree() {
                this.loading = true;
                this.flag=false;
                request("/api/hip-flowable/api/v1/catalog/getCatalogInfos", {
                    method: "POST",
                    body: {
                        catalogUuid: this.searchForm.uuid || null,
                        catalogName: this.searchForm.name || null,
                        tenantid: this.searchForm.tenantid || null,
                        processDefNamePre: this.searchForm.process ||null
                    }
                }).then(res => {
                    this.loading = false;
                    this.treeDataTable = res;
                    let treeData=JSON.parse(JSON.stringify(this.treeDataTable));
                    treeData.forEach(item => {
                        item.title = item.catalogName;
                        item.key = item.id;
                        item.level = 1;
                    })
                    this.treeData[0].children = treeData;
                    this.treeData = [...this.treeData];
                    this.flag=true;
                })
            },
            formReset() {
                this.searchForm = {
                    uuid: null,
                    name: null,
                    tenantid: null,
                    process: null,
                    status: undefined
                }
                this.getParentTree()
            },
            select(selectedKeys, e) {
                this.nodeId=selectedKeys;
                this.level = e.node.dataRef.level + 1;
                if (e.node.dataRef.level === 1) {
                    this.processForm.systemId = e.node.dataRef.id;
                    request("/api/hip-flowable/api/v1/flowProcess/getFlowProcess", {
                        method: "POST",
                        body: {
                            catalogId: e.node.dataRef.id
                        }
                    }).then(res => {
                        this.processData = res;
                    })
                } else if (e.node.dataRef.level === 2) {
                    request("/api/hip-flowable/api/v1/node/getFlowProcessNode", {
                        method: "POST",
                        body: {
                            flowProcessId: e.node.dataRef.id
                        }
                    }).then(res => {
                        this.nodeData = res;
                    })
                } else {
                    this.nodeDetails=e.node.dataRef;
                }
            },
            modificationDelails(record) {
                this.system.title = "修改系统";
                this.systemForm.uuid = record.catalogUuid;
                this.systemForm.name = record.catalogName;
                this.systemForm.tenantid = record.tenantid;
                this.systemForm.process = record.processDefNamePre;
                this.systemForm.id = record.id;
                this.system.visible = true;
            },
            delSystem(record) {
                let that = this;
                this.$confirm({
                    title: '提示',
                    content: '确认删除当前系统吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk() {
                        request("/api/hip-flowable/api/v1/catalog/delCatalogInfo/" + record.id, {
                            method: "DELETE"
                        }).then(res => {
                            that.getParentTree()
                        })
                    }
                });
            },
            systemHandleOk() {
                this.$refs.systemRuleForm.validate(valid => {
                    if (!valid) {
                        return
                    }
                    this.system.confirmLoading = true;
                    let url;
                    let json = {
                        catalogUuid: this.systemForm.uuid,
                        catalogName: this.systemForm.name,
                        tenantid: this.systemForm.tenantid,
                        processDefNamePre: this.systemForm.process
                    }
                    if (this.system.title === "新增系统") {
                        url = "/api/hip-flowable/api/v1/catalog/addCatalogInfo";
                    } else {
                        url = "/api/hip-flowable/api/v1/catalog/updateCatalogInfo";
                        json.id = this.systemForm.id;
                    }
                    request(url, {
                        method: "POST",
                        body: json
                    }).then(res => {
                        this.system.confirmLoading = false;
                        if (this.system.title === "新增系统"&&!res.code){
                            this.system.visible = false;
                            this.getParentTree();
                            this.$refs.systemRuleForm.resetFields();
                        }else if (this.system.title === "修改系统"&&!res){
                            this.system.visible = false;
                            this.getParentTree();
                            this.$refs.systemRuleForm.resetFields();
                        }
                    })
                })
            },
            systemHandleCancel() {
                this.processForm={
                        nameEn: null,
                        nameCn: null,
                        status: undefined,
                        path: null,
                        url: null,
                        systemId: null,
                        id: null
                };
                try {
                    this.$refs.systemRuleForm.resetFields();
                } catch (e) {

                }
                this.system.visible = false;
            },
            add() {
                if (this.level === 1) {
                    this.system.title = '新增系统';
                    this.system.visible = true;
                } else if (this.level === 2) {
                    this.process.title = '新增流程';
                    this.process.visible = true;
                }else if (this.level === 3){
                    this.node.title = "新增节点";
                    this.node.visible = true;
                }
            },
            processHandleOk() {
                this.$refs.processRuleForm.validate(valid => {
                    if (!valid) {
                        return
                    }
                    this.processForm.confirmLoading = true;
                    let url;
                    let json = {
                        flowCatalogId: this.processForm.systemId,
                        processNameEn: this.processForm.nameEn,
                        processNameCn: this.processForm.nameCn,
                        closeStatus: this.processForm.status,
                        basicInfoApiPath: this.processForm.path,
                        processResultHandleUrl: this.processForm.url
                    }
                    if (this.process.title === "新增流程") {
                        url = "/api/hip-flowable/api/v1/flowProcess/addFlowProcess"
                    } else {
                        url = "/api/hip-flowable/api/v1/flowProcess/updateFlowProcess"
                        json.id = this.processForm.id
                    }
                    request(url, {
                        method: "POST",
                        body: json
                    }).then(res => {
                            this.process.confirmLoading=false;
                            if (this.process.title === "新增流程"&&!res.code){
                                this.$refs.processRuleForm.resetFields();
                                this.process.visible=false;
                                this.getParentTree();
                                request("/api/hip-flowable/api/v1/flowProcess/getFlowProcess", {
                                    method: "POST",
                                    body: {
                                        catalogId: this.nodeId[0]
                                    }
                                }).then(res => {
                                    this.processData = res;
                                })
                            }else if (this.process.title === "修改流程"&&!res){
                                this.$refs.processRuleForm.resetFields();
                                this.process.visible=false;
                                this.getParentTree();
                                request("/api/hip-flowable/api/v1/flowProcess/getFlowProcess", {
                                    method: "POST",
                                    body: {
                                        catalogId: this.nodeId[0]
                                    }
                                }).then(res => {
                                    this.processData = res;
                                })
                            }
                    })
                })
            },
            processHandleCancel() {
                this.nodeForm={
                    nameCn:null,
                    nameEn:null
                };
                try {
                    this.$refs.processRuleForm.resetFields();
                } catch (e) {

                }
                this.process.visible = false;
            },
            modificationProcess(record) {
                this.process.title = "修改流程";
                this.processForm.id = record.id;
                this.processForm.nameEn = record.processNameEn;
                this.processForm.nameCn = record.processNameCn;
                this.processForm.status = record.closeStatus;
                this.processForm.path = record.basicInfoApiPath;
                this.processForm.url = record.processResultHandleUrl;
                this.process.visible = true;
            },
            delProcess(record) {
                let that = this;
                this.$confirm({
                    title: '提示',
                    content: '确认删除当前流程吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk() {
                        request("/api/hip-flowable/api/v1/flowProcess/delFlowProcess/" + record.id, {
                            method: "DELETE"
                        }).then(res => {
                            if (res === null) {
                                that.getParentTree();
                                request("/api/hip-flowable/api/v1/flowProcess/getFlowProcess", {
                                    method: "POST",
                                    body: {
                                        catalogId: that.nodeId[0]
                                    }
                                }).then(res => {
                                    that.processData = res;
                                })
                            }
                        })
                    }
                })
            },
            nodeHandleOk(){
                this.$refs.nodeRuleForm.validate(valid=>{
                    if (!valid){
                        return
                    }
                    let url;
                    let json={
                        flowWfprocessId:this.nodeId[0].split("-")[1],
                        processNodeNameEn:this.nodeForm.nameEn,
                        processNodeNameCn:this.nodeForm.nameCn
                    }
                    this.node.confirmLoading=true;
                    if (this.node.title==="新增节点"){
                        url="/api/hip-flowable/api/v1/node/addFlowProcessNode"
                    }else{
                        url="/api/hip-flowable/api/v1/node/updateFlowProcessNode"
                        json.id=this.nodeForm.id;
                    }
                    request(url,{
                        method:"POST",
                        body:json
                    }).then(res=>{
                        this.node.confirmLoading=false;
                        if (this.node.title === "新增节点"&&!res.code){
                            this.node.visible=false;
                            this.$refs.nodeRuleForm.resetFields();
                            this.getParentTree();
                            request("/api/hip-flowable/api/v1/node/getFlowProcessNode", {
                                method: "POST",
                                body: {
                                    flowProcessId: this.nodeId[0].split("-")[1]
                                }
                            }).then(response => {
                                this.nodeData = response;
                            })
                        }else if (this.node.title === "修改节点"&&!res){
                            this.node.visible=false;
                            this.$refs.nodeRuleForm.resetFields();
                            this.getParentTree();
                            request("/api/hip-flowable/api/v1/node/getFlowProcessNode", {
                                method: "POST",
                                body: {
                                    flowProcessId: this.nodeId[0].split("-")[1]
                                }
                            }).then(response => {
                                this.nodeData = response;
                            })
                        }
                    })
                })
            },
            nodeHandleCancel(){
                try {
                    this.$refs.nodeRuleForm.resetFields();
                } catch (e) {

                }
                this.node.visible=false;
            },
            modificationNode(record){
                this.nodeForm.nameCn=record.processNodeNameCn;
                this.nodeForm.nameEn=record.processNodeNameEn;
                this.nodeForm.id=record.id;
                this.node.title="修改节点";
                this.node.visible=true;
            },
            delNode(record){
                let that=this;
                this.$confirm({
                    title: '提示',
                    content: '确认删除当前流程吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk(){
                        request("/api/hip-flowable/api/v1/node/delFlowProcessNode/"+record.id,{
                            method:"DELETE"
                        }).then(res=>{
                            if (res === null) {
                                that.getParentTree();
                                request("/api/hip-flowable/api/v1/node/getFlowProcessNode", {
                                    method: "POST",
                                    body: {
                                        flowProcessId: that.nodeId[0].split("-")[1]
                                    }
                                }).then(res => {
                                    that.nodeData = res;
                                })
                            }
                        })
                    }
                })
            }
        },
        created() {
            this.getParentTree()
        },
    }
</script>

<style scoped lang="less">
    .flowcenter-systemConfiguration {
        padding: 20px;

        .search {
            width: 230px;

            button {
                margin-right: 10px;
            }
        }

        .container {
            margin-top: 20px;
            height: calc(100vh - 210px);
            border: 1px solid #cccccc;
            border-radius: 2px;
            position: relative;

            .container-tree {
                width: 220px;
                height: 100%;
                float: left;
                border-right: 1px solid #cccccc;
                overflow-y: auto;
            }

            .container-box {
                position: absolute;
                top: 0;
                left: 220px;
                right: 0;
                bottom: 0;
                padding: 10px;
                overflow-y: auto;
                transition: 1s;
                .container-header{
                    display: flex;
                    justify-content: space-between;
                    .add {
                        margin-bottom: 10px;
                    }
                }
                .nodeDelails{
                    h5{
                        font-size: 18px;
                        font-weight: bold;
                    }
                    p{
                        padding-left: 20px;
                        padding-top: 6px;
                    }
                }
            }
        }
    }
</style>
