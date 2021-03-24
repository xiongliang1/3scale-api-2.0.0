package com.hisense.gateway.developer.beans;

import com.hisense.gateway.developer.constant.Audience;
import com.hisense.gateway.developer.utils.JwtTokenUtil;
import com.hisense.gateway.library.beans.CommonBean;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.exception.UnAuthorized;
import com.hisense.gateway.library.model.crypto.Claims;
import com.hisense.gateway.library.model.crypto.JWT;
import com.hisense.gateway.library.model.dto.web.LoginInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class CommonBeanService implements CommonBean {

    @Autowired
    private Audience audience;
    @Override
    public String getLoginUserName() {
        LoginInfo loginInfo = null;
        try {
            loginInfo = validateToken();
        } catch (Exception e) {
            log.info("Exception {}", e.getMessage());
        }

        return loginInfo == null ? "hisense-api-manager" : loginInfo.getUsername();
    }

    private LoginInfo validateToken(){
        HttpServletRequest httpServletRequest = null;

        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                httpServletRequest = attr.getRequest();
            }
        } catch (Exception e) {
            log.error("getRequestAttributes exception:",e);
        }

        if (httpServletRequest == null) {
            throw new NotExist("validateToken fail invalid HttpServletRequest");
        }

        String token = httpServletRequest.getHeader("Authorization");
        if (token == null || token.equalsIgnoreCase("") || token.length() <= "Bearer ".length()) {
            throw new UnAuthorized("no token specified or in unknown format");
        }

        String[] parts = token.split(" ");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("Bearer")) {
            throw new UnAuthorized("not bearer token");
        }
        audience.setExpiresSecond(2*60*60*1000);
        io.jsonwebtoken.Claims claims = JwtTokenUtil.parseJWT(parts[1], audience.getBase64Secret(),audience.getExpiresSecond());
        String uid = String.valueOf(claims.get("uid"));
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUsername(uid);
        return loginInfo;
    }
}
