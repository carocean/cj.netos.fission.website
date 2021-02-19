package cj.netos.fission.program;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.IRecommenderService;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PersonInfo;
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
import java.util.Collection;

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
//    @CjServiceRef
//    IAttachmentService attachmentService;

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

        Collection<PersonInfo> persons = recommenderService.recommend(unionid, 100);
        printPersonList(persons, document, (String) httpFrame.session().attribute("accessToken"));

        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printPersonList(Collection<PersonInfo> personList, Document document, String accessToken) {
        Element ul = document.select(".persons").first();
        Element li = ul.select(".person").first().clone();
        ul.empty();
        for (PersonInfo personInfo : personList) {
            Person person = personInfo.getPerson();
            Element cli = li.clone();
            cli.attr("person", person.getId());
            cli.select(".img-box > img").attr("src", person.getAvatarUrl());
            cli.select(".nick-name").html(person.getNickName());
            long balance = personInfo.getBalance();
            BigDecimal decimal = new BigDecimal(balance).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            cli.select(".cashier-balance .balance-v").html(decimal.toString());
            ul.appendChild(cli);
        }
    }

}
