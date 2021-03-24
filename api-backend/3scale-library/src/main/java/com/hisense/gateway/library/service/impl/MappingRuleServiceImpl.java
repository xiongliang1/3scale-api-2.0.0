package com.hisense.gateway.library.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.InstancePartition;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleInfo;
import com.hisense.gateway.library.model.pojo.base.ApiMappingRule;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.repository.MappingRuleRepository;
import com.hisense.gateway.library.service.MappingRuleService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.model.ModelConstant.API_DELETE;
import static com.hisense.gateway.library.model.Result.FAIL;
import static com.hisense.gateway.library.model.Result.OK;

@Slf4j
@Service
public class MappingRuleServiceImpl implements MappingRuleService {
    @Autowired
    MappingRuleRepository mappingRuleRepository;

    @Override
    public List<ApiMappingRule> findRuleByApiId(Integer apiId) {
        return mappingRuleRepository.findRuleByApiId(apiId);
    }

    @Override
    public List<ApiMappingRuleInfo> findRuleByApi(Integer apiId,String rule) {
        List<ApiMappingRuleInfo> infos = new ArrayList<>();
        List<ApiMappingRule> mappingRuleInfos = findRuleByApiId(apiId);
        System.out.println(mappingRuleInfos);
        for (ApiMappingRule info : mappingRuleInfos ){
            //判断是否类似
            Boolean status = rule.contains(info.getPattern());
            if(status){
                ApiMappingRuleInfo mappingRuleInfo = new ApiMappingRuleInfo();
                mappingRuleInfo.setHttpMethod(info.getHttpMethod());
                mappingRuleInfo.setPattern(info.getPattern());
                infos.add(mappingRuleInfo);
            }else{
                if(info.getPattern().contains(rule) || rule.contains(info.getPattern()) ){
                    ApiMappingRuleInfo mappingRuleInfo = new ApiMappingRuleInfo();
                    mappingRuleInfo.setHttpMethod(info.getHttpMethod());
                    mappingRuleInfo.setPattern(info.getPattern());
                    infos.add(mappingRuleInfo);
                }
            }

        }
        return infos;
    }

    @Transactional
    @Override
    public Result<List<Integer>> saveRules(@NonNull List<ApiMappingRule> rules, @NonNull Integer apiId) {
        for (ApiMappingRule rule : rules) {
            PublishApi publishApi = new PublishApi();
            publishApi.setId(apiId);
            rule.setPublishApi(publishApi);
        }
        return new Result<>(OK, "", saveRules(rules));
    }

    @Transactional
    @Override
    public Result<List<Integer>> updateRules(List<ApiMappingRule> rules, Integer apiId) {
        Result<List<Integer>> result = new Result<>(FAIL, "", null);
        /*if (rules.stream().map(ApiMappingRule::getId).collect(Collectors.toSet()).size() < rules.size()) {
            result.setMsg("MappingRule id 不能为空");
            return null;
        }*/

        // 物理删除
        mappingRuleRepository.deleteByApiId(apiId);

        // 新增
        List<Integer> resultIds = new ArrayList<>();
        for (ApiMappingRule rule : rules) {
            if (rule.getPublishApi() == null) {
                PublishApi publishApi = new PublishApi();
                publishApi.setId(apiId);
                rule.setPublishApi(publishApi);
            }

            ApiMappingRule ruleRes = mappingRuleRepository.saveAndFlush(rule);
            resultIds.add(ruleRes.getId());
        }

        result.setCode(OK);
        result.setData(resultIds);
        return result;
    }

    public Result<List<PublishApi>> existApisWithSameMappingRule(List<ApiMappingRule> apiMappingRules,Integer envCode) {
        Result<List<PublishApi>> result = new Result<>(FAIL, "", null);
        StringBuilder sb = new StringBuilder();
        Set<PublishApi> publishApis = new HashSet<>();
        for (ApiMappingRule rule : apiMappingRules) {
            List<ApiMappingRule> ruleRes =
                    mappingRuleRepository.findByPartitionMethodPattern(
                            rule.getPartition(),
                            rule.getHttpMethod(),
                            rule.getPattern(),envCode);

            if (MiscUtil.isNotEmpty(ruleRes)) {
                sb.append("路由规则(")
                        .append(InstancePartition.fromCode(rule.getPartition())).append("-")
                        .append(rule.getHttpMethod()).append("-")
                        .append(rule.getPattern())
                        .append(")已存在, 对应的API是(");

                Set<PublishApi> apiRes = ruleRes.stream().map(ApiMappingRule::getPublishApi).collect(Collectors.toSet());
                for (PublishApi api : apiRes) {
                    sb.append("名称:").append(api.getName());
                    if (api.getGroup() != null) {
                        sb.append(",分组:").append(api.getGroup().getName());
                    }
                    sb.append(")");
                }
                sb.append("\n");
                publishApis.addAll(apiRes);
            }
        }

        // 排除已删除的API
        publishApis.removeIf(publishApi -> publishApi.getStatus().equals(API_DELETE));

        if (MiscUtil.isNotEmpty(publishApis)) {
            result.setCode(OK);
            result.setMsg(String.format("校验失败, %s", sb.toString()));
            result.setData(new ArrayList<>(publishApis));
            log.info("{}Found apis with same rule {}", TAG, sb.toString());
        }
        return result;
    }

    private List<Integer> saveRules(@NonNull List<ApiMappingRule> rules) {
        List<ApiMappingRule> ruleRes = mappingRuleRepository.saveAll(rules);
        return ruleRes.stream().map(ApiMappingRule::getId).collect(Collectors.toList());
    }
}
