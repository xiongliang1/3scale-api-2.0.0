package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.PolicieConfig;
import com.hisense.gateway.library.stud.model.PolicieConfigDto;
import com.hisense.gateway.library.stud.model.Proxy;
import com.hisense.gateway.library.stud.model.ProxyDto;

import java.util.List;

public interface ProxyPoliciesStud {
    PolicieConfigDto updateAnonymousProxyPolicies(String host, String accessToken, String serviceId, String appId,
                                                  String appKey, String userKey);

    ProxyDto readProxy(String host, String accessToken, String serviceId);

    ProxyDto editProxy(String host, String accessToken, String serviceId, String apiBackend, Proxy proxy, String endpoint,String sandboxEndpoint,String secretToken);

    PolicieConfigDto proxyPoliciesChainShow(String host, String accessToken, String serviceId);

    PolicieConfigDto offAnonymousProxyPolicies(String host, String accessToken, String serviceId,
                                               List<PolicieConfig> policieConfigs);

    PolicieConfigDto updateUrlRewritingProxyPolicies(String host, String accessToken, String serviceId, String url,String httpMethod);

    PolicieConfigDto updateBatcherProxyPolicies(String host, String accessToken, String serviceId);

    PolicieConfigDto changeUrlRewritingUrl(String host, String accessToken, String serviceId, String url,String httpMethod);

    PolicieConfigDto configHisenseLog(String host, String accessToken, String serviceId, boolean enable, boolean verbose, Integer apiId);

    PolicieConfigDto updateAuthCachingPolicies(String host, String accessToken, String serviceId);

    PolicieConfigDto openEdgeLimiting(String host, String accessToken, String serviceId,Long window,Integer count);

    PolicieConfigDto closeEdgeLimiting(String host, String accessToken, String serviceId);
}
