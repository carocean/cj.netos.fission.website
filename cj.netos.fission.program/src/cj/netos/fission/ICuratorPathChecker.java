package cj.netos.fission;

import org.apache.curator.framework.CuratorFramework;

public interface ICuratorPathChecker {
    void check(CuratorFramework framework, String path) throws Exception;
}
