package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.alert.AlertApiInfo;
import com.hisense.gateway.library.model.base.alert.AlertApiInfoQuery;
import com.hisense.gateway.library.model.base.alert.AlertPolicyQuery;
import com.hisense.gateway.library.model.base.alert.BatchOperation;
import com.hisense.gateway.library.model.dto.web.AlertPolicyDto;
import com.hisense.gateway.library.service.AlertPolicyService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_ALERT_POLICY;
import static com.hisense.gateway.library.model.Result.OK;

/**
 * 告警策略
 */
@Slf4j
@RequestMapping(URL_ALERT_POLICY)
@RestController
public class AlertPolicyController {
    @Autowired
    private AlertPolicyService alertPolicyService;

    /**
     * 创建策略时, 不考虑策略内容是否相同, 只入库即可
     */
    @ApiOperation("新增一条告警策略")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", paramType = "path", value = "租户ID", required = true, dataType = "string", defaultValue = "tenant_id_1"),
            @ApiImplicitParam(name = "projectId", paramType = "path", value = "项目ID", required = true, dataType = "string",defaultValue = "PID_TEST1"),
            @ApiImplicitParam(name = "environment", paramType = "path", value = "运行环境  \nstaging:测试环境  \nproduction:生产环境", required = true, dataType = "string"),
            @ApiImplicitParam(name = "name", paramType = "body", value = "告警策略名称", required = true, dataType = "string"),
            @ApiImplicitParam(name = "msgSendInterval", paramType = "body", value = "告警消息发送间隔,单位是分钟", required = true, dataType = "int"),
            @ApiImplicitParam(name = "msgReceivers", paramType = "body", value = "告警消息的接收人,数组", required = false, dataType = "string",allowMultiple=true),
            @ApiImplicitParam(name = "msgSendTypes", paramType = "body", value = "告警方式: 多选  \n0-短信  \n1-邮箱  \n2-信鸿公众号  \n3-微信公众号", required = true, dataType = "int",allowMultiple=true),
            @ApiImplicitParam(name = "triggerType", paramType = "body", value = "触发类型:  \n0-调用异常告警  \n1-响应超时告警  \n", required = true, dataType = "int"),
            @ApiImplicitParam(name = "alertLevel", paramType = "body", value = "告警级别:  \n0-主要告警  \n1-次要告警  \n", required = true, dataType = "int"),
            @ApiImplicitParam(name = "responseTime", paramType = "body", value = "响应超时告警 时 指定的接口调用最大时间阈值,秒为单位", required = false,  defaultValue = "2", dataType = "int")
    })
    @PostMapping("/addPolicy")
    public Result<Boolean> addPolicy(@PathVariable("tenantId") String tenantId,
                                     @PathVariable("projectId") String projectId,
                                     @PathVariable("environment") String environment,
                                     @ApiParam(value = "请求体json实例") @RequestBody AlertPolicyDto alertPolicyDto) {
        return alertPolicyService.saveAlertPolicy(projectId, InstanceEnvironment.fromCode(environment).getCode(), alertPolicyDto);
    }

    @ApiOperation("批量删除1条或多条告警策略")
    @ApiImplicitParam(name = "deleteIds", paramType = "body", value = "一个数组,存放待删除的策略的id", required = true,dataType = "int", allowMultiple=true)
    @PostMapping("/deleteAlertPolicies")
    public Result<Boolean> deleteAlertPolicies(@ApiParam(value = "请求体json实例")   @RequestBody BatchOperation batchOperation) {
        return alertPolicyService.deleteAlertPolicies(batchOperation.getDeleteIds());
    }

    @ApiOperation("更新一条指定的策略")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", paramType = "path", value = "租户ID", required = true, dataType = "string", defaultValue = "tenant_id_1"),
            @ApiImplicitParam(name = "projectId", paramType = "path", value = "项目ID", required = true, dataType = "string",defaultValue = "PID_TEST1"),
            @ApiImplicitParam(name = "environment", paramType = "path", value = "运行环境  \nstaging:测试环境  \nproduction:生产环境", required = true, dataType = "string"),
            @ApiImplicitParam(name = "name", paramType = "body", value = "告警策略名称", required = true, dataType = "string"),
            @ApiImplicitParam(name = "msgSendInterval", paramType = "body", value = "告警消息发送间隔,单位是分钟", required = true, dataType = "int"),
            @ApiImplicitParam(name = "msgReceivers", paramType = "body", value = "告警消息的接收人,数组", required = false, dataType = "string",allowMultiple=true),
            @ApiImplicitParam(name = "msgSendTypes", paramType = "body", value = "告警方式: 多选  \n0-短信  \n1-邮箱  \n2-信鸿公众号  \n3-微信公众号", required = true, dataType = "int",allowMultiple=true),
            @ApiImplicitParam(name = "triggerType", paramType = "body", value = "触发类型:  \n0-调用异常告警  \n1-响应超时告警  \n", required = true, dataType = "int"),
            @ApiImplicitParam(name = "alertLevel", paramType = "body", value = "告警级别:  \n0-主要告警  \n1-次要告警  \n", required = true, dataType = "int"),
            @ApiImplicitParam(name = "responseTime", paramType = "body", value = "响应超时告警 时 指定的接口调用最大时间阈值,秒为单位", required = false,  defaultValue = "2", dataType = "int")
    })
    @PostMapping("/updateAlertPolicy/{policyId}")
    public Result<Boolean> updateAlertPolicy(@PathVariable("projectId") String projectId,
                                             @PathVariable("policyId") Integer policyId,
                                             @PathVariable("environment") String environment,
                                             @ApiParam(value = "请求体json实例") @RequestBody AlertPolicyDto alertPolicyDto) {
        return alertPolicyService.updateAlertPolicy(projectId, InstanceEnvironment.fromCode(environment).getCode(), policyId,
                alertPolicyDto);
    }

    @ApiOperation("分页查询 当前project+environment下的 所有的 策略")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", paramType = "path", value = "项目ID", required = true, dataType = "string",defaultValue = "PID_TEST1"),
            @ApiImplicitParam(name = "environment", paramType = "path", value = "运行环境  \nstaging:测试环境  \nproduction:生产环境", required = true, dataType = "string"),
            @ApiImplicitParam(name = "page", paramType = "body", value = "分页编号", required = true ,dataType = "int"),
            @ApiImplicitParam(name = "size", paramType = "body", value = "分页size", required = true ,dataType = "int"),
            @ApiImplicitParam(name = "name", paramType = "body", value = "策略名称,模糊查询", required = false ,dataType = "string"),
            @ApiImplicitParam(name = "statusList", paramType = "body", value = "一个数组, 策略状态,0-关闭, 1-开启", required = false ,dataType = "int" ,allowMultiple=true),
            @ApiImplicitParam(name = "sort", paramType = "body", value = "字符串数组  \n第一个字符串为d,表示降序,否则升序;  \n第二个字符串是排序基准字段: createTime ", required = false ,dataType = "string",allowMultiple=true)
    })
    @PostMapping("/findAlertPoliciesByPage")
    public Result<Page<AlertPolicyDto>> findAlertPoliciesByPage(@PathVariable("projectId") String projectId,
                                                                        @PathVariable("environment") String environment,
                                                                        @ApiParam(value = "请求体json实例")  @RequestBody AlertPolicyQuery policyQuery) {
        return new Result<>(Result.OK,"",alertPolicyService.findAlertPoliciesByPage(projectId, InstanceEnvironment.fromCode(environment).getCode(), policyQuery));
    }

    @ApiOperation("全量查询 当前策略下已关联的API列表, 当前project+environment下未绑定策略的API列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "policyId", paramType = "path", value = "策略ID", required = true, dataType = "int"),
            @ApiImplicitParam(name = "tenantId", paramType = "path", value = "租户ID", required = true, dataType = "string", defaultValue = "tenant_id_1"),
            @ApiImplicitParam(name = "projectId", paramType = "path", value = "项目ID", required = true, dataType = "string",defaultValue = "PID_TEST1"),
            @ApiImplicitParam(name = "environment", paramType = "path", value = "运行环境  \nstaging:测试环境  \nproduction:生产环境", required = true, dataType = "string"),
            @ApiImplicitParam(name = "name", paramType = "body", value = "API名称,模糊查询", required = false ,dataType = "int"),
            @ApiImplicitParam(name = "groupIds", paramType = "body", value = "表示分组的ID, int型数组", required = false ,dataType = "int",allowMultiple=true),
            @ApiImplicitParam(name = "partitions", paramType = "body", value = "发布环境字段, int型数组  \n0-内网,1-外网", required = false ,dataType = "int",allowMultiple=true),
            @ApiImplicitParam(name = "bind", paramType = "body", value = "是否为已关联列表  \ntrue:已关联  \nfalse:未关联", required = true ,dataType = "boolean"),
    })
    @PostMapping("/findBindUnBindApiList/{policyId}")
    public Result<List<AlertApiInfo>> findBindUnBindApiList(@PathVariable("tenantId") String tenantId,
                                                            @PathVariable("projectId") String projectId,
                                                            @PathVariable("environment") String environment,
                                                            @PathVariable("policyId") Integer policyId,
                                                            @ApiParam(value = "请求体json实例") @RequestBody AlertApiInfoQuery infoQuery) {
        List<AlertApiInfo> apiInfos = alertPolicyService.findBindUnBindApiList(policyId, tenantId, projectId,
                InstanceEnvironment.fromCode(environment).getCode(), infoQuery);
        return new Result<>(OK, "", apiInfos);
    }

    @ApiOperation("批量绑定策略到API, 或者批量将API从策略解绑")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "policyId", paramType = "path", value = "策略ID", required = true, dataType = "int"),
            @ApiImplicitParam(name = "bindApiIds", paramType = "body", value = "要绑定此策略的API ID 列表", required = false ,dataType = "int",allowMultiple=true),
            @ApiImplicitParam(name = "unBindApiIds", paramType = "body", value = "要从此策略解绑的API ID 列表", required = false ,dataType = "int",allowMultiple=true),
    })
    @PostMapping("/bindToPublishApi/{policyId}")
    public Result<Boolean> bindToPublishApi(@PathVariable("policyId") Integer policyId,
                                            @ApiParam(value = "请求体json实例") @RequestBody BatchOperation batchOperation) {
        return alertPolicyService.bindToPublishApi(policyId, batchOperation.getBindApiIds(),
                batchOperation.getUnBindApiIds());
    }

    @ApiOperation("开启或关闭策略")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "policyId", paramType = "path", value = "策略ID", required = true, dataType = "int"),
            @ApiImplicitParam(name = "enable", paramType = "query", value = "true:开启  \nfalse:关闭", required = true, dataType = "boolean")
    })
    @PutMapping("/enableAlertPolicy/{policyId}")
    public Result<Boolean> enableAlertPolicy(@PathVariable("policyId") Integer policyId, @RequestParam(value = "enable") boolean enable) {
        return alertPolicyService.enableAlertPolicy(policyId, enable);
    }

    @ApiOperation("获取策略的详情")
    @ApiImplicitParam(name = "policyId", paramType = "path", value = "策略ID", required = true, dataType = "int")
    @GetMapping("/{policyId}")
    public Result<AlertPolicyDto> getAlertPolicy(@PathVariable("policyId") Integer policyId) {
        return alertPolicyService.getAlertPolicy(policyId);
    }
}
