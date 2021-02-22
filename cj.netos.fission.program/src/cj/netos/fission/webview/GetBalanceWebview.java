package cj.netos.fission.webview;

import cj.netos.fission.IPersonInfoService;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@CjService(name = "/pages/get-balance.service")
public class GetBalanceWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPersonInfoService personInfoService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            return;
        }
        PersonInfo current = personInfoService.getInfo(unionid);
        Map<String, Object> map = new HashMap<>();
        BigDecimal decimal = new BigDecimal(current.getBalance()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
        map.put("balance", decimal.toString());
        map.put("balanceLong",current.getBalance());
        circuit.content().writeBytes(new Gson().toJson(map).getBytes());
    }

}
