package com.oneinstep.ddd.application.service;

import com.oneinstep.ddd.api.result.Result;
import com.oneinstep.ddd.api.service.money.MoneyBalanceOperationService;
import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import com.oneinstep.ddd.domain.money.service.MoneyAccountService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 应用层服务
 * 不包含任务业务
 * 只包含领域模型操作
 */
@DubboService
@Slf4j
public class MoneyBalanceOperationServiceImpl implements MoneyBalanceOperationService {

    // 资金领域服务
    @Resource
    private MoneyAccountService moneyAccountService;

    @Override
    public Result<Long> deposit(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                                @Nonnull FromSource fromSource) {
        Pair<Boolean, Long> pair = moneyAccountService.deposit(moneyAccountId, money, fromSource);

        if (Boolean.TRUE.equals(pair.getLeft())) {
            return Result.success(pair.getRight());
        }
        return Result.error("充值失败");
    }

    @Override
    public Result<Long> withdraw(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                                 @Nonnull FromSource fromSource, @Nonnull PaymentMethod paymentMethod) {
        Pair<Boolean, Long> pair = moneyAccountService.withdraw(moneyAccountId, money, paymentMethod, fromSource);

        if (Boolean.TRUE.equals(pair.getLeft())) {
            return Result.success(pair.getRight());
        }
        return Result.error("提现失败");
    }

}
