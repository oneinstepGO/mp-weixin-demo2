package com.oneinstep.rule.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rule.log")
public class RuleLogProperties {

    /**
     * 日志文件目录，默认为当前目录
     */
    private String logDir = ".";

    /**
     * 规则执行日志文件名
     */
    private String executionLogFile = "rule-execution.log";

    /**
     * 规则更新日志文件名
     */
    private String updateLogFile = "rule-update.log";
}