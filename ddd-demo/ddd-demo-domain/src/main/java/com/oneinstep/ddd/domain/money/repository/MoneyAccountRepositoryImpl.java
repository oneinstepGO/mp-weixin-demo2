package com.oneinstep.ddd.domain.money.repository;

import com.oneinstep.ddd.common.exception.DomainException;
import com.oneinstep.ddd.domain.money.aggregate.MoneyAccount;
import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceLog;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceOperation;
import com.oneinstep.ddd.facade.account.AccountFacade;
import com.oneinstep.ddd.facade.account.dto.AccountDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 资金账户仓储
 */
@Component
@Slf4j
public class MoneyAccountRepositoryImpl implements IMoneyAccountRepository {

    @Resource
    private IMoneyBalanceRepository moneyBalanceRepository;

    @Resource
    private IMoneyBalanceOperationRepository moneyBalanceOperationRepository;

    @Resource
    private IMoneyBalanceLogRepository moneyBalanceLogRepository;

    @Resource
    private AccountFacade accountFacade;

    @Override
    public MoneyAccount findByAssetAccountId(Long assetAccountId) {
        AccountDTO accountDTO = accountFacade.getAccountByAssetAccountId(assetAccountId);
        if (accountDTO == null) {
            return null;
        }
        List<MoneyBalance> moneyBalances = moneyBalanceRepository
                .findByAssetAccountId(accountDTO.getAssetAccountId());
        if (CollectionUtils.isEmpty(moneyBalances)) {
            return null;
        }
        return new MoneyAccount(
                accountDTO.getAssetAccountId(),
                accountDTO.getAssetAccountType(),
                moneyBalances);
    }

    @Override
    public Pair<Boolean, Long> save(MoneyAccount moneyAccount) throws DomainException {
        // 要么全部成功 要么全部失败

        if (moneyAccount == null) {
            return Pair.of(false, null);
        }

        if (CollectionUtils.isEmpty(moneyAccount.getMoneyBalanceChgs())) {
            return Pair.of(false, null);
        }

        if (moneyAccount.getOperation() == null) {
            return Pair.of(false, null);
        }

        if (CollectionUtils.isEmpty(moneyAccount.getMoneyBalanceLogs())) {
            return Pair.of(false, null);
        }

        boolean success = moneyBalanceRepository.batchIncrementalUpdate(moneyAccount.getMoneyBalanceChgs());
        if (!success) {
            log.error("更新钱包失败, assetAccountId: {}", moneyAccount.getAssetAccountId());
            return Pair.of(false, null);
        }

        // 持久化操作记录
        MoneyBalanceOperation savedOperation = moneyBalanceOperationRepository.save(moneyAccount.getOperation());
        if (savedOperation == null) {
            log.error("保存操作记录失败, assetAccountId: {}", moneyAccount.getAssetAccountId());
            return Pair.of(false, null);
        }

        // 获取钱包变动日志
        List<MoneyBalanceLog> moneyBalanceLogs = moneyAccount.getMoneyBalanceLogs();

        // 设置操作记录ID
        long index = 0;
        for (MoneyBalanceLog moneyBalanceLog : moneyBalanceLogs) {
            moneyBalanceLog.setFromSourceId(savedOperation.getId());
            moneyBalanceLog.setFromSourceSubId(index++);
        }

        // 批量保存钱包变动日志
        boolean saveLogSuccess = moneyBalanceLogRepository.saveLogBatch(moneyBalanceLogs);
        if (!saveLogSuccess) {
            log.error("保存钱包变动日志失败, assetAccountId: {}", moneyAccount.getAssetAccountId());
            return Pair.of(false, null);
        }

        return Pair.of(true, savedOperation.getId());

    }

}
