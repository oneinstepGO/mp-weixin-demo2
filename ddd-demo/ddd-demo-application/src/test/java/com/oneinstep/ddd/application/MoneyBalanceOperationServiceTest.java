package com.oneinstep.ddd.application;

import com.oneinstep.ddd.api.constants.AssetFromSourceTypeConstant;
import com.oneinstep.ddd.api.result.Result;
import com.oneinstep.ddd.api.service.money.MoneyBalanceOperationService;
import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.MoneyCurrency;
import com.oneinstep.ddd.api.valueobj.PaymentMethod;
import com.oneinstep.ddd.domain.money.entity.MoneyBalance;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceLog;
import com.oneinstep.ddd.domain.money.entity.MoneyBalanceOperation;
import com.oneinstep.ddd.domain.money.repository.IMoneyBalanceLogRepository;
import com.oneinstep.ddd.domain.money.repository.IMoneyBalanceOperationRepository;
import com.oneinstep.ddd.domain.money.repository.IMoneyBalanceRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.oneinstep.ddd.domain.money.constant.MoneyBalanceFlagBitConstants.DECREASE_CURRENT_BALANCE;
import static com.oneinstep.ddd.domain.money.constant.MoneyBalanceFlagBitConstants.INCREASE_CURRENT_BALANCE;

/**
 * use Junit5 to test the MoneyBalanceOperationService
 */
@SpringBootTest
@ContextConfiguration(classes = App.class)
class MoneyBalanceOperationServiceTest {

    @Resource
    private MoneyBalanceOperationService moneyBalanceOperationService;

    @Resource
    private IMoneyBalanceRepository moneyBalanceRepository;

    @Resource
    private IMoneyBalanceOperationRepository moneyBalanceOperationRepository;

    @Resource
    private IMoneyBalanceLogRepository moneyBalanceLogRepository;

    @BeforeEach
    public void setUp() {
        // Clear all data before each test
        moneyBalanceRepository.deleteAll();
        moneyBalanceOperationRepository.deleteAll();
        moneyBalanceLogRepository.deleteAll();

        // create a MoneyBalance
        MoneyBalance moneyBalance1 = new MoneyBalance();
        moneyBalance1.setMoneyType(1);
        moneyBalance1.setAssetAccountId(1L);
        moneyBalance1.setCurrentBalance(new BigDecimal("0.000000"));
        moneyBalance1.setFrozenAmount(new BigDecimal("0.000000"));
        moneyBalance1.setVersion(0L);
        moneyBalance1.setCreateTime(LocalDateTime.now());
        moneyBalance1.setUpdateTime(LocalDateTime.now());

        MoneyBalance moneyBalance2 = new MoneyBalance();
        moneyBalance2.setMoneyType(2);
        moneyBalance2.setAssetAccountId(1L);
        moneyBalance2.setCurrentBalance(new BigDecimal("100.000000"));
        moneyBalance2.setFrozenAmount(new BigDecimal("0.000000"));
        moneyBalance2.setVersion(0L);
        moneyBalance2.setCreateTime(LocalDateTime.now());
        moneyBalance2.setUpdateTime(LocalDateTime.now());

        MoneyBalance moneyBalance3 = new MoneyBalance();
        moneyBalance3.setMoneyType(3);
        moneyBalance3.setAssetAccountId(1L);
        moneyBalance3.setCurrentBalance(new BigDecimal("1000.000000"));
        moneyBalance3.setFrozenAmount(new BigDecimal("0.000000"));
        moneyBalance3.setVersion(0L);
        moneyBalance3.setCreateTime(LocalDateTime.now());
        moneyBalance3.setUpdateTime(LocalDateTime.now());

        moneyBalanceRepository.saveBatch(Arrays.asList(moneyBalance1, moneyBalance2, moneyBalance3));
    }

    @Test
    void testDeposit() {
        // 存入100港币
        FromSource fromSource = new FromSource(AssetFromSourceTypeConstant.CASH_DEPOSIT, 1L, 0L);
        Result<Long> result = moneyBalanceOperationService
                .deposit(1L, new MoneyAmount(MoneyCurrency.HKD, new BigDecimal("100")), fromSource);

        Assertions.assertTrue(result.isSuccess());

        // 验证余额
        List<MoneyBalance> moneyBalances = moneyBalanceRepository.findByAssetAccountId(1L);
        assertDepositMoneyBalance(moneyBalances);

        // 验证操作记录
        Long operationId = assertDepositMoneyBalanceOperation(fromSource);

        // 验证日志
        assertDepositMoneyBalanceLog(operationId);
    }

    @Test
    void testWithdraw() {
        FromSource fromSource = FromSource.of(AssetFromSourceTypeConstant.CASH_WITHDRAW, 1L, 0L);
        Result<Long> withdraw = moneyBalanceOperationService.withdraw(1L,
                MoneyAmount.of(MoneyCurrency.USD, new BigDecimal(100)),
                fromSource, PaymentMethod.SINGLE_CURRENCY);
        Assertions.assertTrue(withdraw.isSuccess());

        List<MoneyBalance> moneyBalance = moneyBalanceRepository.findByAssetAccountId(1L);

        assertWithdrawMoneyBalance(moneyBalance);

        // assert operation record
        Long operationId = assertWithdrawMoneyBalanceOperation(fromSource);

        // assert money balance log
        assertWithdrawMoneyBalanceLog(operationId);
    }

    @Test
    void testWithdrawMultipleCurrency() {
        final long accountId = 1L;
        FromSource fromSource = FromSource.of(AssetFromSourceTypeConstant.CASH_WITHDRAW, 1L, 0L);
        Result<Long> withdraw = moneyBalanceOperationService.withdraw(accountId,
                MoneyAmount.of(MoneyCurrency.USD, new BigDecimal(120)),
                fromSource, PaymentMethod.MULTIPLE_CURRENCY);
        Assertions.assertTrue(withdraw.isSuccess());

        List<MoneyBalance> moneyBalance = moneyBalanceRepository.findByAssetAccountId(accountId);

        assertWithdrawMoneyBalanceMulCurrency(moneyBalance);

        // assert operation record
        Long operationId = assertWithdrawMoneyBalanceOperation(fromSource);

        // assert money balance log
        assertWithdrawMoneyBalanceLogMulCurrency(operationId);
    }

    private void assertWithdrawMoneyBalanceLog(Long operationId) {
        List<MoneyBalanceLog> moneyBalanceLogs = moneyBalanceLogRepository
                .findLogsBy2F(AssetFromSourceTypeConstant.CASH_WITHDRAW, operationId);
        Assertions.assertEquals(1, moneyBalanceLogs.size());

        MoneyBalanceLog moneyBalanceLog = moneyBalanceLogs.getFirst();
        Assertions.assertEquals(MoneyCurrency.USD.code(), moneyBalanceLog.getMoneyType());
        Assertions.assertEquals(1L, moneyBalanceLog.getAssetAccountId());

        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_WITHDRAW, moneyBalanceLog.getFromSourceType());
        Assertions.assertEquals(operationId, moneyBalanceLog.getFromSourceId());
        Assertions.assertEquals(0L, moneyBalanceLog.getFromSourceSubId());

        Assertions.assertEquals(new BigDecimal("100.000000"), moneyBalanceLog.getBeforeCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getAfterCurrentBalance());
        Assertions.assertEquals(new BigDecimal("-100.000000"), moneyBalanceLog.getChgCurrentBalance());

        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getBeforeFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getAfterFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getChgFrozenAmount());

        Assertions.assertEquals(DECREASE_CURRENT_BALANCE, moneyBalanceLog.getFlagBit());
    }

    private void assertWithdrawMoneyBalanceLogMulCurrency(Long operationId) {
        List<MoneyBalanceLog> moneyBalanceLogs = moneyBalanceLogRepository
                .findLogsBy2F(AssetFromSourceTypeConstant.CASH_WITHDRAW, operationId);
        Assertions.assertEquals(2, moneyBalanceLogs.size());

        MoneyBalanceLog moneyBalanceLog = moneyBalanceLogs.getFirst();
        Assertions.assertEquals(MoneyCurrency.USD.code(), moneyBalanceLog.getMoneyType());
        Assertions.assertEquals(1L, moneyBalanceLog.getAssetAccountId());

        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_WITHDRAW, moneyBalanceLog.getFromSourceType());
        Assertions.assertEquals(operationId, moneyBalanceLog.getFromSourceId());
        Assertions.assertEquals(0L, moneyBalanceLog.getFromSourceSubId());

        Assertions.assertEquals(new BigDecimal("100.000000"), moneyBalanceLog.getBeforeCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getAfterCurrentBalance());
        Assertions.assertEquals(new BigDecimal("-100.000000"), moneyBalanceLog.getChgCurrentBalance());

        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getBeforeFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getAfterFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getChgFrozenAmount());

        Assertions.assertEquals(DECREASE_CURRENT_BALANCE, moneyBalanceLog.getFlagBit());

        MoneyBalanceLog moneyBalanceLog2 = moneyBalanceLogs.getLast();
        Assertions.assertEquals(MoneyCurrency.CNY.code(), moneyBalanceLog2.getMoneyType());
        Assertions.assertEquals(1L, moneyBalanceLog2.getAssetAccountId());

        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_WITHDRAW, moneyBalanceLog2.getFromSourceType());
        Assertions.assertEquals(operationId, moneyBalanceLog2.getFromSourceId());
        Assertions.assertEquals(1L, moneyBalanceLog2.getFromSourceSubId());

        Assertions.assertEquals(new BigDecimal("1000.000000"), moneyBalanceLog2.getBeforeCurrentBalance());
        Assertions.assertEquals(new BigDecimal("856.400000"), moneyBalanceLog2.getAfterCurrentBalance());
        Assertions.assertEquals(new BigDecimal("-143.600000"), moneyBalanceLog2.getChgCurrentBalance());

        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog2.getBeforeFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog2.getAfterFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog2.getChgFrozenAmount());

        Assertions.assertEquals(DECREASE_CURRENT_BALANCE, moneyBalanceLog2.getFlagBit());
    }

    private Long assertWithdrawMoneyBalanceOperation(FromSource fromSource) {
        MoneyBalanceOperation moneyBalanceOperation = moneyBalanceOperationRepository.findBy3F(fromSource);
        Assertions.assertNotNull(moneyBalanceOperation);
        Assertions.assertEquals(fromSource.fromSourceType(), moneyBalanceOperation.getFromSourceType());
        Assertions.assertEquals(fromSource.fromSourceId(), moneyBalanceOperation.getFromSourceId());
        Assertions.assertEquals(fromSource.fromSourceSubId(), moneyBalanceOperation.getFromSourceSubId());

        return moneyBalanceOperation.getId();
    }

    private void assertWithdrawMoneyBalance(List<MoneyBalance> moneyBalances) {
        MoneyBalance hkdMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.HKD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(hkdMoneyBalance);
        Assertions.assertEquals(new BigDecimal("0.000000"), hkdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), hkdMoneyBalance.getFrozenAmount());

        // HKD -> USD -> CNY   -100USD
        // 0      100    1000
        // 0       0     1000
        MoneyBalance usdMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.USD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(usdMoneyBalance);
        Assertions.assertEquals(new BigDecimal("0.000000"), usdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), usdMoneyBalance.getFrozenAmount());

        MoneyBalance cnyMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.CNY.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(cnyMoneyBalance);
        Assertions.assertEquals(new BigDecimal("1000.000000"), cnyMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), cnyMoneyBalance.getFrozenAmount());
    }

    private void assertWithdrawMoneyBalanceMulCurrency(List<MoneyBalance> moneyBalances) {
        MoneyBalance hkdMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.HKD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(hkdMoneyBalance);
        Assertions.assertEquals(new BigDecimal("0.000000"), hkdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), hkdMoneyBalance.getFrozenAmount());

        // HKD -> USD            -> CNY   -120USD
        // 0      100               1000
        // 0      100 - 100 = 0     1000 - (120 - 100) * 7.18 = 1000 - 143.60 = 856.40 CNY
        MoneyBalance usdMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.USD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(usdMoneyBalance);
        Assertions.assertEquals(new BigDecimal("0.000000"), usdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), usdMoneyBalance.getFrozenAmount());

        MoneyBalance cnyMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.CNY.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(cnyMoneyBalance);
        Assertions.assertEquals(new BigDecimal("856.400000"), cnyMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), cnyMoneyBalance.getFrozenAmount());

    }

    private void assertDepositMoneyBalanceLog(Long operationId) {
        List<MoneyBalanceLog> moneyBalanceLogs = moneyBalanceLogRepository
                .findLogsBy2F(AssetFromSourceTypeConstant.CASH_DEPOSIT, operationId);
        Assertions.assertEquals(1, moneyBalanceLogs.size());

        MoneyBalanceLog moneyBalanceLog = moneyBalanceLogs.getFirst();
        Assertions.assertEquals(MoneyCurrency.HKD.code(), moneyBalanceLog.getMoneyType());
        Assertions.assertEquals(1L, moneyBalanceLog.getAssetAccountId());

        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_DEPOSIT, moneyBalanceLog.getFromSourceType());
        Assertions.assertEquals(operationId, moneyBalanceLog.getFromSourceId());
        Assertions.assertEquals(0L, moneyBalanceLog.getFromSourceSubId());

        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getBeforeCurrentBalance());
        Assertions.assertEquals(new BigDecimal("100.000000"), moneyBalanceLog.getAfterCurrentBalance());
        Assertions.assertEquals(new BigDecimal("100.000000"), moneyBalanceLog.getChgCurrentBalance());

        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getBeforeFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getAfterFrozenAmount());
        Assertions.assertEquals(new BigDecimal("0.000000"), moneyBalanceLog.getChgFrozenAmount());

        Assertions.assertEquals(INCREASE_CURRENT_BALANCE, moneyBalanceLog.getFlagBit());
    }

    private Long assertDepositMoneyBalanceOperation(FromSource fromSource) {
        MoneyBalanceOperation moneyBalanceOperation = moneyBalanceOperationRepository.findBy3F(fromSource);
        Assertions.assertNotNull(moneyBalanceOperation);
        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_DEPOSIT, moneyBalanceOperation.getFromSourceType());
        Assertions.assertEquals(1L, moneyBalanceOperation.getFromSourceId());
        Assertions.assertEquals(0L, moneyBalanceOperation.getFromSourceSubId());

        return moneyBalanceOperation.getId();
    }

    private static void assertDepositMoneyBalance(List<MoneyBalance> moneyBalances) {
        MoneyBalance hkdMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.HKD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(hkdMoneyBalance);
        Assertions.assertEquals(new BigDecimal("100.000000"), hkdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), hkdMoneyBalance.getFrozenAmount());

        MoneyBalance usdMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.USD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(usdMoneyBalance);
        Assertions.assertEquals(new BigDecimal("100.000000"), usdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), usdMoneyBalance.getFrozenAmount());

        MoneyBalance cnyMoneyBalance = moneyBalances.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.CNY.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(cnyMoneyBalance);
        Assertions.assertEquals(new BigDecimal("1000.000000"), cnyMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal("0.000000"), cnyMoneyBalance.getFrozenAmount());
    }
}
