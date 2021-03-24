/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.management.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiCommentDto;
import com.hisense.gateway.library.model.dto.web.ApiCommentQuery;
import com.hisense.gateway.library.model.pojo.base.ApiComment;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.repository.ApiCommentRepository;
import com.hisense.gateway.library.repository.PublishApiRepository;
import com.hisense.gateway.management.service.ApiCommentService;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

import static com.hisense.gateway.library.constant.ApiConstant.ApiCommentType.API_COMMENT_REPLY;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;

@Slf4j
@Service
public class ApiCommentServiceImpl implements ApiCommentService {
    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    ApiCommentRepository apiCommentRepository;

    @Transactional
    @Override
    public Result<Boolean> createComment(ApiCommentDto commentDto) {
        Result<Boolean> result = new Result<>(Result.FAIL, "错误", false);

        if (commentDto.getType() == null) {
            result.setMsg("错误, 请指定正确的类型: 顶级为评论, 次级为回复, 仅支持两级");
            return result;
        }

        if (MiscUtil.isEmpty(commentDto.getContent())) {
            result.setMsg("错误, 评论内容不能为空");
            return result;
        }

        if (commentDto.getApiId() == null || commentDto.getApiId() <= 0) {
            result.setMsg("错误, 评论必须对应一个API");
            return result;
        }

        if (commentDto.getScore() == null || commentDto.getScore() <= 0) {
            result.setMsg("错误, 评论分数无效");
            return result;
        }

        if (commentDto.getCommentType() == API_COMMENT_REPLY &&
                (commentDto.getParentId() == null || commentDto.getParentId() <= 0)) {
            result.setMsg("错误, 回复必须对应一条评论");
            return result;
        }

        PublishApi publishApiRes;
        Integer apiId = commentDto.getApiId();
        if ((publishApiRes = publishApiRepository.findOne(apiId)) == null) {
            result.setMsg("错误, 所对应的Api不存在,禁止评论");
            return result;
        }

        ApiComment commentParent = null;
        if (commentDto.getParentId() != null && (commentParent =
                apiCommentRepository.findOne(commentDto.getParentId())) == null) {
            result.setMsg("错误, 所对应的父级评论/回复 不存在,禁止新增");
            return result;
        }

        ApiComment apiComment = new ApiComment();
        apiComment.setParent(commentParent);
        apiComment.setPublishApi(publishApiRes);
        apiComment.setType(commentDto.getType());
        apiComment.setScore(commentDto.getScore());
        apiComment.setContent(commentDto.getContent());
        apiComment.setCreateTime(new Date());
        apiComment.setCreator(GlobalSettings.getVisitor().getUsername());
        apiComment.setStatus(1);
        ApiComment apiCommentRes = apiCommentRepository.save(apiComment);

        if (commentParent != null) {
            String childIds = commentParent.getChildIds();
            if (MiscUtil.isEmpty(childIds)) {
                commentParent.setChildIds(String.format("%s", apiCommentRes.getId()));
            } else {
                commentParent.setChildIds(String.format("%s,%s", childIds, apiCommentRes.getId()));
            }
            apiCommentRepository.saveAndFlush(commentParent);
        }

        result.setCode(Result.OK);
        result.setMsg(String.format("%s成功", commentDto.getCommentType().getDescription()));
        result.setData(true);

        log.info("{}Success to add one comment {}", TAG, apiComment);

        return result;
    }

    @Transactional
    @Override
    public Result<Boolean> deleteComment(Integer id) {
        Result<Boolean> result = new Result<>(Result.FAIL, "错误", false);
        ApiComment apiComment = apiCommentRepository.findOne(id);
        if (apiComment == null) {
            result.setMsg("错误, 指定的评论不存在");
            return result;
        }

        apiComment.setStatus(0);
        apiCommentRepository.saveAndFlush(apiComment);
        log.info("{}Success to delete one comment {}", TAG, id);

        result.setMsg("删除成功");
        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    @Override
    public Result<Page<ApiCommentDto>> findPageByApiId(ApiCommentQuery commentQuery) {
        Result<Page<ApiCommentDto>> result = new Result<>(Result.FAIL, "错误", null);
        Integer apiId = commentQuery.getApiId();
        if (apiId == null || apiId <= 0) {
            result.setMsg("错误, 指定的API ID 无效");
            return result;
        }

        PublishApi publishApiRes;
        if ((publishApiRes = publishApiRepository.findOne(apiId)) == null) {
            result.setMsg("错误, 所对应的Api不存在");
        }

        String property = "createTime";
        Sort.Direction direction = Sort.Direction.DESC;
        if (commentQuery.getSort() != null && commentQuery.getSort().size() > 1) {
            direction = "d".equalsIgnoreCase(commentQuery.getSort().get(0)) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = commentQuery.getSort().get(1);
        }

        PageRequest pageable = PageRequest.of(
                commentQuery.getPage() != 0 ? commentQuery.getPage() - 1 : 0, commentQuery.getSize(),
                Sort.by(direction, property));

        Specification<ApiComment> specification = (root, query, builder) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(builder.equal(root.get("type").as(Integer.class), 1));
            list.add(builder.equal(root.get("status").as(Integer.class), 1));// 已删除的不显示
            list.add(builder.equal(root.get("publishApi").get("id").as(Integer.class), apiId));
            return builder.and(list.toArray(new Predicate[0]));
        };

        Page<ApiComment> apiComments = apiCommentRepository.findAll(specification, pageable);

        List<ApiComment> apiCommentList;
        if (MiscUtil.isEmpty(apiCommentList = apiComments.getContent())) {
            result.setMsg("错误, 当前API无评论");
            return result;
        }

        List<ApiCommentDto> apiCommentDtoList = new ArrayList<>();
        for (ApiComment apiComment : apiCommentList) {
            ApiCommentDto apiCommentDto = new ApiCommentDto(
                    apiComment.getId(),
                    apiComment.getParent(),
                    apiComment.getPublishApi(),
                    apiComment.getType(),
                    apiComment.getScore(),
                    apiComment.getContent(),
                    apiComment.getCreateTime(),
                    apiComment.getCreator(),
                    apiComment.getChildIds(),
                    apiComment.getLikeNum());
            apiCommentDtoList.add(apiCommentDto);
        }

        buildNestedComments(apiCommentDtoList);

        log.info("{}Query success with comment {}", TAG, apiCommentDtoList.size());
        result.setCode(Result.OK);
        result.setMsg(String.format("查询到%d评论", apiCommentDtoList.size()));
        result.setData(new PageImpl<>(apiCommentDtoList, pageable, apiComments.getTotalElements()));
        return result;
    }

    @Override
    public Result<Boolean> addLikeNum(Integer id) {
        Result<Boolean> result = new Result<>(Result.FAIL, "错误", false);
        ApiComment apiComment = apiCommentRepository.findOne(id);
        if (apiComment == null) {
            result.setMsg("错误, 指定的评论不存在");
            return result;
        }

        int newNum = apiComment.getLikeNum() != null ? (apiComment.getLikeNum() + 1) : 1;
        log.info("{}Success to add one comment[{}] {}", TAG, id, newNum);
        apiCommentRepository.increaseLikeNum(id, newNum);

        result.setMsg("点赞成功");
        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    private void buildNestedComments(Collection<ApiCommentDto> commentDtos) {
        for (ApiCommentDto apiCommentDto : commentDtos) {
            if (MiscUtil.isNotEmpty(apiCommentDto.getChildIds())) {
                List<ApiCommentDto> childComments = apiCommentDto.getChildComments();
                if (childComments == null) {
                    childComments = new ArrayList<>();
                    apiCommentDto.setChildComments(childComments);
                }

                List<ApiCommentDto> apiCommentDtoList =
                        apiCommentRepository.getCommentsByIds(apiCommentDto.getChildIds());
                if (MiscUtil.isNotEmpty(apiCommentDtoList)) {
                    childComments.addAll(apiCommentDtoList);
                }

                if (MiscUtil.isNotEmpty(childComments)) {
                    buildNestedComments(childComments);
                }
            }
        }
    }
}
