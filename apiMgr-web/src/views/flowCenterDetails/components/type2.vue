<template>
    <div class="type">
        <div>
            <p class="title">审批详情</p>
            <ul class="apiInfo clearfix">
                <li><span>审批单号：</span><span>{{info.processInstID}}</span></li>
                <li><span>订单号：</span><span>{{info.sn}}</span></li>
                <li><span>下单人：</span><span>{{info.userCode}}</span></li>
                <li><span>下单时间：</span><span>{{info.gmtCreate}}</span></li>
            </ul>
            <hr class="line">
        </div>

        <div>
            <p class="title">订单信息</p>
            <ul class="apiInfo clearfix">
                <li><span>产品：</span><span>{{info.productName}}</span></li>
                <li v-if="info.items[0]&&info.items[0].params">
                    <span>规格：</span>
                    <span v-for="(item,index) in JSON.parse(info.items[0].params)" :key="index" v-show="item.paramValue">
                        {{item.name}}:{{item.paramValue}}
                    </span>
                </li>
                <li v-if="info.items[0]&&info.items[0].basicPrice"><span>单价(元)：</span><span>{{info.items[0].basicPrice}}</span></li>
                <li v-if="info.items[0]&&info.items[0].amount"><span>数量：</span><span>{{info.items[0].amount}}</span></li>
                <li v-show="info.amount"><span>总价：</span><span>{{info.amount}}</span></li>
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
                default: {
                    items:[]
                }
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
