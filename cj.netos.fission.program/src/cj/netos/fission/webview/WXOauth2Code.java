package cj.netos.fission.webview;

import cj.netos.fission.IPersonService;
import cj.netos.fission.IRandRecommendService;
import cj.netos.fission.WechatUserInfo;
import cj.netos.fission.model.Person;
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
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

@CjService(name = "/wechat/auth.html")
public class WXOauth2Code implements IGatewayAppSiteWayWebView, IServiceAfter {
    OkHttpClient client;
    String appid;
    String appSecret;
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    IRandRecommendService randRecommendService;
    @Override
    public void onAfter(IServiceSite site) {
        appid = site.getProperty("wechat.appid");
        appSecret = site.getProperty("wechat.appSecret");
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

        //注意：如果用户选了"拒绝"而执行以下代码获取令牌，则下次就不会弹出微信的授权页了。
        String url = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", appid, appSecret, code);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()//默认就是GET请求，可以不写
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            String json = response.body().string();
            Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
            String accessToken = (String) map.get("access_token");
            String unionid = (String) map.get("unionid");
            String openid = (String) map.get("openid");
            //采用uc中心的authByWechat接口认证，获取netos体系的accessToken
            if (personService.exists(unionid)) {
                forwardHome(unionid,frame,resource,circuit);
                return;
            }

            WechatUserInfo userInfo = getUserInfo(accessToken, openid);
            Person person=userInfo.toPerson();
            personService.add(person);
            randRecommendService.cachePerson(person);
            forwardHome(unionid,frame,resource,circuit);
        } catch (Exception e) {
            CircuitException ce = CircuitException.search(e);
            if (ce != null) {
                throw ce;
            }
            throw new CircuitException("500", e);
        }
    }

    private void forwardHome(String unionid,Frame frame,IGatewayAppSiteResource resource, Circuit circuit) {
        HttpFrame httpFrame= (HttpFrame) frame;
        httpFrame.session().attribute("unionid",unionid);
        resource.redirect(String.format("%s/home.html",frame.rootPath()),circuit);
    }

    private WechatUserInfo getUserInfo(String accessToken, String openid) throws IOException {
        String url = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN", accessToken, openid);
        Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String json = response.body().string();
        WechatUserInfo userInfo = new Gson().fromJson(json, WechatUserInfo.class);
        return userInfo;
    }
}
