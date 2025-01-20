package com.oneinstep.ddd.domain.money.strategy;

import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.domain.money.entity.MoneyBalance;

import java.util.List;


/**
 * 取款策略
 */
public interface WithdrawStrategy {

    /**
     * 支付
     *
     * @param money 金额
     * @return 支付结果
     */
    List<MoneyBalance> pay(MoneyAmount money);
}
