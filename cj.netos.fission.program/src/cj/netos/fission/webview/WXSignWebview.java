package cj.netos.fission.webview;

import cj.netos.fission.WXAuthUtil;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.*;

@CjService(name = "/get-sign.json")
public class WXSignWebview extends WXAuthUtil implements IGatewayAppSiteWayWebView {
    @CjServiceSite
    IServiceSite site;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String accessToken = getAccessToken(site);

        //2、获取Ticket
        String jsapi_ticket = getTicket(accessToken);

        //3、时间戳和随机字符串
        String noncestr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);//随机字符串
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);//时间戳

        System.out.println("accessToken:" + accessToken + "\njsapi_ticket:" + jsapi_ticket + "\n时间戳：" + timestamp + "\n随机字符串：" + noncestr);

        //4、获取url
        String url = String.format("http://%s/%s/", frame.head("Host"), frame.rootName());
        //根据JSSDK上面的规则进行计算，这里比较简单，我就手动写啦
//    String[] ArrTmp = {"jsapi_ticket","timestamp","nonce","url"};
//    Arrays.sort(ArrTmp);
//    StringBuffer sf = new StringBuffer();
//    for(int i=0;i<ArrTmp.length;i++){
//        sf.append(ArrTmp[i]);
//    }

        //5、将参数排序并拼接字符串
        String str = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url=" + url;
        //6、将字符串进行sha1加密
        String signature = SHA1(str);
        System.out.println("参数：" + str + "\n签名：" + signature);
        String appid = site.getProperty("wechat.appid");
        String jsApiList = site.getProperty("wechat.jsApiList");
        List<String> list = new Gson().fromJson(jsApiList, ArrayList.class);

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("appId", appid);
        map.put("nonceStr", noncestr);
        map.put("signature", signature);
        map.put("jsApiList", list);
        circuit.content().writeBytes(new Gson().toJson(map).getBytes());
    }
}
