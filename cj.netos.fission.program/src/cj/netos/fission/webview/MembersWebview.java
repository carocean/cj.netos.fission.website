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

@CjService(name = "/pages/members.html")
public class MembersWebview implements IGatewayAppSiteWayWebView {
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
        long payeeCount = payRecordService.totalPayee(person.getId());
        long payeeAmount = payRecordService.totalPayeeAmount(person.getId());
        List<PayRecord> payeeReocrds = payRecordService.pagePayeeRecord(person.getId(), 50, 0);
        List<String> ids = payRecordService.pagePayeeId(person.getId(), 50, 0);
        List<Person> persons = personService.findByIds(ids);
        Map<String, Person> personMap = new HashMap<>();
        for (Person p : persons) {
            personMap.put(p.getId(), p);
        }
        Document document = resource.html(frame.relativePath());
        printDocument(document, personMap, payeeReocrds, payeeCount,payeeAmount);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printDocument(Document document, Map<String, Person> personMap, List<PayRecord> payerReocrds, long payeeCount, long payeeAmount) {
        BigDecimal payeesMoney = new BigDecimal(payeeAmount).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
        document.select(".total-member span").html(String.format("你的群已发展%s个成员，共支出%s元",payeeCount,payeesMoney));
        Element memberList = document.select(".member-list").first();
        Element memberLi = memberList.select(".member-info").first().clone();
        Element memberDeliver = memberList.select(".member-deliver").first().clone();
        memberList.empty();
        for (PayRecord record : payerReocrds) {
            Person person = personMap.get(record.getPayee());
            if (person == null) {
                continue;
            }
            Element li = memberLi.clone();
            BigDecimal decimal = new BigDecimal(record.getAmount()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            li.select(".member-amount span").html("-"+decimal.toString());
            li.select(".member-info >img").attr("src", person.getAvatarUrl());
            li.select(".member-title").html(person.getNickName());
            try {
                String ctime= Utils.parseDateTime(record.getCtime());
                li.select(".member-note span").html(ctime);
            } catch (ParseException e) {
                li.select(".member-note span").remove();
                e.printStackTrace();
            }

            memberList.appendChild(li);
            memberList.appendChild(memberDeliver.clone());
        }
    }
}
