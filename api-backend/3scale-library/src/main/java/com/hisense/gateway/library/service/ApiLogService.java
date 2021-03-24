/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.monitor.ApiLogQueryFull;
import com.hisense.gateway.library.model.base.monitor.ApiLogQuerySingle;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * ApiLogService
 *
 * @author liyouzhi
 * @version v1.009-2911-20 10:34
 */
public interface ApiLogService {
    Result<Object> recall(ApiLogQuerySingle apiLogQuerySingle);

    /**
     * 导出日志Excel文件
     * @param response
     * @param apiLogQueryFull
     */
    void download(HttpServletResponse response, ApiLogQueryFull apiLogQueryFull);
}
