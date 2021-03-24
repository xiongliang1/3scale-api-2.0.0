/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.library.utils.meta;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.model.dto.web.LoginInfo;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.stud.model.Proxy;
import com.hisense.gateway.library.beans.CommonBeanUtil;

public class GlobalSettings {
    public static final String ACCESS_PROTOCOL_HTTP = "http";
    public static final String ACCESS_PROTOCOL_HTTPS = "https";
    public static final String HOST_SUFFIX = ".svc.cluster.local";

    public static final String HISENSE_OCP_DEV_BACK_END = "servcie-demo.hisense-apigateway-test:19083";

    private static final String DEFAULT_PROXY = "{\n" +
            "    \"errorStatusAuthFailed\": \"403\",\n" +
            "    \"errorHeadersAuthFailed\": \"text/plain; charset=us-ascii\",\n" +
            "    \"errorAuthFailed\": \"Authentication parameters\",\n" +
            "    \"errorStatusAuthMissing\": \"403\",\n" +
            "    \"errorHeadersAuthMissing\": \"text/plain; charset=us-ascii\",\n" +
            "    \"errorAuthMissing\": \"Authentication parameters\",\n" +
            "    \"errorStatusNoMatch\": \"404\",\n" +
            "    \"errorHeadersNoMatch\": \"text/plain; charset=us-ascii\",\n" +
            "    \"errorNoMatch\": \"No Mapping Rule\",\n" +
            "    \"errorStatusLimitsExceeded\": \"429\",\n" +
            "    \"errorHeadersLimitsExceeded\": \"text/plain; charset=us-ascii\",\n" +
            "    \"errorLimitsExceeded\": \"Usage limit\"\n" +
            "  }";

    private static final LoginInfo metaDataManager = new LoginInfo();

    static {
        metaDataManager.setId(2);
        metaDataManager.setUsername("hicloud");
    }

    /**
     * 主被动同步API时, MetaData存储的API,其创建者均固定为同一个用户
     * 当前设定为User{id=2,name=hicloud}
     *
     * @return
     */
    public static LoginInfo getMetaDataManager() {
        return metaDataManager;
    }

    /**
     * 返回当前HTTP请求调用者, 须确保运行在ServletContext所在线程中
     *
     * @return
     */
    public static LoginInfo getVisitor() {
        LoginInfo user = new LoginInfo();
        //user.setId(Math.toIntExact(CommonBeanUtil.getLoginUserId()));
        user.setUsername(CommonBeanUtil.getLoginUserName());
        return user;
    }

    /*public static boolean isMetaManager(int userId) {
        //return CommonBeanUtil.getLoginUserId() == userId;
        *//*LoginUser manager = SecurityUtils.getLoginUser();
        if (manager != null) {
            return CommonBeanUtil.getLoginUserId() == userId;
        } else {
            return false;
        }*//*
    }*/

    public static int getDefaultPartition() {
        return 0;
    }

    public static String getDefaultEnvironment() {
        return SystemNameConstant.TEST_ENV;
    }

    public static Proxy getDefaultProxy() {
        return MiscUtil.fromJson(DEFAULT_PROXY, Proxy.class);
    }

    public static String getDefaultMappingPattern() {
        return "/";
    }

    public static String getDefaultTenant() {
        return "hicloud_default_tenant";
    }

    public static String getDefaultProject() {
        return "hicloud_default_project";
    }

    public static String getDefaultAuthType() {
        return "noauth";
    }

    public static String isApiPromoteCompatible() {
        return "yes";
    }

    // http
    public static String getDefaultTargetType() {
        return "2";
    }

    public static String getDefaultAccessProtocol() {
        return "http";
    }

    public static String getTargetType(String accessProtocol) {
        return MiscUtil.isEmpty(accessProtocol) ? "http" : accessProtocol.equals("http") ? "2" : "1";
    }

    public static String wrapperHostPort(PublishApiDto publishApiDto) {
        return wrapperHostPort(publishApiDto.getAccessProtocol(), publishApiDto.getHost(), publishApiDto.getPort(),
                null);
    }

    public static String wrapperHostPort(String accessProtocol, String host, String port) {
        return wrapperHostPort(accessProtocol, host, port, null);
    }

    public static String wrapperHostPort(String accessProtocol, String host, String port, String suffix) {
        if (MiscUtil.isEmpty(accessProtocol) ||
                (!accessProtocol.equals(ACCESS_PROTOCOL_HTTPS) && !accessProtocol.equals(ACCESS_PROTOCOL_HTTP)) ||
                MiscUtil.isEmpty(host)) {
            throw new NotExist(String.format("Invalid accessProtocol=%s or host=%s", accessProtocol, host));
        }

        StringBuilder hostPort = new StringBuilder();
        if (host.startsWith(accessProtocol + "://")) {
            hostPort.append(host);
        } else {
            hostPort.append(accessProtocol).append("://");
            hostPort.append(host);
        }

        if (MiscUtil.isNotEmpty(suffix)) {
            hostPort.append(suffix);
        }

        if (MiscUtil.isNotEmpty(port) && hostPort.indexOf(":")==-1) {
            hostPort.append(":").append(port);
        }

        return hostPort.toString();
    }

    public static boolean isDefaultCredentialsWithUserKey() {
        return SystemNameConstant.BACKEND_VERSION.equals("1");
    }
}
