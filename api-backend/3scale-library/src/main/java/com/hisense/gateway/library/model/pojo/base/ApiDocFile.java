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
import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

/**
 * API DOC 文档表
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_doc_file")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiDocFile {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(2048) default null")
    private String path;// 文件绝对路径

    @Column(name="file_name",columnDefinition = "varchar(4000) default null")
    private String fileName;

    @Column(name = "file_size")
    private Long size;// 文件大小

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    //@Column(name="file_type",columnDefinition = "varchar(16) default null COMMENT 'api文档类型(1：封面图片，2：附件)'")
    @Column(name="file_type",columnDefinition = "varchar(16) default null")
    private String fileType;
    /**
     * 文件所属的API
     */
    private Integer apiId;

    /**
     * 上传者
     */
    @JsonIgnore
    private String creator;

    private Integer status;
}
