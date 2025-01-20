package com.oneinstep.ddd.facade.config;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TradeDatePlanFacade {

    /**
     * 获取交易日期
     *
     * @return 交易日期
     */
    public Integer getTradeDate() {
        return LocalDate.now().getDayOfYear();
    }
}
