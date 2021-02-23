package cj.netos.fission.service;

import cj.netos.fission.ICashierBillService;
import cj.netos.fission.mapper.CashierBillMapper;
import cj.netos.fission.model.CashierBill;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.orm.mybatis.annotation.CjTransaction;

import java.util.List;

@CjBridge(aspects = "@transaction")
@CjService(name = "cashierBillService")
public class CashierBillService implements ICashierBillService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.CashierBillMapper")
    CashierBillMapper cashierBillMapper;

    @CjTransaction
    @Override
    public List<CashierBill> pageBill(String unionid, int limit, int offset) {
        return cashierBillMapper.pageBill(unionid,limit,offset);
    }
}
