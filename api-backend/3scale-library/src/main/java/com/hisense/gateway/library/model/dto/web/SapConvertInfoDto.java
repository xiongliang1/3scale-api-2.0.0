package com.hisense.gateway.library.model.dto.web;

import com.hisense.gateway.library.model.pojo.base.SapConvertInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;

/**
 * @Author: huangchen.ex
 * @Date: 2020/12/4 16:20
 */
@Data
public class SapConvertInfoDto extends SapConvertInfo {
    /**
     * 生产与测试版本相同  true 相同 false 不同
     */
    private Boolean versionEquals;
    /**
     * 生成的apiName
     */
    private String apiName;
    /**
     * project
     */
    private String project;
    /**
     * 生成的functionName
     */
    private String functionName;
    /**
     * 生成的api url dev
     */
    private String devApiUrl;
    /**
     * 生成的api url prd
     */
    private String prdApiUrl;
    /**
     * 创建环境 staging  production
     */
    private String env;

    /**
     * sap分组
     */
    private String groupName;
    /**
     * sap分组Id
     */
    private Integer groupId;
    /**
     * 是否发布测试 0:未发布 1发布
     */
    private String devFlag;

    /**
     * 是否发布生产 0:未发布 1发布
     */
    private String prdFlag;

    public SapConvertInfo toSapConvertInfo(){
        SapConvertInfo sapConvertInfo=new SapConvertInfo();
        BeanUtils.copyProperties(this,sapConvertInfo);
        return sapConvertInfo;
    }

    public void toSapConvertInfoDto(SapConvertInfo sapConvertInfo) {
        if(StringUtils.equalsIgnoreCase(sapConvertInfo.getDevVersion(),sapConvertInfo.getPrdVersion())){
            this.versionEquals=true;
        }else {
            this.versionEquals=false;
        }
        BeanUtils.copyProperties(sapConvertInfo, this);
    }
}
