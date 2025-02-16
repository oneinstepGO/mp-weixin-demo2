package com.oneinstep.rule.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则更新日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleUpdateLog {
    /**
     * 日志ID
     */
    private String id;
    /**
     * 规则组
     */
    private String ruleGroup;
    /**
     * 更新规则ID列表
     */
    private List<String> updatedRuleIds;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String errorMessage;
} 