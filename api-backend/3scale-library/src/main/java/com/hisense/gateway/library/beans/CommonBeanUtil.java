package com.hisense.gateway.library.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonBeanUtil {
    public static CommonBean commonBean;

    @Autowired
    public void setCommonBean(CommonBean commonBean) {
        CommonBeanUtil.commonBean = commonBean;
    }

    public static String getLoginUserName() {
        return commonBean.getLoginUserName();
    }
}
