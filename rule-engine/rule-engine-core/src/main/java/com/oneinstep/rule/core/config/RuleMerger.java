package com.oneinstep.rule.core.config;

import com.oneinstep.rule.core.config.loader.RuleLoader;
import com.oneinstep.rule.core.model.RuleDefinition;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则合并器
 */
@Component
public class RuleMerger {

    private final List<RuleLoader> ruleLoaders;

    public RuleMerger(List<RuleLoader> ruleLoaders) {
        this.ruleLoaders = ruleLoaders;
    }

    public List<RuleDefinition> mergeRules() {
        // 按优先级排序加载器
        List<RuleLoader> sortedLoaders = ruleLoaders.stream()
                .sorted(Comparator.comparingInt(RuleLoader::getOrder))
                .toList();

        // 存储合并后的规则
        Map<String, RuleDefinition> mergedRules = sortedLoaders.stream()
                // 将每个加载器的规则流合并
                .flatMap(loader -> loader.loadRules().stream())
                // 收集为Map，使用规则ID作为键
                .collect(Collectors.toMap(
                        RuleDefinition::getRuleId,
                        rule -> rule,
                        // 如果有重复，使用后加载的规则
                        (existing, replacement) -> replacement));

        // 返回合并后的规则列表
        return mergedRules.values().stream().toList();
    }
}