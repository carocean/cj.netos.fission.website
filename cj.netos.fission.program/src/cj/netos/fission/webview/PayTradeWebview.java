package cj.netos.fission.webview;

import cj.netos.fission.IPersonService;
import cj.netos.fission.model.Person;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.openport.CheckAccessTokenException;
import cj.studio.openport.util.Encript;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.security.Base64Utils;
import cj.ultimate.util.StringUtil;
import okhttp3.*;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@CjService(name = "/pages/pay-trade.service")
public class PayTradeWebview implements IGatewayAppSiteWayWebView {
    @CjServiceSite
    IServiceSite site;
    @CjServiceRef
    IPersonService personService;

    //微信jsapi充值参考地址，此为唯一可靠地址:https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_3.shtml
    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String amount = frame.parameter("amount");
        if (StringUtil.isEmpty(amount)) {
            throw new CircuitException("404", "缺少参数:amount");
        }
        long lamount = Long.valueOf(amount);
        if (lamount < 1) {
            throw new CircuitException("500", "充值金额小于1");
        }

        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        Person person = personService.get(unionid);
        String accessToken = (String) session.attribute("accessToken");
        String json = doPayTrade(accessToken, lamount, person);
        Map<String, Object> payMap = new Gson().fromJson(json, HashMap.class);
        circuit.content().writeBytes(new Gson().toJson(payMap).getBytes());
    }


    private String doPayTrade(String accessToken, long amount, Person person) throws CheckAccessTokenException {
        OkHttpClient client = (OkHttpClient) site.getService("@.http");
        String portsWallet = site.getProperty("rhub.ports.wallet.receipt");
        String url = String.format("%s?amount=%s&type=1&note=付款给裂变游戏·交个朋友", portsWallet, amount);
        Map<String, Object> details = new HashMap<>();
        details.put("payeeCode", String.format("%s@gbera.netos", person.getId()));
        details.put("payeeName", person.getNickName());
        details.put("payeeType", "fission-mf");
        details.put("orderno", Encript.md5(UUID.randomUUID().toString()));
        details.put("orderTitle", String.format("付款到%s的裂变游戏·交个朋友出纳柜台", person.getNickName()));
        details.put("serviceid", "fission/mf");
        details.put("serviceName", "裂变游戏·交个朋友");
        details.put("note", "推广费");
        Map<String, Object> data = new HashMap<>();
        data.put("details", new Gson().toJson(details));
        RequestBody body=RequestBody.create(new Gson().toJson(data).getBytes());
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "payTrade")
                .addHeader("cjtoken", accessToken)
                .post(body)
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
