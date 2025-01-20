package com.oneinstep.ddd.domain.money.service;

import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import com.oneinstep.ddd.common.util.lock.IDistributedLock;
import com.oneinstep.ddd.common.util.publisher.DomainEventPublisher;
import com.oneinstep.ddd.domain.money.aggregate.MoneyAccount;
import com.oneinstep.ddd.domain.money.constant.MoneyBalanceOperationType;
import com.oneinstep.ddd.domain.money.event.MoneyBalanceChgEvent;
import com.oneinstep.ddd.domain.money.repository.IMoneyAccountRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金领域服务
 */
@Slf4j
@Service
public class MoneyAccountService {

    @Resource
    private IMoneyAccountRepository moneyAccountRepository;
    @Resource
    private DomainEventPublisher domainEventPublisher;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IDistributedLock<Pair<Boolean, Long>> distributedLock;

    /**
     * 存款
     *
     * @param moneyAccountId 资金账户ID
     * @param money          金额
     * @param fromSource     来源
     * @return Pair<Boolean, Long> 存款是否成功，操作记录ID
     */
    public Pair<Boolean, Long> deposit(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                                       @Nonnull FromSource fromSource) {
        // 参数校验
        if (money.amount() == null || money.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("参数错误，资金帐户ID：{}，金额：{}", moneyAccountId, money);
            return Pair.of(false, null);
        }

        // 使用分布式锁保证并发安全
        String lockKey = String.format("money_account_lock_%d_%d", moneyAccountId, money.currency().code());
        return distributedLock.lock(lockKey, () -> {
            try {

                // 获取聚合根
                MoneyAccount moneyAccount = moneyAccountRepository.findByAssetAccountId(moneyAccountId);
                if (moneyAccount == null) {
                    log.error("资金帐户不存在，资金帐户ID：{}", moneyAccountId);
                    return Pair.of(false, null);
                }

                // 调用聚合根方法
                moneyAccount.deposit(money, fromSource);

                return transactionTemplate.execute(status -> {
                    // 持久化领域模型
                    Pair<Boolean, Long> saveResult = moneyAccountRepository.save(moneyAccount);
                    if (Boolean.FALSE.equals(saveResult.getLeft())) {
                        status.setRollbackOnly();
                        return saveResult;
                    }
                    // 发布领域事件
                    publishCurrentBalanceChgEvent(moneyAccountId,
                            MoneyBalanceOperationType.INCREASE.getCode(),
                            money,
                            saveResult.getRight(), fromSource);
                    return saveResult;
                });

            } catch (Exception e) {
                log.error("存入资金失败，资金帐户ID：{}，金额：{}", moneyAccountId, money, e);
                return Pair.of(false, null);
            }
        });
    }

    /**
     * 取现
     *
     * @param moneyAccountId 资金账户ID
     * @param money          金额
     * @param fromSource     来源
     * @return Pair<Boolean, Long> 取现是否成功，操作记录ID
     */
    public Pair<Boolean, Long> withdraw(@Nonnull Long moneyAccountId, @Nonnull MoneyAmount money,
                                        @Nonnull PaymentMethod paymentMethod,
                                        @Nonnull FromSource fromSource) {
        // 参数校验
        if (money.amount() == null || money.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("参数错误，资金帐户ID：{}，金额：{}", moneyAccountId, money);
            return Pair.of(false, null);
        }

        // 使用分布式锁保证并发安全
        String lockKey = String.format("money_account_lock_%d_%d", moneyAccountId, money.currency().code());
        return distributedLock.lock(lockKey, () -> {
            try {

                // 获取聚合根
                MoneyAccount moneyAccount = moneyAccountRepository.findByAssetAccountId(moneyAccountId);
                if (moneyAccount == null) {
                    log.error("资金帐户不存在，资金帐户ID：{}", moneyAccountId);
                    return Pair.of(false, null);
                }

                // 调用聚合根方法
                moneyAccount.withdraw(money, paymentMethod, fromSource);

                return transactionTemplate.execute(transactionStatus -> {

                    // 持久化领域模型
                    Pair<Boolean, Long> saveResult = moneyAccountRepository.save(moneyAccount);
                    if (Boolean.FALSE.equals(saveResult.getLeft())) {
                        transactionStatus.setRollbackOnly();
                        return saveResult;
                    }
                    // 发布领域事件
                    publishCurrentBalanceChgEvent(moneyAccountId,
                            MoneyBalanceOperationType.DECREASE.getCode(),
                            money,
                            saveResult.getRight(), fromSource);
                    return saveResult;
                });

            } catch (Exception e) {
                log.error("取现失败，资金帐户ID：{}，金额：{}", moneyAccountId, money, e);
                return Pair.of(false, null);
            }
        });
    }

    /**
     * 发布当前余额变更事件
     *
     * @param money          金额
     * @param operationLogId 操作日志ID
     * @param operationType  操作类型
     */
    public void publishCurrentBalanceChgEvent(Long moneyAccountId,
                                              int operationType,
                                              MoneyAmount money,
                                              Long operationLogId,
                                              FromSource fromSource) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                domainEventPublisher.publish(MoneyBalanceChgEvent.builder()
                        // 事件ID
                        .id(System.currentTimeMillis())
                        .assetAccountId(moneyAccountId)
                        .moneyType(money.currency().code())
                        .operationId(operationLogId)
                        .operationType(operationType)
                        .fromSourceType(fromSource.fromSourceType())
                        .fromSourceId(fromSource.fromSourceId())
                        .fromSourceSubId(fromSource.fromSourceSubId())
                        .chgCurrentBalance(operationType == MoneyBalanceOperationType.INCREASE.getCode()
                                ? money.amount()
                                : money.amount().negate())
                        .chgFrozenAmount(BigDecimal.ZERO)
                        .operationTime(LocalDateTime.now())
                        .build());
            }
        });
    }

}
