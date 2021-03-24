/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author peiyun
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.DataItemDto;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.service.DataItemService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_DATAITEMS;

@Slf4j
@RequestMapping(URL_DATAITEMS)
@RestController
public class DataItemController {
    @Resource
    DataItemService dataItemService;

    @ApiOperation("查询所有类目")
    @GetMapping("/searchDataItems")
    public Result<List<DataItem>> searchDataItems(@RequestParam(name = "groupName",required = false)String groupName,
                                                  @RequestParam(name = "itemKey",required = false)String itemKey) {
        return dataItemService.searchDataItems(groupName,itemKey);
    }

    @RequestMapping(value = "/searchDataItem", method = RequestMethod.GET)
    public Result<Page<DataItem>> searchDataItems(@RequestParam(value = "page",defaultValue = "1",required = false)Integer page,
                                                  @RequestParam(value = "size",defaultValue = "10",required = false)Integer size,
                                                  HttpServletRequest request) {
        return dataItemService.searchDataItems(page, size);
    }

    @RequestMapping(value = "/{groupKey}/findDataItems", method = RequestMethod.GET)
    public Result<List<DataItem>> findDataItems(@PathVariable String groupKey, HttpServletRequest request) {
        List<DataItem> result = dataItemService.findDataItems(groupKey);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "/{groupKey}/findSystemDataItems", method = RequestMethod.GET)
    public Result<List<DataItem>> findSystemDataItems(
            @PathVariable String groupKey,
            @RequestParam Integer categoryOne,
            @RequestParam Integer categoryTwo,
            HttpServletRequest request) {
        List<DataItem> result = dataItemService.findSystemDataItems(groupKey, categoryOne, categoryTwo);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "/{groupKey}/findDataItemsByParentId/{parentId}", method = RequestMethod.GET)
    public Result<List<DataItem>> findDataItemsByParentId(
            @PathVariable String groupKey,
            @PathVariable Integer parentId,
            HttpServletRequest request) {
        List<DataItem> result = dataItemService.findDataItemsByParentId(groupKey, parentId);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @GetMapping("/{groupKey}/searchDataItem/{parentId}")
    public Result<List<DataItem>> searchDataItem(@PathVariable Integer parentId,@PathVariable String groupKey,
                                                 @RequestParam(name = "groupName",required = false)String groupName,
                                                 @RequestParam(name = "itemKey",required = false)String itemKey){
        List<DataItem> result = dataItemService.searchDataItem(groupKey,parentId,groupName,itemKey);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setMsg("查询成功");
        returnResult.setCode(Result.OK);
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "/createDataItem", method = RequestMethod.POST)
    public Result<Boolean> createDataItem(@RequestBody DataItemDto dataItemDto, HttpServletRequest request) {
        return dataItemService.createDataItem(dataItemDto);
    }

    @RequestMapping(value = "/deleteDataItem/{id}",method = RequestMethod.DELETE)
    public Result<Boolean> deleteDataItems(@PathVariable Integer id){
        return dataItemService.deleteDataItems(id);
    }

    @RequestMapping(value = "/updateDataItem/{id}",method = RequestMethod.POST)
    public Result<Boolean> updateDataItems(@PathVariable Integer id, @RequestBody DataItemDto dataItemDto){
        return dataItemService.updateDataItems(id, dataItemDto);
    }

    @RequestMapping(value = "/{groupKey}/findAllDataItems", method = RequestMethod.GET)
    public Result<List<DataItem>> findAllDataItems(@PathVariable String groupKey, HttpServletRequest request) {
        List<DataItem> result = dataItemService.findAllDataItems(groupKey);
        Result<List<DataItem>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }
}
