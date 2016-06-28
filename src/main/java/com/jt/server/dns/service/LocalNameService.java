package com.jt.server.dns.service;

import com.jt.server.dns.data.ZoneData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * since 2016/6/24.
 */
@Service
public class LocalNameService {

    @Resource
    private NameChooseService nameChooseService;
    //query ip through name
    public String getIp(String name) {

        Collection<String> ip = ZoneData.getIp(name);
        if (ip != null && !ip.isEmpty()) {
            //有合适的ip
            return nameChooseService.choose(ip);
        }
        return null;
    }

}
