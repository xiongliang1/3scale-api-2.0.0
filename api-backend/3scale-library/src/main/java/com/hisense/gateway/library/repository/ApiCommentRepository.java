package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.dto.web.ApiCommentDto;
import com.hisense.gateway.library.model.pojo.base.ApiComment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApiCommentRepository extends CommonQueryRepository<ApiComment, Integer> {
    @Query("select new com.hisense.gateway.library.model.dto.web.ApiCommentDto(p.id,p.parent,p.publishApi,p.type," +
            "p.score,p.content,p.createTime,p.creator,p.childIds,p.likeNum) from ApiComment p where p.status=1 and p.type=2 and " +
            "p.parent.id =:parentId order by p.createTime desc")
    List<ApiCommentDto> getReplyListByParentId(@Param("parentId") Integer parentId);

    @Query("select new com.hisense.gateway.library.model.dto.web.ApiCommentDto(p.id,p.parent,p.publishApi,p.type," +
            "p.score,p.content,p.createTime,p.creator,p.childIds,p.likeNum) from ApiComment p where p.status=1 and p.type=2 and " +
            "p.id in(:commentIds) order by p.createTime desc")
    List<ApiCommentDto> getCommentsByIds(@Param("commentIds") List<Integer> commentIds);

    @Modifying
    @Transactional
    @Query("update ApiComment set likeNum=:likeNum where id =:id")
    void increaseLikeNum(@Param("id") Integer id, @Param("likeNum") Integer likeNum);
}
