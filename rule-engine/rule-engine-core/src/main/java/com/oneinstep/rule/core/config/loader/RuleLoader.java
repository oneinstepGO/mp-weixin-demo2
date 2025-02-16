package com.oneinstep.rule.core.config.loader;

import com.oneinstep.rule.core.model.RuleDefinition;

import java.util.List;

import static com.oneinstep.rule.core.config.RuleConfigConstants.DRL_EXTENSION;


/**
 * 规则加载器接口
 */
public interface RuleLoader {
    /**
     * 加载规则定义
     */
    List<RuleDefinition> loadRules();

    /**
     * 获取加载器优先级，数字越小优先级越高
     */
    int getOrder();

    /**
     * 解析规则定义
     *
     * @param ruleFile    规则文件
     * @param ruleContent 规则内容
     * @return 规则定义
     */
    default RuleDefinition parseRuleDefinition(String ruleFile, String ruleContent) {
        // 从文件路径解析规则组和规则ID
        // 例如: rules/conditionalOrder/profit_loss_rule.drl
        String[] parts = ruleFile.split("/");
        String ruleGroup = parts.length > 2 ? parts[parts.length - 2] : "";
        String ruleId = parts.length > 1 ? parts[parts.length - 1].replace(DRL_EXTENSION, "") : "";

        return RuleDefinition.builder()
                .ruleId(ruleId)
                .ruleName(ruleId) // 可以从规则内容中解析更友好的名称
                .ruleContent(ruleContent)
                .ruleGroup(ruleGroup)
                .build();
    }
}