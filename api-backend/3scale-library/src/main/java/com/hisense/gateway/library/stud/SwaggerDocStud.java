package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.ApiDocs;

import java.util.List;

public interface SwaggerDocStud {
    List<ApiDocs> findAllSwaggerDoc(String host,String accessToken);

    List<ApiDocs> findSwaggerDocByServiceId(String host,String accessToken, String serviceId);
}
