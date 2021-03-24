<template>
    <div class="addApi">
        <header>
            <a-button icon="left" @click="goBack">返回</a-button>
            <span>当前页面：{{$route.query.text}}</span>
        </header>
        <div class="form_body">
            <ul class="anchor">
                <li :class="{'active':active==='1'}" @click="toTarget('1')">基本信息</li>
                <li :class="{'active':active==='2'}" @click="toTarget('2')">规则配置</li>
                <li :class="{'active':active==='3'}" @click="toTarget('3')">定义API请求</li>
                <li :class="{'active':active==='4'}" @click="toTarget('4')">后端服务配置</li>
            </ul>
            <div class="form">
                <a-form-model :model="form" ref="ruleForm" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
                    <div id="1">
                        <p class="titles">基本信息</p>
                        <a-form-model-item label="所属分组：" prop="grouping" ref="group" :autoLink="false">
                            <a-select v-model="form.grouping" placeholder="请选择分组" class="grouping" @change="getGroupName">
                                <a-select-option v-for="item in groupList" :value="item.id" :key="item.id">
                                    {{item.name}}
                                </a-select-option>
                            </a-select>
                            <a-button icon="sync" class="refresh" @click="getGroupList">刷新</a-button>
                            <span class="add" @click="group=true">新建分组</span>
                        </a-form-model-item>
                        <a-form-model-item label="API名称：" prop="name">
                            <a-input v-model="form.name" class="apiname" placeholder="请输入API名称" :spellcheck="false" />
                            <p class="prompt" style="font-size: 12px;color: rgba(0, 0, 0, 0.35);line-height: 1;">
                                支持汉字、英文、数字或中划线，3-64个字符</p>
                        </a-form-model-item>
                        <a-form-model-item label="描述：" prop="note">
                            <a-textarea v-model="form.note" placeholder="请输入" style="width: 90%;" :spellcheck="false"></a-textarea>
                            <p class="prompt" style="font-size: 12px;color: rgba(0, 0, 0, 0.35);line-height: 1;">
                                1-1000个字符</p>
                        </a-form-model-item>
                        <a-form-model-item label="封面图片">
                            <div class="uploadImage clearfix">
                                <div @click="uploadbtn">
                                    <a-icon type="plus"/>
                                    <p>上传图片</p>
                                    <div v-show="imageUrl">
                                        <img :src="imageUrl">
                                        <div>
                                            <img src="../../assets/eye.png" @click.stop="visibleImage=true">
                                            <a-popconfirm
                                                    title="确认删除当前上传的图片？"
                                                    ok-text="确认"
                                                    cancel-text="取消"
                                                    @confirm="delImg"
                                            >
                                                <img src="../../assets/delete.png" @click.stop="">
                                            </a-popconfirm>
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    上传符合API功能定位的图片可以更好的引导用户产生订阅行为<br/>
                                    图片尺寸200*100px，格式jpg、png，大小不超过500K
                                </div>
                                <input type="file" v-show="false" id="uploadimg" @change="uploadPictures($event)" accept="image/png, image/jpeg">
                            </div>
                        </a-form-model-item>
                        <a-form-model-item label="发布环境" prop="environment">
                            <a-select v-model="form.environment" placeholder="请选择" style="width: 240px;" :disabled="$route.query.text==='修改API'">
                                <a-select-option :value="0" label="内网网关">
                                    内网网关
                                </a-select-option>
                                <a-select-option :value="1" label="外网网关">
                                    外网网关
                                </a-select-option>
                            </a-select>
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
                            <span class="prompt">密度等级为高的API，用户订阅时需要项目管理员和租户管理员审批</span>
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
                            <span class="prompt">如果选择否，用户订阅不需要审批</span>
                        </a-form-model-item>
                        <a-form-model-item label="是否鉴权：" prop="authentication">
                            <a-radio-group name="radioGroup2" v-model="form.authentication" :disabled="!form.subscription" @change="handldChange">
                                <a-radio :value="1">
                                    是
                                </a-radio>
                                <a-radio :value="0">
                                    否
                                </a-radio>
                            </a-radio-group>
                            <span class="prompt">如果选择是，调用方调用API时需要传鉴权信息</span>
                        </a-form-model-item>
                        <a-form-model-item label="是否记录日志：" prop="log">
                            <a-radio-group name="radioGroup3" v-model="form.log" :disabled="!form.authentication">
                                <a-radio :value="1">
                                    是
                                </a-radio>
                                <a-radio :value="0">
                                    否
                                </a-radio>
                            </a-radio-group>
                        </a-form-model-item>
                        <a-form-model-item label="记录出入参" prop="record" v-if="form.log===1">
                            <a-radio-group v-model="form.record">
                                <a-radio :value="1">
                                    是
                                </a-radio>
                                <a-radio :value="0">
                                    否
                                </a-radio>
                            </a-radio-group>
                        </a-form-model-item>
                        <div style="padding-left: 100px">
                            <span style="cursor: pointer;transition: 0.5s" @click="flag=!flag">
                                <a-icon type="right" v-show="!flag"/><a-icon type="down" v-show="flag" />
                                &nbsp;&nbsp;
                                <span>高级配置</span>
                            </span>
                        </div>
                        <a-form-model-item label="接口类型" v-show="flag">
                            <a-radio-group v-model="form.accessProType">
                                <a-radio value="http">
                                    http
                                </a-radio>
                                <a-radio value="webservice">
                                    webservice
                                </a-radio>
                            </a-radio-group>
                        </a-form-model-item>
                        <a-form-model-item label="SecretToken" v-show="flag" prop="secretToken">
                            <a-input v-model.trim="form.secretToken">
                                <a-tooltip slot="suffix" title="在到后端请求header中增加X-3scale-proxy-secret-token:Secret Token参数，避免用户绕过网关直接调用后端服务，提高服务安全性">
                                    <a-icon type="info-circle" style="color: rgba(0,0,0,.45)" />
                                </a-tooltip>
                            </a-input>
                        </a-form-model-item>
                        <a-form-model-item label="白名单/黑名单" v-show="flag">
                            <a-radio-group name="radioGroup" v-model="ipType">
                                <a-radio :value="1">
                                    白名单
                                </a-radio>
                                <a-radio :value="2">
                                    黑名单
                                </a-radio>
                            </a-radio-group>
                            <div v-if="ipType===1">
                                <a-textarea style="height: 1.5cm;" v-model.trim="form.ipWhiteList" placeholder="请输入IP白名单，多个IP以英文逗号分隔"/>
                            </div>
                            <div v-if="ipType===2">
                                <a-textarea style="height: 1.5cm;" v-model.trim="form.ipBlackList" placeholder="请输入IP黑名单，多个IP以英文逗号分隔"/>
                            </div>
                        </a-form-model-item>
                        <a-form-model-item label="requestHeader" v-show="flag">
                            <a-table :columns="columnsRequestHeader" :data-source="form.requestHeader"
                                     :pagination="{hideOnSinglePage:true}" size="small">
                                <div slot="header" slot-scope="text,record">
                                    <a-input v-model.trim="text" placeholder="请填写名称" @change="parameter(text,record,'header')"></a-input>
                                </div>
                                <div slot="value" slot-scope="text,record">
                                    <a-input v-model.trim="text" placeholder="请填写值" @change="parameter(text,record,'value')"></a-input>
                                </div>
                                <div slot="count" slot-scope="text,record">
                                    <a-button type="link" @click="delParames(record,0)">删除</a-button>
                                </div>
                            </a-table>
                            <a-button type="link" @click="addParamer(0)">增加参数</a-button>
                        </a-form-model-item>
                        <a-form-model-item label="responseHeader" v-if="false">
                            <a-table :columns="columnsRequestHeader" :data-source="form.responsetHeader"
                                     :pagination="{hideOnSinglePage:true}" size="small">
                                <div slot="header" slot-scope="text,record">
                                    <a-input v-model.trim="text" placeholder="请填写名称" @change="parameter(text,record,'header')"></a-input>
                                </div>
                                <div slot="value" slot-scope="text,record">
                                    <a-input v-model.trim="text" placeholder="请填写名称" @change="parameter(text,record,'value')"></a-input>
                                </div>
                                <div slot="op" slot-scope="text,record">
                                    <a-select v-model="text" placeholder="请选择" @change="parameter(text,record,'op')">
                                        <a-select-option value="add">
                                            add
                                        </a-select-option>
                                        <a-select-option value="header">
                                            header
                                        </a-select-option>
                                        <a-select-option value="push">
                                            push
                                        </a-select-option>
                                        <a-select-option value="delete">
                                            delete
                                        </a-select-option>
                                    </a-select>
                                </div>
                                <div slot="valueType" slot-scope="text,record">
                                    <a-select v-model="text" placeholder="请选择" @change="parameter(text,record,'valueType')">
                                        <a-select-option value="plain">
                                            plain
                                        </a-select-option>
                                        <a-select-option value="liquid">
                                            liquid
                                        </a-select-option>
                                    </a-select>
                                </div>
                                <div slot="count" slot-scope="text,record">
                                    <a-button type="link" @click="delParames(record,1)">删除</a-button>
                                </div>
                            </a-table>
                            <a-button type="link" @click="addParamer(1)">增加参数</a-button>
                        </a-form-model-item>
                    </div>
                    <div id="2">
                        <p class="titles">规则配置</p>
                        <a-form-model-item label="URL前缀：" prop="url">
                            <a-input placeholder="请输入URL前缀" style="width:90%" v-model="form.url"/>
                            <p style="font-size: 12px;color:rgba(0,0,0,0.35);line-height: 1">以/开头，由1～64位字母、数字组成。例如:
                                /pay。建议以系统名称命名，网关转发过程中会替换掉该前缀。</p>
                        </a-form-model-item>
                        <a-form-model-item label="路由规则：" prop="routeRules">
                            <div v-for="(item,index) in form.routeRules" :key="index"
                                 style="width:90%;position:relative;margin-top:10px" class="clearfix">
                                <a-select mode="multiple" v-model="item.type" placeholder="请选择" option-label-prop="label"
                                          @change="requesType(item,index)" style="width: 22%;float:left">
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
                                <a-input v-model="item.sample" placeholder="请输入" style="width: 75%;float:right"
                                         @blur="requesType(item,index)"/>
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
                        <a-form-item label="Host Header：">
                            <a-input style="width: 90%;" v-model="form.hostHeader"/>
                            <a-tooltip>
                                <template slot="title">
                                    例如输入:api.hisense.com
                                </template>
                                <a-icon type="question-circle" style="margin-left:10px"/>
                            </a-tooltip>
                            <p style="font-size: 12px;color:rgba(0,0,0,0.35);line-height: 1">自定义Host请求头，用于API后端仅接受特定域名请求的场景。</p>
                        </a-form-item>
                    </div>
                    <div id="3">
                        <p class="titles">定义API请求
                            <a-radio-group v-model="apiRequestType">
                                <a-radio-button :value="0">
                                    在线配置(推荐)
                                </a-radio-button>
                                <a-radio-button :value="1">
                                    API文档
                                </a-radio-button>
                                <a-radio-button :value="2">
                                    SDK上传
                                </a-radio-button>
                            </a-radio-group>
                        </p>
                        <div class="methodType clearfix" v-show="apiRequireTypeList.hidden">
                            <!--为数组扩展自定义属性，控制API请求类型列表是否显示-->
                            <ul v-for="(item,index) in apiRequireTypeList" :key="index">
                                <li v-for="(it,num) in item" :ket="num" :class="{'active':it.active}" @click="tab(index,num)">
                                    <a-tooltip>
                                        <template slot="title">
                                            {{it.url}}
                                        </template>
                                        {{it.type}} <span class="url">{{it.url|url}}</span>
                                    </a-tooltip>
                                </li>
                            </ul>
                        </div>
                        <div v-for="(item,index) in apiRequireTypeList" :key="index" v-show="!apiRequestType">
                            <div v-for="(it,num) in item" :key="num" v-show="apiRequireTypeList[index][num].active">
                                <div class="table">
                                    <p class="headline">请求参数</p>
                                    <a-form-model-item :label-col="{ span: 0 }" :wrapper-col="{ span: 24}">
                                        <div class="table-body">
                                            <a-table :columns="columns" :data-source="apiRequireTypeList[index][num]['requestParams']"
                                                     :pagination="{hideOnSinglePage:true}" size="small">
                                                <a slot="name" slot-scope="text,record">
                                                    <a-input v-model.trim="text" placeholder="请填写名称" @change="parameter(text,record,'name')"></a-input>
                                                </a>
                                                <a slot="location" slot-scope="text,record">
                                                    <a-select v-model="text" placeholder="请选择参数位置" @change="parameter(text,record,'location')">
                                                        <a-select-option value="path">
                                                            path
                                                        </a-select-option>
                                                        <a-select-option value="query">
                                                            query
                                                        </a-select-option>
                                                        <a-select-option value="header">
                                                            header
                                                        </a-select-option>
                                                    </a-select>
                                                </a>
                                                <a slot="type" slot-scope="text,record">
                                                    <a-select v-model="text" placeholder="请选择类型" @change="parameter(text,record,'type')">
                                                        <a-select-option value="string">
                                                            string
                                                        </a-select-option>
                                                        <a-select-option value="int">
                                                            int
                                                        </a-select-option>
                                                        <a-select-option value="boolean">
                                                            boolean
                                                        </a-select-option>
                                                        <a-select-option value="byte">
                                                            byte
                                                        </a-select-option>
                                                        <a-select-option value="char">
                                                            char
                                                        </a-select-option>
                                                        <a-select-option value="short">
                                                            short
                                                        </a-select-option>
                                                        <a-select-option value="float">
                                                            float
                                                        </a-select-option>
                                                        <a-select-option value="double">
                                                            double
                                                        </a-select-option>
                                                        <a-select-option value="long">
                                                            long
                                                        </a-select-option>
                                                    </a-select>
                                                </a>
                                                <a slot="necessary" slot-scope="text,record">
                                                    <a-select v-model="text" placeholder="请选择" @change="parameter(text,record,'necessary')">
                                                        <a-select-option value="true">
                                                            必填
                                                        </a-select-option>
                                                        <a-select-option value="false">
                                                            选填
                                                        </a-select-option>
                                                    </a-select>
                                                </a>
                                                <a slot="sample" slot-scope="text,record">
                                                    <a-input v-model="text" placeholder="请填写示例"
                                                             @change="parameter(text,record,'sample')"></a-input>
                                                </a>
                                                <a slot="describe" slot-scope="text,record">
                                                    <a-input v-model="text" placeholder="请填写描述"
                                                             @change="parameter(text,record,'describe')"></a-input>
                                                </a>
                                                <template slot="operation" slot-scope="text, record">
                                                    <div class="addApiAction">
                                                        <span class="delete" @click="onDelete(record.key)">删除</span>
                                                    </div>
                                                </template>
                                            </a-table>
                                            <div class="adds" @click="addParameters">
                                                <a-icon type="plus-circle"/>
                                                添加参数
                                            </div>
                                        </div>
                                    </a-form-model-item>
                                </div>
                                <div class="table" v-show="it.type!=='GET'&&it.type!=='DELETE'">
                                    <p class="headline">请求body
                                        <a-button @click="visible=true;type=0" type="primary" size="small" style="float: right">
                                            自动解析文本
                                        </a-button>
                                    </p>
                                    <a-form-model-item :label-col="{ span: 0 }" :wrapper-col="{ span: 24}">
                                        <div class="table-body">
                                            <a-table :columns="columnsBody" :data-source="apiRequireTypeList[index][num]['requestBody']"
                                                     :pagination="{hideOnSinglePage:true}" size="small">
                                                <a slot="name" slot-scope="text,record" :data-type="record.level">
                                                    <a-input v-model="text" v-show="record.level" placeholder="请输入名称"
                                                             @change="parameter(text,record,'name')"/>
                                                    <div style="color: #333333" v-show="!record.level">requestBody</div>
                                                </a>
                                                <a slot="type" slot-scope="text,record">
                                                    <a-select v-model="text" placeholder="请选择" @change="parameter(text,record,'type')">
                                                        <a-select-option v-for="item in dataType" :value="item" :key="item">
                                                            {{item}}
                                                        </a-select-option>
                                                    </a-select>
                                                </a>
                                                <a slot="sample" slot-scope="text,record">
                                                    <a-input v-model="text" placeholder="请输入示例" @change="parameter(text,record,'sample')"/>
                                                </a>
                                                <a slot="describe" slot-scope="text,record">
                                                    <a-input v-model="text" placeholder="请输入描述" @change="parameter(text,record,'describe')"/>
                                                </a>
                                                <div slot="operation" slot-scope="text,record">
                                                    <div class="addApiAction">
                                                        <span class="delete" @click="addBodyChildNodes(record)" v-show="record.level<=2">添加子节点</span>
                                                        <span class="delete" @click="deleteBody(record)" v-show="record.level">删除</span>
                                                    </div>
                                                </div>
                                            </a-table>
                                        </div>
                                    </a-form-model-item>
                                </div>
                                <div class="table">
                                    <p class="headline">输出参数
                                        <a-button type="primary" @click="visible=true;type=1" size="small" style="float: right">
                                            自动解析文本
                                        </a-button>
                                    </p>
                                    <a-form-model-item :label-col="{ span: 0 }" :wrapper-col="{ span: 24}">
                                        <div class="table-body">
                                            <a-table :columns="columnsOutput" :data-source="apiRequireTypeList[index][num]['responseBody']"
                                                     :pagination="{hideOnSinglePage:true}" size="small">
                                                <a slot="name" slot-scope="text,record" :data-type="record.level">
                                                    <a-input v-model="text" placeholder="请输入名称" @change="parameter(text,record,'name')"
                                                             v-show="record.level"/>
                                                    <div v-show="!record.level" style="color: #333333">responseBody</div>
                                                </a>
                                                <a slot="type" slot-scope="text,record">
                                                    <a-select v-model="text" placeholder="请选择" @change="parameter(text,record,'type')">
                                                        <a-select-option value="string">
                                                            string
                                                        </a-select-option>
                                                        <a-select-option value="int">
                                                            int
                                                        </a-select-option>
                                                        <a-select-option value="boolean">
                                                            boolean
                                                        </a-select-option>
                                                        <a-select-option value="byte">
                                                            byte
                                                        </a-select-option>
                                                        <a-select-option value="char">
                                                            char
                                                        </a-select-option>
                                                        <a-select-option value="short">
                                                            short
                                                        </a-select-option>
                                                        <a-select-option value="float">
                                                            float
                                                        </a-select-option>
                                                        <a-select-option value="double">
                                                            double
                                                        </a-select-option>
                                                        <a-select-option value="long">
                                                            long
                                                        </a-select-option>
                                                        <a-select-option value="object">
                                                            object
                                                        </a-select-option>
                                                        <a-select-option value="Array">
                                                            Array
                                                        </a-select-option>
                                                        <a-select-option value="List">
                                                            List
                                                        </a-select-option>
                                                        <a-select-option value="Set">
                                                            Set
                                                        </a-select-option>
                                                    </a-select>
                                                </a>
                                                <a slot="sample" slot-scope="text,record">
                                                    <a-input v-model="text" placeholder="请输入示例" @change="parameter(text,record,'sample')"/>
                                                </a>
                                                <a slot="describe" slot-scope="text,record">
                                                    <a-input v-model="text" placeholder="请输入描述" @change="parameter(text,record,'describe')"/>
                                                </a>
                                                <div slot="operation" slot-scope="text,record">
                                                    <div class="addApiAction">
                                                        <span class="delete" @click="addOutputChildNodes(record)" v-show="record.level<=2">添加子节点</span>
                                                        <span class="delete" @click="deleteOutput(record)" v-show="record.level">删除</span>
                                                    </div>
                                                </div>
                                            </a-table>
                                        </div>
                                    </a-form-model-item>
                                </div>
                            </div>
                        </div>
                        <div class="upload" v-if="apiRequestType===1">
                            <div>
                                <a-button icon="upload" @click="upload">
                                    上传按钮
                                </a-button>
                                <span>支持扩展名：.doc .docx .xls .xlsx .txt .pdf</span>
                                <input
                                        type="file"
                                        id="uploadFile"
                                        v-show="false"
                                        @change="uploadfiles($event)"
                                        accept="
                  application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,
                  application/vnd.ms-excel,
                  text/plain,
                  application/pdf,
                  application/msword,
                  application/vnd.openxmlformats-officedocument.wordprocessingml.document
                  "
                                >
                            </div>
                            <ul>
                                <li class="clearfix" v-for="item in uploadList.a" :key="item.index">
                                    <a-icon type="paper-clip"/>
                                    <div>
                                        <p class="clearfix">
                                            <a-tooltip>
                                                <template slot="title">
                                                    {{item.name}}
                                                </template>
                                                <span>{{item.name}}</span>
                                            </a-tooltip>
                                            <a-popconfirm
                                                    title="确认删除当前上传的附件？"
                                                    ok-text="确认"
                                                    cancel-text="取消"
                                                    @confirm="delFile(item)"
                                            >
                                                <a-icon type="close"/>
                                            </a-popconfirm>
                                        </p>
                                        <p><span :style="{'width':item.width,'color':item.color}"></span></p>
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <div class="upload" v-if="apiRequestType===2">
                            <div>
                                <a-button icon="upload" @click="upload">
                                    上传按钮
                                </a-button>
                                <span>只支持扩展名：.jar</span>
                                <input
                                        type="file"
                                        id="uploadFile"
                                        v-show="false"
                                        @change="uploadfiles($event)"
                                        accept=".jar"
                                >
                            </div>
                            <ul>
                                <li class="clearfix" v-for="item in uploadList.b" :key="item.index">
                                    <a-icon type="paper-clip"/>
                                    <div>
                                        <p class="clearfix">
                                            <a-tooltip>
                                                <template slot="title">
                                                    {{item.name}}
                                                </template>
                                                <span>{{item.name}}</span>
                                            </a-tooltip>
                                            <a-popconfirm
                                                    title="确认删除当前上传的附件？"
                                                    ok-text="确认"
                                                    cancel-text="取消"
                                                    @confirm="delFile(item)"
                                            >
                                                <a-icon type="close"/>
                                            </a-popconfirm>
                                        </p>
                                        <p><span :style="{'width':item.width,'color':item.color}"></span></p>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div id="4">
                        <p class="titles">后端配置服务</p>
                        <a-form-model-item label="协议：" prop="agreement">
                            <a-radio-group name="radioGroup" v-model="form.agreement">
                                <a-radio value="http">
                                    http
                                </a-radio>
                                <a-radio value="https">
                                    https
                                </a-radio>
                            </a-radio-group>
                        </a-form-model-item>
                        <a-form-model-item label="后端服务：" prop="serve">
                            <a-input placeholder='请输入' v-model="form.serve" style="width:90%"/>
                            <p class="prompt" style="font-size: 12px;color: rgba(0, 0, 0, 0.35);line-height: 1;">
                                服务地址格式："Host:Port"或“IP:Port”</p>
                        </a-form-model-item>
                    </div>
                </a-form-model>
            </div>
        </div>

        <a-modal
                v-model="visible"
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
            <a-textarea v-model="curCode" class="code"/>
        </a-modal>

        <a-modal
                title="图片预览"
                :visible="visibleImage"
                @cancel="visibleImage=false"
                :footer="null"
        >
            <img :src="imageUrl" style="display: block;margin: 10px auto;max-width: 90%" />
        </a-modal>

        <a-modal title="新建分组" :visible="group" :confirm-loading="confirmLoading"
                 okText="确认" cancelText="取消" width="600px" @ok="handleSubmit" @cancel="handleCancel">
            <a-form-model :model="groupForm" ref="form" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }"
                          :rules="groupRules">
                <a-form-model-item label="组名称" prop="name">
                    <a-input placeholder="请输入" v-model="groupForm.name"/>
                </a-form-model-item>
                <a-form-model-item label="描述" prop="note">
                    <a-textarea placeholder="请输入" v-model="groupForm.note"/>
                </a-form-model-item>
                <a-form-model-item label="所属类别" prop="two" ref="two" :autoLink="false">
                    <a-select placeholder="请选择" style="width: 220px;margin-right:20px" v-model="groupForm.one" @change="two()">
                        <a-select-option v-for="item in categoryOne" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                    <a-select placeholder="请选择" style="width: 220px;" v-model="groupForm.two" @change="system">
                        <a-select-option v-for="item in categoryTwo" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                    <div class="ant-form-explain" style="color: #f5222d" v-show="categoryRouter">请选择二级类目</div>
                </a-form-model-item>
                <a-form-model-item label="所属系统" prop="system">
                    <a-select placeholder="请选择" v-model="groupForm.system">
                        <a-select-option v-for="item in systemList" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                </a-form-model-item>
            </a-form-model>
        </a-modal>

        <a-modal v-model="release" :title="null" :footer="null" :closable="false" :maskClosable="false" :keyboard="false" wrapClassName="releaseApi">
            <div class="info clearfix">
                <svg class="icon" style="width: 1em; height: 1em;vertical-align: middle;fill: currentColor;overflow: hidden;" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1965"><path d="M512 0C228.266667 0 0 228.266667 0 512c0 283.733333 228.266667 512 512 512 283.733333 0 512-228.266667 512-512C1024 228.266667 795.733333 0 512 0zM832 384 492.8 723.2C469.333333 746.666667 426.666667 746.666667 403.2 723.2L192 512c0 0-32-32 0-64s64 0 64 0l192 192 320-320c0 0 32-32 64 0S832 384 832 384z" p-id="1966"></path></svg>
                <ul>
                    <li>{{$route.query.text==="修改API"?"修改":"创建"}}成功，是否要发布API？</li>
                    <li>发布后的API将会立即支持订阅</li>
                </ul>
            </div>
            <div class="footer">
                <a-button @click="goBack">
                    完成
                </a-button>
                &nbsp;&nbsp;
                <a-button type="primary" @click="publishApi()">
                    发布
                </a-button>
            </div>
        </a-modal>

        <div class="form_footer">
            <a-button type="primary" @click="submit" :loading="iconLoading" v-if="$route.query.text==='创建API'||$route.query.text==='复制API'">创建</a-button>
            <a-button type="primary" @click="submit" :loading="iconLoading" v-else>{{($route.query.bool-0)?"保存并发布":"修 改"}}</a-button>
            <a-button @click="goBack()">取 消</a-button>
        </div>

        <a-spin :spinning="spinning"/>
    </div>
</template>

<script>
    //word文档、pdf文档、excel文档，txt文档
    import axios from "axios"
    import request from "../../utils/request"
    import {
        filteRequestParams,
        filteRequestBody,
        filteResponseBody,
        filterText,
        xmlObj2json,
        filteRequestHeader,
    } from "./methods/filters";
    import Cookies from "js-cookie";
    import {columns, columnsBody, columnsOutput, dataType, rules, groupRules,columnsRequestHeader,columnsResponsetHeader} from "./methods/data";

    import {codemirror} from 'vue-codemirror'
    import Vue from "vue";
    import "codemirror/theme/ambiance.css";  // 这里引入的是主题样式，根据设置的theme的主题引入，一定要引入！！
    //require("codemirror/mode/JavaScript/JavaScript"); // 这里引入的模式的js，根据设置的mode引入，一定要引入！！
    let id;
    let text;
    let bool;
    export default {
        name: "addApi",
        data() {
            return {
                form: {
                    grouping: undefined,//新建分组
                    name: null,//api名称
                    note: null,//描述
                    image: null,//封面图片
                    environment: undefined,//发布环境
                    subscription: 1,//是否需要订阅
                    authentication: 1,//是否鉴权
                    log: 1,//是否记录日志
                    record: 0,//记录返回值
                    secretLevel:'低',//等级密度
                    ipWhiteList: undefined,//IP白名单
                    ipBlackList: undefined,
                    url: null,//url前缀
                    routeRules: [
                        {
                            type: [],//接口类型
                            sample: null//示例文字
                        }
                    ],//路由规则
                    time: 120,//超时时间
                    hostHeader: null,//Host Header
                    agreement: "http",//协议
                    serve: null,//后端服务
                    requestHeader:[{
                        op:undefined,
                        header:null,
                        valueType:undefined,
                        value:null,
                        key:0
                    }],
                    responsetHeader:[{
                        op:undefined,
                        header:null,
                        valueType:undefined,
                        value:null,
                        key:0
                    }],
                    accessProType:'http',
                    secretToken:null,//secretToken
                },
                groupList: [],//分组列表
                rules,
                visible: false,
                active: null,
                format: "1",//自动解析文本的文件格式
                columns,
                columnsBody,
                columnsOutput,
                columnsRequestHeader,
                columnsResponsetHeader,
                curCode: '',
                cmOptions: {
                    mode: "text/JavaScript",
                    theme: "ambiance",
                    readOnly: false
                },
                type: 0,//自动解析文本按钮，0为请求参数，1为输出参数
                group: false,
                confirmLoading: false,
                groupForm: {
                    name: null,//组名称
                    note: null,//描述
                    one: undefined,//一级类目
                    two: undefined,//二级类目
                    system: undefined//所属系统
                },//新建分组数据
                groupRules:{
                    name: [
                        {required: true, message: '请输入组名称', trigger: 'blur'},
                        {min: 1, max: 60, message: '请不要输入超过60个字符', trigger: 'blur'},
                        {pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9-\u4e00-\u9fa5]+$/, message: '请正确填入组名称', trigger: ['change', 'blur']}
                    ],
                    note: [
                        {min: 0, max: 1000, message: '请不要输入超过1000个字符', trigger: 'blur'},
                    ],
                    two: [
                        {validator:(rule, value, callback)=> {
                                if (!this.groupForm.one&&!this.groupForm.two){
                                    callback(new Error('请选择一级类目和二级类目'))
                                }
                                callback();
                            },trigger: 'change'}
                    ],
                    system: [
                        {required: true, message: '请选择所属系统', trigger: 'blur'},
                    ]
                },//新建分组校验
                categoryOne: [],//一级类目
                categoryTwo: [],//二级类目
                systemList: [],//系统列表
                proxy: null,//修改接口需要的数据，从api详情接口里面来，不需要显示
                apiRequestType: 0,//定义API请求，参数展示类型
                apiRequireTypeList: [],//定义api请求，路由规则配置了多少个请求方式
                uploadList: {//上传的文件列表
                    a:[],
                    b:[]
                },//上传列表
                dataType,
                imageUrl:null,
                visibleImage:false,
                iconLoading:false,
                release:false,
                spinning:false,
                ipType:1,
                url:null,
                apiId:null,
                categoryRouter:false,
                flag:false
            }
        },
        filters: {
            url(str) {
                if (str && str.length > 17) {
                    return "..." + str.slice(0, 16)
                } else {
                    return str
                }
            }
        },
        components: {
            codemirror
        },
        mounted() {
            //用于锚点定位的滚动监听
            // 一次性计算赋值，减少滚动计算节点位置次数
            /*this.distance_team = document.getElementById('2').offsetTop - 14
            this.distance_contact = document.getElementById('3').offsetTop - 14
            this.distance_join = document.getElementById('4').offsetTop - 14
            this.$nextTick(function () {
                document.querySelector('.container').addEventListener('scroll', this.onScroll)
            })
            window.addEventListener('scroll', this.onScroll, true)*/
        },
        methods: {
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
                    this.visible = false;
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
            toTarget(target) {//用于锚点定位
                this.active = target
                const toElement = document.getElementById(target)
                toElement.scrollIntoView(this.scrollIntoViewOptions)
            },
            onScroll() {//用于锚点定位的滚动监听
                const scrolled = window.pageYOffset;
                if (scrolled < this.distance_team) {
                    this.active = '1'
                } else if (scrolled >= this.distance_team && scrolled < this.distance_contact) {
                    this.active = '2'
                } else if (scrolled >= this.distance_contact && scrolled < this.distance_join && this.distance_join - scrolled > 498) {
                    /*
                    * if里面最后一项判断是判断浏览器时候滑动到底部
                    * */
                    this.active = '3'
                } else {
                    this.active = '4'
                }
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
                    console.log(e)
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
                    //console.log(e.target.result) 获得二进制文件流
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
                            ipWhiteList: this.ipType===1&&this.form.ipWhiteList?this.form.ipWhiteList.split(","):null,//IP白名单
                            ipBlackList: this.ipType===2&&this.form.ipBlackList?this.form.ipBlackList.split(","):null,//IP黑名单
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
                            requestHeader:filteRequestHeader(this.form.requestHeader),
                            accessProType:this.form.accessProType,
                            secretToken:this.form.secretToken || null
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
                                    if (this.$multiTab){
                                        this.$multiTab.closeCurrentPage();
                                    }
                                    sessionStorage.setItem("addApi",JSON.stringify({id:this.$route.query.id,publishApi:this.$route.query.bool==1?true:false}))
                                    setTimeout(()=>{
                                        window.vm.$router.push({
                                            path:"/gateway/apiList/apiListCreated",
                                        })
                                    })
                                }else {
                                    this.iconLoading=false;
                                }
                            }
                        })
                    }
                })
            },
            publishApi(){
                request(this.url + "/publishApi/promote", {
                    method: "POST",
                    body: {
                        id: this.apiId,
                        create: true
                    }
                }).then(res => {
                    this.release=false;
                    if ((typeof res==="boolean")&&res) {
                        window.vm.$multiTab.close(window.location.pathname+window.location.search);
                        sessionStorage.setItem("addApi",JSON.stringify({publishApi:true}))
                        window.vm.$router.push({
                            path:"/gateway/apiList/apiListCreated",
                        })
                    }
                })
            },
            getGroupList() {
                request(this.url+"/publishApiGroup/findPublishApiGroup",{
                    method:"GET"
                }).then(res=>{
                    this.groupList = res;
                    if (res.length){
                        this.form.grouping=res[0].id
                    }
                    if (this.$route.query.text==="创建API"){
                        request(this.url+'/publishApiGroup/searchPublishApiGroup',{
                            method:"POST",
                            body:{
                                name:this.groupList[0].name,
                                pageNum:1,
                                pageSize:1
                            }
                        }).then(response=>{
                            this.form.url='/'+response.content[0].systemEnName;
                        })
                    }
                })
            },
            handleSubmit() {//新建分组/
                this.$refs.form.validate(valid => {
                    if (valid) {
                        request(this.url+"/publishApiGroup/createPublishApiGroup",{
                            method:"POST",
                            body: {
                                categoryOne: this.groupForm.one,
                                categoryTwo: this.groupForm.two,
                                name: this.groupForm.name,
                                description: this.groupForm.note,
                                system: this.groupForm.system
                            }
                        }).then(res=>{
                            this.groupForm.one = undefined;
                            this.$refs.form.resetFields()
                            this.group = false;
                            this.getGroupList()
                        })
                    }else{
                        if (this.groupForm.one&&!this.groupForm.two){
                            this.categoryRouter=true;
                        }
                    }
                });
            },
            handleCancel() {//新建分组取消
                this.categoryRouter=false;
                this.group = false;
                this.groupForm.one = undefined;
                this.$refs.form.resetFields()
            },
            one() {//查询一级类目
                request(this.url+"/dataItems/categoryOne/findDataItems",{
                    method:"GET"
                }).then(res=>{
                    this.categoryOne = res;
                })
            },
            two() {//查询二级类目
                this.systemList = [];
                this.categoryTwo = [];
                this.groupForm.two = undefined;
                this.groupForm.system = undefined;
                request(this.url+"/dataItems/categoryTwo/findDataItemsByParentId/" + this.groupForm.one,{
                    method:"GET"
                }).then(res=>{
                    this.categoryTwo = res
                })
                this.$refs.two.onFieldChange();
            },
            system() {//查询所属系统
                this.systemList = [];
                this.groupForm.system = undefined;
                request(this.url+"/dataItems/system/findSystemDataItems?categoryOne="+this.groupForm.one+"&categoryTwo="+this.groupForm.two,{
                    method:"GET"
                }).then(res=>{
                    this.systemList = res
                })
                this.$refs.two.onFieldChange()
            },
            getDetail() {//修改返显参数
                if (!this.$route.query.id) {
                    return
                }
                request(this.url+"/publishApi/" + this.$route.query.id,{
                    method:"GET"
                }).then(res=>{
                    this.form.grouping = res.groupId || undefined;
                    this.form.name = this.$route.query.text === "复制API" ? res.name + "-副本" : res.name;
                    this.form.note = res.description;
                    this.form.environment = res.partition;
                    this.form.subscription = Number(res.needSubscribe);
                    this.form.authentication = Number(res.needAuth);
                    this.form.log = Number(res.needLogging);
                    this.form.record = Number(res.needRecordRet);
                    this.form.secretLevel = res.secretLevel;
                    this.form.secretToken = res.secretToken;
                    this.proxy = res.proxy;
                    this.form.ipWhiteList = res.ipWhiteList?res.ipWhiteList.join(","):undefined;
                    this.form.ipBlackList = res.ipBlackList?res.ipBlackList.join(","):undefined;
                    if (res.ipWhiteList){
                        this.ipType=1;
                    }else if (res.ipBlackList){
                        this.ipType=2;
                    }
                    this.form.url = res.url;
                    this.form.time = res.timeout;
                    this.form.hostHeader = res.hostHeader || null;
                    this.form.agreement = res.accessProtocol || "http";
                    this.form.serve = res.host;
                    this.form.accessProType=res.accessProType;
                    this.form.requestHeader=(function (data) {
                        if (data){
                            data.forEach((item,index)=>{
                                item.key=index
                                item.value=item.value.replace("{{","").replace("}}","")
                                item.op=item.op||undefined
                                item.valueType=item.valueType||undefined
                            })
                        }
                        return data||[{op: undefined,value: null,header: null,valueType: undefined,key: 0}]
                    })(res.requestHeader)
                    this.form.responsetHeader=(function (data) {
                        if (data){
                            data.forEach((item,index)=>{
                                item.key=index;
                                item.value=item.value.replace("{{","").replace("}}","")
                                item.op=item.op||undefined
                                item.valueType=item.valueType||undefined
                            })
                        }
                        return data||[{op: undefined,value: null,header: null,valueType: undefined,key: 0}]
                    })(res.responseHeader)
                    if (this.$route.query.text !== "复制API"){
                        this.form.image = (res.picFiles&&res.picFiles.length)?res.picFiles[0].id:null;
                        if (this.form.image){
                            request(this.url+"/publishApi/showApiDocFile/"+this.form.image,{
                                method:"GET"
                            }).then(res=>{
                                this.imageUrl=res;
                            })
                        }
                        this.uploadList=((list)=>{
                            let arr1=[];//API文档列表
                            let arr2=[];//jar包列表
                            list.forEach((item,index)=>{
                                if (item.fileName.indexOf(".jar")<0){
                                    arr1.push({
                                        name:item.fileName,
                                        id:item.id,
                                        width:'100%',
                                        color:"#00AAA6",
                                        index:index
                                    })
                                }else{
                                    arr2.push({
                                        name:item.fileName,
                                        id:item.id,
                                        width:'100%',
                                        color:"#00AAA6",
                                        index:index
                                    })
                                }
                            })
                            let json={};
                            json.a=arr1
                            json.b=arr2;
                            return json
                        })(res.attFiles)
                    }
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
                        return list
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
                        return arr.reverse();
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
            uploadbtn(){//封面图片点击上传
                return uploadimg.click()
            },
            uploadPictures(event){//封面图片点击上传
                let file = event.target.files[0];
                if (file.size>(1024*500)){
                    this.$message.error('请上传小于500K的图片');
                    return
                }
                let reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload =(e)=> {
                    //this.imageUrl = e.target.result
                }
                let formData = new FormData();
                formData.append('uploadFile', file);
                let config = {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        "Access-controt-allow-0rigin":"*",
                        token: this.$ls.get("Access-Token"),
                        Authorization: `Bearer ${this.$ls.get("Access-Token")}`,
                        "current-id": Cookies.get("current-id")
                    }
                }
                axios.post(this.url+"/publishApi/uploadApiDocFile/1",formData,config).then(res=>{
                    if (res.data.data) {
                        this.form.image=res.data.data;
                        request(this.url+"/publishApi/showApiDocFile/"+res.data.data,{
                            method:"GET"
                        }).then(res=>{
                            this.imageUrl=res;
                        })
                    }
                })
            },
            delImg(){//删除封面图
                request(this.url+"/publishApi/deleteApiDocFile/"+this.form.image,{
                    method:"DELETE"
                }).then(res=>{
                    this.imageUrl=null;
                })
            },
            getGroupName(){
                if (!this.form.grouping){
                    return
                }
                for (let i=0;i<this.groupList.length;i++){
                    if (this.groupList[i].id===this.form.grouping){
                        request(this.url+'/publishApiGroup/searchPublishApiGroup',{
                            method:"POST",
                            body:{
                                name:this.groupList[i].name,
                                pageNum:1,
                                pageSize:10
                            }
                        }).then(res=>{
                            this.form.url='/'+res.content[0].systemEnName;
                        })
                        break;
                    }
                }
                this.$refs.group.onFieldChange()
            },
            goBack(){
                this.release=false;
                if (this.$multiTab){
                    this.$multiTab.closeCurrentPage();
                }
                setTimeout(()=>{
                    window.vm.$router.push({
                        path:"/gateway/apiList/apiListCreated"
                    })
                })
            },
            tabSecretLevel(){
                if (this.form.secretLevel==="高"){
                    this.form.subscription=1;
                }
            },
            addParamer(num){
                let json={
                    op:undefined,
                    header:null,
                    valueType:undefined,
                    value:null
                }
                if (!num){
                    json.key=this.form.requestHeader.length?this.form.requestHeader[this.form.requestHeader.length-1].key+1:0;
                    this.form.requestHeader.push(json);
                }else {
                    json.key=this.form.responsetHeader.length?this.form.responsetHeader[this.form.requestHeader.length-1].key+1:0;
                    this.form.responsetHeader.push(json);
                }
            },
            delParames(record,num){
               if (!num){
                  for (let i=0;i<this.form.requestHeader.length;i++){
                      if (record.key===this.form.requestHeader[i].key){
                          this.form.requestHeader.splice(i,1)
                          break
                      }
                  }
               }else{
                   for (let i=0;i<this.form.responsetHeader.length;i++){
                       console.log(record.key)
                       console.log(this.form.responsetHeader[i].key)
                       if (record.key===this.form.responsetHeader[i].key){
                           this.form.responsetHeader.splice(i,1)
                           break
                       }
                   }
               }
            },
            handldChange(){
                if(!this.form.authentication){
                    this.form.log=0;
                    this.form.record=0;
                }
            }
        },
        created() {
            let data=JSON.parse(localStorage.getItem("appNowCategory")).gateway
            this.url=`/apimgr/api/v1/tenant/tenant_id_1/project/${data[1].id}/${data[0].key}`
            this.getGroupList();
            this.one();
            this.getDetail();
            id=this.$route.query.id;
            text=this.$route.query.text;
            bool=this.$route.query.bool;
        },
        activated(){
            this.release=false;
            this.getGroupList();
            this.getDetail()
        },
        destroyed() {
            window.removeEventListener('scroll', this.onScroll, true);
            //this.delFile();
            /*
            * 创建API----如果已经上传附件，未点保存，点击取消或者关闭窗口，直接删除当前所有已经上传的文件
            * 修改API----修改的时候点取消，把刚刚上传的文件删除，已经上传的文件点击删除，直接删除，无需点击确认
            * */
        },
        watch:{
            "$store.state.appNowCategory":{
                deep: true,
                handler:function (oldValue,newValue) {
                    if (JSON.stringify(oldValue)===JSON.stringify(newValue)){
                        return;
                    }
                    this.$multiTab.closeAll();
                }
            }
        }
    }
</script>

<style scoped lang="less">
    .addApi {

        header {
            padding: 24px 24px 0;
            > span {
                font-size: 14px;
                margin-left: 16px;
            }
        }

        .form_body {
            margin-top: 16px;
            margin-bottom: 48px;
            transition: 0.5s;

            .form {
                width: calc(~"100% - 217px");
                background: #ffffff;
                padding-bottom: 40px;

                .titles {
                    height: 72px;
                    line-height: 72px;
                    font-size: 16px;
                    color: #000000;
                    text-indent: 24px;
                }
                .uploadImage{
                    >div{
                        float: left;
                        line-height: 1.5;
                    }
                    >div:nth-child(1){
                        width: 104px;
                        height: 104px;
                        background: rgba(0, 0, 0, 0.02);
                        border-radius: 4px;
                        border: 1px dashed rgba(0, 0, 0, 0.15);
                        cursor: pointer;
                        position: relative;
                        >i:nth-child(1){
                            font-size: 24px;
                            display: block;
                            margin: 24px auto 15px;
                        }
                        >p:nth-child(2){
                            text-align: center;
                            font-size: 14px;
                            color: rgba(0, 0, 0, 0.65);
                        }
                        >div:nth-child(3){
                            position: absolute;
                            width: 104px;
                            height: 104px;
                            top: -1px;
                            left: -1px;
                            border: 1px solid rgba(0, 0, 0, 0.15);
                            background-color: #ffffff;
                            border-radius: 4px;
                            padding: 8px;
                            >img{
                                display: block;
                                width: 100%;
                                height: 100%;
                            }
                            >div{
                                position: absolute;
                                top: 8px;
                                right: 8px;
                                bottom: 8px;
                                left: 8px;
                                background: rgba(0, 0, 0, 0.45);
                                padding: 34px 22px;
                                display: flex;
                                justify-content: space-between;
                            }
                        }
                    }
                    >div:nth-child(2){
                        padding-left: 24px;
                        color: rgba(0, 0, 0, 0.35);
                        font-size: 12px;
                        padding-top: 28px;
                        line-height: 1.5;
                    }
                }
                .methodType {
                    padding: 0 24px 18px;
                    ul{
                        float: left;
                    }
                    li {
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

                        > span {
                            display: block;
                            width: 100%;
                            height: 100%;
                        }

                        .url {
                            float: right;
                            width: 120px;
                            overflow: hidden;
                        }
                    }

                    .active {
                        background-color: #00AAA6;
                        color: #ffffff;
                    }
                }

                .upload {
                    padding-left: 24px;

                    > div:nth-child(1) {
                        > span {
                            font-size: 14px;
                            padding-left: 24px;
                            color: rgba(0, 0, 0, 0.35);
                        }
                    }

                    > ul:nth-child(2) {
                        margin-top: 12px;

                        li {
                            width: 166px;

                            > i {
                                float: left;
                                font-size: 18px;
                                margin-right: 7px;
                                margin-top: 2px;
                                color: rgba(0, 0, 0, 0.45);
                            }

                            > div {
                                float: left;
                                width: 140px;
                                font-size: 14px;
                                color: rgba(0, 0, 0, 0.45);
                                margin-bottom: 8px;

                                p:nth-child(1) {
                                    padding-bottom: 8px;

                                    span:nth-child(1) {
                                        float: left;
                                        width: 120px;
                                        overflow: hidden;
                                        text-overflow: ellipsis;
                                        white-space: nowrap;
                                    }
                                }

                                p:nth-child(2) {
                                    width: 100%;
                                    height: 2px;
                                    background: rgba(0, 0, 0, 0.04);
                                    border-radius: 32px;
                                    margin-top: 1px;
                                    span {
                                        float: left;
                                        height: 100%;
                                        border-radius: 32px;
                                        background-color: #00AAA6;
                                        transition: 0.3s;
                                    }
                                }
                            }
                        }
                    }
                }

                .grouping {
                    width: 240px;
                    margin-right: 16px;
                }

                .refresh {
                    margin-right: 16px;
                }

                .add {
                    font-size: 14px;
                    color: #00AAA6;
                    cursor: pointer;
                    -webkit-user-select: none;
                    -ms-user-select: none;
                    user-select: none;
                }

                .apiname {
                    width: 90%;
                }

                .prompt {
                    font-size: 12px;
                }

                .whitelist {
                    font-size: 12px;
                    color: #00AAA6;
                    display: inline-block;
                    cursor: pointer;
                    margin-top: -16px;
                }

                .icon {
                    font-size: 16px;
                    position: relative;
                    top: -2px;
                    cursor: pointer;
                }

                .table {
                    margin-bottom: 30px;

                    .headline {
                        width: 95%;
                        margin: 0 auto;
                        color: #000;
                        position: relative;
                        font-size: 14px;
                        padding-left: 10px;
                    }

                    .headline:before {
                        position: absolute;
                        top: 4px;
                        left: 0;
                        width: 4px;
                        height: 14px;
                        background: #00AAA6;
                        content: "";
                    }

                    .table-body {
                        width: 95%;
                        margin: 10px auto 0;

                        :nth-child(3)[data-type="0"] {
                            float: right;
                            width: 85%;
                        }

                        :nth-child(3)[data-type="1"] {
                            float: right;
                            width: 74%;
                        }

                        :nth-child(3)[data-type="2"] {
                            float: right;
                            width: 61%;
                        }

                        :nth-child(3)[data-type="3"] {
                            float: right;
                            width: 52%;
                        }
                        .addApiAction{
                            .delete {
                                color: #00AAA6;
                                font-size: 14px;
                                cursor: pointer;
                                line-height: 34px;
                                float: left;
                                margin-right: 10px;
                            }
                        }

                    }

                    .adds {
                        font-size: 12px;
                        color: #00AAA6;
                        border-bottom: none !important;
                        cursor: pointer;
                        display: inline-block;
                    }
                }
            }

            .anchor {
                position: fixed;
                top: 120px;
                right: 65px;
                width: 136px;
                height: 114px;
                border-left: 1px solid rgba(0, 0, 0, 0.09);

                li {
                    height: 25%;
                    line-height: 28.5px;
                    padding-left: 16px;
                    color: rgba(0, 0, 0, 0.65);
                    position: relative;
                    cursor: pointer;
                }

                .active {
                    color: #00AAA6;
                    transition: 1s;
                }

                .active:after {
                    content: "";
                    clear: both;
                    position: absolute;
                    width: 8px;
                    height: 8px;
                    border: 2px solid #00aaa6;
                    background: #ffffff;
                    border-radius: 50%;
                    top: 11px;
                    left: -4px;
                    transition: 1s;
                }
            }
        }

        .form_footer {
            position: fixed;
            left: 0;
            bottom: 0;
            width: 100%;
            height: 48px;
            z-index: 2;
            background: #ffffff;
            box-shadow: 0px -1px 8px 0px rgba(0, 0, 0, 0.1);
            padding-right: 24px;

            > button {
                float: right;
                margin-top: 8px;
                margin-left: 8px;
            }
        }

        .ant-spin{
            border: 1px solid #91d5ff;
            background-color: #e6f7ff7d;
            padding-top: 320px;
            width: 100%;
            height: 100%;
            position: fixed;
            z-index: 9999;
            top: 0;
            left: 0;
        }
    }

    .addApi::-webkit-scrollbar, .form::-webkit-scrollbar {
        display: none;
    }
</style>
<style>
    ul,li{
        list-style: none;
        padding: 0;
    }
    .addApi_uploadFile .code {
        margin-top: 20px;
        height: 200px;
    }
    .releaseApi .info svg{
        float: left;
        color: #52C41A;
        font-size: 22px;
    }
    .releaseApi .info ul{
        float: left;
        margin-left: 16px;
    }
    .releaseApi .info ul li:nth-child(1){
        font-size: 16px;
        color: rgba(0, 0, 0, 0.85);
        font-weight: bold;
    }
    .releaseApi .info ul li:nth-child(2){
        color: rgba(0, 0, 0, 0.65);
        font-size: 14px;
        padding-top: 16px;
    }
    .releaseApi .footer{
        text-align: center;
        float: right;
    }
    .releaseApi .ant-modal-body::after{
        content: "";
        display: block;
        clear: both;
    }
</style>
