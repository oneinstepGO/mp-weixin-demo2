package com.oneinstep.rule.demo.controller;

import com.oneinstep.rule.demo.model.trade.ConditionalOrder;
import com.oneinstep.rule.demo.service.ConditionalOrderService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conditional-orders")
@Slf4j
public class ConditionalOrderController {

    private final ConditionalOrderService conditionalOrderService;

    public ConditionalOrderController(ConditionalOrderService conditionalOrderService) {
        this.conditionalOrderService = conditionalOrderService;
    }

    /**
     * 创建条件单
     */
    @PostMapping
    public ConditionalOrder createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Creating conditional order: {}", request);
        return conditionalOrderService.createOrder(
                request.getStockCode(),
                request.getCostPrice(),
                request.getQuantity(),
                request.getTakeProfitRate(),
                request.getStopLossRate()
        );
    }

    /**
     * 检查条件单触发
     */
    @PostMapping("/check")
    public ConditionalOrder checkOrder(@RequestBody ConditionalOrder order) {
        log.info("Checking conditional order: {}", order);
        return conditionalOrderService.checkOrderTrigger(order, order.getCurrentPrice());
    }

    /**
     * 创建条件单请求
     */
    @Data
    public static class CreateOrderRequest {
        private String stockCode;
        private double costPrice;
        private double quantity;
        private double takeProfitRate = 0.5; // 默认50%止盈
        private double stopLossRate = 0.2;    // 默认20%止损
    }
} 