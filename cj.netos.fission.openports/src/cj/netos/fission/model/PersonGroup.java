package cj.netos.fission.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户当前组<br>
 *     如果用户没有点完当前推荐的一组用户，则不可换组。
 *     当前组点完了，则用户必须完成换组任务才可切换下一组。
 *     如果未完就是退出重进也是显示当前组成员，界面会提醒哪个未点击
 *     所以，所谓推荐，不是直接将推荐结果写回客户端，而是写入当前组，客户端只取当前组
 */
public class PersonGroup {
    String groupOwner;//为person标识
    int capacity;
    Map<String,Boolean> workItems;//key为person表示成员;value是否被当前组拥有者点击过。此工作项语义为：成员是否被拥有者点击过
    boolean isDone;
    String ctime;

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public PersonGroup() {
        workItems = new HashMap<>();
    }

    public String getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Map<String, Boolean> getWorkItems() {
        return workItems;
    }

    public void setWorkItems(Map<String, Boolean> workItems) {
        this.workItems = workItems;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
