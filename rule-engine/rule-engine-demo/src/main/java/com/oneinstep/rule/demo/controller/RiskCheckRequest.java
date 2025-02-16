package com.oneinstep.rule.demo.controller;

import com.oneinstep.rule.demo.model.trade.Account;
import com.oneinstep.rule.demo.model.trade.Order;
import lombok.Data;

/**
 * 风险检查请求
 */
@Data
class RiskCheckRequest {
    private Order order;      // 订单信息
    private Account account;  // 账户信息
} 