package cj.netos.fission.webview;

import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.HashMap;
import java.util.Map;

@CjService(name = "/snatchEnvelope.service")
public class SnatchEnvelopeWebview implements IGatewayAppSiteWayWebView , IServiceAfter {
    String cashierReceiptUrl;
    OkHttpClient client;

    @Override
    public void onAfter(IServiceSite site) {
        cashierReceiptUrl = site.getProperty("rhub.ports.cashier.receipt");
        client=new OkHttpClient();
    }

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String person=frame.parameter("person");
        if (StringUtil.isEmpty(person)) {
            throw new CircuitException("404","未指定person参数。");
        }
        snatchEnvelope(person,frame,circuit);
    }

    private void snatchEnvelope(String person, Frame frame, Circuit circuit) throws CircuitException {
        HttpFrame httpFrame=(HttpFrame)frame;
        ISession session=httpFrame.session();
        String accessToken= (String) session.attribute("accessToken");
        String url = String.format("%s?person=%s", cashierReceiptUrl,person);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("Rest-Command","snatchEnvelope")
                    .header("cjtoken",accessToken)
                    .get()//默认就是GET请求，可以不写
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            String json = response.body().string();
            Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
            int status= Double.valueOf(map.get("status")+"").intValue();
            if(200!=status){
                throw new CircuitException(status+"",(String)map.get("message"));
            }
        }catch (Exception e){
            throw new CircuitException("500",e);
        }
    }
}
