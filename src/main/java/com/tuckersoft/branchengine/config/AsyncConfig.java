package com.tuckersoft.branchengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Pool de hilos del Informe de Realidad.
 *
 * Sin @EnableAsync el listener correria en el hilo de la peticion y el POST
 * /api/v1/decisions dejaria de responder en menos de 1.5 s.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "branchExecutor")
    public Executor branchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("branch-worker-");
        executor.initialize();
        return executor;
    }
}
