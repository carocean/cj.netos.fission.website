package cj.netos.fission.service;

import cj.netos.fission.ICashierBalanceService;
import cj.netos.fission.mapper.CashierBalanceMapper;
import cj.netos.fission.model.CashierBalance;
import cj.netos.fission.model.CashierBalanceExample;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.orm.mybatis.annotation.CjTransaction;

import java.util.ArrayList;
import java.util.List;

@CjBridge(aspects = "@transaction")
@CjService(name = "cashierBalanceService")
public class CashierBalanceService implements ICashierBalanceService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.CashierBalanceMapper")
    CashierBalanceMapper cashierBalanceMapper;

    @CjTransaction
    @Override
    public CashierBalance getBalance(String unionid) {
        CashierBalance balance = cashierBalanceMapper.selectByPrimaryKey(unionid);
        if (balance == null) {
            balance = new CashierBalance();
            balance.setBalance(0L);
            balance.setPerson(unionid);
            cashierBalanceMapper.insert(balance);
        }
        return balance;
    }

    @CjTransaction
    @Override
    public List<String> listGreaterThan(List<String> unionIds, long balance) {
        if (unionIds.isEmpty()) {
            return new ArrayList<>();
        }
        CashierBalanceExample example = new CashierBalanceExample();
        example.createCriteria().andPersonIn(unionIds).andBalanceGreaterThan(balance);
        List<CashierBalance> balances = cashierBalanceMapper.selectByExample(example);
        List<String> ids = new ArrayList<>();
        for (CashierBalance cb : balances) {
            ids.add(cb.getPerson());
        }
        return ids;
    }
}
