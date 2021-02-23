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


@CjService(name = "/pages/prop-tag-manager.service")
public class PropTagManagerWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    ITagService tagService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String action = frame.parameter("action");
        String tagId = frame.parameter("tagId");
        if (StringUtil.isEmpty(action)) {
            throw new CircuitException("404", "缺少参数:action");
        }
        if (StringUtil.isEmpty(tagId)) {
            throw new CircuitException("404", "缺少参数:tagId");
        }
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        switch (action) {
            case "remove":
                tagService.removePropTag(unionid, tagId);
                break;
            case "select":
                tagService.selectPropTag(unionid, tagId);
                break;
        }
    }


}
