/*
 * @author guilai.ming
 * @date 2020/8/4
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiCommentDto;
import com.hisense.gateway.library.model.dto.web.ApiCommentQuery;
import com.hisense.gateway.management.service.ApiCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.constant.BaseConstants.URL_COMMENT;

@Slf4j
@Api("评论控制器")
@RequestMapping(URL_COMMENT)
@RestController
public class ApiCommentController {
    @Autowired
    ApiCommentService apiCommentService;

    @ApiOperation("评论 创建一条评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentId", value = "父节点的ID", required = false, paramType = "body", dataType =
                    "int"),
            @ApiImplicitParam(name = "apiId", value = "API的ID", required = true, paramType = "body", dataType = "int"),
            @ApiImplicitParam(name = "type", value = "类型: 1-评论, 2-对评论的回复,或者对回复的回复", required = true, paramType =
                    "body", dataType = "int"),
            @ApiImplicitParam(name = "score", value = "分数: 1~5", required = true, paramType = "body", dataType = "int"),
            @ApiImplicitParam(name = "content", value = "评论或者回复的内容", required = true, paramType = "body", dataType =
                    "String"),
    })

    @PostMapping("/createComment")
    public Result<Boolean> createComment(@RequestBody ApiCommentDto apiComment) {
        return apiCommentService.createComment(apiComment);
    }

    @ApiOperation("评论 删除 指定Id对应的评论")
    @DeleteMapping("/deleteComment/{id}")
    public Result<Boolean> deleteComment(@PathVariable("id") Integer id) {
        return apiCommentService.deleteComment(id);
    }

    @ApiOperation("评论 分页查询 指定apiId对应的评论")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiId", value = "api ID", required = true, paramType = "body", dataType = "int"),
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "body", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "页内显示记录数量", required = true, paramType = "body", dataType = "int"),
            @ApiImplicitParam(name = "sort",
                    value = "排序方式,数组类型,第一字符串为d或者a,表降序或升序; 第二字符串表示排序字段名称",
                    required = true, paramType = "body", dataType = "List<String>"),
    })

    @PostMapping("/findApiComments")
    public Result<Page<ApiCommentDto>> findApiComments(@RequestBody ApiCommentQuery apiCommentQuery) {
        Result<Page<ApiCommentDto>> pageResult = apiCommentService.findPageByApiId(apiCommentQuery);
        log.info("{}Message {}", TAG, pageResult.getMsg());
        if (pageResult.isFailure()) {
            return new Result<>(Result.FAIL,"失败",null);
        } else {
            return new Result<>(Result.OK, "", pageResult.getData());
        }
    }

    @ApiOperation("评论 点赞 指定Id对应的评论")
    @PutMapping("/addLikeNum/{id}")
    public Result<Boolean> addLikeNum(@PathVariable("id") Integer id) {
        return apiCommentService.addLikeNum(id);
    }
}
