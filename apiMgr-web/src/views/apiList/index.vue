<template>
    <div class="created">
        <header class="clearfix">
            <div class="switch">
                <a-radio-group v-model="index"
                               @change="pagination.current=1;selectedRowKeys=[];time=undefined;getData()">
                    <a-radio-button :value="0">
                        未发布
                    </a-radio-button>
                    <a-radio-button :value="1">
                        已发布
                    </a-radio-button>
                </a-radio-group>
            </div>

            <a-button type="primary" icon="plus" class="createdApi" @click="create">
                创建API
            </a-button>

            <a-select v-model="batch" placeholder="批量操作" v-show="!index" @change="batchOperation" class="batch">
                <a-select-option value="1">
                    批量发布
                </a-select-option>
                <a-select-option value="2">
                    批量删除
                </a-select-option>
                <a-select-option value="3">
                    批量分组
                </a-select-option>
            </a-select>
            <a-button v-show="index" @click="batchOnline">
                批量下线
            </a-button>
            &nbsp;&nbsp;
            <a-button @click="EurekaFlag=true" v-show="enviroment==='staging'">
                注册中心拉取
            </a-button>

            <div class="search">
                <a-select style="width: 120px" v-model="select">
                    <a-select-option value="1">
                        API名称
                    </a-select-option>
                    <a-select-option value="2">
                        创建时间
                    </a-select-option>
                </a-select>
                <a-input v-model.trim="name" v-show="select==='1'"/>
                <a-range-picker v-show="select==='2'" :placeholder="['开始时间','结束时间']" :show-time="{
              defaultValue: [moment('00:00:00', 'HH:mm:ss'), moment('11:59:59', 'HH:mm:ss')],
            }"
                                format="YYYY-MM-DD HH:mm:ss" @change="switchTime"/>
                &nbsp;&nbsp;
                <a-button type="primary" @click="getData">
                    查询
                </a-button>
                &nbsp;&nbsp;
                <a-button @click="reset">重置</a-button>
            </div>
        </header>
        <div class="body">
            <a-table
                    :columns="column"
                    :data-source="list"
                    :rowKey="row=>row.id"
                    :pagination="pagination"
                    @change="handleChange"
                    :loading="loading"
                    size="small"
                    :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
            >
                <a slot="name" slot-scope="text,record" :data-type="record.status">
                    <span class="name" :title="text"
                          @click="details(record)">{{text}}</span>
                </a>
                <div slot="groupId" slot-scope="text,record">
                    {{record.publishApiGroupDto.name}}
                </div>
                <div slot="partition" slot-scope="text">
                    {{text | partition}}
                </div>
                <div slot="bindingPolicyName" slot-scope="text,record" v-show="record.bindingPolicyEnabled ">
                    {{text}}
                </div>
                <div slot="createUserName" slot-scope="text">
                    {{text || "Eureka"}}
                </div>
                <div slot="alertPolicyName" slot-scope="text" style="cursor: pointer;color:#00aaa6">
                    <div @click="jump">{{text}}</div>
                </div>
                <div slot="operation" slot-scope="text,record">
                    <span @click="details(record)" class="operation">详情</span>
                    <span @click="route(record)"
                          :class="{'status':record.status===1,'operation':index}" v-show="index">修改
					</span>
                    <span v-show="!index">
                        <a-tooltip>
                            <template slot="title">
                                当前API正在订阅审批中，无法修改
                            </template>
                            <a-button type="link" style="padding: 0;" disabled v-if="record.status===2">修改</a-button>
                        </a-tooltip>
                        <a-button type="link" style="padding: 0;" v-if="record.status!==2"  @click="Modify(record);">修改</a-button>
                    </span>
                    <a-button type="link" @click="releases(record.id)" v-show="!index" :disabled="record.status===2"
                              style="padding: 0;">
                        发布
                    </a-button>
                    <a-dropdown v-show="!index">
                        <a class="ant-dropdown-link" @click="e => e.preventDefault()">
                            更多
                            <a-icon type="down"/>
                        </a>
                        <a-menu slot="overlay">
                            <a-menu-item>
                                <div @click="limit(record)">流控策略</div>
                            </a-menu-item>
                            <a-menu-item>
                                <div @click="copy(record.id)">复制API</div>
                            </a-menu-item>
                            <a-menu-item :disabled="record.status===2">
                                <div @click="del(text,record.id,record.status)">删除</div>
                            </a-menu-item>
                        </a-menu>
                    </a-dropdown>
                    <a-dropdown v-show="index">
                        <a class="ant-dropdown-link" @click="e => e.preventDefault()">
                            更多
                            <a-icon type="down"/>
                        </a>
                        <a-menu slot="overlay">
                            <a-menu-item>
                                <div @click="limit(record)">流控策略</div>
                            </a-menu-item>
                            <a-menu-item>
                                <div @click="copy(record.id)">复制API</div>
                            </a-menu-item>
                            <a-menu-item>
                                <div @click="publishs(record)"
                                     v-show="$store.state.appNowCategory.gateway[0].key==='staging'">一键发生产
                                </div>
<!--                                <div @click="publishs(record)">一键发生产</div>-->
                            </a-menu-item>
                            <a-menu-item v-show="record.isOnline">
                                <div @click="online(record.id)">下线</div>
                            </a-menu-item>
                        </a-menu>
                    </a-dropdown>
                </div>
            </a-table>
        </div>

        <!--复制到生产弹窗-->
        <a-modal title="一键发生产" :visible="visible" :confirm-loading="confirmLoading" @ok="onSubmit"
                 @cancel="handleCancel"
                 cancelText="取消" okText="直接发布" width="1060px">
            <a-form-model ref="ruleForm" :model="form" :rules="rules" :label-col="labelCol" :wrapper-col="wrapperCol">
                <a-form-model-item label="项目名称" prop="projectName">
                    <a-select default-value="lucy" v-model="form.projectName" placeholder="请选择">
                        <a-select-option v-for="item in systemList" :key="item.id" :value="item.id">
                            {{item.name}}
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
                <a-form-model-item label="API名称" prop="apiName">
                    <a-input v-model="form.apiName" placeholder="请输入"/>
                </a-form-model-item>
                <a-form-model-item label="协议" prop="resource">
                    <a-radio-group v-model="form.resource">
                        <a-radio value="http">
                            HTTP
                        </a-radio>
                        <a-radio value="https">
                            HTTPS
                        </a-radio>
                    </a-radio-group>
                </a-form-model-item>
                <a-form-model-item label="后端服务" prop="serve">
                    <a-input v-model="form.serve" placeholder='服务地址格式：Host:Port或IP:Port'/>
                </a-form-model-item>
                <div style="padding-left: 20px;padding-bottom: 20px">
                    <span style="cursor: pointer" @click="configuration=!configuration"><a-icon :type="configuration?'down':'right'" style="padding-right: 4px" />高级配置</span>
                </div>
                <div v-if="configuration">
                    <a-form-model-item label="描述：" prop="note" v-show="configuration">
                        <a-textarea v-model="form.note" placeholder="请输入" style="width: 90%;" :spellcheck="false"></a-textarea>
                        <p class="prompt" style="font-size: 12px;color: rgba(0, 0, 0, 0.35);line-height: 1;">
                            1-1000个字符</p>
                    </a-form-model-item>
                    <a-form-model-item label="密度等级：" prop="secretLevel">
                        <a-radio-group name="radioGroup4" v-model="form.secretLevel" @change="tabSecretLevel">
                            <a-radio value="低">
                                低
                            </a-radio>
                            <a-radio value="高">
                                高
                            </a-radio>
                        </a-radio-group>
                        <span style="font-size: 12px">密度等级为高的API，用户订阅时需要项目管理员和租户管理员审批</span>
                    </a-form-model-item>
                    <a-form-model-item label="是否需要审批：" prop="subscription">
                        <a-radio-group name="radioGroup" v-model="form.subscription" :disabled="Boolean(form.secretLevel==='高'&&form.subscription)">
                            <a-radio :value="1">
                                是
                            </a-radio>
                            <a-radio :value="0">
                                否
                            </a-radio>
                        </a-radio-group>
                        <span style="font-size: 12px">如果选择否，用户订阅不需要审批</span>
                    </a-form-model-item>
                    <a-form-model-item label="是否鉴权：" prop="authentication">
                        <a-radio-group name="radioGroup2" v-model="form.authentication" :disabled="!form.subscription">
                            <a-radio :value="1">
                                是
                            </a-radio>
                            <a-radio :value="0">
                                否
                            </a-radio>
                        </a-radio-group>
                        <span style="font-size: 12px">如果选择是，调用方调用API时需要传鉴权信息</span>
                    </a-form-model-item>
                    <a-form-model-item label="是否记录日志：" prop="log">
                        <a-radio-group name="radioGroup3" v-model="form.log">
                            <a-radio :value="1">
                                是
                            </a-radio>
                            <a-radio :value="0">
                                否
                            </a-radio>
                        </a-radio-group>
                    </a-form-model-item>
                    <a-form-model-item label="URL前缀：" prop="url">
                        <a-input placeholder="请输入URL前缀" style="width:90%" v-model="form.url"/>
                        <p style="font-size: 12px;color:rgba(0,0,0,0.35);line-height: 1">以/开头，由1～64位字母、数字组成。例如:
                            /pay。建议以系统名称命名，网关转发过程中会替换掉该前缀。</p>
                    </a-form-model-item>
                    <a-form-model-item label="路由规则：" prop="routeRules">
                        <div v-for="(item,index) in form.routeRules" :key="index"
                             style="width:90%;position:relative;margin-top:10px" class="clearfix">
                            <a-select mode="multiple" v-model="item.type" placeholder="请选择" option-label-prop="label"
                                      @change="requesType(item,index)"
                                      style="width: 22%;float:left">
                                <a-select-option value="GET" label="GET">
                                    GET
                                </a-select-option>
                                <a-select-option value="POST" label="POST">
                                    POST
                                </a-select-option>
                                <a-select-option value="PUT" label="PUT">
                                    PUT
                                </a-select-option>
                                <a-select-option value="DELETE" label="DELETE">
                                    DELETE
                                </a-select-option>
                                <a-select-option value="PATCH" label="PATCH">
                                    PATCH
                                </a-select-option>
                                <a-select-option value="HEAD" label="HEAD">
                                    HEAD
                                </a-select-option>
                                <a-select-option value="OPTIONS" label="OPTIONS">
                                    OPTIONS
                                </a-select-option>
                            </a-select>
                            <a-input v-model="item.sample" placeholder="请输入" style="width: 75%;float:right"/>
                            <a-icon type="delete" style="color:rgba(0,0,0,0.25);position:absolute;right:-20px;top: 30%"
                                    v-show="index" @click="form.routeRules.splice(index,1);apiRequireTypeList.splice(index,1)"/>
                        </div>
                        <div class="whitelist" @click="form.routeRules.push({type:[],sample:null});apiRequireTypeList.push([])">
                            <a-icon type="plus-circle"/>
                            添加路由规则
                        </div>
                    </a-form-model-item>
                    <a-form-model-item label="超时时间(s)：" prop="time">
                        <a-input-number v-model="form.time" :min="1"/>
                    </a-form-model-item>
                    <a-form-model-item label="SecretToken" prop="secretToken">
                        <a-input v-model="form.secretToken">
                            <a-tooltip slot="suffix" title="在到后端请求header中增加X-3scale-proxy-secret-token:Secret Token参数，避免用户绕过网关直接调用后端服务，提高服务安全性">
                                <a-icon type="info-circle" style="color: rgba(0,0,0,.45)" />
                            </a-tooltip>
                        </a-input>
                    </a-form-model-item>
                </div>
            </a-form-model>
        </a-modal>
        <!--接口发布弹窗-->
        <a-modal v-model="release" :footer="null" class="created_release" :closable="false" :maskClosable="false">
            <p>
                <svg viewBox="64 64 896 896" data-icon="question-circle" width="1em" height="1em" fill="currentColor"
                     aria-hidden="true"
                     focusable="false" class="">
                    <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 708c-22.1 0-40-17.9-40-40s17.9-40 40-40 40 17.9 40 40-17.9 40-40 40zm62.9-219.5a48.3 48.3 0 0 0-30.9 44.8V620c0 4.4-3.6 8-8 8h-48c-4.4 0-8-3.6-8-8v-21.5c0-23.1 6.7-45.9 19.9-64.9 12.9-18.6 30.9-32.8 52.1-40.9 34-13.1 56-41.6 56-72.7 0-44.1-43.1-80-96-80s-96 35.9-96 80v7.6c0 4.4-3.6 8-8 8h-48c-4.4 0-8-3.6-8-8V420c0-39.3 17.2-76 48.4-103.3C430.4 290.4 470 276 512 276s81.6 14.5 111.6 40.7C654.8 344 672 380.7 672 420c0 57.8-38.1 109.8-97.1 132.5z"></path>
                </svg>
                接口即将发布，修改的内容是否向下兼容？
            </p>
            <p>如果选择不兼容，系统将给订阅该API的用户发送邮件提醒</p>
            <div class="foot clearfix">
                <a-button @click="compatibility('no')">不兼容</a-button>
                &nbsp;&nbsp;
                <a-button @click="compatibility('yes')" type="primary">兼容</a-button>
            </div>
        </a-modal>
        <!--    注册中心拉取-->
        <a-modal v-model="EurekaFlag" title="注册中心拉取" width="660px" okText="确认" cancelText="取消"
                 class="createdEurekaConfiguration"
                 @ok="handleOks">
            <a-radio-group v-model="registry" button-style="solid" style="margin-bottom: 15px;" @click="clearData">
                <a-radio-button value="a">
                    Eureka
                </a-radio-button>
                <a-radio-button value="b">
                    Nacos
                </a-radio-button>
            </a-radio-group>
            <a-form-model ref="registryForm" :model="registryData" :label-col="{span: 5}" :wrapper-col="{span: 17}" v-if="registry==='a'" :rules="registryRules">
                <a-form-model-item label="Eureka地址：" prop="addressEureka">
                    <a-input v-model.trim="registryData.addressEureka" />
                </a-form-model-item>
                <a-form-model-item label="Eureka服务名称：">
                    <a-input v-model.trim="registryData.serveNameEureka" />
                </a-form-model-item>
                <a-form-model-item label="所属分组：" prop="groupEurekaEureka">
                    <a-select placeholder="请选择分组" :showSearch="true" v-model="registryData.groupEurekaEureka">
                        <a-select-option v-for="item in groupList" :value="item.id" :key="item.id">
                            {{item.name}}
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
                <a-form-model-item label="定时任务：" prop="checkEureka">
                    <a-switch v-model="registryData.checkEureka"/>
                    <a-tooltip>
                        <template slot="title">
                            开启后，系统会在每天12:00和24:00时从Eureka拉取API数据至「未发布」列表
                        </template>
                        <a-icon type="question-circle" style="margin-left: 15px;" />
                    </a-tooltip>
                </a-form-model-item>
            </a-form-model>
            <a-form-model ref="registryForm" :model="registryData" :label-col="{span: 5}" :wrapper-col="{span: 17}" v-if="registry==='b'" :rules="registryRules">
                <a-form-model-item label="Nacos地址：" prop="addressNacos">
                    <a-input v-model.trim="registryData.addressNacos" />
                </a-form-model-item>
                <a-form-model-item label="Nacos服务名称：" prop="serveNameNacos">
                    <a-input v-model.trim="registryData.serveNameNacos" />
                </a-form-model-item>
                <a-form-model-item label="所属分组：" prop="groupNacos">
                    <a-select placeholder="请选择分组" :showSearch="true" v-model="registryData.groupNacos">
                        <a-select-option v-for="item in groupList" :value="item.id" :key="item.id">
                            {{item.name}}
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
                <a-form-model-item label="定时任务：">
                    <a-switch v-model="registryData.checkNacos"/>
                    <a-tooltip>
                        <template slot="title">
                            开启后，系统会在每天12:00和24:00时从Eureka拉取API数据至「未发布」列表
                        </template>
                        <a-icon type="question-circle" style="margin-left: 15px;" />
                    </a-tooltip>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal v-model="publish" title="提示" ok-text="确认" cancel-text="取消" @cancel="publish=false;batch=undefined"
                 @ok="publishFun">
            确认发布当前选中的API吗？
        </a-modal>
        <a-modal v-model="batchDel" title="提示" ok-text="确认" cancel-text="取消" @cancel="batchDel=false;batch=undefined"
                 @ok="batchDelFun">
            确认删除当前选中的API吗？
        </a-modal>
        <a-modal v-model="batchGroup" title="提示" ok-text="确认" cancel-text="取消"
                 @cancel="batchGroup=false;group=undefined;batch=undefined"
                 @ok="batchGroupFun">
            <p style="position: relative;top: -12px;">确认为当前选中的API设定分组吗？</p>
            <a-select style="width: 100%" placeholder="请选择分组" :showSearch="true" v-model="group">
                <a-select-option v-for="item in groupList" :value="item.id" :key="item.id">
                    {{item.name}}
                </a-select-option>
            </a-select>
        </a-modal>
        <a-modal title="流控策略" width="440px" :visible="limitVisible" @ok="handleOkLimit" @cancel="limitClose">
            <a-form-model ref="ruleForm" :label-col="labelCol" :wrapper-col="{span:20}" :model="flowControl"
                          :rules="flowControlRules">
                <a-form-model-item label="是否启用：">
                    <a-switch checked-children="开" un-checked-children="关" v-model="flowControl.bool"/>
                </a-form-model-item>
                <a-form-model-item label="限流阈值：" prop="num">
                    <a-input suffix="次" v-model="flowControl.num" placeholder="请输入" :disabled="!flowControl.bool"/>
                </a-form-model-item>
                <a-form-model-item label="时间窗口：" prop="type">
                    <a-select placeholder="请选择" v-model="flowControl.type" :disabled="!flowControl.bool">
                        <a-select-option value="second">
                            秒
                        </a-select-option>
                        <a-select-option value="minute">
                            分钟
                        </a-select-option>
                        <a-select-option value="hour">
                            小时
                        </a-select-option>
                        <a-select-option value="day">
                            天
                        </a-select-option>
                        <a-select-option value="week">
                            周
                        </a-select-option>
                        <a-select-option value="month">
                            月
                        </a-select-option>
                        <a-select-option value="year">
                            年
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal
                v-model="visibles"
                title="自动解析文本"
                @ok="handleOk"
                @cancel="handleCancelText"
                cancelText="取消"
                okText="解析文本"
                width="924px"
                wrapClassName="addApi_uploadFile">
            <a-radio-group name="radioGroup" v-model="format" style="width: 100%">
                <span>文本格式：</span>
                <a-radio value="1">
                    JSON
                </a-radio>
                <a-radio value="2">
                    XML
                </a-radio>
                <a-button type="primary" size="small" style="float: right" @click="fileImport">
                    文件导入
                </a-button>
                <input type="file" v-if="format==='1'" v-show="false" id="file" @change="fileContent($event)"
                       accept="application/json">
                <input type="file" v-if="format==='2'" v-show="false" id="file" @change="fileContent($event)"
                       accept="text/xml, application/xml">
            </a-radio-group>

            <codemirror
                    ref="editor"
                    :value="curCode"
                    :options="cmOptions"
                    v-show="false"
                    class="code">
            </codemirror>
            <a-textarea v-model="curCode" style="height: 260px;margin: 10px 0"/>
        </a-modal>
    </div>
</template>

<script>
    import moment from "moment"
    import request from "../../utils/request.js";
    import {
        filteRequestBody,
        filteRequestParams,
        filteResponseBody, filterIpWhiteList,
        filterText,
        validateRoute,
        xmlObj2json
    } from "../addApi/methods/filters";
    import Cookies from "js-cookie";
    import axios from "axios";
    import {columns, columnsBody, columnsOutput, dataType, rules, groupRules} from "../addApi/methods/data";
    let groupList = [];
    import {codemirror} from 'vue-codemirror'
    import "codemirror/theme/ambiance.css";
    export default {
        name: "created",
        data() {
            return {
                select: '1', //组名称or创建时间
                batch: undefined, //批量发布or批量删除
                name: null, //组名称
                timer: ["", ""], //时间
                index: 0, //已发布or未发布
                visible: false,
                confirmLoading: false,
                labelCol: {
                    span: 4
                },
                wrapperCol: {
                    span: 14
                },
                loading: false,
                form: {
                    projectName: undefined,
                    apiName: null,
                    resource: '1',
                    serve: null,
                    subscription:1,
                    authentication:1,
                    log: 0,
                    record: 1,
                    secretLevel:'低',
                    note: null,
                    url:null,
                    routeRules: [
                        {
                            type: [],
                            sample: null
                        }
                    ],
                    time:0,
                    SecretToken:null
                },
                ipType:1,
                apiRequireTypeList: [],
                uploadList: {
                    a:[],
                    b:[]
                },
                rules: {
                    projectName: [{
                        required: true,
                        message: '请填写项目名称',
                        trigger: 'blur'
                    }],
                    apiName: [{
                        required: true,
                        message: '请填写api名称',
                        trigger: 'blur'
                    },],
                    resource: [{
                        required: true,
                        message: 'Please select activity resource',
                        trigger: 'change'
                    },],
                    serve: [{
                        required: true,
                        message: '请填写后端名称',
                        trigger: 'blur'
                    }],
                    secretLevel:[{
                        required: true,
                        message: '请选择密度等级',
                        trigger: 'blur'
                    }],
                    subscription:[{
                        required: true,
                        message: '请选择是否需要审批',
                        trigger: 'blur'
                    }],
                    log:[{
                        required: true,
                        message: '请选择是否记录日志',
                        trigger: 'blur'
                    }],
                    authentication:[{
                        required: true,
                        message: '请选择是否鉴权',
                        trigger: 'blur'
                    }],
                    url:[
                        {required: true, message: '请输入URL前缀', trigger: 'blur', whitespace: true},
                        {min: 1, max: 64, message: '请输入1-64个字符', trigger: 'blur'},
                        {pattern: /^[/][A-Za-z0-9/-]+$/, message: '请正确输入URL前缀', trigger: 'blur'}
                    ],
                    time:[
                        {required: true, message: '请输入超时时间', trigger: 'blur'},
                        {pattern:/^\d+$/,message: '只支持整数',trigger: ['blur','change']}
                    ],
                    routeRules: [
                        {required: true, message: '请填写路由规则', trigger: 'blur'},
                        {validator: validateRoute, trigger: 'blur'}
                    ],
                    secretToken:[
                        {validator: (rule, value, callback)=>{
                                let reg=/.*[\u4e00-\u9fa5]+.*$/;
                                if(reg.test(value)){
                                    callback(new Error('不允许输入中文'));
                                }
                                callback()
                            }, trigger: 'blur'}
                    ]
                },
                rulesRests:{
                    subscription: [
                        {required: true, message: '请选择是否需要订阅', trigger: 'blur'}
                    ],
                    authentication: [
                        {required: true, message: '请选择是否需要鉴权', trigger: 'blur'}
                    ],
                    log: [
                        {required: true, message: '请选择是否需要记录日志', trigger: 'blur'}
                    ],
                    url: [
                        {required: true, message: '请输入URL前缀', trigger: 'blur', whitespace: true},
                        {min: 1, max: 64, message: '请输入1-64个字符', trigger: 'blur'},
                        {pattern: /^[/][A-Za-z0-9]+$/, message: '请正确输入URL前缀', trigger: 'blur'}
                    ],
                    routeRules: [
                        {required: true, message: '请填写路由规则', trigger: 'blur'},
                        {validator: validateRoute, trigger: 'blur'}
                    ],
                    record: [
                        {required: true, message: '请选择是否记录返回值', trigger: 'blur'}
                    ],
                    secretLevel:[
                        {required: true, message: '请选择等级密度', trigger: 'blur'}
                    ],
                    routeRules: [
                        {required: true, message: '请填写路由规则', trigger: 'blur'},
                        {validator: validateRoute, trigger: 'blur'}
                    ],
                },
                release: false,
                EurekaFlag: false,
                registry:'a',
                registryData:{},
                registryRules:{
                    addressEureka:[
                        {required: true, message: '请输入Eureka地址', trigger: 'blur'}
                    ],
                    groupEurekaEureka:[
                        {required: true, message: '请选择分组', trigger: 'blur'}
                    ],
                    addressNacos:[
                        {required: true, message: '请输入Nacos地址', trigger: 'blur'}
                    ],
                    serveNameNacos:[
                        {required: true, message: '请输入Nacos服务名称', trigger: 'blur'}
                    ],
                    groupNacos:[
                        {required: true, message: '请选择分组', trigger: 'blur'}
                    ]
                },
                list: [], //列表数据
                groupList: [], //分组列表全部数据
                group:null,
                that: this,
                pagination: { //分页信息
                    total: 0,
                    current: 1,
                    defaultCurrent: 1,
                    showSizeChanger: true,
                    defaultPageSize: 10,
                    pageSize: 10,
                    pageSizeOptions: ['10', '20', '30', '40'],
                    showQuickJumper: true,
                    showTotal: ((total) => {
                        return `共 ${total} 条`;
                    }),
                },
                publish: false, //批量发布
                batchDel: false, //批量删除
                batchGroup: false, //批量分组
                selectedRowKeys: [],
                searchData: {
                    group: [],
                    partitions: [],
                    time: undefined,
                    pageSize: 10
                },
                limitVisible: false, //流控策略
                flowControl: {
                    bool: false,
                    num: null,
                    type: undefined
                },
                flowControlRules: {
                    num: [{
                        pattern: /^\d+$/,
                        message: '只支持整数',
                        trigger: 'blur'
                    },
                        {
                            validator: (rule, value, callback) => {
                                if (this.flowControl.bool) {
                                    if (!value) {
                                        callback(new Error('请输入限流阈值'));
                                        return
                                    }
                                }
                                callback();
                            },
                            trigger: 'blur'
                        }
                    ],
                    type: [{
                        validator: (rule, value, callback) => {
                            if (this.flowControl.bool) {
                                if (!value) {
                                    callback(new Error('请选择时间窗口'));
                                    return
                                }
                            }
                            callback();
                        },
                        trigger: 'blur'
                    }]
                },
                systemId: undefined,
                id: null,
                enviroment: JSON.parse(localStorage.getItem("appNowCategory")).gateway[0].key,
                url: null,
                systemList: [],
                apiRequireTypeList: [],
                apiId:null,
                filteredInfo:null,
                configuration:false,
                apiRequestType:0,
                columns,
                columnsBody,
                columnsOutput,
                curCode: '',
                cmOptions: {
                    mode: "text/JavaScript",
                    theme: "ambiance",
                    readOnly: false
                },
                dataType,
                type: 0,
                visibles:false,
                format:"1",
                original_data:null
            }
        },
        components: {
            codemirror
        },
        filters: {
            group(num, that) {
                if (!num) {
                    return "默认"
                } else {
                    for (let i = 0; i < that.groupList.length; i++) {
                        if (num === that.groupList[i].id) {
                            return that.groupList[i].name
                        }
                    }
                }
            },
            partition(num) {
                if (!num) {
                    return "内网"
                } else if (num === 1) {
                    return "外网"
                }
            },
            url(str) {
                if (str && str.length > 17) {
                    return "..." + str.slice(0, 16)
                } else {
                    return str
                }
            }
        },
        computed:{
            column(){
                let {filteredInfo} = this;
                filteredInfo = filteredInfo || {}
                let columns=[
                    {
                        title: 'api名称',
                        dataIndex: 'name',
                        key: 'name',
                        scopedSlots: {
                            customRender: 'name'
                        }
                    },
                    {
                        title: '分组名称',
                        dataIndex: 'groupId',
                        key: 'groupId',
                        scopedSlots: {
                            customRender: 'groupId'
                        },
                        ellipsis: true,
                        filters: groupList,
                        filteredValue: filteredInfo.groupId || null,
                    },
                    {
                        title: '发布环境',
                        dataIndex: 'partition',
                        key: 'partition',
                        scopedSlots: {
                            customRender: 'partition'
                        },
                        filters: [{
                            text: '内网',
                            value: '0'
                        },
                            {
                                text: '外网',
                                value: '1'
                            },
                        ],
                        filterMultiple: false
                    },
                    {
                        title: '流控策略',
                        dataIndex: 'bindingPolicyName',
                        key: 'bindingPolicyName',
                        scopedSlots: {
                            customRender: 'bindingPolicyName'
                        }
                    },
                    {
                        title: '创建者',
                        dataIndex: 'creator',
                        key: 'creator',
                        ellipsis: true,
                        scopedSlots: {
                            customRender: 'createUserName'
                        },
                    },
                    {
                        title: '创建时间',
                        dataIndex: 'createTime',
                        key: 'createTime',
                        sorter: true,
                        width: 180
                    },
                    {
                        title: '告警策略',
                        dataIndex: 'alertPolicyName',
                        key: 'alertPolicyName',
                        ellipsis: true,
                        width: 160,
                        scopedSlots: {
                            customRender: 'alertPolicyName'
                        },
                    },
                    {
                        title: '操作',
                        scopedSlots: {
                            customRender: 'operation'
                        },
                    }
                ];
                return columns
            }
        },
        methods: {
            modification(){
                if (!this.form.projectName){
                    this.$message.error("请选择项目")
                    return
                }
                this.visible=false;
                this.configuration=true
            },
            handleCancel() {
                try {
                    this.$refs.ruleForm.resetFields()
                }catch (e) {

                }
                this.visible = false;
            },
            onSubmit() {
                this.$refs.ruleForm.validate(valid=>{
                    if (!valid) {
                        return
                    }
                    this.confirmLoading=true;
                    let mappingRulesDtos = [];//路由规则
                    if (this.configuration){
                        let apiRequireTypeList = this.apiRequireTypeList.flat();
                        apiRequireTypeList.forEach(item => {
                            let json = {
                                httpMethod: item.type,
                                pattern: item.url,
                            }
                            mappingRulesDtos.push(json)
                        })
                        this.form.routeRules.forEach(item => {
                            item.type.forEach(i => {
                                if (i === "OPTIONS") {
                                    let json = {
                                        httpMethod: i,
                                        pattern: item.sample
                                    }
                                    mappingRulesDtos.push(json)
                                }
                            })
                        })
                    }
                    request(this.url + "/publishApi/prodPromote/" + this.id, {
                        method: "POST",
                        body: {
                            accessProtocol: this.form.resource,
                            apiName: this.form.apiName,
                            host: this.form.serve,
                            projectId: this.form.projectName,
                            secretLevel: this.configuration?this.form.secretLevel:this.original_data.secretLevel,
                            needAuth: this.configuration?Boolean(this.form.authentication):this.original_data.needAuth,//是否鉴权
                            needLogging: this.configuration?Boolean(this.form.log):this.original_data.needLogging,//是否记录日志
                            needRecordRet: this.configuration?Boolean(this.form.record):this.original_data.needRecordRet,//是否记录返回值
                            needSubscribe: this.configuration?Boolean(this.form.subscription):this.original_data.needSubscribe,//是否订阅
                            description: this.configuration?(this.form.note || null):this.original_data.description,//描述
                            apiMappingRuleDtos: this.configuration?mappingRulesDtos:this.original_data.apiMappingRuleDtos,
                            url: this.configuration?this.form.url:this.original_data.url,
                            timeout: this.configuration?this.form.time:this.original_data.timeout,
                            secretToken:this.configuration?this.form.secretToken:this.original_data.secretToken
                        }
                    }).then(res => {
                        if (typeof res === "boolean" && res) {
                            this.confirmLoading = false;
                            this.getData();
                            this.visible = false;
                            this.form.projectName = undefined;
                            this.configuration = false;
                        }else{
                            this.confirmLoading=false;
                        }
                    }).catch(error=>{
                        this.confirmLoading=false;
                    })
                })
            },
            resetForm() {
                this.$refs.ruleForm.resetFields();
            },
            moment,
            switchTime(date, dateString) { //时间插件
                this.timer = dateString;
            },
            releases(id) { //发布
                if (!this.index) {
                    let that = this;
                    this.$confirm({
                        title: '确认要发布该条api吗？',
                        okText: '确认',
                        cancelText: '取消',
                        maskClosable: true,
                        onOk() {
                            request(that.url + "/publishApi/promote", {
                                method: "POST",
                                body: {
                                    id: id,
                                    create: true
                                }
                            }).then(res => {
                                if (res) {
                                    that.getData();
                                }
                            })
                        }
                    });
                } else {
                    this.release = true;
                }
            },
            del(text, id, status) {
                if (status === 2) {
                    return
                }
                let that = this
                this.$confirm({
                    title: '确认要删除该条api吗？',
                    okText: '确认',
                    cancelText: '取消',
                    maskClosable: true,
                    onOk() {
                        request(that.url + "/publishApi/deletePublishApi/" + id, {
                            method: "DELETE"
                        }).then(res => {
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
                request(this.url + "/publishApi", {
                    method: "POST",
                    body: {
                        timeQuery: this.timer[0] ? time : null,
                        groupIds: this.searchData.group.length ? this.searchData.group : null,
                        name: this.name || null,
                        partitions: this.searchData.partitions.length ? this.searchData.partitions : null,
                        published: Boolean(this.index),
                        pageNum: this.pagination.current,
                        pageSize: this.pagination.pageSize,
                        sort: this.searchData.time === "ascend" ? ["a", "createTime"] : ["d", "createTime"]
                    }
                }).then(res => {
                    this.loading = false;
                    this.pagination.total = res.totalElements;
                    this.list = res.content;
                })
            },
            getGroupList() { //获得全部的分组列表数据
                groupList=[];
                this.column[1].filters=[];
                request(this.url + "/publishApiGroup/findPublishApiGroup", {
                    method: "GET"
                }).then(res => {
                    this.groupList = res;
                    res.forEach(item => {
                        groupList.push({
                            text: item.name,
                            value: item.id + ""
                        })
                    })
                    this.column[1].filters=groupList;
                })
            },
            handleChange(pagination, filters, sorter) { //表格的change事件
                this.filteredInfo = filters;
                let json = {
                    group: filters.groupId ? filters.groupId.map(Number) : [],
                    partitions: filters.partition ? filters.partition.map(Number) : [],
                    time: sorter.order,
                    pageSize: pagination.pageSize
                };
                if (
                    this.searchData.pageSize === json.pageSize &&
                    JSON.stringify(this.searchData.group) === JSON.stringify(json.group) &&
                    JSON.stringify(this.searchData.partitions) === JSON.stringify(json.partitions) &&
                    this.searchData.time === json.time
                ) {
                    this.pagination.current = pagination.current;
                    this.getData();
                } else {
                    this.pagination.current = 1;
                    this.pagination.pageSize = json.pageSize;
                    this.searchData = {
                        group: json.group,
                        partitions: json.partitions,
                        time: json.time,
                        pageSize: json.pageSize
                    }
                    this.getData();
                }
            },
            reset() {
                this.name = null;
                this.timer = ["", ""];
                this.select = "1";
                this.getData();
            },
            batchOperation() { //批量操作
                if (!this.selectedRowKeys.length) {
                    this.$message.error("请先勾选API");
                    this.batch = undefined;
                    return
                }
                if (this.batch === "1") { //发布
                    this.publish = true;
                } else if (this.batch === "2") { //删除
                    this.batchDel = true;
                } else if (this.batch === "3") { //分组
                    this.batchGroup = true;
                }
            },
            handleOks() { //注册中心拉取
                this.$refs.registryForm.validate(valid=>{
                    if (!valid){
                        throw new Error("参数校验不通过")
                    }
                    let json={}
                    if(this.registry==='a'){
                        json.eurekaUrl=this.registryData.addressEureka;
                        json.serviceName=this.registryData.serveNameEureka;
                        json.groupId=this.registryData.groupEurekaEureka;
                        json.scheduleEnable=this.registryData.checkEureka || false;
                        json.type="eureka";
                    }else {
                        json.eurekaUrl=this.registryData.addressNacos;
                        json.serviceName=this.registryData.serveNameNacos;
                        json.groupId=this.registryData.groupNacos;
                        json.scheduleEnable=this.registryData.checkNacos || false;
                        json.type="nacos";
                    }
                    json.userId=window.vm.$store.state.user.userId;
                    localStorage.setItem("apiMgrEurekaInfo", JSON.stringify(json))
                    request(this.url + "/eureka/metadata/pullAllApis", {
                        method: "POST",
                        body: json
                    }).then(res => {
                        this.EurekaFlag = false;
                    })
                })
            },
            clearData(){
                try {
                    this.$refs.registryForm.clearValidate();
                }catch (e) {

                }
            },
            compatibility(str) { //确认该API是否向下兼容
                request(this.url + "/publishApi/promote", {
                    method: "POST",
                    body: {
                        id: this.apiId,
                        isCompatible: str,
                        create: false
                    }
                }).then(res => {
                    this.release = false;
                    sessionStorage.removeItem("save")
                })
            },
            publishFun() { //批量发布
                this.batch = undefined;
                request(this.url + "/publishApi/promotePublishApis", {
                    method: "POST",
                    body: {
                        ids: this.selectedRowKeys
                    }
                }).then(res => {
                    this.publish = false;
                    this.selectedRowKeys = [];
                    this.index = 1;
                    this.getData();
                })
            },
            batchDelFun() { //批量删除
                this.batch = undefined;
                request(this.url + "/publishApi/deletePublishApis", {
                    method: "POST",
                    body: {
                        ids: this.selectedRowKeys
                    }
                }).then(res => {
                    this.batchDel = false;
                    this.pagination.current = 1;
                    this.selectedRowKeys = [];
                    this.getData();
                })
            },
            batchGroupFun() { //批量分组
                if (!this.group) {
                    this.$message.error("请选择分组")
                    return
                }
                this.batch = undefined;
                request(this.url + "/publishApi/setGroupForPublishApis", {
                    method: "POST",
                    body: {
                        groupId: this.group,
                        ids: this.selectedRowKeys
                    }
                }).then(res => {
                    this.batchGroup = false;
                    this.group = undefined;
                    this.getData();
                    this.selectedRowKeys = [];
                })
            },
            route(item) { //修改跳转判断
                if (item.status === 2 || item.status === 3) {
                    return
                }
                window.vm.$router.push({
                    path: '/gateway/apiListalterApi',
                    query: {
                        text: '修改API',
                        bool: this.index,
                        id: item.id
                    }
                })
            },
            copy(id) { //复制API
                window.vm.$router.push({
                    path: '/gateway/apiListCopyApi',
                    query: {
                        text: '复制API',
                        bool: 0,
                        id: id
                    }
                })
            },
            online(id) { //单个下线
                request(this.url + "/publishApi/offlinePublishApis", {
                    method: "POST",
                    body: {
                        ids: [id]
                    }
                }).then(res => {
                    this.getData();
                    this.selectedRowKeys=[];
                })
            },
            batchOnline() { //批量下线
                if (!this.selectedRowKeys.length) {
                    this.$message.error("请先勾选API")
                    return
                }
                request(this.url + "/publishApi/offlinePublishApis", {
                    method: "POST",
                    body: {
                        ids: this.selectedRowKeys
                    }
                }).then(res => {
                    this.getData();
                    this.selectedRowKeys=[];
                })
            },
            onSelectChange(selectedRowKeys) {
                this.selectedRowKeys = selectedRowKeys;
            },
            limit(item) { //流控策略
                this.flowControl = {
                    bool: true,
                    num: null,
                    type: undefined,
                    id: null,
                    appId: null
                }
                this.limitVisible = true;
                this.flowControl = {
                    bool: item.hasOwnProperty("bindingPolicyEnabled") ? item.bindingPolicyEnabled : false,
                    num: item.bindingPolicyName ? item.bindingPolicyName.split("/")[0].replace("次", "") - 0 : null,
                    type: item.bindingPolicyName ? this.filtersTime(item.bindingPolicyName.split("/")[1]) : undefined,
                    id: item.bindingPolicyId || null,
                    appId: item.id
                }
            },
            filtersTime(data) {
                if (data === "秒") {
                    return "second"
                }else if (data === "分钟") {
                    return "minute"
                } else if (data === "小时") {
                    return "hour"
                } else if (data === "天") {
                    return "day"
                } else if (data === "周") {
                    return "week"
                } else if (data === "月") {
                    return "month"
                } else if (data === "年") {
                    return "year"
                }else if (data==="second"){
                    return "秒"
                } else if (data === "minute") {
                    return "分钟"
                } else if (data === "hour") {
                    return "小时"
                } else if (data === "day") {
                    return "天"
                } else if (data === "week") {
                    return "周"
                } else if (data === "month") {
                    return "月"
                } else if (data === "year") {
                    return "年"
                }
            },
            handleOkLimit() {
                this.$refs.ruleForm.validate(valid => {
                    if (!valid) {
                        return
                    }
                    if (!this.flowControl.id) {
                        if (!this.flowControl.num){
                            this.limitVisible = false;
                            return;
                        }
                        request(this.url + "/limits/createLimitPolicy", {
                            method: "POST",
                            body: {
                                apiId: this.flowControl.appId, // apiId 必须
                                enabled: this.flowControl.bool,
                                name: this.flowControl.num + "次/" + this.filtersTime(this.flowControl.type),
                                period: this.flowControl.type,
                                value: this.flowControl.num
                            }
                        }).then(res => {
                            this.limitVisible = false;
                            this.getData()
                        })
                    } else {
                        request(this.url + "/limits/updateLimitPolicy/" + this.flowControl.id, {
                            method: "POST",
                            body: {
                                apiId: this.flowControl.appId, // apiId 必须
                                enabled: this.flowControl.bool,
                                name: this.flowControl.num + "次/" + this.filtersTime(this.flowControl.type),
                                period: this.flowControl.type,
                                value: this.flowControl.num
                            }
                        }).then(res => {
                            this.limitVisible = false;
                            this.getData()
                        })
                    }
                });
            },
            limitClose(){
              this.limitVisible=false;
              try {
                  this.$refs.ruleForm.resetFields();
              }catch (e) {

              }
            },
            publishs(record) {
                this.visible = true;
                this.id = record.id;
                this.form.apiName = record.name;
                this.form.resource = record.accessProtocol;
                this.form.serve = record.host;
                request(this.url+"/publishApi/" + record.id).then(res=>{
                    this.original_data=res;
                    this.form.note = res.description;
                    this.form.subscription = Number(res.needSubscribe);
                    this.form.authentication = Number(res.needAuth);
                    this.form.log = Number(res.needLogging);
                    this.form.record = Number(res.needRecordRet);
                    this.form.secretLevel = res.secretLevel;
                    this.form.time = res.timeout;
                    this.form.ipWhiteList = (function (str) {
                        if (!str) {
                            return [{name: null}]
                        }
                        let arr = [];
                        str.forEach(item => {
                            arr.push({
                                name: item.trim()
                            })
                        })
                        return arr
                    })(res.ipWhiteList);
                    this.form.ipBlackList = (function (str) {
                        if (!str) {
                            return [{name: null}]
                        }
                        let arr = [];
                        str.forEach(item => {
                            arr.push({
                                name: item.trim()
                            })
                        })
                        return arr
                    })(res.ipBlackList);
                    if (res.ipWhiteList){
                        this.ipType=1;
                    }else if (res.ipBlackList){
                        this.ipType=2;
                    }
                    this.form.url = res.url;
                    this.form.hostHeader = res.hostHeader || null;
                    this.form.routeRules = (function (data) {
                        let arr = [];
                        data.forEach(item => {
                            arr.push(item.pattern);
                        })
                        arr = Array.from(new Set(arr))
                        let list = [];
                        arr.forEach(item => {
                            let json = {
                                sample: item,
                                type: []
                            };
                            data.forEach(i => {
                                if (item === i.pattern) {
                                    json.type.push(i.httpMethod)
                                }
                            })
                            list.push(json)
                        })
                        list.reverse();
                        return list.reverse();
                    })(res.apiMappingRuleDtos);
                    this.apiRequireTypeList=(function (data) {
                        let arr=[];
                        let type=[];
                        for (let i=0;i<data.length;i++){
                            if (data[i].httpMethod==="OPTIONS"){
                                continue
                            }
                            if (type.indexOf(data[i].pattern)===-1){
                                type.push(data[i].pattern)
                            }
                        }
                        type.forEach(item=>{
                            let array=[];
                            for (let i=0;i<data.length;i++){
                                if (data[i].httpMethod==="OPTIONS"){
                                    continue
                                }
                                if (data[i].pattern===item){
                                    let json={
                                        type: data[i].httpMethod,
                                        url: data[i].pattern,
                                        active: false,
                                        requestParams:requestParam(data[i].requestParams),
                                        requestBody:requestBody(data[i].requestBody),
                                        responseBody:responseBody(data[i].responseBody)
                                    }
                                    array.push(json)
                                }
                            }
                            arr.push(array)
                        })
                        return arr
                    })(res.apiMappingRuleDtos);
                    function requestParam(data) {
                        if (!data) {
                            return [{key: 0, name: null, type: undefined, necessary: undefined, sample: null, describe: null, location: undefined}]
                        }
                        let arr=[]
                        data.forEach((item,index)=>{
                            arr.push({
                                key: index,
                                name: item.name,
                                location: item.paramType || undefined,
                                type: item.dataType || undefined,
                                necessary: item.required + '',
                                sample: item.defaultValue,
                                describe: item.value
                            })
                        })
                        return arr
                    }
                    function requestBody(data) {
                        if (!data) {
                            return [{key: 0, name: null, type: undefined, sample: null, describe: null, level: 0}]
                        }
                        let arr = [];
                        arr.push({
                            key: 0,
                            name: data.name,
                            type: data.dataType || undefined,
                            sample: data.defaultValue,
                            describe: data.value,
                            level: 0
                        })
                        if (data.object) {
                            arr[0].children = [];
                            data.object.forEach((item, index) => {
                                let json = {
                                    key: '0-' + index,
                                    name: item.name,
                                    type: item.dataType || undefined,
                                    sample: item.defaultValue,
                                    describe: item.description,
                                    level: 1,
                                    node: index//node节点，用于删除
                                };
                                if (item.object) {
                                    json.children = [];
                                    item.object.forEach((it, i) => {
                                        let josnTwo = {
                                            key: json.key + '-' + i,
                                            name: it.name,
                                            type: it.dataType || undefined,
                                            sample: it.defaultValue,
                                            describe: it.description,
                                            level: 2,
                                            node: i//node节点，用于删除
                                        }
                                        if (it.object) {
                                            josnTwo.children = [];
                                            it.object.forEach((m, j) => {
                                                let jsonThree = {
                                                    key: josnTwo.key + '-' + j,
                                                    name: m.name,
                                                    type: m.dataType || undefined,
                                                    sample: m.defaultValue,
                                                    describe: m.description,
                                                    level: 3,
                                                    node: j//node节点，用于删除
                                                }
                                                josnTwo.children.push(jsonThree)
                                            })
                                        }
                                        json.children.push(josnTwo)
                                    })
                                }
                                arr[0].children.push(json);
                            })
                        }
                        return arr
                    }
                    function responseBody(data) {
                        if (!data) {
                            return [{key: 0, name: null, type: undefined, sample: null, describe: null, level: 0}]
                        }
                        let arr = [];
                        arr.push({
                            key: 0,
                            name: data.name,
                            type: data.dataType || undefined,
                            sample: data.defaultValue,
                            describe: data.description,
                            level: 0
                        })
                        if (data.object) {
                            arr[0].children = [];
                            data.object.forEach((item, index) => {
                                let json = {
                                    key: '0-' + index,
                                    name: item.name,
                                    type: item.dataType||undefined,
                                    sample: item.defaultValue,
                                    describe: item.description,
                                    level: 1,
                                    node: index//node节点，用于删除
                                };
                                if (item.object) {
                                    json.children = [];
                                    item.object.forEach((it, i) => {
                                        let josnTwo = {
                                            key: json.key + '-' + i,
                                            name: it.name,
                                            type: it.dataType||undefined,
                                            sample: it.defaultValue,
                                            describe: it.description,
                                            level: 2,
                                            node: i//node节点，用于删除
                                        }
                                        if (it.object) {
                                            josnTwo.children = [];
                                            it.object.forEach((m, j) => {
                                                let jsonThree = {
                                                    key: josnTwo.key + '-' + j,
                                                    name: m.name,
                                                    type: m.dataType || undefined,
                                                    sample: m.defaultValue,
                                                    describe: m.description,
                                                    level: 3,
                                                    node: j//node节点，用于删除
                                                }
                                                josnTwo.children.push(jsonThree)
                                            })
                                        }
                                        json.children.push(josnTwo)
                                    })
                                }
                                arr[0].children.push(json);
                            })
                        }
                        return arr
                    }
                    if (this.apiRequireTypeList[0].length > 1) {
                        this.apiRequireTypeList.hidden = true;
                    } else {
                        this.apiRequireTypeList.hidden = false;
                    }
                    if (this.apiRequireTypeList[0].length > 0) {
                        this.apiRequireTypeList[0][0].active = true;
                        this.apiRequireTypeList.type = this.apiRequireTypeList[0][0].type;
                    }
                })
            },
            create() {
                window.vm.$router.push({
                    path: "/gateway/apiListAddApi",
                    query: {text: '创建API', bool: 0}
                })
            },
            details(record) {
                let bool;
                if (record.status === 4) {
                    bool = 1
                } else {
                    bool = 0
                }
                window.vm.$router.push({
                    path: '/gateway/apiDetails',
                    query: {type: 0, id: record.id, partition: record.partition, bool: bool}
                });
            },
            Modify(record) {
                window.vm.$router.push({
                    path: '/gateway/apiListalterApi',
                    query: {text: '修改API', bool: this.index, id: record.id}
                })
            },
            getSyatemList() {
                request("/api/v1/userSystemInfos/production", {
                    method: "GET"
                }).then(res => {
                    this.systemList = res;
                })
            },
            jump() {
                window.vm.$router.push({
                    path: '/gateway/alarm'
                })
            },
            tabSecretLevel(){
                if (this.form.secretLevel==="高"){
                    this.form.subscription=1;
                }
            },
            requesType(item, index) {//路由规则，接口请求类型切换
                let arr = [];
                for (let i = 0; i < item.type.length; i++) {
                    if (item.type[i] !== "OPTIONS") {
                        let json = {
                            type: item.type[i],
                            url: item.sample,
                            active: false,
                            requestParams: [
                                {
                                    key: 0,
                                    name: null,
                                    location: undefined,
                                    type: undefined,
                                    necessary: undefined,
                                    sample: null,
                                    describe: null
                                }
                            ],
                            requestBody: [
                                {
                                    key: 0,
                                    name: null,
                                    type: undefined,
                                    sample: null,
                                    describe: null,
                                    level: 0
                                }
                            ],
                            responseBody: [
                                {
                                    key: 0,
                                    name: null,
                                    type: undefined,
                                    sample: null,
                                    describe: null,
                                    level: 0
                                }
                            ]
                        }
                        arr.push(json)
                    }
                }
                this.apiRequireTypeList[index] = arr;
                if (this.apiRequireTypeList.flat().length > 1) {
                    this.apiRequireTypeList.hidden = true;
                } else {
                    this.apiRequireTypeList.hidden = false;
                }
                if (this.apiRequireTypeList.flat().length > 0) {
                    this.apiRequireTypeList[0][0].active = true;
                    this.apiRequireTypeList.type = this.apiRequireTypeList[0][0].type;
                }
                this.$forceUpdate();
            },
            handleOk() {//弹窗确认
                if (!this.$refs.editor.codemirror.getValue().trim()) {
                    this.$message.error("请填写要解析的文本")
                    return;
                }
                let data;
                try {
                    //这里使用eval解析json格式，避免json数组里面多余逗号引发报错
                    data = eval('(' + this.$refs.editor.codemirror.getValue() + ')')
                    this.curCode = "";
                    if (Object.prototype.toString.call(data) !== '[object Object]') {
                        this.$refs.editor.codemirror.setValue("");
                        this.$message.error("请填写正确的json")
                        return;
                    }
                    this.$refs.editor.codemirror.setValue("");
                    this.visibles = false;
                } catch (e) {
                    this.$refs.editor.codemirror.setValue("");
                    this.$message.error("请填写正确的json")
                    return;
                }
                for (let i = 0; i < this.apiRequireTypeList.length; i++) {
                    for (let j = 0; j < this.apiRequireTypeList[i].length; j++) {
                        if (this.apiRequireTypeList[i][j].active && !this.type) {
                            this.apiRequireTypeList[i][j].requestBody[0].type = 'object';
                            this.apiRequireTypeList[i][j].requestBody[0].children = filterText(data);
                        } else {
                            this.apiRequireTypeList[i][j].responseBody[0].type = 'object';
                            this.apiRequireTypeList[i][j].responseBody[0].children = filterText(data);
                        }
                    }
                }
                this.format = "0";
                let timer = setTimeout(() => {
                    this.format = '1'
                    clearTimeout(timer)
                })//当format控制input file隐藏之后，再重新显示，就可以上传同一个文件了
            },
            handleCancelText() {//弹框取消
                this.$refs.editor.codemirror.setValue("");
                this.format = "0";
                let timer = setTimeout(() => {
                    this.format = '1'
                    clearTimeout(timer)
                })
                this.visible = false;
                this.curCode = "";
            },
            fileImport() {//文件导入按钮
                return file.click()
            },
            fileContent(event) {//获得上传文件的内容
                this.curCode = "";//清空编辑器原有内容
                let resultFile = event.target.files[0]
                this.file = resultFile
                // 获取文件名
                this.fileName = resultFile.name
                // 使用 FileReader 来读取文件
                let reader = new FileReader()
                // 读取纯文本文件,且编码格式为 utf-8
                reader.readAsText(resultFile, 'UTF-8')
                // 读取文件,会触发 onload 异步事件,可使用回调函数 来获取最终的值.
                reader.onload = (e) => {
                    let fileContent = e.target.result;
                    if (this.format === "1") {//1为json文件，2为xml文件
                        this.curCode = fileContent;
                    } else {
                        this.curCode = JSON.stringify(xmlObj2json(fileContent), null, "\t");
                    }
                }
            },
            upload() {//附件形式 点击上传按钮
                return uploadFile.click()
            },
            uploadfiles(event) {//附件形式 上传文件
                if (!event.target.files[0].size){
                    this.$message.error("上传的文件内容为空，无法上传")
                    return;
                }
                this.spinning=true;
                let formData = new FormData();
                formData.append('uploadFile', event.target.files[0]);
                let config = {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        "Access-controt-allow-0rigin":"*",
                        token: this.$ls.get("Access-Token"),
                        Authorization: `Bearer ${this.$ls.get("Access-Token")}`,
                        "current-id": Cookies.get("current-id")
                    }
                }
                let file = event.target.files[0];
                let reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload =(e)=> {

                }
                if (this.apiRequestType===1){
                    let index = !this.uploadList.a.length ? 0 : this.uploadList.a[this.uploadList.a.length - 1].index + 1;
                    this.uploadList.a.push({
                        name: event.target.files[0].name,
                        id: null,
                        width: 0,
                        color: "#00AAA6",
                        index: index
                    })
                    /*
                    * 由于上传完成之前需要进度条递增，又没有数据的ID，所以要用index来找指定的数据
                    * */
                    let num = 0;
                    let timer;
                    for (let i = 0; i < this.uploadList.a.length; i++) {
                        if (this.uploadList.a[i].index === index) {
                            num += 5
                            this.uploadList.a[i].width = num + "%";
                            timer = setInterval(() => {
                                num += 5
                                this.uploadList.a[i].width = num + "%";
                                if (num === 95) {
                                    axios.post(this.url+"/publishApi/uploadApiDocFile/2",formData,config).then(res=>{
                                        this.spinning=false;
                                        if (res.data.data) {
                                            this.uploadList.a[i].width ="100%";
                                            this.uploadList.a[i].id =res.data.data;
                                        }else{
                                            this.uploadList.a=this.uploadList.a.pop()
                                        }
                                    }).catch(res=>{
                                        this.spinning=false;
                                        this.uploadList.a=this.uploadList.a.pop()
                                    })

                                    clearInterval(timer)
                                }
                            }, 50)
                            break
                        }
                    }
                }else{
                    let index = !this.uploadList.b.length ? 0 : this.uploadList.b[this.uploadList.b.length - 1].index + 1;
                    this.uploadList.b.push({
                        name: event.target.files[0].name,
                        id: null,
                        width: 0,
                        color: "#00AAA6",
                        index: index
                    })
                    /*
                    * 由于上传完成之前需要进度条递增，又没有数据的ID，所以要用index来找指定的数据
                    * */
                    let num = 0;
                    let timer;
                    for (let i = 0; i < this.uploadList.b.length; i++) {
                        if (this.uploadList.b[i].index === index) {
                            num += 5
                            this.uploadList.b[i].width = num + "%";
                            timer = setInterval(() => {
                                num += 5
                                this.uploadList.b[i].width = num + "%";
                                if (num === 95) {
                                    axios.post(this.url+"/publishApi/uploadApiDocFile/2",formData,config).then(res=>{
                                        this.spinning=false;
                                        if (res.data.data) {
                                            this.uploadList.b[i].width ="100%";
                                            this.uploadList.b[i].id =res.data.data;
                                        }else{
                                            this.uploadList.b.pop()
                                        }
                                    }).catch(res=>{
                                        this.spinning=false;
                                        this.uploadList.b.pop()
                                    })
                                    clearInterval(timer)
                                }
                            }, 50)
                            break
                        }
                    }
                }
            },
            delFile(item) {//附件形式 文件删除
                if (this.apiRequestType===1){
                    for (let i = 0; i < this.uploadList.a.length; i++) {
                        if (this.uploadList.a[i].index === item.index) {
                            request(this.url+"/publishApi/deleteApiDocFile/"+this.uploadList.a[i].id,{
                                method:"DELETE"
                            }).then(res=>{
                                this.uploadList.a.splice(i, 1);
                            })
                            break
                        }
                    }
                }else{
                    for (let i = 0; i < this.uploadList.b.length; i++) {
                        if (this.uploadList.b[i].index === item.index) {
                            request(this.url+"/publishApi/deleteApiDocFile/"+this.uploadList.b[i].id,{
                                method: "DELETE"
                            }).then(res=>{
                                this.uploadList.b.splice(i, 1);
                            })
                            break
                        }
                    }
                }
            },
            requesType(item, index) {//路由规则，接口请求类型切换
                let arr = [];
                for (let i = 0; i < item.type.length; i++) {
                    if (item.type[i] !== "OPTIONS") {
                        let json = {
                            type: item.type[i],
                            url: item.sample,
                            active: false,
                            requestParams: [
                                {
                                    key: 0,
                                    name: null,
                                    location: undefined,
                                    type: undefined,
                                    necessary: undefined,
                                    sample: null,
                                    describe: null
                                }
                            ],
                            requestBody: [
                                {
                                    key: 0,
                                    name: null,
                                    type: undefined,
                                    sample: null,
                                    describe: null,
                                    level: 0
                                }
                            ],
                            responseBody: [
                                {
                                    key: 0,
                                    name: null,
                                    type: undefined,
                                    sample: null,
                                    describe: null,
                                    level: 0
                                }
                            ]
                        }
                        arr.push(json)
                    }
                }
                this.apiRequireTypeList[index] = arr;
                if (this.apiRequireTypeList[0].length > 1) {
                    this.apiRequireTypeList.hidden = true;
                } else {
                    this.apiRequireTypeList.hidden = false;
                }
                if (this.apiRequireTypeList[0].length > 0) {
                    this.apiRequireTypeList[0][0].active = true;
                    this.apiRequireTypeList.type = this.apiRequireTypeList[0][0].type;
                }
                this.$forceUpdate();
            },
            tab(index, num) {
                this.apiRequireTypeList.forEach(item => {
                    item.forEach(it => {
                        it.active = false
                    })
                })
                this.apiRequireTypeList[index][num].active = true;
                this.$forceUpdate();
            },
            addParameters() {//请求参数-----添加参数
                for (let i = 0; i < this.apiRequireTypeList.length; i++) {
                    for (let j = 0; j < this.apiRequireTypeList[i].length; j++) {
                        if (this.apiRequireTypeList[i][j].active) {
                            this.apiRequireTypeList[i][j].requestParams.push({
                                key: this.apiRequireTypeList[i][j].requestParams.length ? this.apiRequireTypeList[i][j].requestParams[this.apiRequireTypeList[i][j].requestParams.length - 1].key + 1 : 0,
                                name: null,
                                type: undefined,
                                necessary: undefined,
                                sample: null,
                                describe: null
                            })
                            this.$forceUpdate();
                        }
                        break
                    }
                }
            },
            onDelete(key) {//请求参数-----删除数据
                for (let m = 0; m < this.apiRequireTypeList.length; m++) {
                    for (let n = 0; n < this.apiRequireTypeList[m].length; n++) {
                        if (this.apiRequireTypeList[m][n].active) {
                            for (let i = 0; i < this.apiRequireTypeList[m][n].requestParams.length; i++) {
                                if (this.apiRequireTypeList[m][n].requestParams[i].key === key) {
                                    this.apiRequireTypeList[m][n].requestParams.splice(i, 1);
                                    this.$forceUpdate();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            },
            deleteBody(item) {//请求body-----删除节点
                for (let m = 0; m < this.apiRequireTypeList.length; m++) {
                    for (let n = 0; n < this.apiRequireTypeList[m].length; n++) {
                        if (this.apiRequireTypeList[m][n].active) {
                            if (item.level === 1) {//删除1级子节点
                                let fid = item.key.split("-")[0];//截取到父节点
                                for (let i = 0; i < this.apiRequireTypeList[m][n].requestBody.length; i++) {
                                    if (this.apiRequireTypeList[m][n].requestBody[i].key == fid) {
                                        for (let j = 0; j < this.apiRequireTypeList[m][n].requestBody[i].children.length; j++) {
                                            if (item.node === this.apiRequireTypeList[m][n].requestBody[i].children[j].node) {
                                                this.apiRequireTypeList[m][n].requestBody[i].children.splice(j, 1)//删除子节点，不好使
                                                if (!this.apiRequireTypeList[m][n].requestBody[i].children.length) {
                                                    delete this.apiRequireTypeList[m][n].requestBody[i].children
                                                }
                                                this.$forceUpdate();
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            } else if (item.level === 2) {
                                let fid = item.key.split("-")[0];
                                for (let i = 0; i < this.apiRequireTypeList[m][n].requestBody.length; i++) {
                                    if (this.apiRequireTypeList[m][n].requestBody[i].key == fid) {
                                        let pid = item.key.split("-")[1];
                                        for (let j = 0; j < this.apiRequireTypeList[m][n].requestBody[i].children.length; j++) {
                                            if (this.apiRequireTypeList[m][n].requestBody[i].children[j].node == pid) {
                                                for (let m = 0; m < this.apiRequireTypeList[m][n].requestBody[i].children[j].children.length; m++) {
                                                    if (item.node === this.apiRequireTypeList[m][n].requestBody[i].children[j].children[m].node) {
                                                        this.apiRequireTypeList[m][n].requestBody[i].children[j].children.splice(m, 1);
                                                        if (!this.apiRequireTypeList[m][n].requestBody[i].children[j].children.length) {
                                                            delete this.apiRequireTypeList[m][n].requestBody[i].children[j].children
                                                        }
                                                        this.$forceUpdate();
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            } else if (item.level === 3) {
                                let fid = item.key.split("-")[0];
                                for (let i = 0; i < this.apiRequireTypeList[m][n].requestBody.length; i++) {
                                    if (this.apiRequireTypeList[m][n].requestBody[i].key == fid) {
                                        let pid = item.key.split("-")[1];
                                        for (let j = 0; j < this.apiRequireTypeList[m][n].requestBody[i].children.length; j++) {
                                            if (this.apiRequireTypeList[m][n].requestBody[i].children[j].node == pid) {
                                                for (let k = 0; k < this.apiRequireTypeList[m][n].requestBody[i].children[j].children.length; k++) {
                                                    let sid = item.key.split("-")[2];
                                                    if (this.apiRequireTypeList[m][n].requestBody[i].children[j].children[k].node == sid) {
                                                        for (let q = 0; q < this.apiRequireTypeList[m][n].requestBody[i].children[j].children[k].children.length; q++) {
                                                            if (item.node === this.apiRequireTypeList[m][n].requestBody[i].children[j].children[m].children[q].node) {
                                                                this.apiRequireTypeList[m][n].requestBody[i].children[j].children[m].children.splice(q, 1);
                                                                if (!this.apiRequireTypeList[m][n].requestBody[i].children[j].children[k].children.length) {
                                                                    delete this.apiRequireTypeList[m][n].requestBody[i].children[j].children[k].children
                                                                }
                                                                this.$forceUpdate();
                                                                break;
                                                            }
                                                        }
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                        break
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            },
            deleteOutput(item) {//输出参数-------删除节点
                for (let m = 0; m < this.apiRequireTypeList.length; m++) {
                    for (let n = 0; n < this.apiRequireTypeList[m].length; n++) {
                        if (this.apiRequireTypeList[m][n].active) {
                            if (item.level === 1) {//删除1级子节点
                                let fid = item.key.split("-")[0];//截取到父节点
                                for (let i = 0; i < this.apiRequireTypeList[m][n].responseBody.length; i++) {
                                    if (this.apiRequireTypeList[m][n].responseBody[i].key == fid) {
                                        for (let j = 0; j < this.apiRequireTypeList[m][n].responseBody[i].children.length; j++) {
                                            if (item.node === this.apiRequireTypeList[m][n].responseBody[i].children[j].node) {
                                                this.apiRequireTypeList[m][n].responseBody[i].children.splice(j, 1)//删除子节点，不好使
                                                if (!this.apiRequireTypeList[m][n].responseBody[i].children.length) {
                                                    delete this.apiRequireTypeList[m][n].responseBody[i].children
                                                }
                                                this.$forceUpdate();
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            } else if (item.level === 2) {
                                let fid = item.key.split("-")[0];
                                for (let i = 0; i < this.apiRequireTypeList[m][n].responseBody.length; i++) {
                                    if (this.apiRequireTypeList[m][n].responseBody[i].key == fid) {
                                        let pid = item.key.split("-")[1];
                                        for (let j = 0; j < this.apiRequireTypeList[m][n].responseBody[i].children.length; j++) {
                                            if (this.apiRequireTypeList[m][n].responseBody[i].children[j].node == pid) {
                                                for (let m = 0; m < this.apiRequireTypeList[m][n].responseBody[i].children[j].children.length; m++) {
                                                    if (item.node === this.apiRequireTypeList[m][n].responseBody[i].children[j].children[m].node) {
                                                        this.apiRequireTypeList[m][n].responseBody[i].children[j].children.splice(m, 1);
                                                        if (!this.apiRequireTypeList[m][n].responseBody[i].children[j].children.length) {
                                                            delete this.apiRequireTypeList[m][n].responseBody[i].children[j].children
                                                        }
                                                        this.$forceUpdate();
                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            } else if (item.level === 3) {
                                let fid = item.key.split("-")[0];
                                for (let i = 0; i < this.apiRequireTypeList[m][n].responseBody.length; i++) {
                                    if (this.apiRequireTypeList[m][n].responseBody[i].key == fid) {
                                        let pid = item.key.split("-")[1];
                                        for (let j = 0; j < this.apiRequireTypeList[m][n].responseBody[i].children.length; j++) {
                                            if (this.apiRequireTypeList[m][n].responseBody[i].children[j].node == pid) {
                                                for (let k = 0; k < this.apiRequireTypeList[m][n].responseBody[i].children[j].children.length; k++) {
                                                    let sid = item.key.split("-")[2];
                                                    if (this.apiRequireTypeList[m][n].responseBody[i].children[j].children[k].node == sid) {
                                                        for (let q = 0; q < this.apiRequireTypeList[m][n].responseBody[i].children[j].children[k].children.length; q++) {
                                                            if (item.node === this.apiRequireTypeList[m][n].responseBody[i].children[j].children[k].children[q].node) {
                                                                this.apiRequireTypeList[m][n].responseBody[i].children[j].children[k].children.splice(n, 1);
                                                                if (!this.apiRequireTypeList[m][n].responseBody[i].children[j].children[k].children.length) {
                                                                    delete this.apiRequireTypeList[m][n].responseBody[i].children[j].children[k].children
                                                                }
                                                                this.$forceUpdate();
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

            },
            addBodyChildNodes(item) {//请求body------添加子节点
                if (!item.level) {//判断是否是0级参数添加子节点
                    if (!item.children) {
                        item.children = [{
                            key: item.key + '-0',
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 1,
                            node: 0//node节点，用于删除
                        }]
                    } else {
                        let arr = item.children[item.children.length - 1].key.split("-");
                        arr[item.level + 1] = arr[item.level + 1] - 0 + 1;
                        let key = arr.join("-");//拿到key值，切割成数组，最后一项加1，最后转换成字符串
                        item.children.push({
                            key: key,
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 1,
                            node: arr[item.level + 1] - 0
                        })
                    }
                } else if (item.level === 1) {//判断是1级子节点
                    if (!item.children) {
                        item.children = [{
                            key: item.key + '-0',
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 2,
                            node: 0
                        }]
                    } else {
                        let arr = item.children[item.children.length - 1].key.split("-");
                        arr[item.level + 1] = arr[item.level + 1] - 0 + 1;
                        let key = arr.join("-");
                        item.children.push({
                            key: key,
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 2,
                            node: arr[item.level + 1] - 0
                        })
                    }
                } else if (item.level === 2) {//判断是2级子节点
                    if (!item.children) {
                        item.children = [{
                            key: item.key + '-0',
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 3,
                            node: 0
                        }]
                    } else {
                        let arr = item.children[item.children.length - 1].key.split("-");
                        arr[item.level + 1] = arr[item.level + 1] - 0 + 1;
                        let key = arr.join("-");
                        item.children.push({
                            key: key,
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 3,
                            node: arr[item.level + 1] - 0
                        })
                    }
                }
                this.$forceUpdate();
            },
            addOutputChildNodes(item) {//输出参数添加子节点
                if (!item.level) {//判断是否是0级参数添加子节点
                    if (!item.children) {
                        item.children = [{
                            key: item.key + '-0',
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 1,
                            node: 0//node节点，用于删除
                        }]
                    } else {
                        let arr = item.children[item.children.length - 1].key.split("-");
                        arr[item.level + 1] = arr[item.level + 1] - 0 + 1;
                        let key = arr.join("-");//拿到key值，切割成数组，最后一项加1，最后转换成字符串
                        item.children.push({
                            key: key,
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 1,
                            node: arr[item.level + 1] - 0
                        })
                    }
                } else if (item.level === 1) {//判断是1级子节点
                    if (!item.children) {
                        item.children = [{
                            key: item.key + '-0',
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 2,
                            node: 0
                        }]
                    } else {
                        let arr = item.children[item.children.length - 1].key.split("-");
                        arr[item.level + 1] = arr[item.level + 1] - 0 + 1;
                        let key = arr.join("-");
                        item.children.push({
                            key: key,
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 2,
                            node: arr[item.level + 1] - 0
                        })
                    }
                } else if (item.level === 2) {//判断是2级子节点
                    if (!item.children) {
                        item.children = [{
                            key: item.key + '-0',
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 3,
                            node: 0
                        }]
                    } else {
                        let arr = item.children[item.children.length - 1].key.split("-");
                        arr[item.level + 1] = arr[item.level + 1] - 0 + 1;
                        let key = arr.join("-");
                        item.children.push({
                            key: key,
                            name: null,
                            type: undefined,
                            sample: null,
                            describe: null,
                            location: undefined,
                            level: 3,
                            node: arr[item.level + 1] - 0
                        })
                    }
                }
                this.$forceUpdate();
            },
            parameter(text, item, field) {//请求参数------表格内容参数修改
                item[field] = text;
                this.$forceUpdate();
            },
            submit() {//表单提交
                this.$refs.ruleForm.validate(valid => {
                    if (valid) {
                        this.iconLoading=true;
                        let mappingRulesDtos = [];//路由规则
                        let apiRequireTypeList = this.apiRequireTypeList.flat();
                        apiRequireTypeList.forEach(item => {
                            let json = {
                                httpMethod: item.type,
                                pattern: item.url,
                                requestParams: filteRequestParams(item.requestParams),
                                responseBody: filteResponseBody(item.responseBody)
                            }
                            if (item.type !== "GET" || item.type !== "DELETE") {
                                json.requestBody = filteRequestBody(item.requestBody)
                            }
                            mappingRulesDtos.push(json)
                        })
                        this.form.routeRules.forEach(item => {
                            item.type.forEach(i => {
                                if (i === "OPTIONS") {
                                    let json = {
                                        httpMethod: i,
                                        pattern: item.sample
                                    }
                                    mappingRulesDtos.push(json)
                                }
                            })
                        })
                        let uploadfiles=[];
                        if (this.form.image){
                            uploadfiles.push(this.form.image)
                        }
                        if (this.uploadList.a){
                            this.uploadList.a.forEach(item=>{
                                uploadfiles.push(item.id)
                            })
                        }
                        if (this.uploadList.b){
                            this.uploadList.b.forEach(item=>{
                                uploadfiles.push(item.id)
                            })
                        }
                        let json = {
                            accessProtocol: this.form.agreement,//协议
                            description: this.form.note || null,//描述
                            groupId: this.form.grouping,//分组ID
                            host: this.form.serve,//后端服务
                            ipWhiteList: this.ipType===1?filterIpWhiteList(this.form.ipWhiteList):null,//IP白名单
                            ipBlackList: this.ipType===2?filterIpWhiteList(this.form.ipBlackList):null,//IP黑名单
                            apiMappingRuleDtos: mappingRulesDtos,//路由规则
                            name: this.form.name,//api名称
                            needAuth: Boolean(this.form.authentication),//是否鉴权
                            needLogging: Boolean(this.form.log),//是否记录日志
                            needRecordRet: Boolean(this.form.record),//是否记录返回值
                            needSubscribe: Boolean(this.form.subscription),//是否订阅
                            partition: this.form.environment,//发布环境 0内 1外
                            timeout: this.form.time,//超时时间
                            url: this.form.url,//URL前缀
                            hostHeader: this.form.hostHeader || null,
                            fileDocIds:uploadfiles,
                            secretLevel:this.form.secretLevel,//等级密度
                        }
                        let url;
                        if (this.$route.query.text !== "修改API") {
                            url = "/publishApi/createPublishApi"
                        } else {
                            url = "/publishApi/updatePublishApi/" + this.$route.query.id
                            json.proxy = this.proxy
                        }
                        request(this.url+url,{
                            method:"POST",
                            body:json
                        }).then(res=>{
                            if (this.$route.query.text!== "修改API"){
                                if ((typeof res==="number")&&res){
                                    this.release=true;
                                    this.apiId=res;
                                }else {
                                    this.iconLoading=false;
                                }
                            }else{
                                if ((typeof res==="boolean")&&res){
                                    if (this.$route.query.bool-0){
                                        sessionStorage.setItem("save", 1);//这里设置save，主要是区分该发布的api是否选择向下兼容，这个字段存在，就是为选择向下兼容
                                    }
                                    window.vm.$multiTab.close(window.location.pathname+window.location.search);
                                    sessionStorage.setItem("addApi",JSON.stringify({id:this.$route.query.id,publishApi:this.$route.query.bool==1?true:false}))
                                    window.vm.$router.push({
                                        path:"/gateway/apiList/apiListCreated",
                                        // query:{
                                        //     id: this.$route.query.id,
                                        //     publishApi:this.$route.query.bool==1?true:false
                                        // }
                                    })
                                }else {
                                    this.iconLoading=false;
                                }
                            }
                        })
                    }
                })
            },
        },
        created() {
            let data = JSON.parse(localStorage.getItem("appNowCategory")).gateway;
            this.url = `/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getSyatemList();
            if (sessionStorage.getItem("group")) { //从分组的api数量里面跳进来，默认选择点击的分组
                this.columns[1].filteredValue = [sessionStorage.getItem("group") + ""];
                this.filteredInfo = [sessionStorage.getItem("group") + ""];
                this.searchData.group = [sessionStorage.getItem("group") - 0];
                sessionStorage.removeItem("group");
                this.getData()
            }else {
                this.getData()
            }
            this.getGroupList();
            if (JSON.parse(localStorage.getItem("apiMgrEurekaInfo")) && JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).userId == window.vm.$store.state.user.userId ) {
                if (JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).type==='eureka'){
                    this.registryData.addressEureka=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).eurekaUrl;
                    this.registryData.serveNameEureka=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).serviceName;
                    this.registryData.checkEureka=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).scheduleEnable;
                }else{
                    this.registryData.addressNacos=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).eurekaUrl;
                    this.registryData.serveNameNacos=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).serviceName;
                    this.registryData.checkNacos=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).scheduleEnable;
                }
            }
        },
        mounted() {
            if (sessionStorage.getItem("save")) { //添加api有相应解释
                window.addEventListener('beforeunload', () => {
                    this.compatibility("yes")
                });
            }
        },
        activated() {
            this.getGroupList();
            if (sessionStorage.getItem("addApi")&&JSON.parse(sessionStorage.getItem("addApi")).publishApi) {
                this.index = 1;
                this.getData();
            } else {
                this.getData()
            }
            if (sessionStorage.getItem("addApi") && JSON.parse(sessionStorage.getItem("addApi")).id && sessionStorage.getItem("save")) {
                this.release = true;
                this.index = 1;
                this.apiId=JSON.parse(sessionStorage.getItem("addApi")).id;
                sessionStorage.removeItem("addApi")
            }
        },
        beforeDestroy() {
            groupList = [];
            this.columns[1].defaultFilteredValue = [];
            if (sessionStorage.getItem("save")) {
                this.compatibility("yes")
            }
        },
        watch: {
            "$store.state.appNowCategory": {
                deep: true,
                handler: function (oldValue,newValue) {
                    if (JSON.stringify(oldValue)===JSON.stringify(newValue)){
                        return;
                    }
                    this.$multiTab.closeAll();
                    return;
                    this.selectedRowKeys=[];
                    this.columns[1].defaultFilteredValue = [];
                    this.searchData.group = [];
                    this.filteredInfo=null;
                    this.enviroment = this.$store.state.appNowCategory.gateway[0].key;
                    this.url = `/apimgr/api/v1/tenant/tenant_id_1/project/${this.$store.state.appNowCategory.gateway[1].id}/${this.$store.state.appNowCategory.gateway[0].key}`
                    if (window.vm.$route.name === 'apiListCreated') {
                        this.pagination.current=1;
                        this.pagination.pageSize=10;
                        this.getData();
                        this.getGroupList();
                        try {
                            if (JSON.parse(localStorage.getItem("apiMgrEurekaInfo")) && JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).userId == window.vm.$store.state.user.userId ) {
                                if (JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).type==='eureka'){
                                    this.registryData.addressEureka=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).eurekaUrl;
                                    this.registryData.serveNameEureka=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).serviceName;
                                    this.registryData.checkEureka=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).scheduleEnable;
                                }else{
                                    this.registryData.addressNacos=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).eurekaUrl;
                                    this.registryData.serveNameNacos=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).serviceName;
                                    this.registryData.checkNacos=JSON.parse(localStorage.getItem("apiMgrEurekaInfo")).scheduleEnable;
                                }
                            }
                        }catch (e) {
                            this.registryData={}
                        }
                    }
                }
            }
        },
    }
</script>

<style scoped lang="less">
    .created {
        header {
            padding: 16px 16px 0;

            .switch {
                float: left;
                background: #ffffff;
                margin-right: 16px;
            }

            .createdApi {
                margin-right: 8px;
            }

            .search {
                float: right;

                .ant-input {
                    width: 200px;
                }
            }

            .batch {
                width: 120px;
            }
        }

        .body {
            min-height: calc(~"100vh - 163px");
            background-color: #ffffff;
            padding: 16px;

            .status {
                color: #00aaa67a;
                cursor: not-allowed;
            }
        }
    }
</style>
<!--以下为修改UI框架部分-->
<style>
    ul,li{
        padding: 0;
        margin: 0;
        list-style: none;
    }
    .created_compile {
        text-align: center;
        width: 100%;
        height: 40px;
        background: rgba(255, 255, 255, 1);
        border-radius: 4px;
        border: 1px solid rgba(217, 217, 217, 1);
        line-height: 40px;
        cursor: pointer;
        color: #000000;
    }

    .created_release .ant-modal-body p:nth-child(1) {
        font-size: 16px;
        color: #000000;
    }

    .created_release .ant-modal-body p:nth-child(1) svg {
        color: #FAAD14;
        font-size: 22px;
        position: relative;
        top: 5px;
    }

    .created_release .ant-modal-body p:nth-child(2) {
        font-size: 14px;
        color: #000;
        padding: 12px 0 24px 27px;
    }

    .created_release .ant-modal-body .foot {
        text-align: right;
    }

    .created_release .ant-modal-body .foot button:nth-child(2) {
        background: #D9D9D9;
        color: #F5222D;
    }

    .body .operation {
        color: #00aaa6;
        cursor: pointer;
        margin-right: 4px;
    }

    .batch .ant-select-selection__placeholder {
        color: #5b5454;
    }

    .publishApiModel .methodType{
        padding: 0 24px 18px;
    }
    .publishApiModel .methodType ul{
        float: left;
    }
    .publishApiModel .methodType li{
        width: 188px;
        height: 32px;
        padding: 0 8px;
        line-height: 32px;
        float: left;
        margin-right: 8px;
        background-color: #EEF3F2;
        color: #8DB6B5;
        border-radius: 1px;
        margin-bottom: 10px;
        cursor: pointer;
        user-select: none;
    }
    .publishApiModel .methodType li>span{
        display: block;
        width: 100%;
        height: 100%;
    }
    .publishApiModel .methodType li>.url{
        float: right;
        width: 120px;
        overflow: hidden;
    }
    .publishApiModel .methodType li.active{
        background-color: #00AAA6;
        color: #ffffff;
    }
    .publishApiModel .upload{
        padding-left: 24px;
    }
    .publishApiModel .upload>div:nth-child(1)>span{
        font-size: 14px;
        padding-left: 24px;
        color: rgba(0, 0, 0, 0.35);
    }
    .publishApiModel .upload>ul:nth-child(2){
        margin-top: 12px;
    }
    .publishApiModel .upload>ul:nth-child(2) li{
        width: 166px;
    }
    .publishApiModel .upload>ul:nth-child(2) li{
        width: 166px;
    }
    .publishApiModel .upload>ul:nth-child(2) li>i{
        float: left;
        font-size: 18px;
        margin-right: 7px;
        margin-top: 2px;
        color: rgba(0, 0, 0, 0.45);
    }
    .publishApiModel .upload>ul:nth-child(2) li>div{
        float: left;
        width: 140px;
        font-size: 14px;
        color: rgba(0, 0, 0, 0.45);
        margin-bottom: 8px;
    }
    .publishApiModel .upload>ul:nth-child(2) li>div p:nth-child(1){
        padding-bottom: 8px;
    }
    .publishApiModel .upload>ul:nth-child(2) li>div p:nth-child(1) span:nth-child(1){
        float: left;
        width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .publishApiModel .upload>ul:nth-child(2) li>div p:nth-child(2){
        width: 100%;
        height: 2px;
        background: rgba(0, 0, 0, 0.04);
        border-radius: 32px;
        margin-top: 1px;
    }
    .publishApiModel .upload>ul:nth-child(2) li>div p:nth-child(2) span{
        float: left;
        height: 100%;
        border-radius: 32px;
        background-color: #00AAA6;
        transition: 0.3s;
    }
    .publishApiModel .table{
        margin-bottom: 30px;
    }
    .publishApiModel .table .headline{
        width: 95%;
        margin: 0 auto;
        color: #000;
        position: relative;
        font-size: 14px;
        padding-left: 10px;
    }
    .publishApiModel .table .headline:before{
        position: absolute;
        top: 4px;
        left: 0;
        width: 4px;
        height: 14px;
        background: #00AAA6;
        content: "";
    }
    .publishApiModel .table .table-body{
        width: 95%;
        margin: 10px auto 0;
    }
    .publishApiModel .table .table-body :nth-child(3)[data-type="0"] {
        float: right;
        width: 85%;
    }
    .publishApiModel .table .table-body :nth-child(3)[data-type="1"] {
        float: right;
        width: 74%;
    }
    .publishApiModel .table .table-body :nth-child(3)[data-type="2"] {
        float: right;
        width: 61%;
    }
    .publishApiModel .table .table-body :nth-child(3)[data-type="3"] {
        float: right;
        width: 52%;
    }
    .publishApiModel .table .table-body .addApiAction .delete {
        color: #00AAA6;
        font-size: 14px;
        cursor: pointer;
        line-height: 34px;
        float: left;
        margin-right: 10px;
    }
    .publishApiModel .table .adds{
        font-size: 12px;
        color: #00AAA6;
        border-bottom: none !important;
        cursor: pointer;
        display: inline-block;
    }
</style>
