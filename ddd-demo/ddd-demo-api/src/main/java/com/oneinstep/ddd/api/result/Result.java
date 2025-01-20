package com.oneinstep.ddd.api.result;

import lombok.Data;

@Data
public class Result<T> {

    private T data;
    private String code;
    private String message;

    public static final String SUCCESS_CODE = "0000";
    public static final String SUCCESS_MESSAGE = "操作成功";
    public static final String ERROR_CODE = "9999";

    public Result(T data, String code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, SUCCESS_CODE, SUCCESS_MESSAGE);
    }

    public static <T> Result<T> error(String code, String message) {
        return new Result<>(null, code, message);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(null, ERROR_CODE, message);
    }

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }

}
