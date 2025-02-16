package com.oneinstep.rule.core.config.loader;

import com.oneinstep.rule.core.config.RuleMerger;
import com.oneinstep.rule.core.event.RuleUpdateEvent;
import com.oneinstep.rule.core.model.RuleDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieContainer;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicRuleLoaderTest {

    @Mock
    private RuleMerger ruleMerger;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DynamicRuleLoader dynamicRuleLoader;

    @BeforeEach
    void setUp() {
        dynamicRuleLoader = new DynamicRuleLoader(eventPublisher);
    }

    @Test
    void shouldLoadRulesFromLocalAndNacos() {
        // given
        String ruleGroup = "testGroup";
        List<RuleDefinition> localRules = Arrays.asList(
                new RuleDefinition("rule1", "Test Rule 1", "package rules.rule1", ruleGroup),
                new RuleDefinition("rule2", "Test Rule 2", "package rules.rule2", ruleGroup),
                new RuleDefinition("rule3", "Test Rule 3", "package rules.rule3", ruleGroup));

        when(ruleMerger.mergeRules()).thenReturn(localRules);

        // when
        // 合并加载规则
        List<RuleDefinition> rules = ruleMerger.mergeRules();
        // 更新规则管理服务
        dynamicRuleLoader.updateRules(rules);

        verify(eventPublisher).publishEvent(any(RuleUpdateEvent.class));

        // 验证缓存
        KieContainer kieContainer = dynamicRuleLoader.getContainer(ruleGroup);
        assertNotNull(kieContainer);

        // 再次获取应该直接从缓存返回相同实例
        KieContainer kieContainer2 = dynamicRuleLoader.getContainer(ruleGroup);
        assertSame(kieContainer, kieContainer2);
    }

    @Test
    void shouldUpdateRulesAndInvalidateCache() {
        // given
        String ruleGroup = "testGroup";
        List<RuleDefinition> initialRules = List.of(
                new RuleDefinition("rule1", "Test Rule 1", "package rules.rule1", ruleGroup));
        List<RuleDefinition> updatedRules = List.of(
                new RuleDefinition("rule1", "Test Rule 1", "package rules.rule1", ruleGroup));


        when(ruleMerger.mergeRules()).thenReturn(initialRules);
        List<RuleDefinition> rules = ruleMerger.mergeRules();
        dynamicRuleLoader.updateRules(rules);
        KieContainer originalContainer = dynamicRuleLoader.getContainer(ruleGroup);


        when(ruleMerger.mergeRules()).thenReturn(updatedRules);
        List<RuleDefinition> rules2 = ruleMerger.mergeRules();
        dynamicRuleLoader.updateRules(rules2);
        KieContainer newContainer = dynamicRuleLoader.getContainer(ruleGroup);


        verify(eventPublisher, times(2)).publishEvent(any(RuleUpdateEvent.class));

        assertNotNull(newContainer);
        assertNotSame(originalContainer, newContainer);
    }

}