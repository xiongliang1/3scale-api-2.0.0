package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.management.service.SubscribedApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_SUBSCRIBED_API;


/**
 * api列表-我订阅的
 */
@Api
@Slf4j
@RequestMapping(URL_SUBSCRIBED_API)
@RestController
public class SubscribedApiController {
    @Autowired
    SubscribedApiService subscribedApiService;

    @ApiOperation("api列表-我订阅的")
    @PostMapping()
    public Result<Page<ProcessRecordDto>> listByPage(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable String environment,
            @RequestBody SubscribedApiQuery apiQuery) {
        Page<ProcessRecordDto> p = subscribedApiService.findByPage(tenantId, projectId,environment, apiQuery);

        return new Result<>(Result.OK,"",p);
    }

    @ApiOperation("我订阅的-取消订阅")
    @PostMapping("/unSubscribeApi")
    public Result<List<String>> unSubscribeApi(@RequestBody PublishApiBatch publishApiBatch){
        return subscribedApiService.unSubscribeApi(publishApiBatch);
    }


}
