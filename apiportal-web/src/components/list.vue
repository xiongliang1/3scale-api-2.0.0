<template>
    <div id="list" class="clearfix">
        <div class="filters">
            <div class="search clearfix">
                <a-icon type="api"/>
                <span>API市场</span>
                <a-input-search placeholder="请输入你需要的服务" v-model="apiName" @search="onSearch"/>
            </div>
            <ul class="filtrate">
                <li class="clearfix">
                    <span>发布环境：</span>
                    <span @click="partition=null;categoryTwoList=[];getData()" :class="{'active':partition===null}">全部</span>
                    <span @click="partition=0;categoryTwoList=[];getData()" :class="{'active':partition===0}">内网</span>
                    <span @click="partition=1;categoryTwoList=[];getData()" :class="{'active':partition===1}">外网</span>
                </li>
                <li class="clearfix">
                    <span>类别：</span>
                    <span @click="checkOut(null,null,0)" :class="{'active':categoryOne===null}">全部</span>
                    <span v-for="(item,index) in category" :key="index" @click="checkOut(item.id,index,0)"
                          :class="{'active':categoryOne===item.id}">{{item.itemName}}</span>
                </li>
                <li class="clearfix" v-if="JSON.stringify($route.query)==='{}'">
                    <span>子类别：</span>
                    <span @click="checkOut(null,null,1)" :class="{'active':categoryTwo===null}">全部</span>
                    <span v-for="(item,index) in categoryTwoList" v-show="item.hidden===false" :key="index" @click="checkOut(item.id,index,1)"
                          :class="{'active':categoryTwo===item.id}">{{item.itemName}}</span>
                </li>
                <li class="clearfix" v-else>
                    <span>子类别：</span>
                    <span @click="checkOut(null,null,1)" :class="{'active':categoryTwo===null}">全部</span>
                    <span v-for="(item,index) in categoryTwoList" :key="index" @click="checkOut(item.id,index,1)"
                          :class="{'active':categoryTwo===item.id}">{{item.itemName}}</span>
                </li>
                <li class="clearfix">
                    <span>排序：</span>
                    <span @click="sort='updateTimeDesc';getData()"
                          :class="{'active':sort==='updateTimeDesc'}">更新时间降序</span>
                    <span @click="sort='updateTimeAsc';getData()"
                          :class="{'active':sort==='updateTimeAsc'}">更新时间升序</span>
                    <span @click="sort='subscriptCountDesc';getData()" :class="{'active':sort==='subscriptCountDesc'}">订阅数量降序</span>
                    <span @click="sort='subscriptCountAsc';getData()" :class="{'active':sort==='subscriptCountAsc'}">订阅数量升序</span>
                </li>
                <li class="clearfix">
                    <span>所属系统：</span>
                    <a-select v-model="system" placeholder="请选择" :allowClear="true" @change="getData">
                        <a-select-option v-for="item in systemList" :value="item.id" :key="item.id">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>
                </li>
            </ul>
        </div>
        <div class="portList">
            <div class="choose">
                <a-checkbox :indeterminate="indeterminate" :checked="checkAll" @change="onCheckAllChange"
                            :disabled="!total">
                    全选
                </a-checkbox>
                <a-button type="primary" @click="subscription=true" :disabled="!checkAll">
                    批量订阅
                </a-button>
            </div>
            <ul class="list">
                <li v-for="item in apiList" :key="item.id">
                    <a-checkbox @change="check(item)" :checked="item.checked"></a-checkbox>
                    <div class="img" @click="jump(item.id)">
                        <img :src="item.image" v-if="item.image">
                        <span>订阅{{item.subscribeCount||0}}</span>
                    </div>
                    <div class="info" @click="jump(item.id)">
                        <div class="data">
                            <span>{{item.name}}</span>
                            <span v-show="item.partition">外网</span>
                            <span v-show="!item.partition">内网</span>
                            <span>{{item.publishApiGroupDto.systemName}}</span>
                        </div>
                        <div class="note" :title="item.description">{{item.description||"暂无"}}</div>
                        <div class="time">
                            更新于 {{item.updateTime}}
                        </div>
                    </div>
                </li>
            </ul>
            <a-empty v-show="!total"/>
        </div>
        <a-pagination :total="total" v-show="total" :show-total="total => `共${total}条`" size="small" show-size-changer
                      show-quick-jumper @change="onChange" @showSizeChange="onChange"/>

        <a-modal
                title="订阅确认"
                :visible="subscription"
                :confirm-loading="confirmLoading"
                okText="提交"
                @ok="handleOk"
                @cancel="handleCancel"
                width="1000px"
                dialogClass="subscription_modal"
        >
            <div class="title">订阅系统</div>
            <div>
                <span>你希望给哪个系统订阅接口？</span>
                <a-config-provider>
                    <template v-if="!newCategory.length" #renderEmpty>
                        <a-empty>
                            <span slot="description">暂无数据，如需新增系统，点击通知管理员按钮 </span>
                            <a-button type="primary" @click="inform" :loading="loading">
                                通知管理员
                            </a-button>
                        </a-empty>
                    </template>
                    <!--<a-select style="width: 763px" v-model="subscriptionSystem" placeholder="请选择" mode="multiple"
                              show-search @popupScroll="handlePopupScroll" @blur="scrollPage=1;newCategory=systems.slice(0, 80)" @search="handleSearch">
                        <a-select-option v-for="item in newCategory" :key="item.itemName" :value="item.itemName">
                            {{item.itemName}}
                        </a-select-option>
                    </a-select>-->
                    <a-select
                            mode="multiple"
                            placeholder="请选择"
                            :value="selectedItems"
                            style="width: 763px"
                            @change="handleChange"
                            :filter-option="filterOption"
                    >
                        <a-select-option v-for="item in filteredOptions" :key="item.id" :value="item.id">
                            {{ item.itemName }}
                        </a-select-option>
                    </a-select>
                </a-config-provider>
            </div>
            <div class="title" style="padding-top:10px">接口列表</div>
            <div class="table">
                <ul class="head clearfix">
                    <li>序号</li>
                    <li>接口名称</li>
                    <li>申请说明</li>
                </ul>
                <ul class="body">
                    <li class="clearfix" v-for="(item,index) in apiList" :key="index" v-show="item.checked">
                        <span>{{index<9?"0"+(index+1):index+1}}</span>
                        <span>{{item.name}}</span>
                        <span>
                          <a-textarea placeholder="请输入申请说明" v-model="item.desc" auto-size/>
                        </span>
                    </li>
                </ul>
            </div>
        </a-modal>
    </div>
</template>

<script>
    import Vue from "vue"

    export default {
        name: "list",
        data() {
            return {
                system: undefined,
                categoryOne: null,
                categoryTwo: null,
                sort: 'updateTimeDesc',
                indeterminate: false,
                checkAll: false,
                subscription: false,
                confirmLoading: false,
                subscriptionSystem: undefined,
                category: [],
                newCategory: [],
                scrollPage: 1,
                categoryTwoList: [],
                systemList: [],
                apiList: [],
                total: 0,
                pageNum: 1,
                pageSize: 10,
                apiName: null,
                image: require("../assets/api-cover-default.png"),
                partition: null,
                systems: JSON.parse(sessionStorage.getItem("systemList")),
                fromPath: null,
                searchValue: null,
                searchSystem:[],
                loading:false,
                selectedItems:[]
            }
        },
        computed: {
            filteredOptions() {
                return this.systems.filter(o => !this.selectedItems.includes(o.itemName));
            },
        },
        methods: {
            onSearch() {
                this.getData();
            },
            onCheckAllChange(e) {
                this.checkAll = e.target.checked;
                if (!e.target.checked) {
                    this.indeterminate = false
                }
                this.apiList.forEach(item => {
                    item.checked = e.target.checked;
                });
                this.$forceUpdate();
            },
            onChange(pageNumber, pageSize) {
                this.pageNum = pageNumber;
                this.pageSize = pageSize;
                this.getData();
            },
            handleOk() {
                /*let ids = [];
                for (let i = 0; i < this.systems.length; i++) {
                    for (let j = 0; j < this.subscriptionSystem.length; j++) {
                        if (this.systems[i].itemName === this.subscriptionSystem[j]) {
                            ids.push(this.systems[i].id)
                            break
                        }
                    }
                }*/
                let arr = [];
                this.apiList.forEach(item => {
                    if (item.checked) {
                        arr.push({
                            id: item.id + "",
                            system: this.selectedItems,
                            description: item.desc || null
                        })
                    }
                })
                this.confirmLoading = true;
                this.axios.post("/applications/subscribeApi", arr).then(res => {
                    this.confirmLoading = false;
                    if (res.code === "0" || res.code === "2") {
                        this.$notification.success({
                            message: '通知',
                            description: res.msg,
                            duration: 3
                        });
                        this.$router.push({path: "/principal/developerCenter/myApply"})
                    } else {
                        this.$notification.error({
                            message: '通知',
                            description: res.msg,
                            duration: 2
                        })
                    }
                }).catch(err => {
                    this.confirmLoading = false;
                })
            },
            handleCancel() {
                this.subscription = false;
                this.subscriptionSystem = undefined;
            },
            jump(id) {
                this.$router.push({path: '/principal/portDetails', query: {id: id}})
            },
            checkOut(id, index, num) {
                if (!num) {
                    if (this.categoryOne === id) {
                        return
                    }
                    this.systemList = [];
                    this.system = undefined;
                    this.categoryOne = id;
                    this.categoryTwo = null;
                    if (typeof index === "number") {
                        this.categoryTwoList = this.category[index].dataItemList;
                    } else {
                        this.categoryTwoList = []
                    }
                    if (!this.category[index]) {
                        this.getData();
                        return;
                    }
                    this.category[index].dataItemList.forEach(item => {
                        if (!item.dataItemList) {
                            return
                        }
                        item.dataItemList.forEach(it => {
                            this.systemList.push(it)
                        })
                    })
                } else {
                    this.categoryTwo = id;
                    if (typeof index === "number") {
                        this.systemList = this.categoryTwoList[index].dataItemList;
                    } else {
                        this.systemList = [];
                        this.system = undefined;
                        for (let i = 0; i < this.category.length; i++) {
                            if (this.category[i].id === this.categoryOne) {
                                this.category[i].dataItemList.forEach(item => {
                                    item.dataItemList.forEach(it => {
                                        this.systemList.push(it)
                                    })
                                })
                                break;
                            }
                        }
                    }
                }
                this.getData();
            },
            getData() {
                if (!sessionStorage.getItem("status")){
                    if (this.partition===null){
                        this.category=JSON.parse(sessionStorage.getItem("categoryTwoAndApi")).all
                    }else if (this.partition===0){
                        this.category=JSON.parse(sessionStorage.getItem("categoryTwoAndApi"))[0]
                    }else if (this.partition===1){
                        this.category=JSON.parse(sessionStorage.getItem("categoryTwoAndApi"))[1]
                    }
                }else {
                    this.category=JSON.parse(sessionStorage.getItem("category"))
                }
                this.axios.get("/publishApi/pagePublishApi", {
                    params: {
                        page: this.pageNum,
                        size: this.pageSize,
                        name: this.apiName,
                        categoryOne: this.categoryOne,
                        categoryTwo: this.categoryTwo,
                        system: this.system,
                        sort: this.sort,
                        partition: this.partition
                    }
                }).then(res => {
                    if (res.code === "0") {
                        this.apiList = res.data.content;
                        this.total = res.data.totalElements;
                        this.apiList.forEach((item, index) => {
                            item.checked = false;
                            this.shwoImage(item, index)
                        })
                    } else {
                        this.apiList = [];
                        this.total = 0;
                    }
                })
            },
            check(item) {
                item.checked = !item.checked;
                this.$forceUpdate();
                let flag = this.apiList[0].checked;
                for (let i = 0; i < this.apiList.length; i++) {
                    if (flag !== this.apiList[i].checked) {
                        this.indeterminate = true;
                        break;
                    } else {
                        this.checkAll = true;
                        this.indeterminate = false;
                    }
                }
            },
            shwoImage(item, index) {
                if (item.picFiles.length) {
                    this.axios.get("/publishApi/showApiDocFile/" + item.picFiles[0].id).then(res => {
                        Vue.set(this.apiList[index], "image", res.data)
                    })
                } else {
                    if (this.apiList[index].secretLevel === "低") {
                        this.apiList[index].image = require("../assets/api-cover-default-green.png");
                    } else {
                        this.apiList[index].image = require("../assets/api-cover-default-red.png");
                    }
                }
            },
            creat() {
                if (sessionStorage.getItem("status")){
                    this.category=JSON.parse(sessionStorage.getItem("category"))
                    this.apiName = this.$route.query.apiName || null;
                    this.categoryOne = this.$route.query.categoryOne - 0 || null;
                    if (this.categoryOne) {
                        for (let i = 0; i < this.category.length; i++) {
                            if (this.category[i].id === this.categoryOne) {
                                this.categoryTwoList = this.category[i].dataItemList;
                                break
                            }
                        }
                    }
                    this.categoryTwo = this.$route.query.categoryTwo - 0 || null;
                    if (this.categoryTwo) {
                        for (let i = 0; i < this.categoryTwoList.length; i++) {
                            if (this.categoryTwoList[i].id === this.categoryTwo) {
                                this.systemList = this.categoryTwoList[i].dataItemList;
                                break;
                            }
                        }
                    }
                    if (this.$route.query.sort) {
                        this.sort = "subscriptCountDesc"
                    }
                }else {
                    this.category=JSON.parse(sessionStorage.getItem("categoryTwoAndApi")).all;
                    this.apiName = this.$route.query.apiName || null;
                    this.categoryOne = this.$route.query.categoryOne - 0 || null;
                    if (this.categoryOne) {
                        for (let i = 0; i < this.category.length; i++) {
                            if (this.category[i].id === this.categoryOne) {
                                this.categoryTwoList = this.category[i].dataItemList;
                                break
                            }
                        }
                    }
                    this.categoryTwo = this.$route.query.categoryTwo - 0 || null;
                    if (this.categoryTwo) {
                        for (let i = 0; i < this.categoryTwoList.length; i++) {
                            if (this.categoryTwoList[i].id === this.categoryTwo) {
                                this.systemList = this.categoryTwoList[i].dataItemList;
                                break;
                            }
                        }
                    }
                    if (this.$route.query.sort) {
                        this.sort = "subscriptCountDesc"
                    }
                }
                this.getData();
            },
            handlePopupScroll(e) {
                if (this.searchValue){
                    return;
                }
                const {target} = e;
                // scrollHeight：代表包括当前不可见部分的元素的高度
                // scrollTop：代表当有滚动条时滚动条向下滚动的距离，也就是元素顶部被遮住的高度
                // clientHeight：包括padding但不包括border、水平滚动条、margin的元素的高度
                const rmHeight = target.scrollHeight - target.scrollTop;
                const clHeight = target.clientHeight;
                if (rmHeight - clHeight <= 10) {
                    if (this.systems.length === this.newCategory.length) {
                        return
                    }
                    this.scrollPage++;
                }
            },
            handleSearch(val) {
                if (!val){
                    this.newCategory = this.systems.slice(0, 80);
                    return
                }
                let timer=setTimeout(()=>{
                    this.scrollPage=1;
                    this.searchValue=val;
                    this.newCategory=[]
                    this.systems.forEach(item=>{
                        if (item.itemName.indexOf(val)>-1){
                            this.newCategory.push(item)
                        }
                    })
                    clearTimeout(timer)
                },300)
            },
            inform(){
                this.loading=true;
                let name=document.querySelector(".ant-select-search__field").value;
                this.axios.get(`/dataItems/getSystem/sendEmail?systemName=${name}&status=1`).then(res=>{
                    this.loading=false;
                    if (res.code==="0"){
                        this.$notification.success({
                            message:'通知',
                            description:res.msg,
                            duration:2
                        })
                    }
                })
            },
            filterOption(input, option) {
                return (
                    option.componentOptions.children[0].text
                        .toLowerCase()
                        .indexOf(input.toLowerCase()) >= 0
                )
            },
            handleChange(selectedItems) {
                this.selectedItems = selectedItems;
            },
        },
        created() {
            this.newCategory = this.systems.slice(0, 80);
            this.creat()
        },
        watch: {
            scrollPage(newValue, oldValue) {
                if (newValue > 1) {
                    this.newCategory.push(...this.systems.slice(oldValue * 80, newValue * 80));
                } else {
                    this.newCategory = this.systems.slice(0, 80);
                }
            }
        },
        beforeDestroy() {
            sessionStorage.removeItem("status")
        }
    }
</script>

<style lang="less">
    #list {
        width: 1200px;
        margin: 16px auto;

        .filters {
            background-color: #ffffff;
            border-radius: 4px;
            padding: 20px 24px 0;

            .search {
                > .anticon:nth-child(1) {
                    background: linear-gradient(180deg, rgb(255, 186, 114) 0%, rgb(252, 141, 82) 100%);
                    width: 24px;
                    height: 24px;
                    text-align: center;
                    line-height: 24px;
                    border-radius: 8px;
                    color: #ffffff;
                    padding-top: 1px;
                    float: left;
                    margin-top: 3px;
                }

                > span:nth-child(2) {
                    font-size: 20px;
                    color: #333333;
                    padding-left: 12px;
                    float: left;
                }

                > .ant-input-search {
                    float: left;
                    width: 240px;
                    height: 32px;
                    margin-left: 26px;
                }
            }

            .filtrate {
                padding-top: 20px;

                li {
                    padding-bottom: 16px;

                    > span:nth-child(1) {
                        color: #333333;
                        float: left;
                        width: 72px;
                    }

                    > span:nth-child(n+2) {
                        padding: 4px 10px;
                        margin-right: 12px;
                        border-radius: 4px;
                        display: inline-block;
                        cursor: pointer;
                        color: rgba(0, 0, 0, 0.65);
                    }

                    > span.active {
                        background: rgba(0, 170, 166, 0.1);
                        color: #00AAA6;
                    }

                    .ant-select {
                        width: 240px;
                        height: 32px;
                        position: relative;
                        top: -4px;
                    }
                }
            }
        }

        .portList {
            background-color: #ffffff;
            margin-top: 16px;
            padding-bottom: 0.18px;

            .choose {
                padding: 7px 24px;
            }

            button {
                margin-left: 20px;
            }

            .list {
                width: 1200px;

                li {
                    height: 92px;
                    box-shadow: 0px -1px 0px 0px rgba(0, 0, 0, 0.1);
                    overflow: hidden;

                    .ant-checkbox-wrapper {
                        margin: 38px 16px 0;
                        float: left;
                    }

                    .img {
                        float: left;
                        margin: 12px 24px 8px 0;
                        width: 66px;
                        height: 66px;
                        position: relative;
                        cursor: pointer;

                        img {
                            width: 100%;
                            height: 100%;
                            display: block;
                        }

                        span {
                            position: absolute;
                            bottom: 0;
                            left: 0;
                            height: 16px;
                            width: auto;
                            font-size: 12px;
                            line-height: 16px;
                            padding: 0 2px;
                            background: rgba(0, 0, 0, 0.5);
                            color: #ffffff;
                        }
                    }

                    .info {
                        margin: 8px 0;
                        float: left;
                        cursor: pointer;

                        .data {
                            display: inline-block;

                            span:nth-child(1) {
                                font-size: 16px;
                                color: #333333;
                                margin-right: 16px;
                            }

                            span:nth-child(n+2) {
                                display: inline-block;
                                height: 20px;
                                padding: 0 4px;
                                margin-right: 8px;
                                font-size: 12px;
                                position: relative;
                                top: -2px;
                                border-radius: 1px;
                            }

                            span:nth-child(2) {
                                background-color: #E2E5F0;
                                color: #8088AA;
                            }

                            span:nth-child(3) {
                                background-color: #E2F0EF;
                                color: #80AAA8;
                            }

                            span:nth-child(4) {
                                background-color: #F1F1F1;
                                color: #ACACAC;
                            }
                        }

                        .note {
                            padding-top: 6px;
                            color: #999999;
                            width: 845px;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            font-size: 12px;
                        }

                        .time {
                            color: #999999;
                            padding-top: 4px;
                        }
                    }
                }
            }

            .ant-empty {
                margin: 30px 8px;
            }
        }

        .ant-pagination {
            margin-top: 16px;
            margin-bottom: 10px;
            float: right;
        }
    }
</style>
<style>
    .subscription_modal .title {
        color: #000000;
        font-size: 16px;
    }

    .subscription_modal .table {
        width: 952px;
        margin: 10px auto 0;
        max-height: 300px;
        overflow-y: auto;
    }

    .subscription_modal .table .head {
        height: 46px;
        line-height: 46px;
        background: rgba(0, 0, 0, 0.02);
    }

    .subscription_modal .table .head li {
        float: left;
    }

    .subscription_modal .table .head li:nth-child(1) {
        width: 10%;
        text-indent: 16px;
    }

    .subscription_modal .table .head li:nth-child(2) {
        width: 20%;
    }

    .subscription_modal .table .body span {
        float: left;
    }

    .subscription_modal .table .body span:nth-child(1) {
        line-height: 46px;
        text-indent: 16px;
        width: 10%;
        float: left;
    }

    .subscription_modal .table .body span:nth-child(2) {
        width: 20%;
        line-height: 46px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        float: left;
    }

    .subscription_modal .table .body span:nth-child(3) {
        width: 70%;
        float: left;
    }

    .subscription_modal .table .body span:nth-child(3) textarea {
        display: block;
        height: 100%;
        margin-top: 8px;
    }
</style>
