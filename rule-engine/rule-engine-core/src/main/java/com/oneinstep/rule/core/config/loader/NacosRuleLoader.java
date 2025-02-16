package com.oneinstep.rule.core.config.loader;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.oneinstep.rule.core.loader.RuleFileLoader;
import com.oneinstep.rule.core.model.RuleDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.oneinstep.rule.core.config.RuleConfigConstants.LOCAL_RULES_PATH;
import static com.oneinstep.rule.core.config.RuleConfigConstants.RULE_GROUP;

/**
 * Nacos规则加载器
 * 从Nacos加载规则 会覆盖本地规则
 */
@Component
@Slf4j
public class NacosRuleLoader implements RuleLoader {

    private final ConfigService configService;
    private final DynamicRuleLoader dynamicRuleLoader;

    public NacosRuleLoader(ConfigService configService,
                           DynamicRuleLoader dynamicRuleLoader) {
        this.configService = configService;
        this.dynamicRuleLoader = dynamicRuleLoader;
    }

    private void handleRuleUpdate(String ruleFile, String newConfig) {
        try {
            log.info("Received rule configuration update: {}", newConfig);
            // 将配置信息转换为规则定义列表
            RuleDefinition ruleDefinition = parseRuleDefinition(ruleFile, newConfig);
            // 更新规则管理服务
            dynamicRuleLoader.updateRules(Collections.singletonList(ruleDefinition));
        } catch (Exception e) {
            log.error("Failed to process rule configuration update", e);
        }
    }

    @Override
    public List<RuleDefinition> loadRules() {

        // 从本地资源加载规则文件
        Map<String, String> ruleFiles = RuleFileLoader.loadLocalRules(LOCAL_RULES_PATH);

        List<RuleDefinition> rules = new ArrayList<>();

        //
        for (Map.Entry<String, String> entry : ruleFiles.entrySet()) {
            String ruleFile = entry.getKey();

            String dataId = convertToDataId(ruleFile);
            try {
                String content = configService.getConfig(
                        dataId,
                        RULE_GROUP,
                        5000
                );

                if (content != null && !content.trim().isEmpty()) {
                    log.info("get rule from dataId: {} from nacos: {}", dataId, content);
                    // 使用Nacos配置覆盖本地配置
                    // 从文件路径解析规则信息
                    RuleDefinition rule = parseRuleDefinition(ruleFile, content);

                    rules.add(rule);
                    log.info("Loaded rule from Nacos: {}", dataId);
                } else {
                    // 使用本地配置
                    log.info("Using local rule for: {}", ruleFile);
                }

                // 添加配置监听
                addConfigListener(dataId, ruleFile);

            } catch (Exception e) {
                log.error("Failed to load rule from Nacos: {}", dataId, e);

            }
        }

        log.info("Successfully loaded {} rules from local resources", rules.size());
        return rules;
    }

    @Override
    public int getOrder() {
        // Nacos 优先级最高
        return 1;
    }

    /**
     * 添加Nacos配置监听
     *
     * @param dataId   数据ID
     * @param ruleFile 规则文件
     */
    private void addConfigListener(String dataId, String ruleFile) {
        try {
            configService.addListener(dataId, RULE_GROUP, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String newConfig) {
                    handleRuleUpdate(ruleFile, newConfig);
                }
            });
            log.debug("Added listener for rule: {} -> {}", dataId, ruleFile);
        } catch (Exception e) {
            log.error("Failed to add listener for rule: {}", dataId, e);
        }
    }

    /**
     * 将规则文件路径转换为Nacos dataId
     *
     * @param ruleFile 规则文件路径
     * @return Nacos dataId
     */
    private String convertToDataId(String ruleFile) {
        // 将规则文件路径转换为Nacos dataId
        // 例如: rules/conditionalOrder/profit_loss_rule.drl -> conditionalOrder.profit_loss_rule
        String path = ruleFile.replace(LOCAL_RULES_PATH + "/", "");
        path = path.replace(".drl", "");
        return path.replace('/', '.');
    }
}