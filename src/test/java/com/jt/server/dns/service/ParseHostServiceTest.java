package com.jt.server.dns.service;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

public class ParseHostServiceTest {


    @Test
    public void test01() throws Exception {
        URL resource = Resources.getResource("hosts.txt");
        System.out.println(resource);
        List<String> strings = Files.readLines(new File(resource.getFile()), Charsets.UTF_8);
        System.out.println(strings);
    }

    @Test
    public void test03() throws Exception {
        File file = new File("d:/tmp/hosts.txt");
        System.out.println(file.exists());
    }

    @Test
    public void test04() throws Exception {
        String hostFile = ParseHostService.getHostFile();
        File file = new File(hostFile);
        System.out.println(file.exists());
        System.out.println(hostFile);
    }
}