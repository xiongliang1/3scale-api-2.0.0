package com.hisense.gateway.management.web.interceptor;

import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.utils.RequestUtils;
import com.hisense.gateway.management.config.Properties;
import com.hisense.gateway.library.exception.UnAuthorized;
import com.hisense.gateway.library.model.crypto.Claims;
import com.hisense.gateway.library.model.crypto.JWT;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.library.repository.InstanceRepository;
import com.hisense.gateway.library.repository.UserInstanceRelationshipRepository;
import com.hisense.gateway.library.utils.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
public class BearerTokenValidator extends HandlerInterceptorAdapter {
    private final Properties properties;
    //private final RestTemplate restTemplate;
    private final UserInstanceRelationshipRepository userInstanceRelationshipRepository;
    private final InstanceRepository instanceRepository;

    public BearerTokenValidator(Properties properties,
                                UserInstanceRelationshipRepository userInstanceRelationshipRepository,
                                InstanceRepository instanceRepository) {
        this.properties = properties;
        this.userInstanceRelationshipRepository = userInstanceRelationshipRepository;
        this.instanceRepository = instanceRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String forTest;
        LoginInfo info = null;
        Namespace namespace = null;
        log.info("System.getProperty(local.debug)={}", System.getProperty("local.debug"));
        if (true/*CommonUtil.isNotEmpty(forTest=System.getProperty("local.debug"))&&forTest.equals("true")*/) {
//            LoginUser metaManager = SecurityUtils.getLoginUser();
          /*  if (metaManager != null) {
                info = new LoginInfo();
                info.setId(Math.toIntExact(metaManager.getUserId()));
                info.setUsername(metaManager.getUsername());
                namespace = parseNamespace(request, info);
                String url = request.getRequestURI();
                if (url.contains("/tenant/") && url.contains("/project")) {
                    String tenantId = url.substring(url.indexOf("tenant/") + 7, url.indexOf("/project"));
                    info.setTenantId(tenantId);
                }
                info.setUserId(Math.toIntExact(metaManager.getUserId()));
            }*/
        } else {
            info = validateToken(request);
            namespace = parseNamespace(request, info);
            String url = request.getRequestURI();
            String tenantId = url.substring(url.indexOf("tenant/") + 7, url.indexOf("/clusters"));
            String clusterId = url.substring(url.indexOf("clusters/") + 9, url.indexOf("/project"));
            info.setTenantId(tenantId);
            info.setClusterId(clusterId);
            //syncUser(info);
        }
        request.setAttribute(GatewayConstants.LOGIN_INFO, info);
        request.setAttribute(GatewayConstants.NAMESPACE, namespace);
        return true;
    }

    /*private UserInfo getUserInfo(LoginInfo info) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("username", info.getUsername());
        headers.add("Authorization", String.format("token %s", info.getToken()));
        HttpEntity<Void> request = new HttpEntity<>(null, headers);
        String url = String.format("%s/api/v2/users/%d", properties.getPaasApi(), info.getUserId());
        ResponseEntity<UserInfoResponse> response = restTemplate.exchange(url, HttpMethod.GET, request,
                UserInfoResponse.class);
        return response.getBody().getData();
    }*/

    private void validateProject(HttpServletRequest request, Namespace space, LoginInfo info) {
        if (Namespace.Type.PROJECT.equals(space.getType())) {
            boolean validate = false;
            RestTemplate restTemplate = SpringBeanUtil.getBean(RestTemplate.class);
            Properties properties = SpringBeanUtil.getBean(Properties.class);
            String paasApi = properties.getPaasApi();
            if (System.getenv("PAAS_API") != null && !System.getenv("PAAS_API").equals("")) {
                paasApi = System.getenv("PAAS_API");
            }

            String url = String.format("%s/api/v2/users/%s/projects?", paasApi, info.getUserId());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", request.getHeader("Authorization"));
            HttpEntity<Void> req = new HttpEntity<>(null, headers);
            ResponseEntity<ProjectInfoBody> response = restTemplate.exchange(url, HttpMethod.GET, req,
                    ProjectInfoBody.class);
            List<ProjectInfo> pis = response.getBody().data;
            if (pis == null || pis.isEmpty()) {
                throw new UnAuthorized("cannot access the project: " + space.getNamespace());
            }

            for (ProjectInfo pi : pis) {
                if (pi.getNamespace().equals(space.getNamespace())) {
                    validate = true;
                    break;
                }
            }

            if (!validate) {
                log.error("cannot access the project: " + space.getNamespace());
                throw new UnAuthorized("cannot access the project: " + space.getNamespace());
            }
        }
    }

    private LoginInfo validateToken(HttpServletRequest request) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || token.equalsIgnoreCase("") || token.length() <= "Bearer ".length()) {
            throw new UnAuthorized("no token specified or in unknown format");
        }

        String[] parts = token.split(" ");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("Bearer")) {
            throw new UnAuthorized("not bearer token");
        }

        Claims claims = JWT.validate(parts[1]);
        if (claims.expired()) {
            throw new UnAuthorized("token expired");
        }
        return LoginInfo.parse(claims.getEncrypted());
    }

    private Namespace parseNamespace(HttpServletRequest request, LoginInfo info) {
        Headers headers = new Headers(request);
        return Namespace.parse(headers, info);
    }

    private final class Headers implements Namespace.Headers {
        private final HttpServletRequest request;

        private Headers(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        public String getHeader(String key) {
            return request.getHeader(key);
        }
    }
}
