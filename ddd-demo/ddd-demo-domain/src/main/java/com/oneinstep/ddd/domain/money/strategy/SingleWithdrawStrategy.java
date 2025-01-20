package com.oneinstep.ddd.domain.money.strategy;

import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.common.exception.DomainException;
import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 单币种取款策略
 */
@Slf4j
public class SingleWithdrawStrategy implements WithdrawStrategy {

    /**
     * 资产帐户ID
     */
    private final Long assetAccountId;
    /**
     * 钱包列表
     */
    private final List<MoneyBalance> moneyBalances;

    public SingleWithdrawStrategy(Long assetAccountId, List<MoneyBalance> moneyBalances) {
        this.assetAccountId = assetAccountId;
        this.moneyBalances = moneyBalances;
    }

    @Override
    public List<MoneyBalance> pay(MoneyAmount money) {
        // 单币种支付
        MoneyBalance balance = MoneyBalance.findBalance(moneyBalances, money.currency().code());
        if (balance == null) {
            log.error("钱包不存在，资产帐户ID：{}，币种类型：{}", assetAccountId, money.currency().code());
            throw new DomainException(
                    String.format("钱包不存在，资产帐户ID：%d，币种类型：%d", assetAccountId, money.currency().code()));
        }

        MoneyBalance copy = balance.copy();

        // 取款
        copy.withdraw(money);

        // 添加到需要更新余额的钱包列表
        return List.of(copy);
    }

}
