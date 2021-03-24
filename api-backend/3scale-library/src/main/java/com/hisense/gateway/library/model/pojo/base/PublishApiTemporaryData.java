/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author wangjinshan
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.*;
import com.hisense.gateway.library.model.dto.buz.ApiInstanceDto;
import com.hisense.gateway.library.stud.model.Proxy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publish_api_temp_data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApiTemporaryData implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")//修改API時候的临时数据
    private String tempData;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "api_id", nullable = false)
    private PublishApi publishApi;

    private Integer status;//状态（0:删除，1：启用）

    private String creator;//创建人

    private String updator;//修改人

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;//创建时间

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;//修改时间
}
