package cj.netos.fission.webview;

import cj.netos.fission.ICashierService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.IRandRecommendService;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.Utils;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceAfter;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.studio.openport.util.Encript;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CjService(name = "/wechat/auth.html")
public class WXOauth2Code implements IGatewayAppSiteWayWebView, IServiceAfter {
    OkHttpClient client;
    String appid;
    String appKey;
    String appSecret;
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    IRandRecommendService randRecommendService;
    @CjServiceRef
    ICashierService cashierService;
    String authPorts;

    @Override
    public void onAfter(IServiceSite site) {
        authPorts = site.getProperty("rhub.ports.auth");
        appid = site.getProperty("gbera.appid");
        appKey = site.getProperty("gbera.appKey");
        appSecret = site.getProperty("gbera.appSecret");
        client = new OkHttpClient();
    }

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String code = frame.parameter("code");
        String state = frame.parameter("state");
        CJSystem.logging().info(getClass(), String.format("微信转发来的验证信息：%s %s", state, code));
        if (StringUtil.isEmpty(code)) {
            return;
        }
        String nonce = Encript.md5(UUID.randomUUID().toString());
        String signe = Encript.md5(String.format("%s%s%s", appKey, nonce, appSecret));
        //注意：如果用户选了"拒绝"而执行以下代码获取令牌，则下次就不会弹出微信的授权页了。
        String url = String.format("%s?deviceType=web", authPorts);
        Map<String, Object> form = new HashMap<>();
        form.put("code", code);
        form.put("state", state);
        form.put("device", String.format("%s", Encript.md5("web")));
        RequestBody data = RequestBody.create(new Gson().toJson(form).getBytes());
        try {
            Request request = new Request.Builder()
                    .header("Rest-Command", "authByWeChat")
                    .header("app-id", appid)
                    .header("app-key", appKey)
                    .header("app-nonce", nonce)
                    .header("app-sign", signe)
                    .url(url)
                    .post(data)//默认就是GET请求，可以不写
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            String json = response.body().string();
            Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
//            CJSystem.logging().info(getClass(),String.format("auth服务返回：%s",map));
            String dataText = (String) map.get("dataText");
            Map<String, Object> obj = new Gson().fromJson(dataText, HashMap.class);
//            System.out.println(obj);
            Map<String, Object> subject = (Map<String, Object>) obj.get("subject");
            Map<String, Object> token = (Map<String, Object>) obj.get("token");
            String accessToken = (String) token.get("accessToken");
            String unionid = (String) subject.get("accountCode");
            //采用uc中心的authByWechat接口认证，获取netos体系的accessToken
            if (personService.exists(unionid)) {
                forwardHome(unionid, accessToken, frame, resource, circuit);
                return;
            }

            Person person = Person.parse(subject, token);
            personService.add(person);
            randRecommendService.cachePerson(person);
            forwardHome(unionid, accessToken, frame, resource, circuit);
        } catch (Exception e) {
            CircuitException ce = CircuitException.search(e);
            if (ce != null) {
                throw ce;
            }
            throw new CircuitException("500", e);
        }
    }

    private void forwardHome(String unionid, String accessToken, Frame frame, IGatewayAppSiteResource resource, Circuit circuit) {
        String referrer = frame.parameter("referrer");
        if (!StringUtil.isEmpty(referrer)) {
            referrer = Utils.getSimplePerson(referrer);
            if (!unionid.equals(referrer)) {
                Person person = personService.get(referrer);
                if (person != null) {
                    cashierService.setReferrer(unionid, person.getId(), person.getNickName());
                }
            }
        }
        HttpFrame httpFrame = (HttpFrame) frame;
        httpFrame.session().attribute("unionid", unionid);
        httpFrame.session().attribute("accessToken", accessToken);
        String url = String.format("%s/home.html?state=%s", frame.rootPath(), frame.parameter("state"));
        resource.redirect(url, circuit);
    }


}
