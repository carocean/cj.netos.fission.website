package cj.netos.fission.model;

/**
 * Table: others_to_me_condition
 */
public class OthersToMeCondition {
    /**
     * Column: person
     */
    private String person;

    /**
     * Column: type
     * Remark: 限定类型，如： - tag限定为标签 - area地域
     */
    private String type;

    /**
     * Column: condition
     * Remark: 条件，如是标签则为标签标识 如是地域则为： - 附件1000米 - 附近5000米 - 所在区县 - 所在市 - 所在省
     */
    private String condition;

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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition == null ? null : condition.trim();
    }
}