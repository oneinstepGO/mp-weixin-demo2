package com.oneinstep.rule.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则执行日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionLog {
    /**
     * 日志ID
     */
    private String id;
    /**
     * 规则ID
     */
    private String ruleId;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 规则组
     */
    private String ruleGroup;
    /**
     * 业务键
     */
    private String businessKey;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;
    /**
     * 执行时长(毫秒)
     */
    private Long executeDuration;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 输入数据
     */
    private String inputData;
    /**
     * 输出数据
     */
    private String outputData;
} 