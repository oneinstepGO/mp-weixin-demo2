package com.oneinstep.ddd.api.valueobj;

/**
 * 支付方式
 *
 * @param multipleCurrency 是否多币种支付
 */
public record PaymentMethod(boolean multipleCurrency) {

    public static PaymentMethod of(boolean multipleCurrency) {
        return new PaymentMethod(multipleCurrency);
    }

    /**
     * 单币种支付
     */
    public static final PaymentMethod SINGLE_CURRENCY = new PaymentMethod(false);

    /**
     * 多币种支付
     */
    public static final PaymentMethod MULTIPLE_CURRENCY = new PaymentMethod(true);

}
