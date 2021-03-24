package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;

/**
 * API批量操作
 */
@Data
public class PublishApiBatch {
    List<Integer> ids;// api ID
    List<Integer> partition;// 批量发布时，3scale发布环境
    Integer groupId;// 分组
    String environment;//管理端环境
}
