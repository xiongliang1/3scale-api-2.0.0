package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.stud.model.AnalyticsDto;

public interface AnalyticsStud {
    AnalyticsDto serviceAnalytics(String host, String accessToken, ApiTrafficStatQuery statQuery);

    AnalyticsDto serviceAnalytics2(String host, String accessToken, ApiTrafficStatQuery statQuery);
}
