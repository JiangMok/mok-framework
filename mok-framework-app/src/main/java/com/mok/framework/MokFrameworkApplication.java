package com.mok.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 开启定时任务注解
@EnableScheduling
@SpringBootApplication
public class MokFrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MokFrameworkApplication.class, args);
    }

}
