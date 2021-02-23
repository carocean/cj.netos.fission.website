package cj.netos.fission;

import cj.netos.fission.model.CashierBill;

import java.util.List;

public interface ICashierBillService {
    List<CashierBill> pageBill(String unionid, int limit, int offset);


}
