<template>
    <div class="type">
        <p class="title">信鸿公众号</p>
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
            <li><span>申请环境：</span><span>{{info.data.env}}</span></li>
            <li><span>公共号名称：</span><span>{{info.data.hichatName}}</span></li>
            <li><span>公共号管理员：</span><span>{{info.data.hichatAdmin}}</span></li>
            <li v-show="info.data.hiChatTester"><span>测试人员账号：</span><span>{{info.data.hiChatTester}}</span></li>
            <li><span>公共号摘要：</span><span>{{info.data.hichatSummary}}</span></li>
            <li><span>公共号图标：</span><span><a-button icon="download" v-show="info.data.hichatImage" @click="downloadIamge(info.data.hichatImage,'公众号图标')"></a-button></span></li>
            <li><span>申请理由：</span><span>{{info.data.hichatReason}}</span></li>
            <li style="width: 100%"><span>推送范围：</span><span>{{info.data.hichatRange}}</span></li>
        </ul>
        <hr class="line">
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
        methods:{
            downloadIamge(imgsrc, name) {//下载图片地址和图片名
                let image = new Image();
                // 解决跨域 Canvas 污染问题
                image.setAttribute("crossOrigin", "anonymous");
                image.onload = function() {
                    var canvas = document.createElement("canvas");
                    canvas.width = image.width;
                    canvas.height = image.height;
                    let context = canvas.getContext("2d");
                    context.drawImage(image, 0, 0, image.width, image.height);
                    let url = canvas.toDataURL("image/png"); //得到图片的base64编码数据

                    let a = document.createElement("a"); // 生成一个a元素
                    let  event = new MouseEvent("click"); // 创建一个单击事件
                    a.download = name || "photo"; // 设置图片名称
                    a.href = url; // 将生成的URL设置为a.href属性
                    a.dispatchEvent(event); // 触发a的单击事件
                };
                image.src = imgsrc;
            },
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
