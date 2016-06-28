package com.jt.server.dns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * since 2016/6/25.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Main {

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }
}
