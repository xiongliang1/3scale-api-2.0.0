package com.hisense.gateway.library.model.base.monitor;

import com.hisense.gateway.library.stud.model.AnalyticsPeriod;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * API 调用 响应时间 统计
 *
 * @author guilai.ming 2020/09/22
 */
@Data
public class ApiResponseStatistics {
    AnalyticsPeriod period;
    /**
     * 调用时间占据-百分比: p50,p75,p90,p95,p99
     */
    private List<ResponseStat> values;

    @Data
    public static final class ResponseStat {
        private String date;// x轴
        private boolean showX;//是否显示x坐标轴的点
        private Map<String,Integer> map;// 5个等级(p50,p75,p90,p95,p99)分别对应的调用时间
    }
}
