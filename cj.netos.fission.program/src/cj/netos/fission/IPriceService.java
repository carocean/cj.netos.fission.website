package cj.netos.fission;

import java.math.BigDecimal;

public interface IPriceService {
    long getLowestPrice();

    BigDecimal getRatio(long price);

}
