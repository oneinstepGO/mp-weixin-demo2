package com.oneinstep.rule.demo.controller;

import com.oneinstep.rule.demo.model.trade.RiskCheckResult;
import com.oneinstep.rule.demo.service.RiskControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 风险控制接口
 */
@Slf4j
@RestController
@RequestMapping("/api/risk-control")
@RequiredArgsConstructor
public class RiskControlController {

    private final RiskControlService riskControlService;

    /**
     * 检查交易风险
     *
     * @param request 风险检查请求
     * @return 风险检查结果
     */
    @PostMapping("/check")
    public RiskCheckResult checkTradeRisk(@RequestBody RiskCheckRequest request) {
        log.info("Checking trade risk for order: {}", request.getOrder().getOrderId());
        return riskControlService.checkTradeRisk(request.getOrder(), request.getAccount());
    }
}

