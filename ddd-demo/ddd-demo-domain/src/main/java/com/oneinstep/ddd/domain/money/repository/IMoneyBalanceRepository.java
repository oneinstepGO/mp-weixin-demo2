package com.oneinstep.ddd.domain.money.repository;

import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.domain.money.valueobj.MoneyBalanceChg;

import java.util.List;

public interface IMoneyBalanceRepository {

    /**
     * 根据资产帐户ID查询资金余额
     *
     * @param assetAccountId 资产帐户ID
     * @return 资金余额列表
     */
    List<MoneyBalance> findByAssetAccountId(Long assetAccountId);

    /**
     * 批量增量更新资金余额
     *
     * @param moneyBalanceChgs 资金余额变动列表
     * @return 更新结果
     */
    boolean batchIncrementalUpdate(List<MoneyBalanceChg> moneyBalanceChgs);

    /**
     * 保存资金余额
     *
     * @param moneyBalance 资金余额
     */
    MoneyBalance save(MoneyBalance moneyBalance);

    /**
     * 批量保存资金余额
     *
     * @param moneyBalances 资金余额列表
     * @return 保存结果
     */
    boolean saveBatch(List<MoneyBalance> moneyBalances);
}
