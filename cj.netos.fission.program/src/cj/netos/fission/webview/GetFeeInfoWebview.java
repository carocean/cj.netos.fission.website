package cj.netos.fission.webview;

import cj.netos.fission.IPriceService;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;

@CjService(name = "/pages/get-fee-info.html")
public class GetFeeInfoWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPriceService priceService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String amount = frame.parameter("amount");
        if (StringUtil.isEmpty(amount)) {
            return;
        }
        Document document = resource.html("/pages/profile.html");
        long lngAmount = new BigDecimal((Double.valueOf(amount) * 100.00)).longValue();
        BigDecimal lowestPriceBD = new BigDecimal(lngAmount / 100.00).setScale(2);
        BigDecimal ratio = priceService.getRatio(lngAmount);
        BigDecimal ratioPer = ratio.multiply(new BigDecimal("100.00")).setScale(2);
        Element maskMain = document.select(".profile-mask .mask-main").first();
        Element amountInputE = maskMain.select(".mask-input input").first();
        amountInputE.attr("placeholder", String.format("起充金额：%s元", lowestPriceBD));
        amountInputE.attr("lowestAmount", lowestPriceBD.toString());
        Element maskNote = maskMain.select(".mask-note").first();
        BigDecimal fee = lowestPriceBD.multiply(ratio).setScale(2);
        BigDecimal gain = lowestPriceBD.subtract(fee);
        maskNote.select(".gain").html(String.format("¥%s", gain.toString()));
        maskNote.select(".fee").html(String.format("¥%s", fee.toString()));
        maskNote.select(".tax").html(String.format("%s%%", ratioPer.toString()));
        String html = maskNote.html();
        circuit.content().writeBytes(html.getBytes());
    }

}
