package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.pojo.base.PublishApplication;

/**
 * @ClassName: ApiInvokeRecordService
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2021/1/18
 * @version: 1.0
 */
public interface ApiInvokeRecordService {

    /**
     * 获取当前3scale今日调用量并更新
     * @param app
     */
    void saveInvokeCount(PublishApplication app);

    /**
     * 更新appId、appKey
     * @param appId applicationId
     */
    void updateApiIdAndAppKey(Integer appId);

    /**
     * 更新appId、appKey
     * @param apiId apiId
     */
    void updateApiIdAndAppKeyByApiId(Integer apiId);

    /**
     * 更新appId、appKey
     * @param id id
     * @param appId appId
     * @param appKey appKey
     */
    void updateApiIdAndAppKey(Integer id,String appId,String appKey);


}
