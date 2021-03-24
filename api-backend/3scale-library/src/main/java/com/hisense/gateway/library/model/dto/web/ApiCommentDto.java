/*
 * @author guilai.ming
 * @date 2020/8/4
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.pojo.base.ApiComment;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;
import static com.hisense.gateway.library.constant.ApiConstant.ApiCommentType;

@Slf4j
@Data
public class ApiCommentDto {
    private Integer id;

    private Integer parentId;// 回复所对应的评论

    private Integer apiId;// api ID

    @JsonIgnore
    private ApiCommentType commentType;// 类型: 1-评论, 2-评论回复

    private Integer score;// 评论分数

    private String content;// 评论内容

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;// 创建时间

    private String creator;// 评论创建者名称

    private List<ApiCommentDto> childComments;// 评论对应的回复

    private List<Integer> childIds;// 多级嵌套时,存储子节点ID

    private Integer likeNum = 0;// 点赞数

    public Integer getType() {
        return commentType != null ? commentType.getType() : null;
    }

    public void setType(Integer type) {
        this.commentType = type != null ? ApiCommentType.from(type) : null;
    }

    @JsonIgnore
    public ApiCommentType getCommentType() {
        return commentType;
    }

    public ApiCommentDto() {
    }

    public ApiCommentDto(Integer id, ApiComment parent, PublishApi api, Integer type, Integer score, String content,
                         Date createTime, String userName, String childIds, Integer likeNum) {
        this.id = id;
        this.parentId = parent != null ? parent.getId() : null;
        this.apiId = api != null ? api.getId() : null;
        this.commentType = ApiCommentType.from(type);
        this.score = score;
        this.content = content;
        this.createTime = createTime;
        this.creator = userName;
        this.likeNum = likeNum != null ? likeNum : 0;

        String[] childIdArray = MiscUtil.splitItems(childIds, ",");
        if (MiscUtil.isNotEmpty(childIdArray)) {
            this.childIds = new ArrayList<>();
            for (String child : childIdArray) {
                this.childIds.add(Integer.valueOf(child));
            }
        }
    }
}
