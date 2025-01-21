package com.oneinstep.ddd.domain.money.valueobj;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoneyBalanceChg {

    /**
     * 钱包ID
     */
    private Long id;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 当前余额
     */
    private BigDecimal chgCurrentBalance;

    /**
     * 冻结金额
     */
    private BigDecimal chgFrozenAmount;

}
