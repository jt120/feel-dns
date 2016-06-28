package com.jt.server.dns.service;

import com.google.common.primitives.Ints;
import com.jt.server.dns.data.ZoneData;
import com.jt.server.dns.util.C;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * parse zk info
 * since 2016/6/24.
 */
@Service
public class ZkService implements Watcher, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(ZkService.class);

    private ZooKeeper zk;
    @Value("${zk.ip}")
    private String zkIp;
    @Value("${zk.port}")
    private int zkPort;
    private int sessionTimeout = 15000;

    private volatile boolean stop = false;
    @Resource
    private UpstreamStateService upstreamStateService;

    @Override
    public void afterPropertiesSet() throws Exception {
        startZk();
        if (!stop) {
            getNames();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (zk != null) {
            try {
                zk.close();
                stop = true;
            } catch (InterruptedException e) {
                log.warn("close zk fail", e);
                stop = true;
            }
        }
    }

    void startZk() {
        try {
            zk = new ZooKeeper(zkIp + ":" + zkPort, sessionTimeout, this);
        } catch (IOException e) {
            log.warn("start zk fail", e);
            stop = true;
        }
    }

    public boolean isStop() {
        return stop;
    }

    private IpCallback cb = new IpCallback();

    private void parseHostInfo(String name, Collection<String> ips) {
        log.info("name {}, ips {}", name, ips);
        //name = name.replace(root_path + "/", "");


        for (String ip : ips) {
            getIpData(name, ip);
        }
    }

    private void getIpData(String name, String ip) {
        String path = null;
        if (ip == null) {
            path = name;
        } else {
            path = name + "/" + ip;
        }
        log.info("get ip data path {}", path);
        zk.getData(path, upstreamStateChangeWatcher, cb, null);
    }

    private UpstreamStateChangeWatcher upstreamStateChangeWatcher = new UpstreamStateChangeWatcher();


    private class UpstreamStateChangeWatcher implements Watcher {

        @Override
        public void process(WatchedEvent e) {
            log.info("got event: {}", e);
            if (e.getType() == Event.EventType.NodeDataChanged) {
                getIpData(e.getPath(), null);
            }
        }
    }

    private class IpCallback implements AsyncCallback.DataCallback {

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    break;
                case NONODE:
                    break;
                case OK:
                    String replace = path.replace(C.name_parent_path, "");
                    String name = replace.substring(1, replace.lastIndexOf("/"));
                    String ip = path.substring(path.lastIndexOf("/") + 1);
                    String s = new String(data);
                    log.info("path data {} {}", path, s);
                    String[] split = StringUtils.split(s, ":");
                    if (split != null) {
                        if (split.length != 2) {
                            log.info("not match {}", s);
                            return;
                        }
                        Collection<String> zips = ZoneData.getIp(name);
                        if (CollectionUtils.isEmpty(zips) || !zips.contains(ip)) {
                            log.info("add new upstream name {} ip {}", name, ip);
                            ZoneData.putIp(name, ip);
                        }
                        parseStateInfo(name, ip, Ints.tryParse(split[0]), Ints.tryParse(split[1]));
                    }


                    break;
                default:
                    log.error("Error when reading data.",
                            KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    }

    private void parseStateInfo(String name, String ip, int red, int blue) {
        upstreamStateService.updateRedState(ip, red);
        upstreamStateService.updateBlueState(ip, blue);
        log.info("update state {} {} {} {}", name, ip, red, blue);
        if (red <= 0) {
            log.warn("remove state {} {} {} {}", name, ip, red, blue);
            ZoneData.removeIp(name, ip);
        }
    }

    @Override
    public void process(WatchedEvent e) {
        log.info("got event: {}", e);
        if (e.getType() == Event.EventType.None) {
            switch (e.getState()) {
                case SyncConnected:
                    log.info("sync connected");
                    break;
                case Disconnected:
                    stop = true;
                    log.info("disconnected");
                    break;
                case Expired:
                    stop = true;
                    log.error("Session expiration");
                default:
                    break;
            }
        }
    }


    private Watcher namesChangeWatcher = new Watcher() {
        public void process(WatchedEvent e) {
            if (e.getType() == Event.EventType.NodeChildrenChanged) {
                getNames();
            }
        }
    };

    private Watcher ipsChangeWatcher = new Watcher() {
        public void process(WatchedEvent e) {
            if (e.getType() == Event.EventType.NodeChildrenChanged) {
                log.info("ips change path {}", e.getPath());
                getIps(e.getPath());
            }
        }
    };

    public void getNames() {
        zk.getChildren(C.name_parent_path,
                namesChangeWatcher,
                namesGetChildrenCallback,
                null);
    }

    private void getIps(Object obj) {
        log.info("get ips {}", obj);
        if (obj instanceof String) {
            getIp((String) obj);
        } else if (obj instanceof List) {
            List<String> list = (List<String>) obj;
            for (String name : list) {
                getIp(name);
            }
        }


    }

    private void getIp(String name) {
        String path = null;
        if (name.contains(C.root_path)) {
            path = name;
        } else {
            path = C.name_parent_path + "/" + name;
        }
        zk.getChildren(path,
                ipsChangeWatcher,
                ipsGetChildrenCallback,
                null);
    }

    private AsyncCallback.ChildrenCallback namesGetChildrenCallback = new AsyncCallback.ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    getNames();
                    break;
                case OK:
                    log.info("got names {}", children);
                    getIps(children);
                    break;
                default:
                    log.error("getChildren failed",
                            KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };


    private AsyncCallback.ChildrenCallback ipsGetChildrenCallback = new AsyncCallback.ChildrenCallback() {
        public void processResult(int rc, String path, Object ctx, List<String> children) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    break;
                case OK:
                    log.info("got ips {}", children);
                    parseHostInfo(path, children);
                    break;
                default:
                    log.error("getChildren failed",
                            KeeperException.create(KeeperException.Code.get(rc), path));
            }
        }
    };
}
