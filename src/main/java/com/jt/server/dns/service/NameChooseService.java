package com.jt.server.dns.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * choose a ip for name
 * since 2016/6/24.
 */
@Service
public class NameChooseService {

    private static final Logger log = LoggerFactory.getLogger(NameChooseService.class);

    @Resource
    private UpstreamStateService upstreamStateService;

    public String choose(Collection<String> ips) {
        if (ips.size() == 1) {
            String next = ips.iterator().next();
            log.info("only one ip {}", next);
            return next;
        }
        return chooseBestUpstream(ips);
    }

    private String chooseBestUpstream(Collection<String> ips) {
        int max = 0;
        int maxIndex = -1;
        String c = null;
        int size = ips.size();
        List<String> cips = (List<String>) ips;
        for (int i = 0; i < size; i++) {
            int count = upstreamStateService.redState(cips.get(i));
            if (max < count) {
                max = count;
                maxIndex = i;
            }
        }
        if (maxIndex > -1) {
            log.info("choose ip {} {}", cips.get(maxIndex), max);
            c = cips.get(maxIndex);
        }
        return c;
    }
}
