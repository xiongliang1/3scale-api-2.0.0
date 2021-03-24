/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/24 @author peiyun
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publish_api_instance_relationship")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class PublishApiInstanceRelationship implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    Integer apiId;
    Integer instanceId;

    private Long scaleApiId;

    public PublishApiInstanceRelationship(Integer apiId, Integer instanceId, Long scaleApiId) {
        this.apiId = apiId;
        this.instanceId = instanceId;
        this.scaleApiId = scaleApiId;
    }
}
