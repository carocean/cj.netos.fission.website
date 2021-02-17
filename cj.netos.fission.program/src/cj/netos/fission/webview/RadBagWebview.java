package cj.netos.fission.webview;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import org.jsoup.nodes.Document;

@CjService(name = "/pages/red-bag.html")
public class RadBagWebview implements IGatewayAppSiteWayWebView {

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        Document document = resource.html(frame.relativePath());
        circuit.content().writeBytes(document.html().getBytes());
    }

}
