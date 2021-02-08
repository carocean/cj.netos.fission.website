package cj.netos.fission;

import cj.netos.fission.model.PropertyTag;

import java.util.List;

public interface IPropertyTagService {
    public  static  String _KEY_COL="fission.mf.property.tags";
    List<PropertyTag> listTag(String unionid);

}
