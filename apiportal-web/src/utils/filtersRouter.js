function fun(text) {
   if(text==="/principal/main"){
      return "首页"
   }else if (text==="/principal/list"){
      return "功能与服务"
   }else if (text==="/principal/developerCenter/mySubscription"){
      return "开发者中心-我的订阅"
   }else if (text==="/principal/developerCenter/myApply"){
      return "开发者中心-我的申请"
   }else if (text==="/principal/developerCenter/logs"){
      return "开发者中心-日志审计"
   }else if (text==="/principal/apiDetails"){
      return "开发者中心-api详情"
   }else if (text==="/principal/portDetails"){
      return "功能与服务-api详情"
   }else if (text==="/principal/developerCenter/flowChart"){
      return "流程图"
   }else if (text==="/principal/monitoring"){
      return "接口监控"
   }else if (text==="/principal/developerCenter/dashboard"){
      return "统计信息"
   }
}


export default fun
