package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiBasicInfo;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.library.model.pojo.base.ProcessRecord;
import com.hisense.gateway.library.service.PublishApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.constant.BaseConstants.URL_PUBLISH_API;

@Api
@Slf4j
@RequestMapping(URL_PUBLISH_API)
@RestController
public class PublishApiController {
    @Autowired
    PublishApiService publishApiService;

    @ApiOperation("get publishApi detail")
    @GetMapping("/{id}")
    public Result<PublishApiDto> getPublishApi(@PathVariable Integer id, HttpServletRequest request) {
        PublishApiDto result = publishApiService.getPublishApi(id);
        Result<PublishApiDto> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "/createPublishApi", method = RequestMethod.POST)
    public Result<Integer> createPublishApi(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PublishApiDto publishApiDto,
            HttpServletRequest request) throws Exception {
        return publishApiService.createPublishApi(tenantId, projectId,environment, publishApiDto);
    }

    @RequestMapping(value = "/listAll3ScaleApiIds", method = RequestMethod.GET)
    public List<Integer> listAll3ScaleApiIds(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("projectId") String projectId,
            HttpServletRequest request) {
        List<Integer> apiIdList = publishApiService.listAll3ScaleApiIds(tenantId);
        return new ArrayList<>(apiIdList);
    }

    @RequestMapping(value = "/deletePublishApi/{id}", method = RequestMethod.DELETE)
    public Result<Boolean> deletePublishApi(
            @PathVariable Integer id,
            HttpServletRequest request) {
        return publishApiService.deletePublishApi(id);
    }

    @RequestMapping(value = "/offlinePublishApi/{id}", method = RequestMethod.POST)
    public Result<Boolean> offlinePublishApi(
            @PathVariable Integer id,
            HttpServletRequest request) {
        return publishApiService.offlinePublishApi(id);
    }

    @RequestMapping(value = "/onlinePublishApi/{id}", method = RequestMethod.POST)
    public Result<Boolean> onlinePublishApi(
            @PathVariable Integer id,
            HttpServletRequest request) {
        return publishApiService.onlinePublishApi(id);
    }

    @RequestMapping(value = "/updatePublishApi/{id}", method = RequestMethod.POST)
    public Result<Boolean> updatePublishApi(
            @PathVariable String projectId,
            @PathVariable Integer id,
            @PathVariable String environment,
            @RequestBody PublishApiDto publishApiDto,
            HttpServletRequest request) {
        return publishApiService.updatePublishApi(id, null, projectId, publishApiDto,environment);
    }

    @ApiOperation("list all apis by page")
    @PostMapping()
    public Result<Page<PublishApiDto>> listByPage(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PublishApiQuery apiQuery) {
        log.info("apiQuery {}",apiQuery);

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (apiQuery.getSort() != null && apiQuery.getSort().size() > 1) {
            direction = "d".equalsIgnoreCase(apiQuery.getSort().get(0)) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = apiQuery.getSort().get(1);
        }

        PageRequest pageable = PageRequest.of(
                0 != apiQuery.getPageNum() ? apiQuery.getPageNum() - 1 : 0, apiQuery.getPageSize(),
                Sort.by(direction, property,"id"));// guilai.ming for spb 2.2.5-排序新增id字段，避免字段字段为空时重复

        Page<PublishApiDto> p = publishApiService.findByPage(tenantId, projectId, environment, pageable, apiQuery);

        return new Result<>(Result.OK, "", p);
    }

    @RequestMapping(value = "/promote", method = RequestMethod.POST)
    public Result<Boolean> promoteApi(
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody PromoteRequestInfo promoteRequestInfo,
            HttpServletRequest request) {
        return publishApiService.promotePublishApi(promoteRequestInfo,environment);
    }

    @RequestMapping(value = "/promoteList/{id}", method = RequestMethod.GET)
    public Result<List<PublishApiDto>> promoteList(
            @PathVariable String projectId,
            @PathVariable Integer id,
            HttpServletRequest request) {
        return publishApiService.getConfigPromoteList(id);
    }

    @RequestMapping(value = "/findAppIdAndKey/{id}", method = RequestMethod.GET)
    public Result<Map<String, String>> findAppIdAndKey(
            @PathVariable String projectId,
            @PathVariable Integer id,
            HttpServletRequest request) {
        return publishApiService.findAppIdAndKey(id);
    }

    /**
     * 获取API的订阅系统信息
     */
    @ApiOperation("获取API的订阅信息")
    @RequestMapping(value = "/findApiSubscribeSystem/{id}", method = RequestMethod.POST)
    public Result<Page<SubscribeSystemInfo>> findApiSubscribeSystem(
            @PathVariable Integer id,
            @RequestBody SubscribeSystemQuery subscribeSystemQuery) {
        return publishApiService.findApiSubscribeSystem(id, subscribeSystemQuery);
    }

    @ApiOperation("获取API的发布信息")
    @RequestMapping(value = "/findApiPublishInfos/{id}", method = RequestMethod.POST)
    public Result<Page<ProcessRecord>> findApiPublishInfos(
            @PathVariable Integer id,
            @RequestBody PublishApiQuery publishApiQuery) {
        return publishApiService.findApiPublishInfos(id, publishApiQuery);
    }

    @RequestMapping(value = "/checkApiName", method = RequestMethod.POST)
    public Result<Boolean> checkApiName(
            @PathVariable String projectId,
            @RequestBody PublishApiDto publishApiDto,
            HttpServletRequest request) {
        return publishApiService.checkApiName(projectId, publishApiDto);
    }

    @RequestMapping(value = "/checkUrl", method = RequestMethod.POST)
    public Result<Boolean> checkUrl(
            @PathVariable String projectId,
            @RequestBody PublishApiDto publishApiDto,
            HttpServletRequest request) {
        return publishApiService.checkUrl(projectId, publishApiDto);
    }

    /**
     * 批量删除
     *
     * @param publishApiBatch PublishApiBatch
     * @param request         HttpServletRequest
     * @return Result<List < String>>
     */
    @RequestMapping(value = "/deletePublishApis", method = RequestMethod.POST)
    public Result<List<String>> deletePublishApis(
            @RequestBody PublishApiBatch publishApiBatch,
            HttpServletRequest request) {
        return publishApiService.deletePublishApis(publishApiBatch);
    }

    @RequestMapping(value = "/deletePublishApisByScaleIds", method = RequestMethod.POST)
    public Result<Boolean> deletePublishApisByScaleIds(@RequestBody List<Long> scaleIds) {
        return publishApiService.deletePublishApisByScaleIds(scaleIds);
    }

    /**
     * 批量设置分组
     */
    @RequestMapping(value = "/setGroupForPublishApis", method = RequestMethod.POST)
    public Result<List<String>> setGroupForPublishApis(
            @RequestBody PublishApiBatch publishApiBatch,
            HttpServletRequest request) {
        return publishApiService.setGroupForPublishApis(publishApiBatch);
    }

    /**
     * 批量发布
     */
    @RequestMapping(value = "/promotePublishApis", method = RequestMethod.POST)
    public Result<List<String>> promotePublishApis(
            @RequestBody PublishApiBatch publishApiBatch,
            @PathVariable String environment,
            HttpServletRequest request) {
        publishApiBatch.setEnvironment(environment);
        return publishApiService.promotePublishApis(publishApiBatch);
    }

    /**
     * 批量下线
     */
    @RequestMapping(value = "/offlinePublishApis", method = RequestMethod.POST)
    public Result<List<String>> offlinePublishApis(
            @RequestBody PublishApiBatch publishApiBatch,
            HttpServletRequest request) {
        return publishApiService.offlinePublishApis(publishApiBatch);
    }

    @RequestMapping(value = "/getSubscribeStatusForApis", method = RequestMethod.POST)
    public Result<Map<Integer, Boolean>> getSubscribeStatusForApis(
            @RequestBody PublishApiBatch publishApiBatch,
            HttpServletRequest request) {
        return publishApiService.getSubscribeStatusForApis(publishApiBatch);
    }

    @PostMapping(value = "/uploadApiDocFile/{type}",consumes = "multipart/*",headers = "content-type=multipart/form-data")
    @ApiOperation("上传API文档,POST Form形式")
    @ApiImplicitParam(name = "type", value = "api文档类型(1：封面图片，2：附件)",dataType="String", required = true)
//    @ApiImplicitParam(name = "uploadFile", value = "表单 Input<name=\"uploadFile\",type=\"file\">", required = true)
    public Result<Integer> uploadApiDocFile(@PathVariable(value = "type") String type,
                                            @ApiParam(value="uploadFile",required = true) MultipartFile uploadFile,
                                            HttpSession session) {
        return publishApiService.uploadApiDoc(type,uploadFile, session);
    }

    @PostMapping(value = "/deleteApiDocFiles")
    @ApiOperation("删除API文档")
    public Result<List<String>> deleteApiDocFiles(@RequestBody PublishApiBatch apiBatch) {
        return publishApiService.deleteApiDocs(apiBatch);
    }

    @DeleteMapping(value = "/deleteApiDocFile/{id}")
    @ApiOperation("根据id删除API文档")
    public Result<Boolean> deleteApiDocFile(@PathVariable Integer id) {
        return publishApiService.deleteApiDoc(id);
    }

    @GetMapping("/showApiDocFile/{id}")
    @ApiOperation("API图片预览")
    public Result<String> showImage(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        return publishApiService.getImageBase64Str(id);
    }

    @RequestMapping(value="/prodPromote/{id}",method = RequestMethod.POST)
    @ApiOperation("一键发生产")
    public  Result<Boolean> oneClickPromoteApiToProd(@PathVariable String tenantId,
                                                     @PathVariable String projectId,
                                                     @PathVariable String environment,
            @PathVariable Integer id,@RequestBody PromoteApiToProd promoteApiToProd) throws Exception {
        return publishApiService.oneClickPromoteApiToProd(environment,tenantId,projectId,id,promoteApiToProd);
    }

    @ApiOperation("下载API文档")
    @GetMapping(value = "/downloadApiDocFile/{id}")
    public Result<Integer> downloadApiDocFile(@PathVariable Integer id, HttpServletResponse response)throws IOException {
        return publishApiService.downloadApiDoc(id, response);
    }

    @ApiOperation("流程中心获取api流程基本信息")
    @GetMapping(value = "/getApiBasicInfo/{processId}")
    public Result<PublishApiBasicInfo> getApiBasicInfo(@PathVariable String processId){
        return publishApiService.getApiBasicInfo(processId);
    }

    @ApiOperation("根据路由查询API信息")
    @GetMapping(value = "/findApiByRule")
    public Result<Page<PublishApiRuleInfo>> findApiByRule(@RequestParam String rule,
                                               @RequestParam(name = "page", defaultValue = "1", required = false)Integer page,
                                               @RequestParam(name = "size",defaultValue = "100",required = false)Integer size){
        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size);
        return publishApiService.findApiByRule(rule,pageable);
    }
}
