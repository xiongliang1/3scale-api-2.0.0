<template>
    <div class="type">
        <div>
            <p class="title">基础信息</p>
            <ul class="apiInfo clearfix">
                <li><span>申请人：</span><span>{{info.data.proposerName}}</span></li>
                <li class="ellipsis">
                    <span>申请人邮箱：</span>
                    <a-tooltip>
                        <template slot="title">
                            {{info.data.proposerEmail}}
                        </template>
                        {{info.data.proposerEmail}}
                    </a-tooltip>
                </li>
                <li><span>手机号：</span><span>{{info.data.proposerMobile}}</span></li>
                <li><span>申请系统：</span><span>{{info.data.sysName}}</span></li>
                <li><span>系统英文缩写：</span><span>{{info.data.sysAbbreviation}}</span></li>
            </ul>
            <hr class="line">
        </div>

        <div>
            <p class="title">短信账号申请信息</p>
            <ul class="apiInfo clearfix">
                <li><span>短信类型：</span><span>{{info.data.smsType}}</span></li>
                <li><span>短信发送量（每月）：</span><span>{{info.data.smsQuantity}}</span></li>
                <li><span>短信模板样例：</span><span>{{info.data.smsType}}</span></li>
                <li><span>申请理由：</span><span>{{info.data.smsReason}}</span></li>
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
        },
        filters:{
            type(num){
                if (num==="1"){
                    return "非营销类"
                }else if (num==="2"){
                    return "营销类"
                }else{
                    return ""
                }
            }
        },
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
