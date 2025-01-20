package com.oneinstep.ddd.api.service.money;

import com.oneinstep.ddd.api.result.Result;
import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import jakarta.annotation.Nonnull;

public interface MoneyBalanceOperationService {

    /**
     * 存入
     *
     * @param moneyAccountId 钱包ID
     * @param money          金额
     * @param fromSource     来源
     * @return Pair<Boolean, Long> 存入是否成功，操作记录ID
     */
    Result<Long> deposit(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                         @Nonnull FromSource fromSource);

    /**
     * 提现
     *
     * @param moneyAccountId 钱包ID
     * @param money          金额
     * @param fromSource     来源
     * @return Pair<Boolean, Long> 提现是否成功，操作记录ID
     */
    Result<Long> withdraw(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                          @Nonnull FromSource fromSource, @Nonnull PaymentMethod paymentMethod);

}
