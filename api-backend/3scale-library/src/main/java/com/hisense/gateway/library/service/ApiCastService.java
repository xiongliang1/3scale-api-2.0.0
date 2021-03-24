package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.PolicyHeader;
import com.hisense.gateway.library.stud.model.ApiCastParam;
import com.hisense.gateway.library.stud.model.PolicieConfig;
import com.hisense.gateway.library.stud.model.PolicieConfigDto;

import java.util.List;

public interface ApiCastService {
    /**
     * 配置IP黑白名单
     *
     * @param ipAddress 地址列表
     * @param blackList 是否为黑名单
     */
    Result<PolicieConfigDto> configIpBlackWhiteList(ApiCastParam param, List<String> ipAddress, boolean blackList);

    /**
     * 配置接口超时
     *
     * @param timeout 超时
     */
    Result<PolicieConfigDto> configConnectionTimeout(ApiCastParam param, Integer timeout);

    /**
     * 配置request header和response header
     * @param param
     * @param requestHeader
     * @param responseHeader
     * @return
     */
    Result<PolicieConfigDto> configHeaderpolicy(ApiCastParam param, List<PolicyHeader> requestHeader,List<PolicyHeader> responseHeader);

}
