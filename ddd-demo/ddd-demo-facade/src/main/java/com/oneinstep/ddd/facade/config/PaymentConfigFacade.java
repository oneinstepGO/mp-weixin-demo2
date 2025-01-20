package com.oneinstep.ddd.facade.config;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PaymentConfigFacade {

    /**
     * 获取系统配置的组合支付可用币种及其顺序
     *
     * @return 组合支付可用币种及其顺序
     */
    public List<Integer> getConfigPaymentCurrencies() {
        // get data from rpc, but mock here
        return Arrays.asList(1, 2, 3);
    }

}
