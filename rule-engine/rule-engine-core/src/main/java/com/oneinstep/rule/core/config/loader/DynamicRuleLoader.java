package com.oneinstep.rule.core.config.loader;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.rule.core.event.RuleUpdateEvent;
import com.oneinstep.rule.core.model.RuleDefinition;
import com.oneinstep.rule.core.model.RuleUpdateLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态规则加载器
 */
@Slf4j
@Component
public class DynamicRuleLoader {

    private final ApplicationEventPublisher eventPublisher;

    // KieContainer 缓存 key: group , value: KieContainer
    private final Map<String, KieContainer> ruleCache = new ConcurrentHashMap<>();

    private volatile boolean init = false;

    public DynamicRuleLoader(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public KieContainer getContainer(String ruleGroup) {
        if (!init) {
            throw new IllegalStateException("Not init.");
        }
        return ruleCache.get(ruleGroup);
    }

    /**
     * 更新规则
     */
    public synchronized void updateRules(List<RuleDefinition> rules) {

        if (rules == null || rules.isEmpty()) {
            log.warn("更新规则为空");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("更新规则: {}", JSON.toJSONString(rules));
        }
        // 按组分类规则
        Map<String, List<RuleDefinition>> groupedRules = rules.stream()
                .collect(Collectors.groupingBy(RuleDefinition::getRuleGroup));

        // 更新每个组的规则
        groupedRules.forEach(this::updateGroup);

        this.init = true;
    }

    /**
     * 更新组内规则
     */
    private void updateGroup(String group, List<RuleDefinition> rules) {
        if (StringUtils.isBlank(group) || CollectionUtils.isEmpty(rules)) {
            return;
        }
        List<String> updatedRuleIds = new ArrayList<>();

        RuleUpdateLog updateLog = null;

        try {
            // 获取KieServices实例
            KieServices kieServices = KieServices.Factory.get();
            // 创建KieFileSystem实例
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            // 添加规则到文件系统
            rules.forEach(rule -> {
                // 构建规则文件路径
                String path = String.join(File.separator, "src", "main", "resources", "rules", group, rule.getRuleId() + ".drl");
                // 将规则内容写入文件系统
                kieFileSystem.write(path, rule.getRuleContent());
                // 记录规则ID
                updatedRuleIds.add(rule.getRuleId());
            });

            // 编译规则
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            // 编译所有规则
            kieBuilder.buildAll();

            // 检查编译错误
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                throw new IllegalStateException("规则编译错误:\n" + kieBuilder.getResults().toString());
            }

            // 更新容器
            KieModule kieModule = kieBuilder.getKieModule();
            // 创建新的KieContainer实例
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            // 替换旧容器
            KieContainer oldContainer = ruleCache.put(group, newContainer);
            // 如果旧容器存在，则销毁
            if (oldContainer != null) {
                oldContainer.dispose();
            }

            // 记录更新日志
            updateLog = RuleUpdateLog.builder()
                    .id(UUID.randomUUID().toString())
                    .ruleGroup(group)
                    .updatedRuleIds(updatedRuleIds)
                    .operator("NACOS")
                    .updateTime(LocalDateTime.now())
                    .success(true)
                    .build();

            // 输出缓存统计

        } catch (Exception e) {
            // 记录失败日志
            updateLog = RuleUpdateLog.builder()
                    .id(UUID.randomUUID().toString())
                    .ruleGroup(group)
                    .updatedRuleIds(updatedRuleIds)
                    .operator("SYSTEM")
                    .updateTime(LocalDateTime.now())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();

            throw e;
        } finally {
            // 发布更新事件
            if (updateLog != null) {
                eventPublisher.publishEvent(new RuleUpdateEvent(this, group, updateLog));
            }
        }
    }


}