<template>
  <div class="mySubscription">
    <header>
      <div class="clearfix">
        <span>已订阅列表</span>
        <a-input-search placeholder="请输入你要查询的服务" style="width: 300px" v-model.trim="apiName" @search="current=1;getData()"/>
      </div>
      <div class="content">
        <a-table
          :columns="columns"
          :data-source="data"
          :rowKey="row=>row.id"
          size="small"
          :pagination="{total:0,hideOnSinglePage:true,pageSize:pageSize}"
          @change="handleTableChange"
        >
          <div slot="apiName" slot-scope="text">
            {{text}}
          </div>
          <div slot="status" slot-scope="text">
            {{text|status}}
          </div>
          <div slot="instructions" slot-scope="text,record">
            <a-tooltip>
              <template slot="title">
                {{record.remark}}
              </template>
              <img src="../../assets/flietext.png" style="cursor: pointer">
            </a-tooltip>
          </div>
          <div slot="action" slot-scope="text,record">
          <span style="color: #00AAA6;margin-right:12px;cursor: pointer" @click="$router.push({path:'/principal/apiDetails',query:{id:record.apiId,type:1,systemId:record.system,listId:record.id}})">
            详情
          </span>
            <span style="color: #00AAA6;margin-right:12px;cursor: pointer" @click="$router.push({path:'/principal/monitoring',query:{id:record.apiId,type:1,systemId:record.system,listId:record.id}})">接口监控</span>
            <span style="color: #00AAA6;cursor: pointer" @click="cancel(record.id)">
            取消订阅
          </span>
          </div>
        </a-table>
        <a-pagination
                :show-total="total => `共${total}条`"
                size="small"
                v-model="current"
                :total="total"
                @change="onChange"
                @showSizeChange="onShowSizeChange"
                show-size-changer
                show-quick-jumper
                show-less-items />
      </div>
    </header>
  </div>
</template>

<script>
  const columns = [
    {
      title: '服务名称',
      dataIndex: 'apiName',
      key: 'apiName',
      scopedSlots: { customRender: 'apiName' },
      width:180
    },
    {
      title: '订阅系统',
      dataIndex: 'appSystemName',
      key: 'appSystemName',
    },
    {
      title: '订阅时间',
      key: 'createTime',
      dataIndex: 'createTime',
      sorter:true
    },
    {
      title: '补充说明',
      key: 'instructions',
      dataIndex: 'instructions',
      scopedSlots: { customRender: 'instructions' },
    },
    {
      title: '操作',
      key: 'action',
      dataIndex: 'action',
      scopedSlots: { customRender: 'action' },
    }
  ];
  export default {
    name: "mySubscription",
    data() {
      return {
        data:[],
        columns,
        total:0,
        current:1,
        pageSize:10,
        apiName:null,
        sort:null
      }
    },
    filters:{
      status(num){
        if(num===0){
          return "订阅取消"
        }else if (num===1){
          return "待审批"
        }else if (num===2){
          return "审批通过"
        }else if (num===3){
          return "审批不通过"
        }
      }
    },
    methods: {
      getData(){
        this.axios.get("/applications/recommend/",{
          params:{
            page:this.current,
            size:this.pageSize,
            apiName:this.apiName,
            sort:this.sort==="ascend"?"createTimeAsc":"createTimeDesc"
          }
        }).then(res=>{
          if (res.code==="0"){
            this.total=res.data.totalElements;
            this.data=res.data.content;
          }else{
            this.data=[];
            this.total=0;
          }
        })
      },
      cancel(id){
        let that=this;
        this.$confirm({
          title: '提示',
          content: '确认取消订阅当前API吗？',
          okText: '确认',
          cancelText: '取消',
          onOk(){
            that.axios.post("/applications/unSubscribeApi",{
              ids:[id-0]
            }).then(res=>{
              if (res.code==="0"){
                that.$notification.success({
                  message: '通知',
                  description:res.msg,
                  duration:3
                });
                that.getData();
              }
            })
          }
        })
      },
      onChange(val){
        this.current=val;
        this.getData();
      },
      onShowSizeChange(current, pageSize){
        this.current=1;
        this.pageSize=pageSize;
        this.getData();
      },
      handleTableChange(pagination, filters, sorter){
        this.current=1;
        this.sort=sorter.order;
        this.getData()
      }
    },
    created() {
      this.getData()
    }
  }
</script>

<style lang="less">
  .mySubscription {
    header{
      line-height: 66px;
      >div{
        >span:nth-child(1){
          float: left;
          padding-left: 24px;
          color: #666666;
          font-size: 16px;
        }
        .ant-input-search{
          float: right;
          margin-top: 14px;
          margin-right: 24px;
        }
      }
      .content{
        height: calc(~"100vh - 264px");
        overflow-y: auto;
      }
      .ant-pagination{
        text-align: right;
        margin: 20px;
      }
    }
    .ant-table-wrapper{
      width: 1022px;
      margin: 0 auto;
    }
  }
</style>
