package com.oneinstep.ddd.api.valueobj;

/**
 * 币种
 *
 * @param code   币种代码 system_currency_code
 * @param symbol 币种符号
 * @param name   币种名称
 */
public record MoneyCurrency(int code, String symbol, String name) {

    public static MoneyCurrency of(int code, String symbol, String name) {
        return new MoneyCurrency(code, symbol, name);
    }

    public static MoneyCurrency of(int code) {
        return switch (code) {
            case 1 -> HKD;
            case 2 -> USD;
            case 3 -> CNY;
            default -> throw new IllegalArgumentException("Invalid money currency code: " + code);
        };
    }

    /**
     * 港币
     */
    public static final MoneyCurrency HKD = new MoneyCurrency(1, "HKD", "港币");

    /**
     * 美元
     */
    public static final MoneyCurrency USD = new MoneyCurrency(2, "USD", "美元");

    /**
     * 人民币
     */
    public static final MoneyCurrency CNY = new MoneyCurrency(3, "CNY", "人民币");

}
