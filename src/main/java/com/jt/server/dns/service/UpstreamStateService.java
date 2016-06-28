package com.jt.server.dns.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * since 2016/6/24.
 */
@Service
public class UpstreamStateService {

    //body
    private static Map<String, Integer> red = new HashMap<String, Integer>();
    //magic
    private static Map<String, Integer> blue = new HashMap<String, Integer>();

    //body depend on memory
    public int redState(String ip) {
        Integer ret = red.get(ip);
        if (ret != null) {
            return ret;
        }
        return 0;
    }

    //magic depend on cpu usage
    public int blueState(String ip) {
        Integer ret = blue.get(ip);
        if (ret != null) {
            return ret;
        }
        return 0;
    }

    public void updateRedState(String ip, int count) {
        red.put(ip, count);
    }

    public void updateBlueState(String ip, int count) {
        blue.put(ip, count);
    }

}
