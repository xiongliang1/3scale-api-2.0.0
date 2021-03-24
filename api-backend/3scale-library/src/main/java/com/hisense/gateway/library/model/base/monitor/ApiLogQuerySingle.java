package com.hisense.gateway.library.model.base.monitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author liyouzhi.ex 2020/09/29
 * <p>
 * Api调用记录全量查询, 不指定API\或订阅系统
 */
@Data
public class ApiLogQuerySingle {
    String hashId;// ES存储日志记录的id
    String requestTime;// 请求时间
    Integer apiId;// apiId
}
