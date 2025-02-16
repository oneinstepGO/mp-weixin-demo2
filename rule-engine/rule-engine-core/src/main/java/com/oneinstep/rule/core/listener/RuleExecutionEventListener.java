package com.oneinstep.rule.core.listener;

import com.alibaba.fastjson2.JSON;
import com.oneinstep.rule.core.model.RuleExecutionContext;
import com.oneinstep.rule.core.model.RuleExecutionLog;
import com.oneinstep.rule.core.service.RuleExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 规则执行事件监听器
 */
@Slf4j
public class RuleExecutionEventListener extends DefaultAgendaEventListener {

    /**
     * 规则组
     */
    private final String ruleGroup;
    /**
     * 事实
     */
    private final List<Object> facts;
    /**
     * 日志服务
     */
    private final RuleExecutionLogService logService;
    /**
     * 开始时间
     */
    private final LocalDateTime startTime;

    public RuleExecutionEventListener(RuleExecutionContext context, RuleExecutionLogService logService) {
        this.ruleGroup = context.getRuleGroup();
        this.facts = context.getFacts();
        this.logService = logService;
        this.startTime = LocalDateTime.now();
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        Rule rule = event.getMatch().getRule();
        String ruleId = rule.getId();
        String ruleName = rule.getName();

        try {
            log.debug("Rule executed: {}", ruleId);

            // 记录执行日志
            RuleExecutionLog executionLog = RuleExecutionLog.builder()
                    .id(UUID.randomUUID().toString())
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .ruleGroup(ruleGroup)
                    .executeTime(startTime)
                    .executeDuration(Duration.between(startTime, LocalDateTime.now()).toMillis())
                    .success(true)
                    .inputData(serializeToJson(facts))
                    .outputData(serializeToJson(facts))
                    .build();

            logService.logExecution(executionLog);
        } catch (Exception e) {
            logError(ruleId, ruleName, e);
        }
    }

    /**
     * 记录错误日志
     *
     * @param ruleId   规则ID
     * @param ruleName 规则名称
     * @param error    错误
     */
    private void logError(String ruleId, String ruleName, Exception error) {
        try {
            RuleExecutionLog errorLog = RuleExecutionLog.builder()
                    .id(UUID.randomUUID().toString())
                    .ruleId(ruleId)
                    .ruleName(ruleName)
                    .ruleGroup(ruleGroup)
                    .executeTime(startTime)
                    .executeDuration(Duration.between(startTime, LocalDateTime.now()).toMillis())
                    .success(false)
                    .errorMessage(error.getMessage())
                    .inputData(serializeToJson(facts))
                    .build();

            logService.logExecution(errorLog);
            log.error("Rule execution error: {}", error.getMessage(), error);
        } catch (Exception e) {
            log.error("Failed to log rule execution error", e);
        }
    }

    /**
     * 安全地序列化对象到JSON
     */
    private String serializeToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            if (obj instanceof List<?> list) {
                // 对列表中的每个对象单独序列化
                return list.stream()
                        .map(item -> {
                            try {
                                return JSON.toJSONString(item);
                            } catch (Exception e) {
                                return String.format("[%s]", item);
                            }
                        })
                        .collect(Collectors.joining(", ", "[", "]"));
            }
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            if (obj instanceof Collection<?>) {
                return String.format("[Collection of %d items]", ((Collection<?>) obj).size());
            }
            return String.format("[%s@%s]", obj.getClass().getSimpleName(),
                    Integer.toHexString(System.identityHashCode(obj)));
        }
    }
}

