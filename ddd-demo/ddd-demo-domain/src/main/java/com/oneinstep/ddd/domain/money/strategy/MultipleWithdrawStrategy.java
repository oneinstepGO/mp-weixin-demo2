package com.oneinstep.ddd.domain.money.strategy;

import com.oneinstep.ddd.api.valueobj.ExchangeRate;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.MoneyCurrency;
import com.oneinstep.ddd.common.exception.DomainException;
import com.oneinstep.ddd.common.util.spring.SpringContextUtils;
import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.facade.config.ExchangeRateFacade;
import com.oneinstep.ddd.facade.config.PaymentConfigFacade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 多币种取款策略
 */
@Slf4j
public class MultipleWithdrawStrategy implements WithdrawStrategy {

    /**
     * 资产帐户ID
     */
    private final Long assetAccountId;
    /**
     * 钱包列表
     */
    private final List<MoneyBalance> moneyBalances;

    public MultipleWithdrawStrategy(Long assetAccountId, List<MoneyBalance> moneyBalances) {
        this.assetAccountId = assetAccountId;
        this.moneyBalances = moneyBalances;
    }

    @Override
    public List<MoneyBalance> pay(MoneyAmount money) {

        List<MoneyBalance> needUpdateBalances = new ArrayList<>();

        ExchangeRateFacade exchangeRateFacade = SpringContextUtils.getBean(ExchangeRateFacade.class);
        PaymentConfigFacade paymentConfigFacade = SpringContextUtils.getBean(PaymentConfigFacade.class);

        List<ExchangeRate> exchangeRates = exchangeRateFacade.getExchangeRates();
        List<Integer> configMoneyTypes = paymentConfigFacade.getConfigPaymentCurrencies();
        // 如果是组合支付 查询 组合支付的配置 和 汇率

        if (CollectionUtils.isEmpty(configMoneyTypes)) {
            throw new DomainException("配置币种不能为空");
        }

        if (CollectionUtils.isEmpty(exchangeRates)) {
            throw new DomainException("汇率不能为空");
        }

        // 先算出每个币种需要扣款的金额
        List<MoneyBalance> candidateBalances = new ArrayList<>();

        // 1、将钱包按照配置币种进行排序 没有配置币种的 忽略，只保留配置币种的钱包
        // 2、按配置币种顺序进行扣款 如果余额不足 则将此钱包余额扣除完 剩余金额继续进行下一个钱包的扣款
        // 3、如果所有钱包余额都不充足 则抛出异常
        // 4、其它币种需要扣款的金额，需要进行汇率换算

        configMoneyTypes.forEach(moneyType -> {
            MoneyBalance balance = MoneyBalance.findBalance(moneyBalances, moneyType);
            if (balance != null && balance.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
                candidateBalances.add(balance);
            }

        });

        // 如果候选钱包为空 则抛出异常
        if (candidateBalances.isEmpty()) {
            log.error("候选钱包为空，资产帐户ID：{}", assetAccountId);
            throw new DomainException(
                    String.format("候选钱包为空，资产帐户ID：%d", assetAccountId));
        }

        BigDecimal totalWithdrawAmount = money.amount();
        for (MoneyBalance candidateBalance : candidateBalances) {

            MoneyBalance copy = candidateBalance.copy();
            ExchangeRate exchangeRate = ExchangeRate.findExchangeRate(exchangeRates, money.currency().code(),
                    copy.getMoneyType());

            // 汇率为空 报错
            if (exchangeRate == null) {
                log.error("汇率不存在，资产帐户ID：{}，币种类型：{}", assetAccountId, copy.getMoneyType());
                throw new DomainException(
                        String.format("汇率不存在，资产帐户ID：%d，币种类型：%d", assetAccountId, copy.getMoneyType()));
            }

            // 汇率换算
            BigDecimal withdrawTargetAmount = exchangeRate.rate().multiply(totalWithdrawAmount);

            log.info("需要扣款金额：{}，汇率：{}，目标金额：{}", totalWithdrawAmount, exchangeRate.rate(), withdrawTargetAmount);

            // 比较余额是否充足 余额不充足 此钱包余额全部扣除
            if (copy.getCurrentBalance().compareTo(withdrawTargetAmount) < 0) {

                BigDecimal currentBalance = copy.getCurrentBalance();

                log.info("币种：{}，钱包余额不充足，钱包余额：{}，需要扣款金额：{}", copy.getMoneyType(), currentBalance,
                        withdrawTargetAmount);

                // 余额不充足 此钱包余额全部扣除
                copy.withdraw(new MoneyAmount(MoneyCurrency.of(copy.getMoneyType()), currentBalance));

                // 添加到需要更新余额的钱包列表
                needUpdateBalances.add(copy);

                // 剩余金额 = 剩余金额 - 此钱包余额*汇率
                totalWithdrawAmount = totalWithdrawAmount
                        .subtract(exchangeRate.rate().multiply(currentBalance));

                log.info("资金号：{}，币种：{}，已经扣除余额：{}，还剩余金额：{} {}",
                        assetAccountId,
                        MoneyCurrency.of(copy.getMoneyType()),
                        currentBalance,
                        totalWithdrawAmount,
                        money.currency());
            }
            // 余额充足 余额刚好等于或者大于需要扣款金额
            else {
                copy.withdraw(new MoneyAmount(MoneyCurrency.of(copy.getMoneyType()),
                        withdrawTargetAmount));

                // 添加到需要更新余额的钱包列表
                needUpdateBalances.add(copy);

                log.info("资金号：{}，币种：{}，余额充足，已经扣除余额：{}",
                        assetAccountId,
                        MoneyCurrency.of(copy.getMoneyType()),
                        withdrawTargetAmount);

                // 剩余金额
                totalWithdrawAmount = BigDecimal.ZERO;

                // 结束
                break;
            }

        }

        // 如果剩余金额不为0 则抛出异常
        if (totalWithdrawAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.error("资金号：{}，扣除金额：{}，币种：{}，余额不足，剩余金额：{} {}",
                    assetAccountId,
                    money.amount(),
                    money.currency(),
                    totalWithdrawAmount,
                    money.currency());
            throw new DomainException(
                    String.format("剩余金额不为0，资产帐户ID：%d，剩余金额：%s", assetAccountId, totalWithdrawAmount));
        }

        return Collections.unmodifiableList(needUpdateBalances);
    }

}
