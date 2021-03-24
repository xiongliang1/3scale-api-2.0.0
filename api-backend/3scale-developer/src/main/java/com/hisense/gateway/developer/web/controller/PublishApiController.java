package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiInfo;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.service.PublishApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Api
@Slf4j
@RequestMapping("/api/v1/{environment}/publishApi")
@RestController
public class PublishApiController {

    @Resource
    PublishApiService publishApiService;

    @ApiOperation("get publishApi detail")
    @GetMapping("/{id}")
    public Result<PublishApiDto> getPublishApi(@PathVariable Integer id, HttpServletRequest request) {
        PublishApiDto result = publishApiService.getPublishApi(id);
        Result<PublishApiDto> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }


    @ApiOperation("list all apis by page")
    @GetMapping("/pagePublishApi")
    public  Result<Page<PublishApiDto>> pagePublishApi(@RequestParam(value = "page", defaultValue = "1",required = false)Integer page,
                                                       @RequestParam(value = "size", defaultValue = "10",required = false)Integer size,
                                                       @RequestParam(value = "name",required = false)String name,
                                                       @RequestParam(value = "categoryOne",required = false)Integer categoryOne,
                                                       @RequestParam(value = "categoryTwo",required = false)Integer categoryTwo,
                                                       @RequestParam(value = "system",required = false)Integer system,
                                                       @RequestParam(value = "sort",required = false)String sort,
                                                       @RequestParam(value = "partition",required = false)Integer partition,
                                                       @PathVariable String environment){



        Page<PublishApiDto> p = publishApiService.pagePublishApi(page,size,partition,name,categoryOne,categoryTwo,system,sort,environment);
        Result<Page<PublishApiDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }


    @ApiOperation("下载API文档")
    @GetMapping(value = "/downloadApiDocFile/{id}")
    public Result<Integer> downloadApiDocFile(@PathVariable Integer id, HttpServletResponse response)throws IOException {
        return publishApiService.downloadApiDoc(id, response);
    }


    @ApiOperation("API图片在线预览")
    @GetMapping("/showApiDocFile/{id}")
    public Result<String> showApiFile(@PathVariable Integer id) throws IOException{
        return publishApiService.getFileBase64Str(id);
    }

    @GetMapping("/findApiInfosByCategoryOne")
    public Result<List<PublishApiInfo>> findApiInfosByCategoryOne(@PathVariable(value = "environment")String environment,
                                                                  @RequestParam("cateGoryOneId") Integer cateGoryOneId){
        return publishApiService.findApiInfosByCategoryOne(environment,cateGoryOneId);
    }
}
