package cj.netos.fission;

import cj.netos.fission.model.CashierBalance;

import java.util.List;

public interface ICashierBalanceService {

    CashierBalance getBalance(String unionid);

    List<String> listGreaterThan(List<String> unionIds,  long balance);

}
