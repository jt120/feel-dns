package com.jt.server.dns.controller;

import com.google.common.collect.Multimap;
import com.jt.server.dns.convert.MapHostsConvert;
import com.jt.server.dns.data.ZoneData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/**
 * since 2016/6/25.
 */
@Controller
@RequestMapping("/hosts")
public class DnsController {

    private static final Logger log = LoggerFactory.getLogger(DnsController.class);

    @Resource
    private MapHostsConvert mapHostsConvert;

    @RequestMapping("/show")
    @ResponseBody
    public Object hosts() {
        Multimap<String, String> map = ZoneData.getMap();
        log.info("local map {}", map);
        return mapHostsConvert.convert(map);
    }

    @RequestMapping("/change")
    @ResponseBody
    public String change(@RequestBody Map<String, String> hosts) {
        log.info("change map {}", hosts);
        ZoneData.setMap(mapHostsConvert.reverse().convert(hosts));
        return "ok";
    }
}
