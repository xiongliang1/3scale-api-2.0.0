package com.hisense.gateway.developer.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.DashboardModels.ApiMarketOverview;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @ClassName: DashboardService
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2020/12/24
 * @version: 1.0
 */
public interface DashboardService {


    /**
     * api市场概览
     * @param environment 平台环境
     * @return result
     */
    Page<ApiMarketOverview> apiMarketOverview(String environment, ProcessRecordQuery param);
}
