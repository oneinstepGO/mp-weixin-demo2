package com.oneinstep.rule.demo.model.trade;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
public class Account {
    private String id;                     // 账户ID
    private String level;                  // 账户级别
    private double balance;                // 账户余额
    private int dailyTradeCount;          // 当日交易次数

    @Builder.Default
    private Map<String, Position> positions = new ConcurrentHashMap<>();  // 持仓信息

    @Builder.Default
    private Map<String, Double> stockPositions = new ConcurrentHashMap<>();  // 股票持仓比例

    /**
     * 获取股票持仓集中度
     */
    public double getStockConcentration(String stockCode) {
        return stockPositions.getOrDefault(stockCode, 0.0);
    }

    /**
     * 获取总资产
     */
    public double getTotalAssets() {
        return balance + positions.values().stream()
                .mapToDouble(p -> p.getCurrentPrice() * p.getQuantity())
                .sum();
    }
} 