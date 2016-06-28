package com.jt.server.dns.convert;

import com.google.common.base.Converter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * since 2016/6/25.
 */
@Component
public class MapHostsConvert extends Converter<Multimap<String, String>, Map<String, String>> {


    @Override
    protected Map<String, String> doForward(Multimap<String, String> stringStringMultimap) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(stringStringMultimap.size());
        Collection<Map.Entry<String, String>> entries = stringStringMultimap.entries();
        for (Map.Entry<String, String> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @Override
    protected Multimap<String, String> doBackward(Map<String, String> stringStringMap) {
        Multimap<String, String> map = ArrayListMultimap.create();
        Set<Map.Entry<String, String>> entries = stringStringMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
