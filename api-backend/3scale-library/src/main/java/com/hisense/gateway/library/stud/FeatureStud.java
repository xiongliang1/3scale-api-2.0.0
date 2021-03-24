package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.FeatureDtos;

public interface FeatureStud {
    FeatureDtos findFeatureByServiceId(String host, String accessToken, String serviceId);
}
