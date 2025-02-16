package com.oneinstep.rule.core.config;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Options;
import com.oneinstep.rule.core.config.loader.DynamicRuleLoader;
import com.oneinstep.rule.core.model.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 规则初始化器
 */
@Component
@Slf4j
public class RuleInitializer implements CommandLineRunner {

    private final RuleMerger ruleMerger;
    private final DynamicRuleLoader dynamicRuleLoader;

    public RuleInitializer(RuleMerger ruleMerger, DynamicRuleLoader dynamicRuleLoader) {
        this.ruleMerger = ruleMerger;
        this.dynamicRuleLoader = dynamicRuleLoader;

        // 配置 Aviator 表达式缓存
        configureAviator();
    }

    private void configureAviator() {
        // 启用表达式缓存
        AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();
        instance.setCachedExpressionByDefault(true);
        instance.setOption(Options.OPTIMIZE_LEVEL, AviatorEvaluator.EVAL);
        instance.setOption(Options.MAX_LOOP_COUNT, 500);

        log.info("Configured Aviator expression cache");
    }

    @Override
    public void run(String... args) {
        // 合并加载规则
        List<RuleDefinition> rules = ruleMerger.mergeRules();
        // 更新规则管理服务
        dynamicRuleLoader.updateRules(rules);
        log.info("Successfully initialized {} rules", rules.size());
    }
}