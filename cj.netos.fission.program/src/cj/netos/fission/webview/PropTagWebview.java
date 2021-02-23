package cj.netos.fission.webview;

import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.ITagService;
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

import java.util.List;


@CjService(name = "/pages/prop-tag.html")
public class PropTagWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPersonInfoService personInfoService;
    @CjServiceRef
    ITagService tagService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        PersonInfo current = personInfoService.getInfo(unionid);
        Document document = resource.html(frame.relativePath());
        printDocument(document, current);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printDocument(Document document, PersonInfo current) {
        List<Tag> propTags = current.getPropTags();
        List<Tag> allTags = tagService.listAllTag();
        Element tagUl = document.select(".tag-selected .tag-ul").first();
        Element cTagLi = tagUl.select(".tag-label").first().clone();
        tagUl.empty();
        if (!propTags.isEmpty()) {
            printTags(propTags, tagUl, cTagLi);
        }
        tagUl = document.select(".tag-panel .tag-ul").first();
        cTagLi = tagUl.select(".tag-label").first().clone();
        tagUl.empty();
        if (!propTags.isEmpty()) {
            printTags(allTags, tagUl, cTagLi);
        }
    }

    private void printTags(List<Tag> tags, Element ul, Element cli) {
        for (Tag tag : tags) {
            Element li = cli.clone();
            li.select("span").html(tag.getName());
            li.attr("tag-id", tag.getId());
            ul.appendChild(li);
        }
    }


}
