package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.model.base.DashboardModels.*;
import static com.hisense.gateway.library.model.base.DashboardQueries.*;

/**
 * 2020/10/20 guilai.ming
 */
public interface DashboardService {

    /**
     * API订阅量冷门TOP5柱图
     * @return
     */
    Result<List<PublishApiVO>> hitApiSubscribeCount(String environment,String projectId);

    /**
     * API订阅量TOP5柱图
     * @return
     */
    Result<List<PublishApiVO>> hotApiSubscribeCount(String environment,String projectId);

    /**
     * 获取已发布API数量TOP5项目列表
     */
    Result<List<ApiProjectInfo>> getTopPublishApiCountProjects(String environment,String project);

    /**
     * 获取订阅API数量TOP5项目列表
     * @param environment
     * @param project
     * @return
     */
    Result<List<ApiProjectInfo>> getTopSubscribeApiSystem(String environment,String project);

    /**
     * 获取订阅API数量TOP5项目列表
     */
    Result<Map<String,Object>> getTopSubscribeApiCount(String environment,String project,String value);

    /**
     * 获取访问总量数量TOP5的API信息列表
     */
    Result<Map<String,Object>> getTopInvokeCount(String environment, String projectId);

    /**
     * API发布统计
     * @param environment 平台环境
     * @param project 项目
     * @return result
     */
    Result<ApiReleaseStatisticsVO> getReleaseStatisticsInfos(String environment,String project);

    /**
     * 没有发布API的项目列表
     * @param environment 平台环境
     * @param project 项目
     * @return result
     */
    Result<List<ApiDetailVO>> unreleasedApiProjectInfos(String environment,String project);

    /**
     * API-TOP柱状图
     * @param environment 平台环境
     * @param project 项目
     * @return result
     */
    Result<Map<String,Object>> topApiBarChart(String environment,String project);
}
