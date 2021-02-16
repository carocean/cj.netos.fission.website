package cj.netos.fission.model;

/**
 * Table: recommended
 */
public class Recommended {
    /**
     * Column: recommender
     * Remark: 推荐给谁了
     */
    private String recommender;

    /**
     * Column: person
     * Remark: 推荐给推荐者的人
     */
    private String person;
    private  boolean isWaitingTask;
    private String didTask;
    /**
     * Column: ctime
     * Remark: 什么时候推荐的
     */
    private String ctime;

    public boolean isWaitingTask() {
        return isWaitingTask;
    }

    public void setWaitingTask(boolean waitingTask) {
        isWaitingTask = waitingTask;
    }

    public String getDidTask() {
        return didTask;
    }

    public void setDidTask(String didTask) {
        this.didTask = didTask;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender == null ? null : recommender.trim();
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person == null ? null : person.trim();
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime == null ? null : ctime.trim();
    }
}