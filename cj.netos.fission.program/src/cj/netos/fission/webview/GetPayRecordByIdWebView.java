package cj.netos.fission.webview;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.model.PayRecord;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.Utils;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.app.IGatewayAppSiteResource;
import cj.studio.gateway.socket.app.IGatewayAppSiteWayWebView;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@CjService(name = "/pages/getPayRecordById.service")
public class GetPayRecordByIdWebView implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPayRecordService payRecordService;
    @CjServiceRef
    IPersonService personService;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String record_sn = frame.parameter("record_sn");
        if (StringUtil.isEmpty(record_sn)) {
            throw new CircuitException("404", "缺少记录标识");
        }
        PayRecord record = payRecordService.getRecordBySn(record_sn);
        Map<String, Object> map = new HashMap<>();
        if (record == null) {
            map.put("status", "404");
            map.put("message", "记录不存在，可能还款处理完成");
        } else {
            map.put("status", "200");
            map.put("message", "ok");
            map.put("record", record);
            BigDecimal amount = new BigDecimal(record.getAmount()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
            map.put("amount", amount.toString());
            Document redBagDoc = resource.html("/pages/red-bag.html");
            Element personE = redBagDoc.select(".rb-payee-box .rb-payee-persons .rb-payee-person").first().clone();
            String recordHtml = getRecordHtml(record, personE);
            map.put("recordHtml", recordHtml);

        }
        circuit.content().writeBytes(new Gson().toJson(map).getBytes());
    }

    private String getRecordHtml(PayRecord record, Element li) {
        Person payee = personService.get(record.getPayee());
        li.select(">img").attr("src", payee.getAvatarUrl());
        li.select(".rb-payee-nick").html(payee.getNickName());
        try {
            String time = Utils.parseDateTime(record.getCtime());
            li.select(".rb-payee-sub").html(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BigDecimal decimal = new BigDecimal(record.getAmount()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN);
        li.select(".rb-payee-tip span").html(String.format("%s元", decimal.toString()));
        return li.outerHtml();
    }
}
