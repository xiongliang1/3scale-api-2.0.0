<template>
    <div>
        <a-button
                type="link"
                :size="size"
                @click="btClick()"
        >查看流程图</a-button>
        <a-button type="link" v-if="pageName==='queryPersonWorkItemsWithBizInfo'" :size="size" @click="dispose()">处理</a-button>
        <a-button type="link" v-else :size="size" @click="dispose()">详情</a-button>
        <a-modal
                title="流程图"
                :visible="visible"
                @cancel="visible=false"
                :footer="null"
                width="1200px"
        >
            <iframe :src="url" :frameborder="0" width="100%" height="500px"></iframe>
        </a-modal>
    </div>
</template>

<script>
    export default {
        name: "index",
        data(){
          return{
              url:null,
              visible:false
          }
        },
        props: {
            data: Object,
            size: String,
            selectedRows: Array,
            record: Object,
            pageName: String,
            comKey: String
        },
        methods:{
            btClick(){
               this.url=`/bpm/default/flow/processGraph/processGraph.html?processInstID=${this.record.processInstID}&tenantID=HXJT`;
               this.visible=true;
            },
            dispose(){
                console.log("pageName",this.pageName);
                console.log("comKey",this.comKey);
                window.vm.$router.push({
                    path:'/flowcenter/flowCenterapiDetailsDispose',
                    query:{
                        processInstID:this.record.processInstID,
                        workItemID:this.record.workItemID,
                        activityInstID:this.record.activityInstID,
                        activityInstName:this.record.activityInstName,
                        page:this.pageName,
                        processInstName:this.record.processInstName
                    }
                })
            }
        }
    }
</script>

<style scoped type="less">

</style>
