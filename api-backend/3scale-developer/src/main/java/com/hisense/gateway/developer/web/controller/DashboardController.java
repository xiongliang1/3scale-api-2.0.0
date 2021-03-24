package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.developer.service.DashboardService;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.DashboardModels.ApiMarketOverview;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * @ClassName: DashboardController
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2020/12/24
 * @version: 1.0
 */
@Api
@RequestMapping("/api/v1/{environment}/dashboard")
@RestController
public class DashboardController {

    @Autowired
    DashboardService dashboardService;

    @PostMapping("/apiMarket/overview")
    @ApiOperation("api市场概览")
    public Result<Page<ApiMarketOverview>> apiMarketOverview(@PathVariable String environment,
                                                       @RequestBody ProcessRecordQuery processRecordQuery){
        Page<ApiMarketOverview> apiMarketOverviews = dashboardService.apiMarketOverview(environment, processRecordQuery);
        Result<Page<ApiMarketOverview>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(apiMarketOverviews);
        return returnResult;
    }
}
