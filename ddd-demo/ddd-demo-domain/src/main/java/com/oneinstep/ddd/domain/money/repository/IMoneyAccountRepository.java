package com.oneinstep.ddd.domain.money.repository;

import com.oneinstep.ddd.common.exception.DomainException;
import com.oneinstep.ddd.domain.money.aggregate.MoneyAccount;
import org.apache.commons.lang3.tuple.Pair;

public interface IMoneyAccountRepository {

    /**
     * 根据ID查询资金帐户
     *
     * @param assetAccountId 资产帐户ID
     * @return 资金帐户
     */
    MoneyAccount findByAssetAccountId(Long assetAccountId);

    /**
     * 必须在事务中运行
     * 保存资金帐户
     *
     * @param moneyAccount 资金帐户
     * @return 保存结果
     */
    Pair<Boolean, Long> save(MoneyAccount moneyAccount) throws DomainException;

}