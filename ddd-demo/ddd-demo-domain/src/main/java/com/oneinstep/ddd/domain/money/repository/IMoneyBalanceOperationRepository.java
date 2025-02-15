package com.oneinstep.ddd.domain.money.repository;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceOperation;

public interface IMoneyBalanceOperationRepository {

    /**
     * 根据来源类型、来源ID、来源子ID查询资金变动记录
     *
     * @param fromSource  来源
     * @return 资金变动记录
     */
    MoneyBalanceOperation findBy3F(FromSource fromSource);

    /**
     * 保存资金变动记录
     *
     * @param moneyBalanceOperation 资金变动记录
     * @return 保存结果
     */
    MoneyBalanceOperation save(MoneyBalanceOperation moneyBalanceOperation);

    void deleteAll();
}
