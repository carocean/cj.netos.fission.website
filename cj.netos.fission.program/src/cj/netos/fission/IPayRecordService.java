package cj.netos.fission;

import cj.netos.fission.model.PayRecord;

import java.util.List;

public interface IPayRecordService {
    List<PayRecord> pagePayee(String payer, int limit, int offset);

    long totalPayee(String payer);

    Long totalPayer(String payee);

    long totalPayeeAmount(String payer);

    PayRecord getRecordBySn(String record_sn);

    long totalPayerAmountOnToday(String unionid);

    PayRecord getPayRecord(String sn);

    List<PayRecord> pagePayerRecord(String principal, int limit, long offset);

    List<PayRecord> pagePayeeRecord(String principal, int limit, long offset);

    Long totalPayeeOfDay(String principal, String dayTime);

    Long totalPayerOnDay(String principal, String dayTime);

    List<String> pagePayerId(String principal, int limit, long offset);

    List<String> pagePayeeId(String principal, int limit, long offset);
}
