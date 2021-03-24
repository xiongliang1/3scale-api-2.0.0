<template>
    <div class="type">
        <div>
            <p class="title">API基本信息</p>
            <ul class="apiInfo clearfix">
                <li><span>API名称：</span><span>{{info.apiName}}</span></li>
                <li><span>API所属分组：</span><span>{{info.apiGroup}}</span></li>
                <li><span>一级类目：</span><span>{{info.catageoryOne}}</span></li>
                <li><span>二级类目：</span><span>{{info.catageoryTwo}}</span></li>
                <li><span>所属系统：</span><span>{{info.publishSystem}}</span></li>
                <li><span>环境信息：</span><span>{{info.env}}</span></li>
            </ul>
            <hr class="line">
        </div>

        <div v-if="info.type===6">
            <p class="title">API订阅信息</p>
            <ul class="apiInfo clearfix">
                <li><span>订阅人：</span><span>{{info.subscriber||"暂无"}}</span></li>
                <li><span>订阅系统：</span><span>{{info.subscribeSystem||"暂无"}}</span></li>
                <li><span>订阅时间：</span><span>{{info.subscribeTime||"暂无"}}</span></li>
                <li><span>订阅描述：</span><span>{{info.subscribeDesc||"暂无"}}</span></li>
            </ul>
            <hr class="line">
        </div>
    </div>
</template>

<script>
    export default {
        name: "type1",
        props: {
            info: {
                type: Object,
                default: {}
            }
        }
    }
</script>

<style scoped lang="less">
    ul, li {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .type {
        .ellipsis {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap
        }

        .title {
            font-size: 16px;
            color: #333333;
            margin-top: 30px;
            position: relative;
            padding-left: 12px;
        }

        .title::before {
            content: "";
            position: absolute;
            top: 5%;
            left: 0;
            width: 4px;
            height: 90%;
            background: #00aaa6;
        }

        .line {
            margin-top: 10px;
            border: none;
            border-top: 1px solid #cccccc;
            transform: scaleY(0.5);
        }

        .apiInfo {
            li {
                float: left;
                width: 25%;
                margin-bottom: 10px;
            }
        }
    }
</style>
