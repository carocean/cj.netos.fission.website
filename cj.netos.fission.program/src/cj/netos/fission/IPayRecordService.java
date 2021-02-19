package cj.netos.fission;

import cj.netos.fission.model.PayRecord;

import java.util.List;

public interface IPayRecordService {
    List<PayRecord> pagePayee(String payer, int limit, int offset);

    long totalPayee(String payer);

    long totalPayeeAmount(String payer);

    PayRecord getRecordBySn(String record_sn);

    long totalPayerAmountOnToday(String unionid);
}
