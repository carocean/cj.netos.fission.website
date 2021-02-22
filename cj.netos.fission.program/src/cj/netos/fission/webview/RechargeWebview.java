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
import cj.ultimate.security.Base64Utils;
import cj.ultimate.util.StringUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;


@CjService(name = "/pages/recharge.service")
public class RechargeWebview implements IGatewayAppSiteWayWebView {
    @CjServiceSite
    IServiceSite site;

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
        String note = frame.parameter("note");
        if (note == null) {
            note = "";
        }
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        String accessToken = (String) session.attribute("accessToken");
        String json = doRecharge(accessToken, lamount, note);
        Map<String, Object> payMap = new Gson().fromJson(json, HashMap.class);

        Map<String, Object> map = sign(payMap);
        map.put("record_sn",payMap.get("record_sn"));
        circuit.content().writeBytes(new Gson().toJson(map).getBytes());
    }


    private Map<String, Object> sign(Map<String, Object> payMap) throws CircuitException {
        String appid = site.getProperty("wechat.appid");
        String noncestr = (String) payMap.get("noncestr");
        long timestampLong = System.currentTimeMillis() / 1000;
        String timestamp=String.format("%s",timestampLong);
        String signType = "RSA";
        String prepayid = (String) payMap.get("prepayid");
        String packageStr = String.format("prepay_id=%s", prepayid);
        PrivateKey privateKey = null;
        String privateKeyStr = site.getProperty("wechat.privateKey");
        //
        try {
            privateKey = restorePrivateKey(Base64Utils.decode(privateKeyStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 签名
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%s\n", appid));
        sb.append(String.format("%s\n", timestamp));
        sb.append(String.format("%s\n", noncestr));
        sb.append(String.format("%s\n", packageStr));
        byte[] sing_byte = signIt(privateKey, sb.toString());
        String signature = null;
        try {
            signature = Base64Utils.encode(sing_byte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);//注意：在苹果手机上，如果是数字时间的话会报缺少此参数错误，因此改为string类型
        map.put("appId", appid);
        map.put("nonceStr", noncestr);
        map.put("package", packageStr);
        map.put("signType", signType);
        map.put("paySign", signature);
//        System.out.println(String.format("signature=%s", signature));
        return map;
    }

    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param plain_text 明文
     * @return
     */
    public static byte[] signIt(PrivateKey privateKey, String plain_text) {
        MessageDigest messageDigest;
        byte[] signed = null;
        try {
//            messageDigest = MessageDigest.getInstance("SHA-256");
//            messageDigest.update(plain_text.getBytes());
//            byte[] outputDigest_sign = messageDigest.digest();
//            System.out.println("SHA-256加密后-----》" +bytesToHexString(outputDigest_sign));
            byte[] outputDigest_sign = plain_text.getBytes();
            Signature Sign = Signature.getInstance("SHA256withRSA");
            Sign.initSign(privateKey);
            Sign.update(outputDigest_sign);
            signed = Sign.sign();
//            System.out.println("SHA256withRSA签名后-----》" + bytesToHexString(signed));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return signed;
    }


    public PrivateKey restorePrivateKey(byte[] keyBytes) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = factory
                    .generatePrivate(pkcs8EncodedKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String doRecharge(String accessToken, long amount, String note) throws CheckAccessTokenException {
        OkHttpClient client = (OkHttpClient) site.getService("@.http");
        String portsWallet = site.getProperty("rhub.ports.wallet.receipt");
        String url = String.format("%s?currency=CNY&amount=%s&payChannel=wechatpay&applyTerminal=jsapi&note=%s", portsWallet, amount, note);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Rest-Command", "rechargeTo")
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
        json = new Gson().fromJson(json, String.class);
        return json;
    }
}
