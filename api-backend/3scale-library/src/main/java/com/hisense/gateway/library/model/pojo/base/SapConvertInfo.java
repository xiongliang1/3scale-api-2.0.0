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

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

/**
 * @author huangchen 2020/12/04
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sap_convert_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SapConvertInfo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    /**
     *  环境
     */
    @Column(name = "environment")
    public String environment;
    /**
     * 系统id
     */
    @Column(name = "project_id")
    public String projectId;
    /**
     * 测试sap基本信息id
     */
    @Column(name = "dev_base_id")
    public Integer devBaseId;
    /**
     * 生成api的id 测试
     */
    @Column(name = "dev_api_id")
    public Integer devApiId;

    /**
     * 测试环境版本 第一次发布是1.0 以后是 2.0 3.0
     */
    @Column(name = "dev_version")
    public String devVersion;
    /**
     * 测试环境版本创建时间
     */
    @Column(name = "dev_create_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    public Date devCreateDate;
    /**
     * 生产sap基本信息id
     */
    @Column(name = "prd_base_id")
    public Integer prdBaseId;
    /**
     * 生成api的id 生产
     */
    @Column(name = "prd_api_id")
    public Integer prdApiId;
    /**
     * 测试环境版本创建时间
     */
    @Column(name = "prd_version")
    public String prdVersion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    /**
     * 生产环境发布时间
     */
    @Column(name = "prd_create_date")
    public Date prdCreateDate;
}
