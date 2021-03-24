package com.hisense.gateway.library.service.impl;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.eureka.EurekaPullConfig;
import com.hisense.gateway.library.model.base.portal.PublishApiVO;
import com.hisense.gateway.library.model.pojo.base.EurekaPullApi;
import com.hisense.gateway.library.repository.EurekaPullApiRepository;
import com.hisense.gateway.library.service.EurekaPullApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EurekaPullApiServiceIml implements EurekaPullApiService {

    @Autowired
    EurekaPullApiRepository eurekaPullApiRepository;

    @Override
    public void createEurekaConfig(EurekaPullConfig config) {
        EurekaPullApi eurekaPullApi = null;
        if (config !=null){
             eurekaPullApi = new EurekaPullApi(config.getGroupId(),config.getEurekaUrl(),
                            config.isScheduleEnable(),new Date(),new Date());
        }
        eurekaPullApiRepository.save(eurekaPullApi);
    }

    @Override
    public Result<EurekaPullApi> findEurekaConfig() {
        Result<EurekaPullApi> result = new Result<>(Result.FAIL,null,null);
        List<EurekaPullApi> eurekaPullApis = eurekaPullApiRepository.findByTime();
        if(CollectionUtils.isEmpty(eurekaPullApis)){
            result.setMsg("上次未配置信息");
        }
        result.setData(eurekaPullApis.get(0));
        result.setCode(Result.OK);
        return result;
    }
}
