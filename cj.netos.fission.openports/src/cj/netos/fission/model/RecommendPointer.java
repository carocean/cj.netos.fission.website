package cj.netos.fission.model;

/**
 * Table: recommend_pointer
 */
public class RecommendPointer {
    /**
     * Column: person
     * Remark: 用户
     */
    private String person;

    /**
     * Column: readed_ctime
     * Remark: 对应person的ctime
     */
    private String readedCtime;

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person == null ? null : person.trim();
    }

    public String getReadedCtime() {
        return readedCtime;
    }

    public void setReadedCtime(String readedCtime) {
        this.readedCtime = readedCtime == null ? null : readedCtime.trim();
    }
}