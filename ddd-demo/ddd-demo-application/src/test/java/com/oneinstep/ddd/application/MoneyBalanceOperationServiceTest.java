package com.oneinstep.ddd.application;

import com.oneinstep.ddd.api.constants.AssetFromSourceTypeConstant;
import com.oneinstep.ddd.api.result.Result;
import com.oneinstep.ddd.api.service.money.MoneyBalanceOperationService;
import com.oneinstep.ddd.api.valueobj.FromSource;
import com.oneinstep.ddd.api.valueobj.MoneyAmount;
import com.oneinstep.ddd.api.valueobj.MoneyCurrency;
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
import java.util.Arrays;
import java.util.List;

/**
 * use Junit5 to test the MoneyBalanceOperationService
 */
@SpringBootTest
@ContextConfiguration(classes = App.class)
public class MoneyBalanceOperationServiceTest {

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
        // create a MoneyBalance
        MoneyBalance moneyBalance1 = new MoneyBalance();
        moneyBalance1.setMoneyType(1);
        moneyBalance1.setAssetAccountId(1L);
        moneyBalance1.setCurrentBalance(new BigDecimal(0));
        moneyBalance1.setFrozenAmount(new BigDecimal(0));
        moneyBalanceRepository.save(moneyBalance1);

        MoneyBalance moneyBalance2 = new MoneyBalance();
        moneyBalance2.setMoneyType(2);
        moneyBalance2.setAssetAccountId(1L);
        moneyBalance2.setCurrentBalance(new BigDecimal(0));
        moneyBalance2.setFrozenAmount(new BigDecimal(0));

        MoneyBalance moneyBalance3 = new MoneyBalance();
        moneyBalance3.setMoneyType(3);
        moneyBalance3.setAssetAccountId(1L);
        moneyBalance3.setCurrentBalance(new BigDecimal(0));
        moneyBalance3.setFrozenAmount(new BigDecimal(0));

        moneyBalanceRepository.saveBatch(Arrays.asList(moneyBalance1, moneyBalance2, moneyBalance3));
    }

    @Test
    public void testDeposit() {

        FromSource fromSource = FromSource.of(AssetFromSourceTypeConstant.CASH_DEPOSIT, 1L, 0L);
        Result<Long> depositResult = moneyBalanceOperationService.deposit(1L,
                MoneyAmount.of(MoneyCurrency.HKD, new BigDecimal(100)),
                fromSource);
        Assertions.assertTrue(depositResult.isSuccess());

        List<MoneyBalance> moneyBalance = moneyBalanceRepository.findByAssetAccountId(1L);

        MoneyBalance hkdMoneyBalance = moneyBalance.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.HKD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(hkdMoneyBalance);
        Assertions.assertEquals(new BigDecimal(100), hkdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal(0), hkdMoneyBalance.getFrozenAmount());

        MoneyBalance usdMoneyBalance = moneyBalance.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.USD.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(usdMoneyBalance);
        Assertions.assertEquals(new BigDecimal(0), usdMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal(0), usdMoneyBalance.getFrozenAmount());

        MoneyBalance cnyMoneyBalance = moneyBalance.stream().filter(mb -> mb.getMoneyType() == MoneyCurrency.CNY.code())
                .findFirst().orElse(null);
        Assertions.assertNotNull(cnyMoneyBalance);
        Assertions.assertEquals(new BigDecimal(0), cnyMoneyBalance.getCurrentBalance());
        Assertions.assertEquals(new BigDecimal(0), cnyMoneyBalance.getFrozenAmount());

        // asert operation record
        MoneyBalanceOperation moneyBalanceOperation = moneyBalanceOperationRepository.findBy3F(fromSource);
        Assertions.assertNotNull(moneyBalanceOperation);
        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_DEPOSIT, moneyBalanceOperation.getFromSourceType());
        Assertions.assertEquals(1L, moneyBalanceOperation.getFromSourceId());
        Assertions.assertEquals(0L, moneyBalanceOperation.getFromSourceSubId());

        // assert money balance log
        List<MoneyBalanceLog> moneyBalanceLogs = moneyBalanceLogRepository
                .findLogsBy2F(AssetFromSourceTypeConstant.CASH_DEPOSIT, 1L);
        Assertions.assertEquals(1, moneyBalanceLogs.size());

        MoneyBalanceLog moneyBalanceLog = moneyBalanceLogs.get(0);
        Assertions.assertEquals(MoneyCurrency.HKD.code(), moneyBalanceLog.getMoneyType());
        Assertions.assertEquals(1L, moneyBalanceLog.getAssetAccountId());

        Assertions.assertEquals(AssetFromSourceTypeConstant.CASH_DEPOSIT, moneyBalanceLog.getFromSourceType());
        Assertions.assertEquals(1L, moneyBalanceLog.getFromSourceId());
        Assertions.assertEquals(0L, moneyBalanceLog.getFromSourceSubId());

        Assertions.assertEquals(new BigDecimal(0), moneyBalanceLog.getBeforeCurrentBalance());
        Assertions.assertEquals(new BigDecimal(100), moneyBalanceLog.getAfterCurrentBalance());
        Assertions.assertEquals(new BigDecimal(100), moneyBalanceLog.getChgCurrentBalance());

        Assertions.assertEquals(new BigDecimal(0), moneyBalanceLog.getBeforeFrozenAmount());
        Assertions.assertEquals(new BigDecimal(0), moneyBalanceLog.getAfterFrozenAmount());
        Assertions.assertEquals(new BigDecimal(0), moneyBalanceLog.getChgFrozenAmount());
    }
}
