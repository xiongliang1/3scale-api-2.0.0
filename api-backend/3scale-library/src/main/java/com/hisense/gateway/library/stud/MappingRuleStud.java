package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.MappingRuleDto;

import java.util.List;

public interface MappingRuleStud {
    MappingRuleDto createMapingRule(String host, String accessToken, String serviceId,
                                    String httpMethod, String pattern, String metricId);

    List<MappingRuleDto> searchMappingRulesList(String host, String accessToken, String serviceId);

    MappingRuleDto updateMappingRules(String host, String accessToken, String serviceId,
                                      String mappingRuleId, String httpMethod, String pattern);


    void deleteMappingRules(String host, String accessToken, String serviceId, String mappingRuleId);
}
