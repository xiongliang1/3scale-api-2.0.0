package com.hisense.gateway.developer.web.interceptor;

import com.hisense.gateway.developer.config.Properties;
import com.hisense.gateway.developer.constant.Audience;
import com.hisense.gateway.library.exception.UnAuthorized;
import com.hisense.gateway.library.model.dto.portal.UserDto;
import com.hisense.gateway.developer.service.LdapHisenseService;
import com.hisense.gateway.developer.service.UserService;
import com.hisense.gateway.library.utils.IpUtils;
import com.hisense.gateway.developer.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SsoCookieValidator extends HandlerInterceptorAdapter {

    private static final String COOKIE_NAME = "ssoLoginToken";

    /**
     * properties
     */
    private final Properties properties;
    /**
     * restTemplate
     */
    private final RestTemplate restTemplate;
    /**
     * ldapHisenseService
     */
    private final LdapHisenseService ldapHisenseService;
    /**
     * userService
     */
    private final UserService userService;

    @Autowired
    private Audience audience;

    public SsoCookieValidator(
            Properties properties,
            RestTemplate restTemplate,
            LdapHisenseService ldapHisenseService,
            UserService userService) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.ldapHisenseService = ldapHisenseService;
        this.userService = userService;
    }

    Logger log = LoggerFactory.getLogger(SsoCookieValidator.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ipAddress= IpUtils.getRealIP(request);
        log.info("USER IP ADDRESS IS =>"+ipAddress);
//        if(IpsConstant.localhost.equals(ipAddress) || IpsConstant.local.equals(ipAddress)){
//            return true;
//        }
        // 获取请求头信息authorization信息
        final String authHeader = request.getHeader(JwtTokenUtil.AUTH_HEADER_KEY);
        log.info("## authHeader= {}", authHeader);

        // wangrang.ex 20200901/17/13
//        return true;
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
            throw new UnAuthorized("not bearer token");
        }
        // 获取token
        final String token = authHeader.substring(7);

        if(audience == null){
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            audience = (Audience) factory.getBean("audience");
        }

        // 验证token是否有效--无效已做异常抛出，由全局异常处理后返回对应信息
        Claims chaims = JwtTokenUtil.parseJWT(token, audience.getBase64Secret(),audience.getExpiresSecond());
        String uid = String.valueOf(chaims.get("uid"));
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String domain = (String)pathVariables.get("domain");
        syncUser(uid,domain);
        UserNameHolder.init(uid);
        return true;
    }

    private String validateAccessToken(HttpServletRequest request) {
        String accessToken = request.getHeader("access_token");
        if (accessToken == null || accessToken.equalsIgnoreCase("")) {
            return null;
        }
        log.info("accessToken:"+accessToken);
        return accessToken;
    }

    private String validateCookieSsoLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            //可能放到header
            String ssoLoginToken = request.getHeader("ssoLoginToken");
            if (ssoLoginToken == null || ssoLoginToken.equalsIgnoreCase("")) {
                return null;
            }
            log.info("ssoLoginToken:"+ssoLoginToken);
            return ssoLoginToken;
        }
        List<Cookie> matchCookies = Arrays.stream(cookies).filter((c) ->
                COOKIE_NAME.equalsIgnoreCase(c.getName().trim())).collect(Collectors.toList());
        if (matchCookies.size() < 1) {
            return null;
        }
        Cookie cookie = matchCookies.get(0);
        if (cookie == null) {
            if (log.isDebugEnabled()) {
                log.info("can not get cookie named:{}", COOKIE_NAME);
            }
            throw new UnAuthorized("no cookie");
        }
        return cookie.getValue();
    }

    public void syncUser(String uid,String domainName) {
        //查数据用户，然后注册
        UserDto userDto = new UserDto();
        userDto.setDescription("for hisense");
        if(StringUtils.isNotBlank(uid)){
            userDto.setEmail(uid+"@hisense.com");
        } else {
            userDto.setEmail("hisensedemo@hisense.com");
        }
        userDto.setName(uid);
        userDto.setOrgName("hisense");
        userDto.setPhone("13112341234");
        userDto.setPwd("123456");
        userService.signUp(userDto);
    }

}
