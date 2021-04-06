package cj.netos.fission.service;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.mapper.CashierBalanceMapper;
import cj.netos.fission.mapper.PayRecordMapper;
import cj.netos.fission.mapper.WithdrawRecordMapper;
import cj.netos.fission.model.PayRecord;
import cj.netos.fission.model.WithdrawRecord;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.orm.mybatis.annotation.CjTransaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@CjBridge(aspects = "@transaction")
@CjService(name = "payRecordService")
public class PayRecordService implements IPayRecordService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.PayRecordMapper")
    PayRecordMapper payRecordMapper;
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.WithdrawRecordMapper")
    WithdrawRecordMapper withdrawRecordMapper;
    @CjTransaction
    @Override
    public List<PayRecord> pagePayee(String payer, int limit, int offset) {
        return payRecordMapper.pagePayee(payer, limit, offset);
    }

    @CjTransaction
    @Override
    public long totalPayee(String payer) {
        return payRecordMapper.totalPayee(payer);
    }

    @CjTransaction
    @Override
    public Long totalPayer(String principal) {
        return payRecordMapper.totalPayer(principal);
    }

    @CjTransaction
    @Override
    public Long totalPayerAmount(String principal) {
        return payRecordMapper.totalPayerAmount(principal);
    }

    @CjTransaction
    @Override
    public long totalPayeeAmount(String payer) {
        return payRecordMapper.sumPayeeAmount(payer);
    }
    @CjTransaction
    @Override
    public long totalCommissionAmount(String principal) {
        return withdrawRecordMapper.totalCommissionAmount(principal);
    }

    @CjTransaction
    @Override
    public long totalPayerAmountOnToday(String payee) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatBegin = new SimpleDateFormat("yyyyMMdd000000000");
        String begin = formatBegin.format(date);
        SimpleDateFormat formatEnd = new SimpleDateFormat("yyyyMMdd235959999");
        String end = formatEnd.format(date);
        return payRecordMapper.totalPayerAmountBetown(payee, begin, end);
    }


    @CjTransaction
    @Override
    public PayRecord getPayRecord(String sn) {
        return payRecordMapper.selectByPrimaryKey(sn);
    }

    @CjTransaction
    @Override
    public List<PayRecord> pagePayerRecord(String payer, int limit, long offset) {
        return payRecordMapper.pagePayerRecord(payer, limit, offset);
    }

    @CjTransaction
    @Override
    public List<PayRecord> pagePayeeRecord(String payee, int limit, long offset) {
        return payRecordMapper.pagePayeeRecord(payee, limit, offset);
    }

    @CjTransaction
    @Override
    public List<String> pagePayerId(String payer, int limit, long offset) {
        return payRecordMapper.pagePayerId(payer, limit, offset);
    }

    @CjTransaction
    @Override
    public List<String> pagePayeeId(String payee, int limit, long offset) {
        return payRecordMapper.pagePayeeId(payee, limit, offset);
    }

    @CjTransaction
    @Override
    public Long totalPayerOnDay(String principal, String dayTime) {
        return payRecordMapper.totalPayerOnDay(principal, dayTime + "%");
    }

    @CjTransaction
    @Override
    public Long totalPayeeOfDay(String principal, String dayTime) {
        return payRecordMapper.totalPayeeOfDay(principal, dayTime + "%");
    }

    @CjTransaction
    @Override
    public PayRecord getRecordBySn(String record_sn) {
        return payRecordMapper.selectByPrimaryKey(record_sn);
    }
}
