<template>
    <div class="types">
        <div>
            <p class="title">审批信息</p>
            <ul class="apiInfo clearfix" v-show="info.type===3">
                <li><span>短信平台账号：</span><span>{{JSON.parse(info.lastestProcessData).smsAccount}}</span></li>
                <li><span>短信平台密码：</span><span>{{JSON.parse(info.lastestProcessData).smsPwd}}</span></li>
            </ul>
            <ul class="apiInfo clearfix" v-show="info.type===4">
                <li><span>EID：</span><span>{{JSON.parse(info.lastestProcessData).pubEid}}</span></li>
                <li><span>公众号ID：</span><span>{{JSON.parse(info.lastestProcessData).pubId}}</span></li>
                <li><span>公众号秘钥：</span><span>{{JSON.parse(info.lastestProcessData).pubSecret}}</span></li>
            </ul>
            <ul class="apiInfo clearfix" v-show="info.type===5">
                <li><span>EID：</span><span>{{JSON.parse(info.lastestProcessData).todoEid}}</span></li>
                <li><span>appid：</span><span>{{JSON.parse(info.lastestProcessData).appId}}</span></li>
                <li><span>APPsecret：</span><span>{{JSON.parse(info.lastestProcessData).appSecret}}</span></li>
            </ul>
        </div>
    </div>
</template>

<script>
    export default {
        name: "info",
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

    .types {

        .title {
            font-size: 16px;
            color: #333333;
            position: relative;
            margin-top: 30px;
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

        .apiInfo {
            padding-left: 16px;
            li {
                margin-bottom: 10px;
            }
        }
    }
</style>
