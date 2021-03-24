package com.hisense.gateway.developer.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.portal.DocRquestInfo;
import com.hisense.gateway.library.stud.model.ApiDocs;

import java.util.List;
import java.util.Map;

public interface SwaggerDocService {

    List<ApiDocs> findAllSwaggerDoc(String domainName);

    List<ApiDocs> findSwaggerDocByServiceId(String domainName, String serviceId);

    Map<String,String> getDefaultKey(String domainName, String serviceId);

    Map<String,Object> docRequest(String domain, String serviceId, DocRquestInfo docRequestInfo);

    Result<Map<String, String>> getAppIdAndkeyParam(String domain, Integer serviceId);
}
