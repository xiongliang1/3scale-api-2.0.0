package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author huangchen 2020/12/18
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sap_service_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SapServiceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;
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
     * SAP系统端口
     */
    @Column(name = "client")
    public String client;
    /**
     * SAP系统标识
     */
    @Column(name = "sap_tag")
    public String sapTag;


}
