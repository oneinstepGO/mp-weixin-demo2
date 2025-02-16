package com.oneinstep.rule.core.config.loader;

import com.oneinstep.rule.core.loader.RuleFileLoader;
import com.oneinstep.rule.core.model.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.oneinstep.rule.core.config.RuleConfigConstants.LOCAL_RULES_PATH;

/**
 * 本地规则资源加载器
 * 从本地resources/rules目录加载所有.drl规则文件
 */
@Component
@Slf4j
public class LocalResourceRuleLoader implements RuleLoader {

    @Override
    public List<RuleDefinition> loadRules() {
        try {
            // 从本地资源加载规则文件
            Map<String, String> ruleFiles = RuleFileLoader.loadLocalRules(LOCAL_RULES_PATH);

            // 将规则文件转换为RuleDefinition
            List<RuleDefinition> rules = new ArrayList<>();
            for (Map.Entry<String, String> entry : ruleFiles.entrySet()) {
                String ruleFile = entry.getKey();
                String ruleContent = entry.getValue();

                // 从文件路径解析规则信息
                RuleDefinition rule = parseRuleDefinition(ruleFile, ruleContent);
                rules.add(rule);
            }

            log.info("Successfully loaded {} rules from local resources", rules.size());
            return rules;

        } catch (Exception e) {
            log.warn("Failed to load rules from local resources", e);
            return Collections.emptyList();
        }
    }


    @Override
    public int getOrder() {
        // 本地资源优先级较低
        return 100;
    }
}