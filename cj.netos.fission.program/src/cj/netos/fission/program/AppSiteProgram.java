package cj.netos.fission.program;

import cj.netos.fission.IBuyerService;
import cj.netos.fission.IPersonRecommendBucketService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.IRandRecommendService;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.Destination;
import cj.studio.gateway.socket.app.GatewayAppSiteProgram;
import cj.studio.gateway.socket.app.ProgramAdapterType;

@CjService(name = "$.cj.studio.gateway.app", isExoteric = true)
public class AppSiteProgram extends GatewayAppSiteProgram {

    @Override
    protected void onstart(Destination dest, String assembliesHome, ProgramAdapterType type) throws CircuitException {
        IPersonService personService = (IPersonService) site.getService("personService");
        personService.createGeoIndex();
        load();
    }

    private void load() throws CircuitException {
        IRandRecommendService randRecommendService = (IRandRecommendService) site.getService("randRecommendService");
        int count = 10000;
        CJSystem.logging().info(getClass(), String.format("欢迎页用户缓冲设定大小为：%s，准备缓冲...", count));
        long cachedCount = randRecommendService.cachePersons(count);
        CJSystem.logging().info(getClass(), String.format("欢迎页用户缓冲完成，实际缓冲了：%s个", cachedCount));

        CJSystem.logging().info(getClass(), String.format("准备缓冲出纳柜台有钱的用户..."));
        IBuyerService buyerService = (IBuyerService) site.getService("buyerService");
        cachedCount = buyerService.cacheHasCashierBalance();
        CJSystem.logging().info(getClass(), String.format("缓冲出纳柜台有钱的用户完成，实际缓冲：%s个", cachedCount));
    }
}
