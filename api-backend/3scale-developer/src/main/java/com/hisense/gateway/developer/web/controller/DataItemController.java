package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiInfo;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.service.DataItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api
@Slf4j
@RequestMapping("/api/v1/{environment}/dataItems")
@RestController
public class DataItemController {

    @Resource
    DataItemService dataItemService;

    @ApiOperation("查询所有类目")
    @GetMapping("/searchDataItems")
    public Result<List<DataItem>> searchDataItems() {
        List<DataItem> result = dataItemService.searchDataItems();
        Map<Integer,DataItem> map=new HashMap<>();
        for(DataItem dataItem : result){
            map.put(dataItem.getId(),dataItem);
        }

        List<DataItem> dataItemList= new ArrayList<>();
        for(DataItem dataItem : result){
            //父节点
            if(dataItem.getParentId()==0){
                dataItemList.add(dataItem);
            }else{
                //根据parentId获取父节点
                DataItem dataItem1=map.get(dataItem.getParentId());
                if(dataItem1.getDataItemList()==null){
                    dataItem1.setDataItemList(new ArrayList<>());
                }
                //把子节点添加进父节点
                dataItem1.getDataItemList().add(dataItem);
            }
        }

        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(dataItemList);
        return returnResult;
    }
    

    @ApiOperation("API热门推荐")
    @GetMapping("/hotApi")
    public Result<List<PublishApiInfo>> hotApis(@PathVariable(value = "environment")String environment){

        return dataItemService.findHotRecommendApi(environment);
    }


    @ApiOperation("all dataItem and api ")
    @GetMapping("/findAllDataItems")
    public Map<String,Map<Integer,Object>> findAllDataItems(@RequestParam(value = "partition",required = false)Integer partition,
                                                            @PathVariable(value = "environment")String environment){

        return this.dataItemService.findAllDataItem(environment,partition);
    }

    @ApiOperation("查询所有一级类目")
    @GetMapping("/getCateGoryOne")
    public Result<List<DataItem>> getCateGoryOne(){
        List<DataItem> dataItemList = dataItemService.getCateGoryOne();
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(dataItemList);
        return returnResult;
    }

    @ApiOperation("查询所有一级类目下的API")
    @GetMapping("/{id}/getCateGoryOneAndApi")
    public Result<List<PublishApiInfo>> getCateGoryOneAndApi(@PathVariable(value = "environment")String environment,@PathVariable("id")Integer id){
        List<PublishApiInfo> getCateGoryOneAndApi = dataItemService.getCateGoryOneAndApi(id,environment);
        Result<List<PublishApiInfo>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(getCateGoryOneAndApi);
        return returnResult;
    }

    @ApiOperation("查询所有系统")
    @GetMapping("/getAllSystems")
    public Result<List<DataItem>> getAllSystems(@RequestParam(value = "systemName",required = false) String systemName,
                                                @RequestParam(value = "status",required = false,defaultValue = "0")Integer status){
        List<DataItem> dataItemList = dataItemService.getAllSystems(systemName,status);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setData(dataItemList);
        return returnResult;
    }

    @ApiOperation("查询系统，系统不存在发送邮件")
    @GetMapping("/getSystem/sendEmail")
    public Result<List<DataItem>> getSystemSendEmail(@RequestParam(value = "systemName",required = false) String systemName,
                                                @RequestParam(value = "status",required = false,defaultValue = "1")Integer status){
        List<DataItem> dataItemList = dataItemService.getAllSystems(systemName,status);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setAlert(1);
        if(Integer.valueOf("1").equals(status) && dataItemList.size()==0){
            returnResult.setMsg("邮件发送成功，等待管理员添加系统！");
        }else {
            returnResult.setMsg("查询成功");
        }
        returnResult.setData(dataItemList);
        return returnResult;
    }


}
