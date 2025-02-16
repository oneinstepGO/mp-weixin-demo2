package com.oneinstep.rule.demo.model.trade;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Position {
    private String stockCode;      // 股票代码
    private double quantity;       // 持仓数量
    private double costPrice;      // 成本价
    private double currentPrice;   // 当前价格
} 