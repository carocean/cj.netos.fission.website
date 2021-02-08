package cj.netos.fission;

import cj.netos.fission.model.Person;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WechatUserInfo {
    String openid;
    String nickname;
    int sex;//值为1时是男性，值为2时是女性，值为0时是未知
    String language;
    String city;
    String province;
    String country;//国家，如中国为CN
    String headimgurl;
    Object privilege;//用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
    String unionid;//这是微信统一标识。只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public Object getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Object privilege) {
        this.privilege = privilege;
    }

    public Person toPerson() {
        Person person = new Person();
        person.setAvatarUrl(headimgurl);
        person.setCity(city);
        person.setCountry(country);
        person.setProvince(province);
        person.setTown(null);
        SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd HHmmss");
        String date=format.format(new Date(System.currentTimeMillis()));
        person.setCtime(date);
        person.setGender(sex);
        person.setId(unionid);
        person.setLanguage(language);
        person.setNickName(nickname);
        person.setOpenid(openid);
        return person;
    }
}
