package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.library.model.pojo.base.SapBaseInfo;
import com.hisense.gateway.management.service.SapConvertInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_SAPCONVERTINFO;

/**
 * @Author: huangchen.ex
 * @Date: 2020/12/4 16:22
 */
@Api
@Slf4j
@RequestMapping(URL_SAPCONVERTINFO)
@RestController
public class SapConvertInfoController {
    @Autowired
    private SapConvertInfoService sapConvertInfoService;
    @ApiOperation("sap转换列表")
    @GetMapping("")
    public Result<Page<SapConvertInfoDto>> getSapConvertInfoList(
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(name = "prdFlag", required = false) String prdFlag,
            @PathVariable(name = "projectId", required = false) String projectId,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @RequestParam(name = "sort", required = false) String[] sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "id";
        if (sort != null && sort.length > 1) {
            direction = "d".equalsIgnoreCase(sort[0]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = sort[1];
        }

        SapConvertInfoDto sapConvertInfoDto = new SapConvertInfoDto();

        sapConvertInfoDto.setProjectId(projectId);
        sapConvertInfoDto.setPrdFlag(prdFlag);
        log.info("getSapConvertInfoList 发送参数"+sapConvertInfoDto);
        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<SapConvertInfoDto> p =
                sapConvertInfoService.findSapConvertInfoList(sapConvertInfoDto, pageable);

        Result<Page<SapConvertInfoDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }
    @ApiOperation("获取sap转换基础信息")
    @GetMapping("/{id}")
    public Result<SapBaseInfoDto> getSapBaseInfoDto(
            @PathVariable(name = "id", required = true) Integer id,
            @RequestParam(name = "env", required = true) String env,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId) {
        SapBaseInfo sapBaseInfo=new SapBaseInfo();
        sapBaseInfo.setEnv(env);
        sapBaseInfo.setId(id);
        Result<SapBaseInfoDto> returnResult = sapConvertInfoService.findSapBaseInfo(sapBaseInfo);
        return returnResult;
    }
    @ApiOperation("获取host、functionName两级联动列表")
    @GetMapping("/hostMenu")
    public Result<List> gethostMenuList(
            @RequestParam(name = "env", required = false) String env,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId) {
        SapBaseInfo sapBaseInfo=new SapBaseInfo();
        sapBaseInfo.setEnv(env);
        sapBaseInfo.setProjectId(projectId);
        Result<List> returnResult = sapConvertInfoService.findhostMenuList(sapBaseInfo);
        return returnResult;
    }
    @ApiOperation("创建sap")
    @PostMapping()
    public Result<Integer> createPublishApi(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody SapBaseInfoDto sapBaseInfoDto,
            HttpServletRequest request) {
        return sapConvertInfoService.saveSapConvertInfo(tenantId, projectId, environment, sapBaseInfoDto);
    }

    @ApiOperation("发布sap api")
    @PostMapping(value = "/publish")
    public Result<Boolean> PublishApi(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody SapConvertPromoteDto sapConvertPromoteDto,
            HttpServletRequest request) {
        return sapConvertInfoService.publishApi(sapConvertPromoteDto);
    }
}
