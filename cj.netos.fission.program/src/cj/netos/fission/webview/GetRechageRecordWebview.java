package cj.netos.fission.webview;

import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.openport.CheckAccessTokenException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CjService(name = "/pages/get-recharge-record.service")
public class GetRechageRecordWebview implements IGatewayAppSiteWayWebView {
    @CjServiceSite
    IServiceSite site;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String recordSn = frame.parameter("recordSn");
        if (StringUtil.isEmpty(recordSn)) {
            throw new CircuitException("404", "缺少参数:recordSn");
        }
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        String accessToken = (String) session.attribute("accessToken");
        String json = getRecharge(accessToken, recordSn);
        circuit.content().writeBytes(json.getBytes());
    }

    private String getRecharge(String accessToken, String recordSn) throws CheckAccessTokenException {
        OkHttpClient client = (OkHttpClient) site.getService("@.http");
        String portsWallet = site.getProperty("rhub.ports.wallet.record");
        String url = String.format("%s?record_sn=%s", portsWallet, recordSn);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "getRechargeRecord")
                .addHeader("cjtoken", accessToken)
                .get()
                .build();
        final Call call = client.newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new CheckAccessTokenException("1002", e);
        }
        if (response.code() >= 400) {
            throw new CheckAccessTokenException("1002", String.format("远程访问失败:%s", response.message()));
        }
        String json = null;
        try {
            json = response.body().string();
        } catch (IOException e) {
            throw new CheckAccessTokenException("1002", e);
        }
        Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
        Double status = Double.parseDouble(map.get("status") + "");
        if (status >= 400) {
            throw new CheckAccessTokenException(status.intValue() + "", map.get("message") + "");
        }
        json = (String) map.get("dataText");
        return json;
    }
}
