/*
 * @author guilai.ming
 * @date 2020/7/5
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.pojo.base.EurekaService;

/**
 * 自动创建API 服务
 */
public interface AutoCreateService {
    /**
     * <h2>使用EurekaService信息创建API</h2>
     *
     * <h2>唯一性判断标准: 组合hash(partition, systemName, url)</h2>
     *
     * <h2>处理过程如下</h2>
     * <p>&#9;1-查询同service下已入库的API列表,基于唯一性hash,与已扫描上来的API列表求交集</p>
     * <p>&#9;2-位于交集之外的</p>
     * <p>&#9;2.1-未入库的,新建API</p>
     * <p>&#9;2.2-已入库的,未上线的,未发布的,在发布流程中的,不做处理</p>
     * <p>&#9;2.3-已入库的,未上线的,未发布的,不在发布流程中的,管理平台逻辑删除,3scale物理删除</p>
     * <p>&#9;2.2-已入库的,未上线的,已发布的,管理平台(3scale)均不能删除。</p>
     * <p>&#9;2.2-已入库的,已上线的,管理平台(3scale)均不能删除</p>
     * <p>
     * <p>&#9;3-位于交集之内的</p>
     * <p>&#9;3.1-未上线的,未发布的,在发布流程中的,不做处理</p>
     * <p>&#9;3.2-未上线的,未发布的,不在发布流程中的,管理平台和3scale均更新不影响发布的字段</p>
     * <p>&#9;3.3-未上线的,已发布的,管理平台和3scale均更新不影响发布的字段</p>
     * <p>&#9;3.4-已上线的,管理平台和3scale均更新不影响发布的字段</p>
     *
     * @param groupId       所属分组
     * @param eurekaService EurekaService信息
     * @return Result<Integer>
     */
    Result<Integer> createApis(Integer groupId, EurekaService eurekaService, String user);

    /**
     * <h2>服务下线,逻辑删除对应的API</h2>
     *
     * @param systemName 服务名
     * @return Result<Boolean>
     */
    Result<Boolean> logicDeleteApis(String systemName);
}
