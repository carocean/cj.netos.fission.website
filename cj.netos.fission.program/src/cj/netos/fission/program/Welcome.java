package cj.netos.fission.program;

import cj.netos.fission.ICashierBalanceService;
import cj.netos.fission.IRandRecommendService;
import cj.netos.fission.model.CashierBalance;
import cj.netos.fission.model.Person;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 按标签推荐
 *
 * @author caroceanjofers
 */
@CjService(name = "/")
public class Welcome implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IRandRecommendService randRecommendService;
    @CjServiceRef
    ICashierBalanceService cashierBalanceService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        Document document = resource.html("/wellcome.html");
        List<Person> personList = randRecommendService.randPersons(7);
        printPersonList(personList, document);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printPersonList(List<Person> personList, Document document) {
        Element ul = document.select(".persons").first();
        Element li = ul.select(".person").first().clone();
        ul.empty();
        for (Person person : personList) {
            CashierBalance balance = cashierBalanceService.getBalance(person.getId());
            if (balance == null) {
                balance=new CashierBalance();
                balance.setBalance(0L);
                balance.setPerson(person.getId());
            }
            Element cli = li.clone();
            cli.select(".img-box >img").attr("src", person.getAvatarUrl());
            cli.select(".nick-name").html(person.getNickName());
            BigDecimal decimal = new BigDecimal(balance.getBalance()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            cli.select(".cashier-balance .balance-v").html(decimal.toString());
            ul.appendChild(cli);
        }
    }

}
