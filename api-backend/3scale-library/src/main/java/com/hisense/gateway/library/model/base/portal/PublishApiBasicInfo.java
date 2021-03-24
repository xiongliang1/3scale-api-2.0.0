package com.hisense.gateway.library.model.base.portal;

import lombok.Data;

@Data
public class PublishApiBasicInfo {
    private String apiName;//api名称
    private String apiGroup;//api分组
    private String catageoryOne;//一级类目
    private String catageoryTwo;// 二级类目
    private String publishSystem;//所属系统
    private String env;//  环境信息
    private String subscriber;//订阅人
    private String subscribeSystem;//订阅系统
    private String subscribeTime;//订阅时间
    private String subscribeDesc;//订阅描述

}
