package com.jt.server.dns;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * since 2016/6/24.
 */
public class PattenTest {


    @Test
    public void test01() throws Exception {
        String s = "gitlab.corp.qunar.com.\t0\tIN\tA";
        Pattern compile = Pattern.compile("(.*)\\s\\d\\s\\w{2,}\\sA");
        Matcher matcher = compile.matcher(s);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }

    @Test
    public void test02() throws Exception {
        String s = "flash.qunar.com.";
        System.out.println(s.substring(0, s.length()-1));
    }

}
