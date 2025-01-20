package com.oneinstep.ddd.domain.money.exception;

import com.oneinstep.ddd.common.exception.DomainException;

/**
 * 余额不足异常
 */
public class MoneyNotEnoughException extends DomainException {

    public MoneyNotEnoughException(String message) {
        super(message);
    }

    public MoneyNotEnoughException(String message, Throwable cause) {
        super(message, cause);
    }

}
