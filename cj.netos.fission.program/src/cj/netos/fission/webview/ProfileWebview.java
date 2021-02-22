package cj.netos.fission.webview;

import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PersonInfo;
import cj.netos.fission.model.Tag;
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
        printDocument(document, current,accessToken);
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
    }

}
