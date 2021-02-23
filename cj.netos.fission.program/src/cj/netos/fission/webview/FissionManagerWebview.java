package cj.netos.fission.webview;

import cj.netos.fission.IAttachmentService;
import cj.netos.fission.ICashierService;
import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.model.Attachment;
import cj.netos.fission.model.Cashier;
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
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@CjService(name = "/pages/fission-manager.html")
public class FissionManagerWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    ICashierService cashierService;
    @CjServiceRef
    IAttachmentService attachmentService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        Cashier cashier = cashierService.getCashier(unionid);
        Attachment attachment = attachmentService.getInfo(unionid);
        Document document = resource.html(frame.relativePath());
        printDocument(document, cashier, attachment);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void printDocument(Document document, Cashier cashier, Attachment attachment) {
        Element stateE = document.select(".card[operator] .card-item-desc[state]").first();
        if (cashier.getState() == 0) {
            stateE.select("label").html("营业中");
            stateE.select("input").attr("checked", "checked");
        } else {
            stateE.select("label").html("已停业");
            stateE.select("input").removeAttr("checked");
        }
        Element attachE = document.select(".card[attach] .card-item-nav .card-item-desc").first();
        Element spanC = attachE.select("span").first().clone();
        attachE.empty();
        if (attachment != null) {
            Element span = null;
            switch (attachment.getType()) {
                case "image":
                    span = spanC.clone();
                    span.html("图片");
                    attachE.appendChild(span);
                    break;
                case "video":
                    span = spanC.clone();
                    span.html("视频");
                    attachE.appendChild(span);
                    break;
            }
        }else{
            attachE.append("<label>无</label>");
        }
    }

}
