import {validateRoute} from "./filters";

export const columns = [
  {
    title: '参数名',
    dataIndex: 'name',
    scopedSlots: {customRender: 'name'},
  },
  {
    title: '参数位置',
    dataIndex: 'location',
    scopedSlots: {customRender: 'location'},
    width: 120
  },
  {
    title: "类型",
    dataIndex: 'type',
    scopedSlots: {customRender: 'type'},
    width: 120
  },
  {
    title: "必填",
    dataIndex: 'necessary',
    scopedSlots: {customRender: 'necessary'},
    width: 120
  },
  {
    title: "示例",
    dataIndex: 'sample',
    scopedSlots: {customRender: 'sample'}
  },
  {
    title: "描述",
    dataIndex: 'describe',
    scopedSlots: {customRender: 'describe'}
  },
  {
    title: '操作',
    dataIndex: 'operation',
    scopedSlots: {customRender: 'operation'},
    width: 80
  },
];

export const columnsBody = [
  {
    title: '名称',
    dataIndex: 'name',
    scopedSlots: {customRender: 'name'},
    width: 230
  },
  {
    title: '类型',
    dataIndex: 'type',
    scopedSlots: {customRender: 'type'},
    width: 120
  },
  {
    title: '示例',
    dataIndex: 'sample',
    scopedSlots: {customRender: 'sample'}
  },
  {
    title: '描述',
    dataIndex: 'describe',
    scopedSlots: {customRender: 'describe'}
  },
  {
    title: '操作',
    dataIndex: 'operation',
    scopedSlots: {customRender: 'operation'},
    width: 160
  }
];

export const columnsOutput = [
  {
    title: '名称',
    dataIndex: 'name',
    scopedSlots: {customRender: 'name'},
    width: 230
  },
  {
    title: '类型',
    dataIndex: 'type',
    scopedSlots: {customRender: 'type'},
    width: 120
  },
  {
    title: '示例',
    dataIndex: 'sample',
    scopedSlots: {customRender: 'sample'}
  },
  {
    title: '描述',
    dataIndex: 'describe',
    scopedSlots: {customRender: 'describe'}
  },
  {
    title: '操作',
    dataIndex: 'operation',
    scopedSlots: {customRender: 'operation'},
    width: 160
  }
];

export const dataType=['string','int','boolean','byte','char','short','float','double','long','object','Array','List','Set'];//数据类型

export const rules={//表单校验
  grouping: [
    {required: true, message: '请选择分组', trigger: 'blur'}
  ],
  name: [
    {required: true, message: '请填写API名称', trigger: 'blur', whitespace: true},
    {min: 3, max: 64, message: '请输入3-64个字符', trigger: 'blur'},
    {pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9-\u4e00-\u9fa5]+$/, message: '请正确填入api名称', trigger: ['change', 'blur']}
  ],
  note: [
    {min: 1, max: 1000, message: '请输入1-1000个字符', trigger: 'blur'},
  ],
  environment: [
    {required: true, message: '请选择网关类型', trigger: 'blur'}
  ],
  subscription: [
    {required: true, message: '请选择是否需要订阅', trigger: 'blur'}
  ],
  authentication: [
    {required: true, message: '请选择是否需要鉴权', trigger: 'blur'}
  ],
  log: [
    {required: true, message: '请选择是否需要记录日志', trigger: 'blur'}
  ],
  url: [
    {required: true, message: '请输入URL前缀', trigger: 'blur', whitespace: true},
    {min: 1, max: 64, message: '请输入1-64个字符', trigger: 'blur'},
    {pattern: /^[/][A-Za-z0-9/-]+$/, message: '请正确输入URL前缀', trigger: 'blur'}
  ],
  routeRules: [
    {required: true, message: '请填写路由规则', trigger: 'blur'},
    {validator: validateRoute, trigger: 'blur'}
  ],
  time: [
    {required: true, message: '请输入超时时间', trigger: 'blur'},
    {pattern:/^\d+$/,message: '只支持整数',trigger: ['blur','change']}
  ],
  agreement: [
    {required: true, message: '请选择协议', trigger: 'blur'}
  ],
  serve: [
    {required: true, message: '请输入后端服务', trigger: 'blur', whitespace: true},
    {
      validator:(rule, value, callback)=>{
        if (value.includes("://")){
          callback(new Error("请输入正确格式的服务地址"))
        }
        callback();
      },trigger: 'blur'
    }
  ],
  record: [
    {required: true, message: '请选择是否记录返回值', trigger: 'blur'}
  ],
  secretLevel:[
    {required: true, message: '请选择等级密度', trigger: 'blur'}
  ],
  secretToken:[
    {validator: (rule, value, callback)=>{
        let reg=/.*[\u4e00-\u9fa5]+.*$/;
        if(reg.test(value)){
          callback(new Error('不允许输入中文'));
        }
        callback()
      }, trigger: 'blur'}
  ]
};

export const groupRules={//分组添加的路由校验
  name: [
    {required: true, message: '请输入组名称', trigger: 'blur'},
    {min: 1, max: 60, message: '请不要输入超过60个字符', trigger: 'blur'},
    {pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9-\u4e00-\u9fa5]+$/, message: '请正确填入组名称', trigger: ['change', 'blur']}
  ],
  note: [
    {min: 0, max: 1000, message: '请不要输入超过1000个字符', trigger: 'blur'},
  ],
  two: [
    {validator:(rule, value, callback)=> {
      console.log(value)
        if (!this.groupForm.one&&!this.groupForm.two){
          callback(new Error('请选择一级类目和二级类目'))
        }
        callback();
      },trigger: 'change'}
  ],
  system: [
    {required: true, message: '请选择所属系统', trigger: 'blur'},
  ]
}
export const columnsRequestHeader=[
  {
    title: '名称',
    dataIndex: 'header',
    scopedSlots: {customRender: 'header'}
  },
  {
    title: '值',
    dataIndex: 'value',
    scopedSlots: {customRender: 'value'}
  },
  {
    title:"操作",
    dataIndex: 'count',
    scopedSlots: {customRender: 'count'},
    width: 100
  }
];
export const columnsResponsetHeader=[
  {
    title: '名称',
    dataIndex: 'header',
    scopedSlots: {customRender: 'header'}
  },
  {
    title: '值',
    dataIndex: 'value',
    scopedSlots: {customRender: 'value'}
  },
  {
    title: '操作',
    dataIndex: 'op',
    scopedSlots: {customRender: 'op'},
    width:200
  },
  {
    title: '类型',
    dataIndex: 'valueType',
    scopedSlots: {customRender: 'valueType'},
    width:200
  },
  {
    title:"",
    dataIndex: 'count',
    scopedSlots: {customRender: 'count'},
    width: 100
  }
];
