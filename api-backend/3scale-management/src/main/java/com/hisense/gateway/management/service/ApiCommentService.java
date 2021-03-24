/*
 * @author guilai.ming
 * @date 2020/8/4
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiCommentDto;
import com.hisense.gateway.library.model.dto.web.ApiCommentQuery;
import org.springframework.data.domain.Page;

public interface ApiCommentService {
    /**
     * 新增一条评论\回复
     *
     * @param apiComment 评论或者回复
     * @return 是否操作成功
     */
    Result<Boolean> createComment(ApiCommentDto apiComment);

    /**
     * 删除评论
     *
     * @param id 评论id
     * @return 是否操作成功
     */
    Result<Boolean> deleteComment(Integer id);

    /**
     * 查询某API对应的评论列表
     *
     * @param apiCommentQuery 查询参数
     * @return 评论列表
     */
    Result<Page<ApiCommentDto>> findPageByApiId(ApiCommentQuery apiCommentQuery);

    /**
     * 点赞一条评论
     *
     * @param id 评论id
     * @return 是否操作成功
     */
    Result<Boolean> addLikeNum(Integer id);
}
