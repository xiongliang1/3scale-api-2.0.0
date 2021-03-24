/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author peiyun
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@Table(name = "data_item")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataItem implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(columnDefinition = "varchar(32) default null")
    private String groupKey;// 字典组key

    @Column(columnDefinition = "varchar(32) default null")
    private String groupName;// 字典组名称

    @Column(columnDefinition = "varchar(32) default null")
    private String itemKey;// 字典key

    @Column(columnDefinition = "varchar(32) default null")
    private String itemName;// 字典名称

    private Integer parentId; // 父字典ID

    private Integer status = 1;// 状态:0 废弃,1有效
    private Integer createId;// remove for later
    private String creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;

    @Transient
    private List<DataItem> dataItemList;

    public DataItem(String groupKey, String groupName, String itemKey, String itemName, Integer parentId,
                    String creator, Date createTime, Date updateTime) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.itemKey = itemKey;
        this.itemName = itemName;
        this.parentId = parentId;
        this.creator = creator;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
