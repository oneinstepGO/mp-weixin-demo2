package com.oneinstep.rule.core.executor;

import com.oneinstep.rule.core.config.loader.DynamicRuleLoader;
import com.oneinstep.rule.core.listener.RuleExecutionEventListener;
import com.oneinstep.rule.core.model.RuleExecutionContext;
import com.oneinstep.rule.core.model.RuleExecutionResult;
import com.oneinstep.rule.core.service.RuleExecutionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * 规则执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleExecutor {
    private final RuleExecutionLogService logService;
    private final DynamicRuleLoader ruleLoader;

    /**
     * 执行规则
     *
     * @param context 规则执行上下文
     * @return 规则执行结果
     */
    public RuleExecutionResult execute(RuleExecutionContext context) {
        log.debug("Starting rule execution for group: {}", context.getRuleGroup());

        RuleExecutionResult result = new RuleExecutionResult();

        // 创建有状态的KieSession
        try (KieSession kieSession = ruleLoader.getContainer(context.getRuleGroup()).newKieSession()) {
            // 添加规则执行监听器
            kieSession.addEventListener(new RuleExecutionEventListener(context, logService));

            // 设置全局变量
            context.getGlobals().forEach(kieSession::setGlobal);

            // 插入所有事实对象
            context.getFacts().forEach(kieSession::insert);

            // 执行规则
            int rulesExecuted = kieSession.fireAllRules(context.getAgendaFilter());
            log.debug("Executed {} rules", rulesExecuted);

            // 设置执行结果
            result.setRulesExecuted(rulesExecuted);
            result.setSuccess(true);

            // 收集执行后的事实对象
            result.setFacts(context.getFacts());

        } catch (Exception e) {
            log.error("Error executing rules", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }
}