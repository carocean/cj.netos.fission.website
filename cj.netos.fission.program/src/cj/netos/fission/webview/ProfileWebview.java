package cj.netos.fission.webview;

import cj.netos.fission.*;
import cj.netos.fission.model.*;
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
import java.util.List;

@CjService(name = "/pages/profile.html")
public class ProfileWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPersonInfoService personInfoService;
    @CjServiceRef
    ICashierService cashierService;
    @CjServiceRef
    IPayRecordService payRecordService;
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    IPriceService priceService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        String accessToken = (String) session.attribute("accessToken");
        PersonInfo current = personInfoService.getInfo(unionid);
        Document document = resource.html(frame.relativePath());
        printDocument(document, current, accessToken);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printDocument(Document document, PersonInfo current, String accessToken) {
        Person person = current.getPerson();
        Element faceE = document.select(".face").first();
        faceE.select(">img").attr("src", person.getAvatarUrl());
        faceE.select(".face-title").html(person.getNickName());
        faceE.select(".face-unionid").html(person.getId());
        Element areaE = faceE.select(".face-area").first();
        if (StringUtil.isEmpty(person.getProvince())) {
            areaE.select(".province").remove();
        } else {
            areaE.select(".province").html(person.getProvince());
        }
        if (StringUtil.isEmpty(person.getCity())) {
            areaE.select(".district").remove();
        } else {
            areaE.select(".district").html(person.getCity());
        }
        BigDecimal balance = new BigDecimal(current.getBalance()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
        document.select(".container .card[pay] .card-item-summary-value span").html(String.format("%s", balance));

        long lowestPrice = priceService.getLowestPrice();
        BigDecimal lowestPriceBD = new BigDecimal(lowestPrice / 100.00).setScale(2);
        BigDecimal ratio = priceService.getRatio(lowestPrice);
        BigDecimal ratioPer=ratio.multiply(new BigDecimal("100.00")).setScale(2);
        Element maskMain = document.select(".profile-mask .mask-main").first();
        Element amountInputE = maskMain.select(".mask-input input").first();
        amountInputE.attr("placeholder", String.format("起充金额：%s元", lowestPriceBD));
        amountInputE.attr("lowestAmount", lowestPriceBD.toString());
        Element maskNote = maskMain.select(".mask-note").first();
        BigDecimal fee=lowestPriceBD.multiply(ratio).setScale(2);
        BigDecimal gain=lowestPriceBD.subtract(fee);
        maskNote.select(".gain").html(String.format("¥%s",gain.toString()));
        maskNote.select(".fee").html(String.format("¥%s",fee.toString()));
        maskNote.select(".tax").html(String.format("%s%%",ratioPer.toString()));

        List<Tag> propTags = current.getPropTags();
        if (propTags.isEmpty()) {
            document.select(".my-tag-box").remove();
        } else {
            Element tagsUl = document.select(".my-tag-box .my-tag-ul").first();
            Element cli = tagsUl.select(">li").first().clone();
            tagsUl.empty();
            for (Tag tag : propTags) {
                Element li = cli.clone();
                li.select("span").html(tag.getName());
                tagsUl.appendChild(li);
            }
        }
        Cashier cashier = cashierService.getCashier(person.getId());
        if (cashier.getState() == 0) {
            document.select(".card[pay] .card-item-nav[operator] span[state]").html("营业中");
        } else {
            document.select(".card[pay] .card-item-nav[operator] span[state]").html("已停业");
        }
        Element friends = document.select(".card[friends]").first();
        Element members = friends.select(".card-item-nav[members]").first();
        Element groups = friends.select(".card-item-nav[groups]").first();
        long payeeCount = payRecordService.totalPayee(person.getId());
        long payerCount = payRecordService.totalPayer(person.getId());
        List<String> payeeIdList = payRecordService.pagePayeeId(person.getId(), 6, 0);
        List<String> payerIdList = payRecordService.pagePayerId(person.getId(), 6, 0);
        List<Person> payeeList = personService.findByIds(payeeIdList);
        List<Person> payerList = personService.findByIds(payerIdList);

        Element memberBox = members.select(".card-item-desc").first();
        Element memberA = memberBox.select("a").first().clone();
        Element memberS = memberBox.select("span").first().clone();
        memberBox.empty();
        for (Person payee : payeeList) {
            Element a = memberA.clone();
            a.select("img").attr("src", String.format("%s", payee.getAvatarUrl()));
            memberBox.appendChild(a);
        }
        memberS.html(String.format("共进群%s人", payeeCount));
        memberBox.appendChild(memberS);

        Element groupBox = groups.select(".card-item-desc").first();
        Element groupA = groupBox.select("a").first().clone();
        Element groupS = groupBox.select("span").first().clone();
        groupBox.empty();
        for (Person payer : payerList) {
            Element a = groupA.clone();
            a.select("img").attr("src", String.format("%s", payer.getAvatarUrl()));
            groupBox.appendChild(a);
        }
        groupS.html(String.format("共加群%s个", payerCount));
        groupBox.appendChild(groupS);
    }

}
