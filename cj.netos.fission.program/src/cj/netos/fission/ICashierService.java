package cj.netos.fission;

import cj.netos.fission.model.Cashier;

import java.util.List;

public interface ICashierService {
    Cashier getCashier(String unionid);

    List<String> listByRuning(List<String> unionIds);

}
