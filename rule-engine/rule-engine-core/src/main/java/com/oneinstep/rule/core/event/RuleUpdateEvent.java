package com.oneinstep.rule.core.event;

import com.oneinstep.rule.core.model.RuleUpdateLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 规则更新事件
 */
@Getter
public class RuleUpdateEvent extends ApplicationEvent {
    // 规则组
    private final String ruleGroup;

    private final RuleUpdateLog updateLog;

    /**
     * 构造函数
     *
     * @param source    事件源
     * @param ruleGroup 规则组
     */
    public RuleUpdateEvent(Object source, String ruleGroup, RuleUpdateLog updateLog) {
        super(source);
        this.ruleGroup = ruleGroup;
        this.updateLog = updateLog;
    }
}