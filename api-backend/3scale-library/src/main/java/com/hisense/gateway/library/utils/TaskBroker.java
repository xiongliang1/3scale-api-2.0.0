package com.hisense.gateway.library.utils;

import com.hisense.api.library.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TaskBroker {
    public static ExecutorService buildSingleExecutorService(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            thread.setPriority(5);
            return thread;
        });
    }

    public static ScheduledExecutorService buildSingleScheduledExecutorService(String name) {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            thread.setPriority(5);
            return thread;
        });
    }
}
