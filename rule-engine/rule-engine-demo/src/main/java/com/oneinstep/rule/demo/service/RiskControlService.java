package com.oneinstep.rule.demo.service;

import com.oneinstep.rule.core.executor.RuleExecutor;
import com.oneinstep.rule.core.model.RuleExecutionContext;
import com.oneinstep.rule.core.model.RuleExecutionResult;
import com.oneinstep.rule.demo.model.trade.Account;
import com.oneinstep.rule.demo.model.trade.Order;
import com.oneinstep.rule.demo.model.trade.RiskCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.oneinstep.rule.demo.constants.RuleGroupConstants.RULE_GROUP_RISK_CONTROL;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlService {

    private final RuleExecutor ruleExecutor;

    public RiskCheckResult checkTradeRisk(Order order, Account account) {
        // 创建风险检查结果对象
        RiskCheckResult result = RiskCheckResult.builder()
                .passed(true)
                .build();

        try {
            RuleExecutionContext context = RuleExecutionContext.builder()
                    .ruleGroup(RULE_GROUP_RISK_CONTROL)
                    .facts(List.of(order, result, account))
                    .build();
            // 执行风险控制规则
            RuleExecutionResult ruleExecutionResult = ruleExecutor.execute(context);

            if (!ruleExecutionResult.isSuccess()) {
                result.addViolation("系统错误", ruleExecutionResult.getErrorMessage());
                return result;
            }

            if (ruleExecutionResult.getFacts() != null) {
                ruleExecutionResult.getFacts().forEach(fact -> {
                    if (fact instanceof RiskCheckResult r) {
                        result.setPassed(r.isPassed());
                        result.setViolations(r.getViolations());
                    }
                });
            }

            log.info("Risk check completed for order: {}, passed: {}",
                    order.getOrderId(), result.isPassed());

            if (!result.isPassed()) {
                log.warn("Risk violations: {}", result.getViolations());
            }

        } catch (Exception e) {
            log.error("Error checking trade risk", e);
            result.addViolation("系统错误", e.getMessage());
        }

        return result;
    }
} 