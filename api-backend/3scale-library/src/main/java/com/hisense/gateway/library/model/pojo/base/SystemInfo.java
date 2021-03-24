package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * @author guilai.ming 2020/09/10
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    String name; // 系统名称
    String code;
    String slmid;
    boolean isDevApiCreated;// 管理端申请创建测试系统时,置为true
    boolean isPrdApiCreated;// 管理端申请创建生产系统时,置为true
    boolean isDevMsgCreated;
    boolean isPrdMsgCreated;//

    String dataItemId;// dataItem表中,本系统对应的ID
    String apiAdminName;//测试环境责任人
    String devApiDevName; //测试环境开发人员
    String devApiTenantName; //测试环境租户管理员
    String msgAdminName;

    String prdApiAdminName;//生产环境责任人
    String prdApiDevName;// 生产环境开发人员
    String prdApiTenantName;//生产环境租户管理员
    String prdMsgAdminName;
    String devMsgDevName;
    String prdMsgDevName;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    Date createTime; // 系统创建时间

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    Date updateTime; // 系统更新时间

    String creator;
}
