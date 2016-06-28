package com.jt.server.dns.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

public class ZoneDataTest {

    ObjectMapper mapper = new ObjectMapper();
    @Test
    public void test01() throws Exception {
        Multimap<String, String> map = ArrayListMultimap.create();
        map.put("hello", "1");
        map.put("hello", "2");
        map.put("hello1", "1");
        map.put("hello2", "3");
        System.out.println(map);
        System.out.println(mapper.writeValueAsString(map));
    }

}