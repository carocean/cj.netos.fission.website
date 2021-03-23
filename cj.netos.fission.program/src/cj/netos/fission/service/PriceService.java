package cj.netos.fission.service;

import cj.netos.fission.IPriceService;
import cj.netos.fission.mapper.BusinessIncomeRatioMapper;
import cj.netos.fission.mapper.ServicePriceMapper;
import cj.netos.fission.model.BusinessIncomeRatio;
import cj.netos.fission.model.BusinessIncomeRatioExample;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.orm.mybatis.annotation.CjTransaction;

import java.math.BigDecimal;
import java.util.List;

@CjBridge(aspects = "@transaction")
@CjService(name = "priceService")
public class PriceService implements IPriceService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.ServicePriceMapper")
    ServicePriceMapper servicePriceMapper;
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.BusinessIncomeRatioMapper")
    BusinessIncomeRatioMapper businessIncomeRatioMapper;

    @CjTransaction
    @Override
    public long getLowestPrice() {
        return servicePriceMapper.getLowestPrice();
    }

    @CjTransaction
    @Override
    public BigDecimal getRatio(long price) {
        BusinessIncomeRatioExample example = new BusinessIncomeRatioExample();
        example.createCriteria().andMinAmountEdgeLessThanOrEqualTo(price).andMaxAmountEdgeGreaterThan(price);
        List<BusinessIncomeRatio> ratioList = businessIncomeRatioMapper.selectByExample(example);
        if (ratioList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return ratioList.get(0).getRatio();
    }
}
