package cj.netos.fission.program;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.IRecommenderService;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PersonInfo;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
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
import java.util.Collection;
import java.util.List;

/**
 * @author caroceanjofers
 */
@CjService(name = "/home.html")
public class Home implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IRecommenderService recommenderService;
    @CjServiceRef
    IPersonInfoService personInfoService;
    @CjServiceRef
    IPayRecordService payRecordService;
    @CjServiceRef
    IPersonService personService;
    @CjServiceSite
    IServiceSite site;
    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        PersonInfo current = personInfoService.getInfo(unionid);

        Document document = resource.html("/home.html");
        Element recommendsE = document.select(".recommends").first();
        //其下是分享js要用到的信息
        recommendsE.attr("state", frame.parameter("state"));
        recommendsE.attr("avatar", current.getPerson().getAvatarUrl());
        recommendsE.attr("nickName", current.getPerson().getNickName());
        recommendsE.attr("balance", new BigDecimal(current.getBalance()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN).toString());
        recommendsE.attr("distance", current.getDistance() + "");
        long totalAmount = payRecordService.totalPayerAmountOnToday(unionid);
        recommendsE.attr("totalAmount", new BigDecimal(totalAmount).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN).toString());
//        Attachment attachment = attachmentService.getInfo(unionid);
//        if(attachment!=null&&"image".equals(attachment.getType())){
//            //附件的图片可能太大了，微信展示不出来，所以先注掉吧
//            String accessToken= (String) session.attribute("accessToken");
//            String url=String.format("%s?accessToken=%s",attachment.getSrc(),accessToken);
//            String remain=url.substring("http://".length());
//            int pos = remain.indexOf("/");
//            remain=remain.substring(pos);
//            url=String.format("http://www.nodespower.com:7100%s",remain);
//            recommendsE.attr("attachment", url);
//        }

        Collection<PersonInfo> persons = recommenderService.recommend(unionid, 20);
        if (persons.isEmpty()) {
            printEmpty(current, document);
        } else {
            document.select(".empty-panel").remove();
            printPersonList(persons, document, (String) httpFrame.session().attribute("accessToken"));
        }
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printEmpty(PersonInfo current, Document document) {
        document.select(".persons").remove();
        Element panel = document.select(".empty-panel").first();
        if (current.getPropTags().isEmpty()) {
            panel.select(".prop-tag-settings").remove();
        }
        if (current.getPayeeTags().isEmpty() && current.getPayeeArea() == null) {
            panel.select(".payee-settings").remove();
        }
    }

    private void printPersonList(Collection<PersonInfo> personList, Document document, String accessToken) {
        Element ul = document.select(".persons").first();
        Element li = ul.select(".person").first().clone();
        ul.empty();
        String openedAmount = site.getProperty("recommender.user.opened.amount");
        if (StringUtil.isEmpty(openedAmount)) {
            openedAmount = "60";
        }
        long oamount = Long.valueOf(openedAmount);
        for (PersonInfo personInfo : personList) {
            if (personInfo.getBalance() < oamount) {
                continue;
            }
            Person person = personInfo.getPerson();
            Element cli = li.clone();
            cli.attr("person", person.getId());
            cli.select(".img-box > img").attr("src", person.getAvatarUrl());
            cli.select(".nick-name").html(person.getNickName());
            long balance = personInfo.getBalance();
            BigDecimal decimal = new BigDecimal(balance).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            cli.select(".cashier-balance .balance-v").html(decimal.toString());
            ul.appendChild(cli);
            printPayees(cli, person);
        }
    }

    private void printPayees(Element e, Person person) {
        long payeeCount = payRecordService.totalPayee(person.getId());
        List<String> ids = payRecordService.pagePayeeId(person.getId(), 5, 0);
        List<Person> payeeList = personService.findByIds(ids);
        Element tips = e.select(".person-yours .tips").first();
        tips.select(".count").html(payeeCount + "");
        Element ul = e.select(".person-yours .friend-ul").first();
        Element cli = ul.select(".friend").first().clone();
        ul.empty();
        for (Person payee : payeeList) {
            Element li = cli.clone();
            li.select("img").attr("src", payee.getAvatarUrl());
            ul.appendChild(li);
        }
    }

}
