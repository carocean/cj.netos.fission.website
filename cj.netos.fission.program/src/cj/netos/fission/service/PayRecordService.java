package cj.netos.fission.service;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.mapper.CashierBalanceMapper;
import cj.netos.fission.mapper.PayRecordMapper;
import cj.netos.fission.model.PayRecord;
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
    public long totalPayeeAmount(String payer) {
        return payRecordMapper.sumPayeeAmount(payer);
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
    public PayRecord getRecordBySn(String record_sn) {
        return payRecordMapper.selectByPrimaryKey(record_sn);
    }
}
