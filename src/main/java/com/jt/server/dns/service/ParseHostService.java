package com.jt.server.dns.service;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.jt.server.dns.data.ZoneData;
import com.jt.server.dns.util.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * since 2016/6/25.
 */
@Service
public class ParseHostService implements DisposableBean, InitializingBean {

    private static Splitter splitter = Splitter.on(" ").omitEmptyStrings().trimResults();

    private static final Logger log = LoggerFactory.getLogger(ParseHostService.class);
    private static final String os = System.getProperty("os.name");

    public void load() {
        List<String> lines = null;
        try {
            String file = getHostFile();
            URL resource = Resources.getResource(file);
            lines = Files.readLines(new File(resource.getFile()), Charsets.UTF_8);
            for (String line : lines) {
                List<String> strings = splitter.splitToList(line);
                if (strings.size() == 2) {
                    log.info("load local host {} {}", strings.get(0), strings.get(1));
                    ZoneData.putIp(strings.get(0), strings.get(1));
                }
            }
        } catch (IOException e) {
            log.warn("parse local host fail", e);
        }

    }

    public static String getHostFile() {
        String file = C.host_file_name;
        //if (StringUtils.containsIgnoreCase(os, "windows")) {
        //    file = ("d:" + file).replace("/","\\\\");
        //}
        return file;
    }

    @Override
    public void destroy() throws Exception {
        String file = getHostFile();
        Multimap<String, String> map = ZoneData.getMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entries()) {
            sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
        }
        log.info("close app and write info to file {}", sb);
        Files.write(sb.toString(), new File(file), Charsets.UTF_8);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
    }
}
