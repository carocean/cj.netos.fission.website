package cj.netos.fission.model;

/**
 * Table: attachment
 */
public class Attachment {
    /**
     * Column: id
     */
    private String id;

    /**
     * Column: src
     */
    private String src;

    /**
     * Column: person
     */
    private String person;

    /**
     * Column: type
     * Remark: image图片 video视频 qrcode二维码
     */
    private String type;

    /**
     * Column: ctime
     */
    private String ctime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src == null ? null : src.trim();
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person == null ? null : person.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime == null ? null : ctime.trim();
    }
}