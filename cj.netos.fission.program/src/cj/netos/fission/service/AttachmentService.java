package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IAttachmentService;
import cj.netos.fission.model.Attachment;
import cj.studio.ecm.annotation.CjService;

@CjService(name = "attachmentService")
public class AttachmentService extends AbstractService implements IAttachmentService {
    static final String _KEY_COL = "fission.mf.attachments";

    @Override
    public Attachment getInfo(String unionid) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.person':'%s'}", _KEY_COL, Attachment.class.getName(), unionid);
        IQuery<Attachment> query = getHome().createQuery(cjql);
        IDocument<Attachment> document = query.getSingleResult();
        if (document == null) {
            return null;
        }
        return document.tuple();
    }
}
