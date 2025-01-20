package com.oneinstep.ddd.domain.money.constant;

import lombok.Getter;

/**
 * 资金余额操作类型
 */
@Getter
public enum MoneyBalanceOperationType {

    /**
     * 增加
     */
    INCREASE(1),
    /**
     * 减少
     */
    DECREASE(2);

    private final int code;

    MoneyBalanceOperationType(int code) {
        this.code = code;
    }

}
