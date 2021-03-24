package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.dto.web.OperationApiDto;
import com.hisense.gateway.library.model.dto.web.OperationApiQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface OperationApiService {

    /**
     * 分页查询操作审计
     * @param pageable
     * @param operationApiQuery
     * @return
     */
    Page<OperationApiDto> findByPage(String environment,PageRequest pageable, OperationApiQuery operationApiQuery);
}
