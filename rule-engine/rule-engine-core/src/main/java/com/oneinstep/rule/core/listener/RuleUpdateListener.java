package com.oneinstep.rule.core.listener;

import com.oneinstep.rule.core.event.RuleUpdateEvent;
import com.oneinstep.rule.core.service.RuleUpdateLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 规则更新监听器
 */
@Slf4j
@Component
public class RuleUpdateListener {

    @Resource
    private RuleUpdateLogService ruleUpdateLogService;

    /**
     * 处理规则更新事件
     *
     * @param event 规则更新事件
     */
    @EventListener
    public void handleRuleUpdate(RuleUpdateEvent event) {
        log.info("Rules updated for group: {}, updated rules: {}",
                event.getRuleGroup(), event.getUpdateLog().getUpdatedRuleIds());

        ruleUpdateLogService.logUpdate(event.getUpdateLog());
    }
} 