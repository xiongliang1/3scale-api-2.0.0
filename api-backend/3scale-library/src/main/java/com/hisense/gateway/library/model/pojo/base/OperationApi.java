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

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "operation_api")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationApi implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String creator;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;
    private Integer type; //0-创建 1-发布 2-修改 3-删除 4-下线 5-取消订阅 6- 一键发生产
    @Column(columnDefinition = "varchar(1024) default null")
    private String msg;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "api_id", nullable = false)
    private PublishApi publishApi;

    public OperationApi(String name, String creator, Date createTime, Date updateTime, Integer type, String msg, PublishApi publishApi) {
        this.name = name;
        this.creator = creator;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.type = type;
        this.msg = msg;
        this.publishApi = publishApi;
    }
}
