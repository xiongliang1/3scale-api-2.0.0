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
 * 告警策略的具体内容
 * <p>
 * 告警策略 -- API 之间的关系约定为 1对多
 *
 * @author guilai.ming 2020/09/10
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert_policy")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertPolicy implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 告警策略名称
     */
    private String name;

    /**
     * 告警触发方式,最多支持两种
     */
    @Column(columnDefinition = "varchar(1024) default null")
    private String triggerMethods;

    /**
     * 告警消息的接收人
     */
    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String msgReceivers;

    /**
     * 告警消息发送间隔
     */
    private Integer msgSendInterval;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;

    /**
     * 告警策略绑定的API
     */
    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String apiIds;

    /**
     * 策略状态：0-删除,1-创建
     */
    //@Column(columnDefinition = "tinyint(1) default 1")
    @Column(columnDefinition = "number(1) default 1")
    private Integer status;

    /**
     * 策略创建者
     */
    private String creator;

    /**
     * 策略是否已开启（生效） or 关闭(不生效)
     */
    //@Column(columnDefinition = "tinyint(1) default 1")
    @Column(columnDefinition = "number(1) default 1")
    private Integer enable;

    /**
     * 策略对应的project
     */
    private String projectId;

    /**
     * 对应的环境
     */
    private Integer environment;

    /**
     * 告警方式
     */
    private String msgSendTypes;
}
