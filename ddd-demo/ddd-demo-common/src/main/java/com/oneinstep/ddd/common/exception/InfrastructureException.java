package com.oneinstep.ddd.common.exception;

// 基础设施层异常基类
public class InfrastructureException extends RuntimeException {

    public InfrastructureException(String message) {
        super(message);
    }

}
