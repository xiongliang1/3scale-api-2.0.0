/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author zhangjian
 */

package com.hisense.gateway.library.model.dto.buz;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SystemUserDto {
    // 系统ID
    private Integer id;
    private String systemName;
    private String slmId;
    //是否开发人员，true：是，false：否
    private boolean isDeveloper;

}
