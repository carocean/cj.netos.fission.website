package cj.netos.fission.program;

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

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author caroceanjofers
 */
@CjService(name = "/home.html")
public class Home implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IRecommenderService recommenderService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        Document document = resource.html("/home.html");
        Collection<PersonInfo> persons = recommenderService.recommend(unionid, 100);
        printPersonList(persons, document, (String) httpFrame.session().attribute("accessToken"));

        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printPersonList(Collection<PersonInfo> personList, Document document, String accessToken) {
        Element ul = document.select(".persons").first();
        Element li = ul.select(".person").first().clone();
        ul.empty();
        for (PersonInfo personInfo : personList) {
            Person person=personInfo.getPerson();
            Element cli = li.clone();
            cli.attr("person", String.format("%s@gbera.netos", person.getId()));
            cli.select(">img").attr("src", person.getAvatarUrl());
            cli.select(".nick-name").html(person.getNickName());
            ul.appendChild(cli);
        }
    }

}
