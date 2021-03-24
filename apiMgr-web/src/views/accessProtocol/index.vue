<template>
    <div class="accessProtocol">
        <div class="box">
            <ul>
                <li></li>
                <li class="title">样例</li>
                <li>备注</li>
            </ul>
            <ul>
                <li class="title">sap配置：</li>
                <li></li>
                <li></li>
            </ul>
            <ul>
                <li>ashost</li>
                <li class="require">
                    <a-input v-model.trim="ashost.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="SAP服务器地址" />
                </li>
            </ul>
            <ul>
                <li>sysnr</li>
                <li class="require">
                    <a-input v-model.trim="sysnr.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="SAP实例编号" />
                </li>
            </ul>
            <ul>
                <li>client</li>
                <li class="require">
                    <a-input v-model.trim="client.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="SAP系统标识" />
                </li>
            </ul>
            <ul>
                <li>user</li>
                <li class="require">
                    <a-input v-model.trim="user.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="SAP用户名" />
                </li>
            </ul>
            <ul>
                <li>passwd</li>
                <li class="require">
                    <a-input v-model.trim="passwd.sample" placeholder="请输入" spellcheck="false" type="password"/>
                    <p></p></li>
                <li>
                    <a-input value="SAP密码" />
                </li>
            </ul>
            <ul>
                <li>functionName</li>
                <li class="require">
                    <a-input v-model.trim="functionName.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="SAP RFC函数名"  />
                </li>
            </ul>
            <ul>
                <li class="title">工程配置：</li>
                <li></li>
                <li></li>
            </ul>
            <ul>
                <li>project</li>
                <li class="require">
                    <a-input v-model.trim="project.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="自动生成代码的工程名称"  />
                </li>
            </ul>
            <ul>
                <li class="title">git配置：</li>
                <li></li>
                <li></li>
            </ul>
            <ul>
                <li>gitPath</li>
                <li class="require">
                    <a-input v-model.trim="gitPath.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="git地址（subgroup）"  />
                </li>
            </ul>
            <ul>
                <li>gitUserName</li>
                <li class="require">
                    <a-input v-model.trim="gitUserName.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="git用户名"  />
                </li>
            </ul>
            <ul>
                <li>gitPassWord</li>
                <li class="require">
                    <a-input v-model.trim="gitPassWord.sample" placeholder="请输入" spellcheck="false" type="password"/>
                </li>
                <li>
                    <a-input value="git密码"  />
                </li>
            </ul>
            <ul>
                <li class="title">ICP配置：</li>
                <li></li>
                <li></li>
            </ul>
            <ul>
                <li>ocpIp</li>
                <li class="require">
                    <a-input v-model.trim="ocpIp.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="OCP地址"  />
                </li>
            </ul>
            <ul>
                <li>ocpPort</li>
                <li class="require">
                    <a-input v-model.trim="ocpPort.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="OCP端口"  />
                </li>
            </ul>
            <ul>
                <li>ocpUserName</li>
                <li class="require">
                    <a-input v-model.trim="ocpUserName.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="OCP用户名"  />
                </li>
            </ul>
            <ul>
                <li>ocpPassWord</li>
                <li class="require">
                    <a-input v-model.trim="ocpPassWord.sample" placeholder="请输入" spellcheck="false" type="password"/>
                </li>
                <li>
                    <a-input value="OCP密码" />
                </li>
            </ul>
            <ul>
                <li>ocpProject</li>
                <li class="require">
                    <a-input v-model.trim="ocpProject.sample" placeholder="请输入" spellcheck="false"/>
                </li>
                <li>
                    <a-input value="OCP的工程名" />
                </li>
            </ul>

            <a-button class="btn" type="primary" @click="commit" :loading="loading">按钮</a-button>
        </div>

        <a-modal
                title="返回参数"
                :visible="visible"
                @ok="visible=false"
                @cancel="visible=false"
                width="800px"
        >
            <a-textarea placeholder="Basic usage" style="height: 300px" v-model="info" spellcheck="false"/>
        </a-modal>
    </div>
</template>

<script>
    import request from "../../utils/request";
    export default {
        name: "index",
        data() {
            return {
                ashost: {
                    sample: "172.16.35.60"
                },
                sysnr: {
                    sample: "00"
                },
                client: {
                    sample: "105"
                },
                user: {
                    sample: "GFFP"
                },
                passwd: {
                    sample: null
                },
                functionName: {
                    sample: "ZMPSSP_BOM_INFO"
                },
                project: {
                    sample: "xyk0202"
                },
                gitPath: {
                    sample: "https://gitlab-cloud.hisense.com/JTIT/jskfb/fuse-ocp/sapauto/"
                },
                gitUserName: {
                    sample: "huangchen.ex"
                },
                gitPassWord: {
                    sample: null
                },
                ocpIp: {
                    sample: "https://devmaster.ocp.hisense.com"
                },
                ocpPort: {
                    sample: "8443"
                },
                ocpUserName: {
                    sample: "huangchen.ex"
                },
                ocpPassWord: {
                    sample: "a.2551062"
                },
                ocpProject: {
                    sample: "hisense-fuse-dev"
                },
                visible: false,
                info: "",
                loading:false
            }
        },
        methods: {
            commit() {
                if (!this.ashost.sample) {
                    this.$message.error("请输入SAP服务器地址")
                    return
                }
                if (!this.sysnr.sample) {
                    this.$message.error("请输入SAP实例编号")
                    return
                }
                if (!this.client.sample) {
                    this.$message.error("请输入SAP系统标识")
                    return
                }
                if (!this.user.sample) {
                    this.$message.error("请输入SAP用户名")
                    return
                }
                if (!this.passwd.sample) {
                    this.$message.error("请输入SAP密码")
                    return
                }
                if (!this.functionName.sample) {
                    this.$message.error("请输入SAP RFC函数名")
                    return
                }
                if (!this.project.sample) {
                    this.$message.error("请输入SAP RFC函数名")
                    return
                }
                if (!this.gitPath.sample) {
                    this.$message.error("请输入git地址（subgroup）")
                    return
                }
                if (!this.gitUserName.sample) {
                    this.$message.error("请输入git用户名")
                    return
                }
                if (!this.gitPassWord.sample) {
                    this.$message.error("请输入git密码")
                    return
                }
                if (!this.ocpIp.sample) {
                    this.$message.error("请输入OCP地址")
                    return
                }
                if (!this.ocpPort.sample) {
                    this.$message.error("请输入OCP端口")
                    return
                }
                if (!this.ocpUserName.sample) {
                    this.$message.error("请输入OCP用户名")
                    return
                }
                if (!this.ocpPassWord.sample) {
                    this.$message.error("请输入OCP密码")
                    return
                }
                if (!this.ocpProject.sample) {
                    this.$message.error("请输入OCP工程名")
                    return
                }
                this.loading=true;
                request("http://"+window.location.host+"/fuse/createSapSrc",{
                    method:"POST",
                    body:{
                        ashost: this.ashost.sample,
                        sysnr: this.sysnr.sample,
                        client: this.client.sample,
                        user: this.user.sample,
                        passwd: this.passwd.sample,
                        functionName: this.functionName.sample,
                        project: this.project.sample,
                        gitPath: this.gitPath.sample,
                        gitUserName: this.gitUserName.sample,
                        gitPassWord: this.gitPassWord.sample,
                        ocpIp: this.ocpIp.sample,
                        ocpPort: this.ocpPort.sample,
                        ocpUserName: this.gitUserName.sample,
                        ocpPassWord: this.ocpPassWord.sample,
                        ocpProject: this.ocpProject.sample
                    }
                }).then(res=>{
                    this.loading=false
                    this.visible=true;
                    this.info=JSON.stringify(res);
                })
            }
        }
    }
</script>

<style lang="less">
    .accessProtocol {
        .box {
            padding: 24px;
            background-color: #ffffff;

            ul {
                display: flex;

                li {
                    font-size: 16px;
                    height: 30px;
                    margin-bottom: 26px;
                    padding-right: 16px;
                    position: relative;
                }

                li:nth-child(1) {
                    width: 14%;
                }

                li:nth-child(2), li:nth-child(3) {
                    width: 43%;
                }

                .require:before {
                    content: "*";
                    position: absolute;
                    top: 4px;
                    left: -14px;
                    color: #f5222d;
                }

                p {
                    line-height: 1;
                    font-size: 12px;
                    padding-top: 4px;
                    color: #979595;
                }

                li:nth-child(3) input{
                    pointer-events: none;
                }
            }

            ul .title {
                font-size: 16px;
                font-weight: bold;
            }

            .btn {
                display: block;
                margin: 30px auto 0;
            }
        }
    }
</style>
<style>
    ul,li{
        padding: 0;
        list-style: none;
    }
</style>
