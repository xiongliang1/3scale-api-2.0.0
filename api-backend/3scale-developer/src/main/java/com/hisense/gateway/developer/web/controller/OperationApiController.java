package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.OperationApiDto;
import com.hisense.gateway.library.model.dto.web.OperationApiQuery;
import com.hisense.gateway.library.service.OperationApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api
@RequestMapping("/api/v1/{environment}/operationApi")
@RestController
public class OperationApiController {

    @Resource
    OperationApiService operationApiService;

    @ApiOperation("审计页面")
    @PostMapping("/operation")
    public Result<Page<OperationApiDto>> listByPage(
            @PathVariable String environment,
            @RequestBody OperationApiQuery operationApiQuery) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (operationApiQuery.getSort() != null && operationApiQuery.getSort().size() > 1) {
            direction = "d".equalsIgnoreCase(operationApiQuery.getSort().get(0)) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = operationApiQuery.getSort().get(1);
        }

        PageRequest pageable = PageRequest.of(
                0 != operationApiQuery.getPageNum() ? operationApiQuery.getPageNum() - 1 : 0, operationApiQuery.getPageSize(),
                Sort.by(direction, property,"id"));

        operationApiQuery.setUserName(CommonBeanUtil.getLoginUserName());
        Page<OperationApiDto> p = operationApiService.findByPage(environment, pageable, operationApiQuery);

        Result<Page<OperationApiDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }
}
