package com.oneinstep.ddd.facade.config;

import com.oneinstep.ddd.api.valueobj.ExchangeRate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


@Component
public class ExchangeRateFacade {

    /**
     * 获取组合支付的汇率
     * 1-港币
     * 2-美元
     * 3-人民币
     *
     * @return 汇率
     */
    public List<ExchangeRate> getExchangeRates() {
        // get data from rpc, but mock here
        return Arrays.asList(
                // HKD to USD rate (1 HKD = 0.128 USD)
                new ExchangeRate(1, 2, new BigDecimal("0.128")),
                // USD to HKD rate (1 USD = 7.82 HKD)
                new ExchangeRate(2, 1, new BigDecimal("7.82")),
                // CNY to HKD rate (1 CNY = 1.09 HKD)
                new ExchangeRate(3, 1, new BigDecimal("1.09")),
                // HKD to CNY rate (1 HKD = 0.92 CNY)
                new ExchangeRate(1, 3, new BigDecimal("0.92")),
                // USD to CNY rate (1 USD = 7.18 CNY)
                new ExchangeRate(2, 3, new BigDecimal("7.18")),
                // CNY to USD rate (1 CNY = 0.139 USD)
                new ExchangeRate(3, 2, new BigDecimal("0.139")));
    }

}
