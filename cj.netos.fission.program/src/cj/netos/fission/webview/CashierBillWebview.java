package cj.netos.fission.webview;

import cj.netos.fission.ICashierBillService;
import cj.netos.fission.model.CashierBill;
import cj.netos.fission.model.Utils;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;


@CjService(name = "/pages/cashier-bill.html")
public class CashierBillWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    ICashierBillService cashierBillService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        Document document = resource.html(frame.relativePath());
        List<CashierBill> billList = cashierBillService.pageBill(unionid, 50, 0);//仅列出50条
        printDocument(document, billList);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printDocument(Document document, List<CashierBill> billList) {
        Element billul = document.select(".bill-box .bill-list").first();
        Element cli = billul.select(".bill-info").first().clone();
        Element cdeliver = billul.select(".bill-deliver").first().clone();
        billul.empty();
        for (CashierBill bill : billList) {
            Element li = cli.clone();
            li.select(".bill-title").html(bill.getTitle());
            BigDecimal amount = new BigDecimal(bill.getAmount()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            BigDecimal balance = new BigDecimal(bill.getBalance()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            li.select(".bill-amount span").html(amount.toString());
            li.select(".bill-balance span").html(balance.toString());
            try {
                String ctime = Utils.parseDateTime(bill.getCtime());
                li.select(".bill-note span").html(ctime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            billul.appendChild(li);
            billul.appendChild(cdeliver.clone());
        }
    }
}
