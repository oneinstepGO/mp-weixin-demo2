package com.oneinstep.ddd.application.controller;

import com.oneinstep.ddd.api.result.Result;
import com.oneinstep.ddd.api.service.money.MoneyBalanceOperationService;
import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/money/")
public class MoneyBalanceOperationController {

    @Resource
    private MoneyBalanceOperationService moneyBalanceOperationService;

    @PostMapping("deposit")
    public Result<Long> deposit(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                                @Nonnull FromSource fromSource) {
        return moneyBalanceOperationService.deposit(moneyAccountId, money, fromSource);
    }

    @PostMapping("withdraw")
    public Result<Long> withdraw(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                                 @Nonnull FromSource fromSource,
                                 @Nonnull PaymentMethod paymentMethod) {
        return moneyBalanceOperationService.withdraw(moneyAccountId, money, fromSource, paymentMethod);
    }

}
