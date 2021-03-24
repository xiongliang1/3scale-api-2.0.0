package com.hisense.gateway.library.model.dto.web;

import com.hisense.gateway.library.model.pojo.base.Instance;
import lombok.Data;

import java.util.List;

/**
 * 订阅api入参
 */
@Data
public class SubscribedApi {
    private Integer id;//apiId
    private List<Integer> system;//订阅系统（支持为多个系统订阅同一个api）
    private String description;//订阅说明
    private String messageStu; //消息 message
    private String userName;
    private Instance ins;
}
