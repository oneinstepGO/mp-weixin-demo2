package com.oneinstep.ddd.common.exception;

// 应用层异常基类
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
}

