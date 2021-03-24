package com.hisense.gateway.library.model.dto.web;

import com.hisense.gateway.library.model.pojo.base.SapBaseInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @Author: huangchen.ex
 * @Date: 2020/12/4 16:20
 */
@Data
public class SapBaseInfoDto extends SapBaseInfo {
    /**
     * sap分组
     */
    private String groupName;
    /**
     * sap分组Id
     */
    private Integer groupId;

    public SapBaseInfo toSapBaseInfo(){
        SapBaseInfo sapBaseInfo=new SapBaseInfo();
        BeanUtils.copyProperties(this,sapBaseInfo);
        return sapBaseInfo;
    }

    public void toSapBaseInfoDto(SapBaseInfo sapBaseInfo) {
        BeanUtils.copyProperties(sapBaseInfo, this);
    }
}
