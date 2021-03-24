/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2020/2/19
 */
package com.hisense.gateway.library.model.dto.buz;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessDataDto {
    private static final long serialVersionUID = 5105223900137785597L;

    Integer groupId;

    Integer apiSystem;

    Integer appSystem;

    String url;

    String isCompatible;

    List<String> clusterPartitions;

    public boolean isValid(){
        return apiSystem!=null;
    }
}
