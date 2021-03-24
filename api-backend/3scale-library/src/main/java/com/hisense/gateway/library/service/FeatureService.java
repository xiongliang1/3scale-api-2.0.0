package com.hisense.gateway.library.service;

import com.hisense.gateway.library.stud.model.FeatureDtos;

public interface FeatureService {
    FeatureDtos findFeatureByServiceId(String domainName, String serviceId);
}
