/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer.config;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * TaskExecutorPoolConfig
 *
 * @Date 2019-11-20 18:37
 * @Author wangjinshan
 * @version v1.0
 * @date 2019-06-05 11:34
 */
@Configuration
@EnableAsync
public class TaskExecutorPoolConfig {

    @Bean("apiPublishAsync")
    public Executor asyncTask() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //线程池维护线程的最少数量
        executor.setCorePoolSize(5);
        //线程池维护线程的最大数量
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        //线程池维护线程所允许的空闲时间,TimeUnit.SECONDS
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("async-task-executor-");
        // 线程池对拒绝任务的处理策略: CallerRunsPolicy策略，当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 构建自定义线程池
     * @param corePoolSize
     * @param maximumPoolSize
     * @param capacity
     * @param KeepAliveSeconds
     * @return
     */
    public static ExecutorService executor(int corePoolSize, int maximumPoolSize, int capacity, int KeepAliveSeconds) {

        ExecutorService executor = new ThreadPoolExecutor(corePoolSize,
                                        maximumPoolSize,
                                        KeepAliveSeconds,
                                        TimeUnit.SECONDS,
                                        new LinkedBlockingDeque(capacity),
                                        Executors.defaultThreadFactory(),
                                        new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }



}
