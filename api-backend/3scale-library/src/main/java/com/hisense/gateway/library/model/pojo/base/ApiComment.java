/*
 * @author guilai.ming
 * @date 2020/8/4
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

/**
 * Api评论表
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_comment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiComment implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 回复对应的评论ID
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "parent_id", nullable = true)
    private ApiComment parent;

    /**
     * 评论所属的API
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "api_id", nullable = false)
    private PublishApi publishApi;

    /**
     * 评论创建者
     */
    private String creator;

    /**
     * 评论类型
     * 1-评论
     * 2-回复
     * {@link com.hisense.gateway.library.constant.ApiConstant.ApiCommentType}
     */
    private Integer type;

    /**
     * 评论分数
     */
    private Integer score;

    /**
     * 评论内容
     */
    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String content;

    /**
     * 评论创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    /**
     * 多级嵌套时,存储子节点ID
     */
    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String childIds;

    /**
     * 0-删除,1-创建成功
     */
    private Integer status;

    @Column(columnDefinition = "number(10) default 0")
    //@Column(columnDefinition = "int default 0")
    private Integer likeNum = 0;// 点赞数
}
