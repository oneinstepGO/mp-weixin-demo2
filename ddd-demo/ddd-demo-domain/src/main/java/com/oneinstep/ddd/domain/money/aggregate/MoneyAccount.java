package com.oneinstep.ddd.domain.money.aggregate;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import com.oneinstep.ddd.common.exception.DomainException;
import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceLog;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceOperation;
import com.oneinstep.ddd.domain.money.exception.MoneyBalanceNotExistException;
import com.oneinstep.ddd.domain.money.strategy.MultipleWithdrawStrategy;
import com.oneinstep.ddd.domain.money.strategy.SingleWithdrawStrategy;
import com.oneinstep.ddd.domain.money.strategy.WithdrawStrategy;
import com.oneinstep.ddd.domain.money.valueobj.MoneyBalanceChg;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 资产帐户聚合根
 */
@Slf4j
@Getter
@ToString
public class MoneyAccount {

    /**
     * 资产账户ID
     */
    @Getter
    private final Long assetAccountId;

    /**
     * 账户类型
     */
    @Getter
    private final int assetAccountType;

    /**
     * 钱包列表
     */
    private final List<MoneyBalance> moneyBalances;

    /**
     * 需要更新的钱包列表
     */
    @Getter
    private List<MoneyBalanceChg> moneyBalanceChgs = new ArrayList<>();

    /**
     * 操作记录
     */
    @Getter
    private MoneyBalanceOperation operation;

    /**
     * 钱包流水
     */
    @Getter
    private List<MoneyBalanceLog> moneyBalanceLogs = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param assetAccountId 资金账户ID
     * @param moneyBalances  钱包列表
     */
    public MoneyAccount(Long assetAccountId,
                        int assetAccountType,
                        List<MoneyBalance> moneyBalances) {
        this.assetAccountId = assetAccountId;
        this.assetAccountType = assetAccountType;
        // 资金帐户现有钱包列表 不允许修改
        this.moneyBalances = Collections.unmodifiableList(moneyBalances);
    }

    /**
     * 存入资金
     *
     * @param money 存入金额
     */
    public void deposit(MoneyAmount money, FromSource fromSource) {
        MoneyBalance balance = findBalance(money.currency().code());
        if (balance == null) {
            log.error("钱包不存在，资产帐户ID：{}，币种类型：{}", assetAccountId, money.currency().code());
            throw new MoneyBalanceNotExistException(
                    String.format("钱包不存在，资产帐户ID：%d，币种类型：%d", assetAccountId, money.currency().code()));
        }

        MoneyBalance copy = balance.copy();

        // 存入
        copy.deposit(money);

        // 构建钱包变动对象
        MoneyBalanceChg chg = buildMoneyBalanceChg(copy, balance);
        if (chg == null) {
            log.error("钱包变动对象为空，资产帐户ID：{}，币种类型：{}", assetAccountId, money.currency().code());
            throw new DomainException(
                    String.format("钱包变动对象为空，资产帐户ID：%d，币种类型：%d", assetAccountId, money.currency().code()));
        }

        // 需要更新余额的钱包列表
        this.moneyBalanceChgs.add(chg);

        // 创建操作记录和日志
        this.operation = MoneyBalanceOperation.newDepositOperation(assetAccountId, balance.getMoneyType(), money,
                fromSource);

        // 钱包流水
        // 有可能产生一条流水，有可能产生多条流水
        MoneyBalanceLog moneyBalanceLog = MoneyBalanceLog.createMoneyBalanceLog(copy,
                findBalance(balance.getMoneyType()),
                fromSource.fromSourceType());

        // 添加流水
        this.moneyBalanceLogs.add(moneyBalanceLog);
    }

    /**
     * 取现
     *
     * @param money         取现金额
     * @param paymentMethod 支付方式
     * @param fromSource    业务来源
     */
    public void withdraw(MoneyAmount money, PaymentMethod paymentMethod, FromSource fromSource) {

        WithdrawStrategy paymentStrategy;
        // 处理组合支付
        if (paymentMethod.multipleCurrency()) {
            paymentStrategy = new MultipleWithdrawStrategy(assetAccountId, moneyBalances);
        } else {
            paymentStrategy = new SingleWithdrawStrategy(assetAccountId, moneyBalances);
        }

        // 支付
        List<MoneyBalance> needUpdateBalances = paymentStrategy.pay(money);

        // 构建钱包变动对象
        this.moneyBalanceChgs = needUpdateBalances.stream()
                .map(copy -> buildMoneyBalanceChg(copy, findBalance(copy.getMoneyType())))
                .collect(Collectors.toList());

        log.info("需要更新的钱包变动对象：{}", this.moneyBalanceChgs);

        // 创建操作记录和日志
        this.operation = MoneyBalanceOperation.newWithdrawOperation(assetAccountId, money, paymentMethod,
                fromSource);

        // 钱包流水
        this.moneyBalanceLogs = needUpdateBalances.stream()
                .map(copy -> MoneyBalanceLog.createMoneyBalanceLog(copy, findBalance(copy.getMoneyType()),
                        fromSource.fromSourceType()))
                .collect(Collectors.toList());

    }

    /**
     * 查找指定币种的钱包
     *
     * @param moneyType 币种类型
     * @return 钱包
     */
    private MoneyBalance findBalance(int moneyType) {
        return moneyBalances.stream()
                .filter(balance -> balance.getMoneyType() == moneyType)
                .findFirst()
                .orElse(null);
    }

    /**
     * 构建钱包变动对象
     *
     * @param newBalance 新钱包
     * @param oldBalance 旧钱包
     * @return 钱包变动对象
     */
    private MoneyBalanceChg buildMoneyBalanceChg(MoneyBalance newBalance, MoneyBalance oldBalance) {
        if (newBalance == null || oldBalance == null) {
            return null;
        }
        if (newBalance.getMoneyType() != oldBalance.getMoneyType()) {
            log.error("币种不一致，新钱包：{}，旧钱包：{}", newBalance.getMoneyType(), oldBalance.getMoneyType());
            return null;
        }

        if (!Objects.equals(newBalance.getId(), oldBalance.getId())) {
            log.error("钱包ID不一致，新钱包：{}，旧钱包：{}", newBalance.getId(), oldBalance.getId());
            return null;
        }

        MoneyBalanceChg chg = new MoneyBalanceChg();
        chg.setId(newBalance.getId());
        chg.setVersion(newBalance.getVersion());
        if (newBalance.getCurrentBalance().compareTo(oldBalance.getCurrentBalance()) != 0) {
            chg.setChgCurrentBalance(newBalance.getCurrentBalance().subtract(oldBalance.getCurrentBalance()));
        } else {
            chg.setChgCurrentBalance(BigDecimal.ZERO);
        }
        if (newBalance.getFrozenAmount().compareTo(oldBalance.getFrozenAmount()) != 0) {
            chg.setChgFrozenAmount(newBalance.getFrozenAmount().subtract(oldBalance.getFrozenAmount()));
        } else {
            chg.setChgFrozenAmount(BigDecimal.ZERO);
        }

        // 如果变动金额都为 0 则返回 null
        if (chg.getChgCurrentBalance().compareTo(BigDecimal.ZERO) == 0
                && chg.getChgFrozenAmount().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return chg;
    }
}
