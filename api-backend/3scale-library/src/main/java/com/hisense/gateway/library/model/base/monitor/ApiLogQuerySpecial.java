package com.hisense.gateway.library.model.base.monitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;

import java.util.List;

/**
 * @author guilai.ming
 * <p>
 * API调用日志通用查询接口-定向查询
 */
@Data
public class ApiLogQuerySpecial {
    private Integer apiId;// 指定某个api
    private Integer appId;// 指定某个订阅系统
    private Integer statType;//统计类型:0-all,1-调用成功的,2-调用失败的,必填
    private TimeQuery timeQuery;//查询起始时间,必填
    private Integer errorCode;//错误码类型：3->2XX;4->4XX;5->5XX

    @JsonIgnore
    private List<String> indexList;

    @JsonIgnore
    private Long scaleServiceId;

    @JsonIgnore
    private String appUserKey;

    @JsonIgnore
    public boolean isValid() {
        if (apiId == null && appId == null) {
            return false;
        }

        if (statType < 0 || statType > 2) {
            return false;
        }

        return timeQuery != null && timeQuery.getStart() != null && timeQuery.getEnd() != null;
    }
}
