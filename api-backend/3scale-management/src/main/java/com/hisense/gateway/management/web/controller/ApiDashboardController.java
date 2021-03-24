package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiVO;
import com.hisense.gateway.management.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.constant.BaseConstants.URL_DASHBOARD;
import static com.hisense.gateway.library.model.base.DashboardModels.*;

@Api
@Slf4j
@RequestMapping(URL_DASHBOARD)
@RestController
public class ApiDashboardController {

    @Autowired
    DashboardService dashboardService;

    /**
     * API订阅数量冷门TOP5柱图
     * @return
     */
    @GetMapping("/hitSubscribeCount")
    @ApiOperation("API订阅量冷门TOP5柱图")
    public Result<List<PublishApiVO>> hitSubscribeCount(@PathVariable String environment,
                                                        @PathVariable String projectId){
        return dashboardService.hitApiSubscribeCount(environment,projectId);
    }

    /**
     * API订阅数量TOP5柱图
     * @return
     */
    @GetMapping("/hotSubscribeCount")
    @ApiOperation("API订阅量TOP5柱图")
    public Result<List<PublishApiVO>> hotSubscribeCount(@PathVariable String environment,
                                                        @PathVariable String projectId){
        return dashboardService.hotApiSubscribeCount(environment,projectId);
    }

    /**
     * 发布API数量TOP5项目列表，展示内容有项目名称，发布API数量，发布API占比
     */
    @GetMapping("/topPublishApiCountProjects")
    @ApiOperation("发布API数量TOP5项目列表")
    public Result<List<ApiProjectInfo>> getTopPublishApiCountProjects(@PathVariable String environment,@PathVariable String projectId) {
        return dashboardService.getTopPublishApiCountProjects(environment,projectId);
    }

    /**
     * 订阅API数量TOP5项目列表，展示内容有项目名称，订阅API数量，发布API占比
     */
    @GetMapping("/topSubscribeApiSystem")
    @ApiOperation("订阅API数量TOP5项目列表")
    public Result<List<ApiProjectInfo>> getTopSubscribeApiSystem(@PathVariable String environment,@PathVariable String projectId) {
        return dashboardService.getTopSubscribeApiSystem(environment,projectId);
    }

    @GetMapping("/topSubscribeApiCount")
    @ApiOperation("订阅API数量TOP5项目列表")
    public Result<Map<String,Object>> getTopSubscribeApiCount(@PathVariable String environment,@PathVariable String projectId) {

        return dashboardService.getTopSubscribeApiCount(environment,projectId,"top");
    }

    @GetMapping("/bottomSubscribeApiCount")
    @ApiOperation("订阅API数量BOTTTOM5项目列表")
    public Result<Map<String,Object>> getBottomSubscribeApiCount(@PathVariable String environment,@PathVariable String projectId) {
        return dashboardService.getTopSubscribeApiCount(environment,projectId,"bottom");
    }

    @GetMapping("/topInvokeCountTotal")
    @ApiOperation("累计API访问量TOP5的API信息列表")
    public Result<Map<String,Object>> topInvokeCountTotal(@PathVariable String environment,@PathVariable String projectId) {
        return dashboardService.getTopInvokeCount(environment,projectId);
    }

    @GetMapping("/releaseStatistics")
    @ApiOperation("API发布统计")
    public Result<ApiReleaseStatisticsVO> releaseStatistics(@PathVariable String environment,@PathVariable String projectId){
        return dashboardService.getReleaseStatisticsInfos(environment, projectId);
    }
    @GetMapping("/unreleasedApiProjectInfos")
    @ApiOperation("没有发布API的项目列表")
    public Result<List<ApiDetailVO>> unreleasedApiProjectInfos(@PathVariable String environment,@PathVariable String projectId){
        return dashboardService.unreleasedApiProjectInfos(environment,projectId);
    }

    @GetMapping("/topApiBarChart")
    @ApiOperation("API-TOP柱状图")
    public Result<Map<String,Object>> topApiBarChart(@PathVariable String environment,@PathVariable String projectId) {
        return dashboardService.topApiBarChart(environment,projectId);
    }

}
