package com.hisense.gateway.library.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.GlobalSearchApiInfo;
import com.hisense.gateway.library.repository.DataItemRepository;
import com.hisense.gateway.library.repository.MappingRuleRepository;
import com.hisense.gateway.library.service.GlobalSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 2020/10/20 guilai.ming
 */
@Slf4j
@Service
public class GlobalSearchServiceImpl implements GlobalSearchService {
    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    MappingRuleRepository mappingRuleRepository;

    @Override
    public Result<GlobalSearchApiInfo> findApiInfoByEndpointUrl(String environment, String path) {
        if (MiscUtil.isEmpty(path)) {
            log.error("invalid path");
            return new Result<>(Result.FAIL, "URL前缀/MappingRule不能为空", null);
        }

        List<GlobalSearchApiInfo> apiInfos =
                mappingRuleRepository.findApiInfoByUrlRulePath(InstanceEnvironment.fromCode(environment).getCode(),
                        path);

        GlobalSearchApiInfo apiInfo = MiscUtil.isNotEmpty(apiInfos) ? apiInfos.get(0) : null;
        if (apiInfo != null) {
            apiInfo.setSystemName(dataItemRepository.findOne(apiInfo.getSystemId()).getItemKey());
        }

        return new Result<>(Result.OK, "", apiInfo);
    }
}
