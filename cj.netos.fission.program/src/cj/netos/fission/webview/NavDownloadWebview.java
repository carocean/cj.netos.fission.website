package cj.netos.fission.webview;

import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Document;


@CjService(name = "/pages/nav-download.html")
public class NavDownloadWebview implements IGatewayAppSiteWayWebView {
    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String title = frame.parameter("title");
        String content = frame.parameter("content");
        String location = frame.parameter("location");
        Document document = resource.html(frame.relativePath());
        document.select(".nav-center .nav-title span").html(title);
        if (StringUtil.isEmpty(content)) {
            document.select(".nav-center .nav-content").remove();
        }else{
            document.select(".nav-center .nav-content span").html(content);
        }
        if (StringUtil.isEmpty(location)) {
            document.select(".nav-center .nav-location").remove();
        }else{
            document.select(".nav-center .nav-location span").html(location);
        }
        circuit.content().writeBytes(document.html().getBytes());
    }
}
