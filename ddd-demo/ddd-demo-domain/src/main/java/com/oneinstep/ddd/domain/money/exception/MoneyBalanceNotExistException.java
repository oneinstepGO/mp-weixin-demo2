package com.oneinstep.ddd.domain.money.exception;

import com.oneinstep.ddd.common.exception.DomainException;

/**
 * 钱包不存在异常
 */
public class MoneyBalanceNotExistException extends DomainException {

    public MoneyBalanceNotExistException(String message) {
        super(message);
    }

}
