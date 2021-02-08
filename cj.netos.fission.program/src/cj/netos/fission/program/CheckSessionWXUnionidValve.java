package cj.netos.fission.program;

import cj.netos.fission.IPersonService;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.Scope;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.http.HttpCircuit;
import cj.studio.ecm.net.http.HttpFrame;
import cj.studio.ecm.net.session.ISession;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.ultimate.util.StringUtil;

@CjService(name = "checkSessionWXUnionidValve", scope = Scope.multiton)
public class CheckSessionWXUnionidValve implements IAnnotationInputValve {
    @CjServiceSite
    IServiceSite site;
    @CjServiceRef
    IPersonService personService;

    @Override
    public int getSort() {
        return 0;
    }

    @Override
    public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
        pipeline.nextOnActive(inputName, this);
    }

    @Override
    public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
        HttpFrame frame = (HttpFrame) request;
        String relpath = frame.relativePath();
        if ("/".equals(relpath)||relpath.startsWith("/wechat") || isResource(relpath)) {
            pipeline.nextFlow(request, response, this);
            return;
        }
        ISession session=frame.session();
        String unionid= (String) session.attribute("unionid");
        if (StringUtil.isEmpty(unionid)) {
            HttpCircuit circuit = (HttpCircuit) response;
            circuit.content().writeBytes("800 拒绝访问".getBytes());
            return;
        }
        pipeline.nextFlow(request, response, this);
    }

    private boolean isResource(String relpath) {
        int pos = relpath.lastIndexOf(".");
        if (pos < 0) {
            return false;
        }
        String ext = relpath.substring(pos);
        String docs = site.getProperty("site.document");
        String[] names = docs.split("\\|");
        for (String v : names) {
            if (ext.equals(v)) {
//                System.out.println(String.format("----%s %s false",relpath,ext));
                return false;
            }
        }
//        System.out.println(String.format("----%s %s true",relpath,ext));
        return true;
    }

    @Override
    public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
        pipeline.nextOnInactive(inputName, this);
    }
}
