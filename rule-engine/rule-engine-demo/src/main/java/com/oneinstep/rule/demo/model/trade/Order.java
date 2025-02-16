package com.oneinstep.rule.demo.model.trade;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order {
    private String orderId;        // 订单ID
    private String accountId;      // 账户ID
    private String stockCode;      // 股票代码
    private String type;          // 订单类型(BUY/SELL)
    private double price;         // 委托价格
    private double quantity;      // 委托数量

    /**
     * 获取订单金额
     */
    public double getAmount() {
        return price * quantity;
    }
} 