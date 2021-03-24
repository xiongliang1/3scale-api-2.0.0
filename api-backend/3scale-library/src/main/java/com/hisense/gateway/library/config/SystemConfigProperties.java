package com.hisense.gateway.library.config;

import com.hisense.gateway.library.model.pojo.base.SystemInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "api.config", ignoreUnknownFields = true)
public class SystemConfigProperties {
    private SystemInfo defaultSystem;

    /**
     * 告警策略配置
     */
    private List<String> alertSyncCronTrigger;

    private Alert alert = new Alert();

    private ElasticSearch elasticSearch = new ElasticSearch();

    private List<Integer> apiRespTimeRates = Arrays.asList(50, 75, 90, 95, 99);

    /**
     * 告警策略
     */
    @Data
    public static class Alert {
        private String kafkaTopic;
        private boolean kafkaEnable;
        private boolean serialSync = false;// 是否启用串行化sync机制: 每次变更都添加到sync任务到队列
    }

    // ES查询
    @Data
    public static class ElasticSearch {
        private String host;//es地址
        private String port;//es端口
        private String index;//日志索引
    }
}
