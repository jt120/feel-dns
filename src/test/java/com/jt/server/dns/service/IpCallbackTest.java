package com.jt.server.dns.service;

import com.jt.server.dns.util.C;
import org.junit.Test;

public class IpCallbackTest {


    @Test
    public void test01() throws Exception {
        String path = "/upstreams/names/flash.qunar.com/169.254.255.13";
        String replace = path.replace(C.name_parent_path, "");
        String name = replace.substring(1, replace.lastIndexOf("/"));
        String ip = path.substring(path.lastIndexOf("/") + 1);
        System.out.println(name);
        System.out.println(ip);
    }
}