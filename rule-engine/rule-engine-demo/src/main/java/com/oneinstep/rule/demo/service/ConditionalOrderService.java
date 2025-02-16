package com.oneinstep.rule.demo.service;

import com.oneinstep.rule.core.executor.RuleExecutor;
import com.oneinstep.rule.core.model.RuleExecutionContext;
import com.oneinstep.rule.core.model.RuleExecutionResult;
import com.oneinstep.rule.demo.model.trade.ConditionalOrder;
import com.oneinstep.rule.demo.model.trade.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.oneinstep.rule.demo.constants.RuleGroupConstants.RULE_GROUP_CONDITIONAL_ORDER;

@Service
@Slf4j
public class ConditionalOrderService {

    private final RuleExecutor ruleExecutor;

    public ConditionalOrderService(RuleExecutor ruleExecutor) {
        this.ruleExecutor = ruleExecutor;
    }

    public ConditionalOrder createOrder(String stockCode, double costPrice,
                                        double quantity, double takeProfitRate, double stopLossRate) {

        ConditionalOrder order = ConditionalOrder.builder()
                .orderId(generateOrderId())
                .stockCode(stockCode)
                .costPrice(costPrice)
                .quantity(quantity)
                .takeProfitRate(takeProfitRate)
                .stopLossRate(stopLossRate)
                .status(OrderStatus.PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        log.info("Created conditional order: {}", order);
        return order;
    }

    public ConditionalOrder checkOrderTrigger(ConditionalOrder order, double currentPrice) {
        order.setCurrentPrice(currentPrice);

        RuleExecutionContext context = RuleExecutionContext.builder()
                .ruleGroup(RULE_GROUP_CONDITIONAL_ORDER)
                .facts(Collections.singletonList(order))
                .build();
        // 执行规则
        RuleExecutionResult ruleExecutionResult = ruleExecutor.execute(context);

        ruleExecutionResult.getFacts().forEach(fact -> {
            if (fact instanceof ConditionalOrder updatedOrder && OrderStatus.TRIGGERED.equals(updatedOrder.getStatus())) {
                executeOrder(updatedOrder);
            }
        });

        return order;
    }

    private void executeOrder(ConditionalOrder order) {
        // 这里实现实际的交易逻辑
        log.info("Executing order: {}", order);
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdateTime(LocalDateTime.now());
    }

    private String generateOrderId() {
        return "CO" + System.currentTimeMillis();
    }
} 