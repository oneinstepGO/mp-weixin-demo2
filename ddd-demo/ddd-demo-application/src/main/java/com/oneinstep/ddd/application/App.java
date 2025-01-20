package com.oneinstep.ddd.application;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.oneinstep.ddd")
@EnableTransactionManagement
@EnableDubbo
@EnableJpaRepositories(basePackages = "com.oneinstep.ddd.infrastructure.repository")
@EntityScan(basePackages = "com.oneinstep.ddd.domain")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}

