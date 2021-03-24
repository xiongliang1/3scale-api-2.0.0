package com.hisense.gateway.management.config;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.config.SystemConfigProperties;
import com.hisense.gateway.library.service.AlertPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.ArrayList;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;

/**
 * @author guilai.ming 2020/09/10
 * <p>
 * 告警策略同步到kafka,定时任务配置
 */
@Slf4j
@Configuration
@EnableScheduling
public class AlertPolicySyncSchedule implements SchedulingConfigurer {
    @Autowired
    SystemConfigProperties configProperties;

    @Autowired
    AlertPolicyService alertPolicyService;

    private static final String FIX_CORN = "0 0 21 ? * *";

    private List<String> getConfigCrons() {
        List<String> crons = configProperties.getAlertSyncCronTrigger();
        if (MiscUtil.isEmpty(crons)) {
            log.error("{} config crons is empty,use default", TAG);
            crons = new ArrayList<>();
            crons.add(FIX_CORN);
        }
        return crons;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        List<String> crons = getConfigCrons();
        for (String cron : crons) {
            addTriggerTask(taskRegistrar, cron);
        }
    }

    private void addTriggerTask(ScheduledTaskRegistrar taskRegistrar, String cron) {
        taskRegistrar.addTriggerTask(() -> {
            log.info("{}AlertPolicySyncTask executed with cron {} ", TAG, cron);
            log.info("{}AlertPolicySyncTask done with {}", TAG, alertPolicyService.syncAlertPolicyToKafka());
        }, triggerContext -> {
            CronTrigger cronTrigger = new CronTrigger(cron);
            return cronTrigger.nextExecutionTime(triggerContext);
        });

        log.info("{}AlertPolicySyncTaskRegistrar created with cron {}", TAG, cron);
    }
}
