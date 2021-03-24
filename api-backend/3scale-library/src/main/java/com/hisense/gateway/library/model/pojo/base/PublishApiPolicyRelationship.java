/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/24 @author peiyun
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

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publish_api_policy_relationship")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class PublishApiPolicyRelationship implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer publishApiId;
    private Integer publishPolicyId;
    private Integer instanceId;
    private Long scalePolicyId;
    private Long metricId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",timezone = "GMT+8")
    private Date createTime;

    public PublishApiPolicyRelationship(Integer publishApiId, Integer publishPolicyId, Long scalePolicyId) {
        this.publishApiId = publishApiId;
        this.publishPolicyId = publishPolicyId;
        this.scalePolicyId = scalePolicyId;
    }

    public PublishApiPolicyRelationship(Integer publishApiId, Integer publishPolicyId, Integer instanceId,
                                        Long scalePolicyId) {
        this.publishApiId = publishApiId;
        this.publishPolicyId = publishPolicyId;
        this.instanceId = instanceId;
        this.scalePolicyId = scalePolicyId;
    }
}
