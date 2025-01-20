package com.oneinstep.ddd.api.valueobj;

import java.math.BigDecimal;
import java.util.List;

/**
 * 汇率
 *
 * @param fromCurrency 源币种
 * @param toCurrency   目标币种
 * @param rate         汇率
 */
public record ExchangeRate(int fromCurrency, int toCurrency, BigDecimal rate) {

    public static ExchangeRate of(int fromCurrency, int toCurrency, BigDecimal rate) {
        return new ExchangeRate(fromCurrency, toCurrency, rate);
    }

    /**
     * 查找汇率
     *
     * @param exchangeRates 汇率列表
     * @param fromCurrency  源币种
     * @param toCurrency    目标币种
     * @return 汇率
     */
    public static ExchangeRate findExchangeRate(List<ExchangeRate> exchangeRates, int fromCurrency, int toCurrency) {
        // 如果 源币种和目标币种相同 则返回 1:1 的汇率
        if (fromCurrency == toCurrency) {
            return new ExchangeRate(fromCurrency, toCurrency, BigDecimal.ONE);
        }
        return exchangeRates.stream()
                .filter(rate -> rate.fromCurrency() == fromCurrency
                        && rate.toCurrency() == toCurrency)
                .findFirst()
                .orElse(null);
    }
}
