package com.oneinstep.rule.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.oneinstep.rule")
public class RuleEngineDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleEngineDemoApplication.class, args);
    }

}
