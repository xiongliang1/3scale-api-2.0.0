package com.hisense.gateway.management.beans;

import com.hisense.gateway.library.beans.CommonBean;
import org.springframework.stereotype.Component;

@Component
public class CommonBeanService implements CommonBean {
    @Override
    public String getLoginUserName() {
//        return SecurityUtils.getLoginUser().getLoginName();
        return null;
    }
}
