package cj.netos.fission.service;

import cj.netos.fission.ICashierService;
import cj.netos.fission.mapper.CashierBalanceMapper;
import cj.netos.fission.mapper.CashierMapper;
import cj.netos.fission.model.Cashier;
import cj.netos.fission.model.CashierExample;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.orm.mybatis.annotation.CjTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@CjBridge(aspects = "@transaction")
@CjService(name = "cashierService")
public class CashierService implements ICashierService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.CashierMapper")
    CashierMapper cashierMapper;

    @CjTransaction
    @Override
    public Cashier getCashier(String unionid) {
        Cashier cashier = cashierMapper.selectByPrimaryKey(unionid);
        if (cashier == null) {
            cashier = new Cashier();
            cashier.setPerson(unionid);
            cashier.setDayAmount(0L);
            cashier.setState(0);
            cashier.setType(0);
            cashier.setCacAverage(30L);
            cashier.setAmplitudeFactor(new BigDecimal("1.6"));
            cashier.setClosedCause(null);
            cashierMapper.insert(cashier);
        }
        return cashier;
    }
    @CjTransaction
    @Override
    public List<String> listByRuning(List<String> unionIds) {
        if (unionIds.isEmpty()) {
            return new ArrayList<>();
        }
//        CJSystem.logging().info(getClass(),String.format("这是错误ids:%s",unionIds));
        CashierExample example = new CashierExample();
        example.createCriteria().andStateEqualTo(0).andPersonIn(unionIds);
        List<Cashier> cashiers = cashierMapper.selectByExample(example);
        List<String> ids = new ArrayList<>();
        for (Cashier cashier : cashiers) {
            ids.add(cashier.getPerson());
        }
        return ids;
    }
}
