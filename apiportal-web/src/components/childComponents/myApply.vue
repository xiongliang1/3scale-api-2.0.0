<template>
  <div class="myApply">
    <header class="clearfix">
      <a-radio-group v-model="value" @change="change">
        <a-radio-button :value="0">
          待审批
        </a-radio-button>
        <a-radio-button :value="1">
          已审批
        </a-radio-button>
      </a-radio-group>
      <a-input-search placeholder="请输入你要查询的服务" v-model.trim="apiName" @search="onSearch"/>
    </header>
    <div class="content">
      <a-table
        :columns="columns"
        :data-source="data"
        :rowKey="row=>row.id"
        :pagination="{total:0,hideOnSinglePage:true,pageSize:pageSize}"
        @change="handleTableChange"
        size="small"
      >
        <div slot="name" slot-scope="text">
          <span>{{text}}</span>
        </div>
        <div slot="status" slot-scope="text">
          {{text|status(that)}}
        </div>
        <div slot="action" slot-scope="text,record">
          <span style="color: #00AAA6;margin-right:12px;cursor: pointer" @click="$router.push({path:'/principal/apiDetails',query:{id:record.apiId,type:value,systemId:record.systemId,listId:record.id}})">
            详情
          </span>
          <span style="color: #00AAA6;margin-right:12px;cursor: pointer" @click="$router.push({path:'/principal/monitoring',query:{id:record.apiId,type:1,systemId:record.system,listId:record.id}})">接口监控</span>
          <span style="color: #00AAA6;cursor: pointer" @click="processRecord(record.processInstID)" v-show="record.processInstID">
            查看流程
          </span>
        </div>
      </a-table>
    </div>
    <a-pagination
            v-model="current"
            :total="total"
            @change="onChange"
            @showSizeChange="onShowSizeChange"
            show-size-changer
            show-quick-jumper
            show-less-items
            :show-total="total => `共${total}条`"
            size="small" />
  </div>
</template>

<script>
  const columns = [
    {
      title: '服务名称',
      dataIndex: 'apiName',
      key: 'apiName',
      scopedSlots: {customRender: 'name'},
      width: 180
    },
    {
      title: "订阅系统",
      dataIndex: 'appSystemName',
      key: 'appSystemName',
      filters:[]
    },
    {
      title: '审批状态',
      dataIndex: 'status',
      key: 'status',
      scopedSlots: {customRender: 'status'},
      filterMultiple:false
    },
    {
      title: '申请时间',
      key: 'createTime',
      dataIndex: 'createTime',
      width:160,
      sorter:true
    },
    {
      title: '操作',
      key: 'action',
      dataIndex: 'action',
      scopedSlots: { customRender: 'action' }
    },
  ];
  export default {
    name: "myApply",
    data() {
      return {
        value: 0,
        data:[],
        columns,
        that:this,
        apiName:null,
        total:0,
        current:1,
        pageSize:10,
        searchData:{
          systemIds:null,
          status:null,
          sort: null
        }
      }
    },
    filters:{
      status(num,that){
        if (!that.value){
          return "待审批"
        }else{
          if (num===2||!num){
            return "通过"
          }else if (num===3){
            return "拒绝"
          }else{
            return ""
          }
        }
      }
    },
    methods: {
      onSearch() {
        this.getData();
      },
      getData(){
        if (!this.value){
          this.columns[2].filters=[];
        }else{
          this.columns[2].filters=[
            {
              text:"通过",
              value:"2"
            },{
              text:"拒绝",
              value:"3"
            }
          ]
        }
        this.axios.post("/processRecord/myApplication",{
          page:this.current,
          size:this.pageSize,
          apiName:this.apiName,
          approvalComplete:Boolean(this.value),
          applicationSystem:this.searchData.systemIds,
          approvalStatus:this.searchData.status?[this.searchData.status-0]:null,
          sort:this.searchData.order==="ascend"?"a":"d"
        }).then(res=>{
          this.data=res.data.content;
          this.total=res.data.totalElements;
        })
      },
      onChange(val){
        this.current=val;
        this.getData();
      },
      change(){
        this.apiName=null;
        this.getData();
        this.current=1;
      },
      handleTableChange(pagination, filters, sorter){
        this.current=1;
        this.searchData={
          systemIds: filters.appSystemName?(filters.appSystemName.length?filters.appSystemName.map(Number):null):null,
          status:filters.status?(filters.status.length?filters.status[0]:null):null,
          order:sorter.order
        }
        this.getData();
      },
      processRecord(id){//查看审批流程
         this.$router.push({path:"/principal/developerCenter/flowChart",query:{id:id}})
      },
      onShowSizeChange(current, pageSize){
        this.current=1;
        this.pageSize=pageSize;
        this.getData();
      },
    },
    created() {
      this.getData();
      let systemList=JSON.parse(sessionStorage.getItem("systemList"));
      systemList.forEach(item=>{
        this.columns[1].filters.push({
          text:item.itemName,
          value:item.id+""
        })
      })
    },
    beforeDestroy() {
      this.columns[1].filters=[];
    }
  }
</script>

<style lang="less">
  .myApply {
    padding: 14px;
    overflow: hidden;
    header {
      margin-bottom: 16px;

      .ant-radio-group {
        float: left;
      }

      .ant-input-search {
        float: right;
        width: 300px;
      }
    }
    .ant-pagination{
      text-align: right;
      margin: 20px;
    }
  }
</style>
