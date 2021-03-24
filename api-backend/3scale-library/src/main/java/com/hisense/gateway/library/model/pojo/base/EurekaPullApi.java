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

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eureka_pull_config")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaPullApi implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    private Integer groupId;
    private String eurekaUrl;
    private boolean scheduleEnable=false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;


    public EurekaPullApi(Integer groupId, String eurekaUrl, boolean scheduleEnable, Date createTime, Date updateTime) {
        this.groupId = groupId;
        this.eurekaUrl = eurekaUrl;
        this.scheduleEnable = scheduleEnable;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public EurekaPullApi(Integer groupId, String eurekaUrl, boolean scheduleEnable) {
        this.groupId = groupId;
        this.eurekaUrl = eurekaUrl;
        this.scheduleEnable = scheduleEnable;
    }
}
