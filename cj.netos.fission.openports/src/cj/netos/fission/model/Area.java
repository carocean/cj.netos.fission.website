package cj.netos.fission.model;

/**
 * Table: area
 */
public class Area {
    /**
     * Column: id
     * Remark: 标识，如： around1000 around5000 district city privence  
     */
    private String id;

    /**
     * Column: label
     * Remark: 周边1公里 周边5公里 所在区县 所在市 所在省
     */
    private String label;

    /**
     * Column: value
     * Remark: 如1000米，5000米，也可能没有值，如所在市，但也可以指定区域，如新郑市
     */
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label == null ? null : label.trim();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? null : value.trim();
    }
}