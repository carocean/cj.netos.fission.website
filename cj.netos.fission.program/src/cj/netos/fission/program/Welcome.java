package cj.netos.fission.program;

import cj.netos.fission.IRandRecommendService;
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

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        Document document = resource.html("/wellcome.html");
        List<Person> personList = randRecommendService.randPersons(2);
        printPersonList(personList, document);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printPersonList(List<Person> personList, Document document) {
        Element ul = document.select(".persons").first();
        Element li=ul.select(".person").first().clone();
        ul.empty();
		for (Person person : personList) {
			Element cli=li.clone();
			cli.select(".img-box >img").attr("src",person.getAvatarUrl());
			cli.select(".nick-name").html(person.getNickName());
			ul.appendChild(cli);
		}
    }

}
