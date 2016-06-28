package com.jt.server.dns.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *
 * since 2016/6/24.
 */
public class ZoneData {

    private static final Logger log = LoggerFactory.getLogger(ZoneData.class);

    private static Multimap<String, String> map = ArrayListMultimap.create();

    public static Collection<String> getIp(String name) {
        return map.get(name);
    }

    public static void putIp(String name, String ip) {
        map.put(name, ip);
    }

    public static void removeIp(String name, String ip) {
        map.remove(name, ip);
    }

    public static void show() {
        log.info("local host {}", map);
    }

    public static Multimap<String, String> getMap() {
        return map;
    }

    public static void setMap(Multimap<String, String> map) {
        ZoneData.map = map;
    }
}
