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
 * @author huangchen 2020/12/04
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sap_base_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SapBaseInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
    /**
     *  环境
     */
    @Column(name = "environment")
    public String environment;
    /**
     *  环境
     */
    @Column(name = "env")
    public String env;
    /**
     * 系统id
     */
    @Column(name = "project_id")
    public String projectId;
    /**
     * sapConvert id
     */
    @Column(name = "convert_info_id")
    public Integer convertInfoId;
    /**
     * SAP服务器地址
     */
    @Column(name = "ashost")
    public String ashost;
    /**
     * SAP实例编号
     */
    @Column(name = "sysnr")
    public String sysnr;
    /**
     * SAP系统标识
     */
    @Column(name = "client")
    public String client;
    /**
     * SAP用户名
     */
    @Column(name = "sap_user")
    public String user;
    /**
     * SAP密码
     */
    @Column(name = "passwd")
    public String passwd;
    /**
     * SAP RFC函数名
     */
    @Column(name = "function_name")
    public String functionName;
    /**
     * 自动生成代码工程名
     */
    @Column(name = "project")
    public String project;
    /**
     * 自动生成api名称
     */
    @Column(name = "api_name")
    public String apiName;

    /**
     * git地址
     */
    @Column(name = "git_path")
    public String gitPath;
    /**
     * git用户名
     */
    @Column(name = "git_user_name")
    public String gitUserName;
    /**
     * git密码
     */
    @Column(name = "git_pass_word")
    public String gitPassWord;
    /**
     * ocp地址
     */
    @Column(name = "ocp_ip")
    public String ocpIp;
    /**
     * ocp端口
     */
    @Column(name = "ocp_port")
    public String ocpPort;
    /**
     * ocp用户名
     */
    @Column(name = "ocp_user_name")
    public String ocpUserName;
    /**
     * ocp用户密码
     */
    @Column(name = "ocp_pass_word")
    public String ocpPassWord;
    /**
     * ocp工程名
     */
    @Column(name = "ocp_project")
    public String ocpProject;

    /**
     * 创建人
     */
    @Column(name = "create_by")
    public String createBy;

}
