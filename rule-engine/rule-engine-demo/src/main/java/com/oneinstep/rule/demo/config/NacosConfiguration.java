package com.oneinstep.rule.demo.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos配置
 */
@Configuration
public class NacosConfiguration {

    @Value("${nacos.config.server-addr}")
    private String serverAddr;

    @Value("${nacos.config.namespace}")
    private String namespace;

    @Bean
    public ConfigService configService() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        properties.put("username", "nacos");
        properties.put("password", "nacos");
        properties.put("contextPath", "/nacos");
        properties.put("encoding", "UTF-8");
        return NacosFactory.createConfigService(properties);
    }
} 