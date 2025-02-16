package com.oneinstep.rule.demo.model.trade;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ConditionalOrder {
    private String orderId;           // 订单ID
    private String stockCode;         // 股票代码
    private String accountId;         // 账户ID
    private double costPrice;         // 成本价
    private double currentPrice;      // 当前价格
    private double quantity;          // 持仓数量
    private double takeProfitRate;    // 止盈比例
    private double stopLossRate;      // 止损比例
    private OrderStatus status;       // 订单状态
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

    @Builder.Default  // 使用@Builder.Default注解设置默认值
    private List<String> triggerReasons = new ArrayList<>();

    public void addTriggerReason(String reason) {
        if (triggerReasons == null) {
            triggerReasons = new ArrayList<>();
        }
        triggerReasons.add(reason);
    }

    // 计算盈亏比例
    public double getProfitRate() {
        if (costPrice == 0) {
            return -1;
        }
        return (currentPrice - costPrice) / costPrice;
    }
} 