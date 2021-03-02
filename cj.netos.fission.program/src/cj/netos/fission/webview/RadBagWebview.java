package cj.netos.fission.webview;

import cj.netos.fission.IPayRecordService;
import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.ITaskService;
import cj.netos.fission.model.*;
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
import cj.studio.openport.util.Encript;
import cj.ultimate.util.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@CjService(name = "/pages/red-bag.html")
public class RadBagWebview implements IGatewayAppSiteWayWebView {
    @CjServiceRef
    IPersonInfoService personInfoService;
    @CjServiceRef
    IPayRecordService payRecordService;
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    ITaskService taskService;
    @CjServiceSite
    IServiceSite site;

    @Override
    public void flow(Frame frame, Circuit circuit, IGatewayAppSiteResource resource) throws CircuitException {
        String redBagOwner = frame.parameter("person");
        if (StringUtil.isEmpty(redBagOwner)) {
            throw new CircuitException("404", "缺少参数红包目标");
        }
        HttpFrame httpFrame = (HttpFrame) frame;
        ISession session = httpFrame.session();
        String accessToken = (String) session.attribute("accessToken");
        String unionid = (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            throw new CircuitException("404", "不确定的用户");
        }
        PersonInfo personInfo = personInfoService.getInfo(unionid);
        PersonInfo redBagOwnerPerson = personInfoService.getInfo(redBagOwner);

        long opTimers = 0;
        TaskCounter counter = taskService.getCounter(unionid);
        if (counter != null) {
            opTimers = counter.getOpTimes();
        }
        String limitOpTimers = site.getProperty("task.counter.limit.opTimers");
        if (StringUtil.isEmpty(limitOpTimers)) {
            limitOpTimers = "3";
        }
        if (opTimers >= Integer.valueOf(limitOpTimers)) {
            String state = frame.parameter("state");
            String condition = counter.getCondition();
            if (!StringUtil.isEmpty(condition) && condition.equals(state)) {
                //说明是由分享链接进来的，则记录任务并清零
                taskService.doEvent(unionid, personInfo.getPerson().getNickName(), "share", "分享");
            } else {
                if (!StringUtil.isEmpty(condition) && !StringUtil.isEmpty(state) && state.startsWith("rdm:")) {//这说明已经分享了，但没有从分享链接进来，因此提示他从分享链接点进来
                    Document document = resource.html("/pages/task-tips.html");
                    Element taskCenterE = document.select(".task-center").first();
                    taskCenterE.attr("state", condition);
                    renderShareInfo(personInfo, taskCenterE);
                    circuit.content().writeBytes(document.html().getBytes());
                    return;
                }
                if (!StringUtil.isEmpty(condition) && !condition.equals(state)) {//说明用户以过期的条件进来
                    Document document = resource.html("/pages/task-tips.html");
                    Element taskCenterE = document.select(".task-center").first();
                    taskCenterE.attr("state", condition);
                    renderShareInfo(personInfo, taskCenterE);
                    document.select(".task-tips .task-subject").html("你不是从最近一次的分享链接进入本游戏！");
                    circuit.content().writeBytes(document.html().getBytes());
                    return;
                }
                //返回分享页面
                if (StringUtil.isEmpty(condition)) {
                    condition = taskService.genCondition(unionid);
                }
                Document document = resource.html("/pages/task-event.html");
                Element taskCenterE = document.select(".task-center").first();
                taskCenterE.attr("state", condition);
                renderShareInfo(personInfo, taskCenterE);
                circuit.content().writeBytes(document.html().getBytes());
                return;
            }
        }
        //以下是抢红包页面
        Document document = resource.html(frame.relativePath());

        printDoc(personInfo, redBagOwnerPerson, document, accessToken);
        circuit.content().writeBytes(document.html().getBytes());
    }

    private void renderShareInfo(PersonInfo current, Element element) {
        element.attr("avatar", current.getPerson().getAvatarUrl());
        element.attr("nickName", current.getPerson().getNickName());
        element.attr("balance", new BigDecimal(current.getBalance()).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN).toString());
        element.attr("distance", current.getDistance() + "");
        long friendCount = payRecordService.totalPayer(current.getPerson().getId());
        element.attr("friendCount", friendCount + "");
        long totalAmount = payRecordService.totalPayerAmount(current.getPerson().getId());
        element.attr("totalAmount", new BigDecimal(totalAmount).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN).toString());
    }

    private void printDoc(PersonInfo snatcher, PersonInfo redBagOwner, Document document, String accessToken) throws CircuitException {
        document.select(".rb-info").attr("red-bag-owner", String.format("%s@gbera.netos", redBagOwner.getPerson().getId()));
        document.select(".rb-info .rb-card .rb-amount").html("<span style='font-size: 40px;'>正在拆包...</span>");
        Element rbPerson = document.select(".rb-person").first();
        Element oImg = rbPerson.select(".rb-face img").first();
        oImg.attr("src", redBagOwner.getPerson().getAvatarUrl());
        Element onick = rbPerson.select(".rb-nick").first();
        onick.html(redBagOwner.getPerson().getNickName());
        Attachment attachment = personInfoService.getAttachment(redBagOwner.getPerson().getId());
        Element attachE = document.select(".rb-attach").first();
        if (attachment == null) {
            attachE.remove();
        } else {
            Element myGallery = document.select(".my-gallery").first();
            String src = String.format("%s?accessToken=%s", attachment.getSrc(), accessToken);
            switch (attachment.getType()) {
                case "image":
                    Element figure = myGallery.select("figure").first().clone();
                    myGallery.empty();
                    figure.select("a").attr("href", src);
                    figure.select("img").attr("src", src);
                    myGallery.appendChild(figure);
                    break;
                case "video":
                    Element video = myGallery.select("video").first().clone();
                    myGallery.empty();
                    Element sourceE = video.select("source").first().clone();
                    video.empty();
                    sourceE.attr("src", src);
                    video.appendChild(sourceE);
                    myGallery.appendChild(video);
                    break;
            }
            Element noteE = attachE.select(".rb-note").first();
            if (StringUtil.isEmpty(attachment.getNote())) {
                noteE.remove();
            } else {
                noteE.select("span").html(attachment.getNote());
            }
        }
        Element rbPayeeBox = document.select(".rb-payee-box").first();
        printPayeeBox(snatcher, redBagOwner, rbPayeeBox);
    }

    private void printPayeeBox(PersonInfo snatcher, PersonInfo redBagOwner, Element rbPayeeBox) {
        int limit = 40;
        long offset = 0;
        rbPayeeBox.attr("limit", limit + "");
        rbPayeeBox.attr("offset", offset + "");
        List<PayRecord> records = payRecordService.pagePayee(redBagOwner.getPerson().getId(), 40, 0);
        long totalPayeeCount = payRecordService.totalPayee(redBagOwner.getPerson().getId());
        long sumPayeeAmount = payRecordService.totalPayeeAmount(redBagOwner.getPerson().getId());
        rbPayeeBox.select(".rb-payee-header span[total]").html(totalPayeeCount + "个");
        String sum = String.format("%s元", new BigDecimal(sumPayeeAmount).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN));
        rbPayeeBox.select(".rb-payee-header span[sum]").html(sum);
        Element ul = rbPayeeBox.select(".rb-payee-persons").first();
        Element cli = ul.select(".rb-payee-person").first().clone();
        Element deliverLi = ul.select(".rb-payee-li-deliver").first().clone();
        ul.empty();
        for (PayRecord record : records) {
            Element li = cli.clone();
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
            ul.appendChild(li);
            ul.appendChild(deliverLi.clone());
        }
    }

}
