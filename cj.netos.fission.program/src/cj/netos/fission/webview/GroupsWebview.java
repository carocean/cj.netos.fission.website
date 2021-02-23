package cj.netos.fission.webview;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.model.PayRecord;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PersonInfo;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CjService(name = "/pages/groups.html")
public class GroupsWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPayRecordService payRecordService;
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    IPersonInfoService personInfoService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        PersonInfo current = personInfoService.getInfo(unionid);
        Person person = current.getPerson();
        long payerCount = payRecordService.totalPayer(person.getId());
        List<PayRecord> payerReocrds = payRecordService.pagePayerRecord(person.getId(), 50, 0);
        List<String> ids = new ArrayList<>();
        Map<String, PayRecord> payerAmounts = new HashMap<>();
        for (PayRecord record : payerReocrds) {
            ids.add(record.getPayee());
            payerAmounts.put(record.getPayee(), record);
        }
        List<Person> payerList = personService.findByIds(ids);

        Document document = resource.html(frame.relativePath());
        printDocument(document, payerList, payerAmounts, payerCount);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printDocument(Document document, List<Person> payerList, Map<String, PayRecord> payerAmounts, long payerCount) {
        document.select(".total-group span").html(String.format("你已加%s个群", payerCount));
        Element memberList = document.select(".group-list").first();
        Element memberLi = memberList.select(".group-info").first().clone();
        Element memberDeliver = memberList.select(".group-deliver").first().clone();
        memberList.empty();
        for (Person person : payerList) {
            Element li = memberLi.clone();
            PayRecord record = payerAmounts.get(person.getId());
            BigDecimal decimal = new BigDecimal(record.getAmount()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            li.select(".group-amount span").html(decimal.toString());
            li.select(".group-info >img").attr("src", person.getAvatarUrl());
            li.select(".group-title").html(person.getNickName());
            try {
                String ctime = Utils.parseDateTime(record.getCtime());
                li.select(".group-note span").html(ctime);
            } catch (ParseException e) {
                li.select(".group-note span").remove();
                e.printStackTrace();
            }

            memberList.appendChild(li);
            memberList.appendChild(memberDeliver.clone());
        }
    }
}
