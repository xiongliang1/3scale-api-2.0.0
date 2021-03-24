package com.hisense.gateway.library.model.base.alert;

import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;

import java.util.List;

/**
 * 告警策略查询条件组合对象
 *
 * @author guilai.ming 2020/09/10
 */
@Data
public class AlertPolicyQuery {
    private Integer page;
    private Integer size;
    private String name;//策略名称
    private List<Integer> statusList;//策略状态
    private List<String> sort;// 排序方式
    private TimeQuery timeQuery;
}
