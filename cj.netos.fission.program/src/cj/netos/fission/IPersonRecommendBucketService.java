package cj.netos.fission;

import java.util.List;

/**
 * 用户推荐桶
 */
public interface IPersonRecommendBucketService {
    List<String> recommend(String unionid, int count);
}
