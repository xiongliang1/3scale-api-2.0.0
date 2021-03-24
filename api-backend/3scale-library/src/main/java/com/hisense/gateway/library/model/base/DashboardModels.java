package com.hisense.gateway.library.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘DTO集合
 * <p>
 * 2020/10/20 guilai.ming
 */
public class DashboardModels {
    /**
     * 发布API数量TOP5的项目
     * <p>
     * for JTIP-274
     */
    @Data
    public static class ApiProjectInfo {
        Integer systemId;//系统id
        String systemName;//系统名称
        Integer apiCount;// 已发布api的数量
        String percentage;// 已发布api的数量的百分比
    }

    /**
     * 订阅API数量TOP5项目
     * <p>
     * for JTIP-274
     */
    @Data
    public static class SubscribeApiProjectInfo {
        String apiName;// 项目名称
        Integer subscribeCount;// 订阅API数量
    }

    /**
     * API访问总量数量, 今日访问数量
     * <p>
     * for JTIP-277, JTIP-278
     */
    @Data
    public static class ApiInvokeInfo {
        String apiName;// api名称+项目名称
        Integer invokeCount=0;// 访问总量

        @JsonIgnore
        private String scaleApplicationId;// 当前订阅对应的3scale-appid

        @JsonIgnore
        String projectId;//项目名称

        @JsonIgnore
        Integer apiId; // apiId
    }

    /**
     * API发布统计
     */
    @Data
    public static class ApiReleaseStatisticsVO{
        Integer apiProjectCount=0;//系统数量
        Integer apiCount=0;// 已发布api的数量
        List<ApiDetailVO> ApiDetails;
        Integer apiSubscribedCount=0;//累计被订阅
        List<ApiSubscribedVO> apiSubscribedVOs;
        Integer apiUseDayCount=0;//发布API日调用量
        Long apiUseSuccessDayount=0l;//发布API日成功次数
        Long apiUseFailDayount=0l;//发布API日失败次数
        BigDecimal apiUseFailDayRate;//发布API日运行错误率
        Integer apiUseTotalCount=0;//发布API总调用量
        Integer apiUseSuccessTotalCount=0;//发布API总成功次数
        Integer apiUseFailTotalount=0;//发布API总失败次数
        BigDecimal apiUseFailTotalRate;//发布API总错误率
        Integer applicationApplyCount=0;//待处理申请
    }

    /**
     * API发布统计
     */
    @Data
    public static class ApiDetailVO{
        String apiId;//api
        String apiName;//api名称
        String apiSystemId;//所属项目id
        String apiSystem;//所属项目
        String pattern;//接口路由
        String host;//后台接口地址
        String creator;//创建人
    }

    /**
     * 累计被订阅
     */
    @Data
    public static class ApiSubscribedVO{
        String apiId;
        String appId;
        String apiName;//项目名称
        String apiSystemId;
        String apiSystem;//发布项目
        String appSystemId;
        String appSystem;//订阅系统
        String subscriber;//订阅人
    }

    /**
     * api市场概览
     */
    @Data
    public static class ApiMarketOverview{

        String apiId;
        String apiName;//api名称
        String apiSystem;//发布项目
        String appSystemId;
        String appSystem;//订阅系统
        Integer apiInvokeTotalCount=0;//总调用量
        Integer apiInvokeDayCount=0;//日调用量
        Integer apiInvokeSuccessDayount=0;//调用成功
        Integer apiInvokeFailDayount=0;//调用失败


    }

}
