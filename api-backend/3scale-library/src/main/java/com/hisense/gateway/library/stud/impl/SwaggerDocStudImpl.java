/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSON;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.stud.SwaggerDocStud;
import com.hisense.gateway.library.stud.model.ApiDocs;
import com.hisense.gateway.library.stud.model.ApiDocsDtos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SwaggerDocStudImpl implements SwaggerDocStud {

    private static final String PATH_ACTIVE_DOCS_LIST = "/admin/api/active_docs.json?access_token=%s";

    @Override
    public List<ApiDocs> findAllSwaggerDoc(String host, String accessToken) {
        log.info("******start to invoke findAllSwaggerDoc,"
                        + "host is {}",
                host);
        List<ApiDocs> apiDocs = new ArrayList<>();
        ApiDocsDtos apiDocsDtos = new ApiDocsDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + String.format(PATH_ACTIVE_DOCS_LIST, accessToken));
            log.info("******end to invoke findAllSwaggerDoc,"
                            + "host is {},rlt is {}",
                    host, rlt);
            if (null == rlt) {
                return apiDocs;
            } else {
                apiDocsDtos = JSON.parseObject(rlt, ApiDocsDtos.class);
                apiDocs = apiDocsDtos.getApiDocs().stream().map(apidocs -> {
                    return apidocs.getApiDoc();
                })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("******fail to invoke findAllSwaggerDoc,"
                            + "host is {},e is {}",
                    host, e);
        }
        return apiDocs;
    }

    @Override
    public List<ApiDocs> findSwaggerDocByServiceId(String host, String accessToken, String serviceId) {
        log.info("******start to invoke findSwaggerDocByServiceId,"
                        + "host is {},serviceId is {}",
                host, serviceId);
        List<ApiDocs> apiDocs = new ArrayList<>();
        ApiDocsDtos apiDocsDtos = new ApiDocsDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + String.format(PATH_ACTIVE_DOCS_LIST, accessToken));
            log.info("******end to invoke findSwaggerDocByServiceId,"
                            + "host is {},serviceId is {},rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return apiDocs;
            } else {
                apiDocsDtos = JSON.parseObject(rlt, ApiDocsDtos.class);
                apiDocs = apiDocsDtos.getApiDocs().stream().map(apidocs -> {
                    return apidocs.getApiDoc();
                })
                        .filter(apidoc -> apidoc.getServiceId().equals(serviceId))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("******fail to invoke findSwaggerDocByServiceId,"
                            + "host is {},serviceId is {},e is {}",
                    host, serviceId, e);
        }
        return apiDocs;
    }
}
