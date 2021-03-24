package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.monitor.*;
import com.hisense.gateway.library.service.ApiLogService;
import com.hisense.gateway.library.service.ElasticSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.MAPPING_PREFIX;

/**
 * @author guilai.ming 2020/09/25
 * 调用日志：分页查询
 */
@Api
@Slf4j
@RequestMapping(MAPPING_PREFIX + "apiInvokeLog")
@RestController
public class ApiLogController {
    @Autowired
    ElasticSearchService elasticSearchService;
    @Autowired
    ApiLogService apiLogService;

    @ApiOperation("分页查询调用日志")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "pageNum",
                    paramType = "body",
                    defaultValue = "1",
                    required = true,
                    dataType = "int",
                    value = "页码"),
            @ApiImplicitParam(
                    name = "pageSize",
                    paramType = "body",
                    defaultValue = "10",
                    required = true,
                    dataType = "int",
                    value = "页长"),
            @ApiImplicitParam(
                    name = "apiName",
                    paramType = "body",
                    defaultValue = "",
                    dataType = "String",
                    value = "指定API的名称"),
            @ApiImplicitParam(name = "sort",
                    paramType = "body",
                    value = "字符串数组  \n第一个字符串为d,表示降序,否则升序;  \n第二个字符串是排序基准字段: startTime ",
                    required = true,
                    dataType = "string",
                    allowMultiple = true),
            @ApiImplicitParam(
                    name = "timeQuery",
                    paramType = "body",
                    defaultValue = "{\"start\":\"2020-09-05 07:42:16\",\"end\":\"2020-09-06 07:42:16\"}",
                    required = true,
                    dataType = "Date",
                    value = "统计区间-起始终止时间"),
            @ApiImplicitParam(
                    name = "statType",
                    paramType = "body",
                    defaultValue = "0",
                    required = true,
                    dataType = "int",
                    value = "统计类型:  \n" +
                            "0: 全部  \n" +
                            "1: 请求成功  \n" +
                            "2: 请求失败  \n")
    })
    @RequestMapping(value = "/queryForPage", method = RequestMethod.POST)
    public Result<Page<ApiLogRecord>> queryForPage(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody ApiLogQueryFull apiLogQueryFull,
            HttpServletRequest request) {
        apiLogQueryFull.setTenantId(tenantId);
        apiLogQueryFull.setProjectId(projectId);
        apiLogQueryFull.setEnvironment(environment);
        Result<Page<ApiLogRecord>> result = elasticSearchService.queryForPage(apiLogQueryFull);
        log.info("QueryForPage {} {}", result.getCode(), result.getMsg());
        return result;
    }

    @ApiOperation("全量查询调用响应时间")
    @RequestMapping(value = "/queryForList", method = RequestMethod.POST)
    public Result<List<ApiResponseTime>> queryForList(
            @RequestBody ApiTrafficStatQuery statQuery,
            HttpServletRequest request) {
        Result<List<ApiResponseTime>> result = elasticSearchService.queryForList(statQuery);
        log.info("QueryForList {} {}", result.getCode(), result.getMsg());
        return result;
    }

    @ApiOperation("单个调用日志详情查询")
    @RequestMapping(value = "/queryForSingle", method = RequestMethod.POST)
    public Result<ApiCastLog> queryForSingle(@RequestBody ApiLogQuerySingle apiLogQuerySingle) {
        return elasticSearchService.queryForSingle(apiLogQuerySingle);
    }

    @ApiOperation("失败重发")
    @RequestMapping(value = "/recall", method = RequestMethod.POST)
    public Result<Object> recall(@RequestBody ApiLogQuerySingle apiLogQuerySingle) {
        return apiLogService.recall(apiLogQuerySingle);
    }

    @ApiOperation("日志导出Excel文件")
    @PostMapping("/download")
    public void download(@PathVariable String environment,
                         @PathVariable String tenantId,
                         @PathVariable String projectId,
                         @RequestBody ApiLogQueryFull apiLogQueryFull, HttpServletResponse response) throws UnsupportedEncodingException{
        //设置响应头和客户端保存文件名
        String fileName = "日志详情";
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;fileName="+ new String(fileName.getBytes("utf-8"),"ISO-8859-1" )+ ".xlsx");
        try {
            apiLogQueryFull.setTenantId(tenantId);
            apiLogQueryFull.setProjectId(projectId);
            apiLogQueryFull.setEnvironment(environment);
            apiLogService.download(response,apiLogQueryFull);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("文件刷新异常");
        }finally {
            try {
                if(response.getOutputStream()!=null){
                    response.getOutputStream().close();
                }
            }catch (IOException e){
                log.error("流关闭异常");
            }
        }
    }

}
