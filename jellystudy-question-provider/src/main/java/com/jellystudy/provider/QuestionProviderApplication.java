package com.jellystudy.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication(scanBasePackages = "com.jellystudy")
public class QuestionProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionProviderApplication.class, args);
    }
}
