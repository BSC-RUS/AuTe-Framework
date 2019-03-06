package ru.bsc.test.at.mock.filter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Map<String, List>
 */
class MultiMap {

    private Map<String, List> map = new HashMap<>();

    Map<String, List> getMap(){
        return map;
    }

    void add(String key, Object value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    List getValues(String key){
        return map.get(key);
    }

    String getValue(String key){

        List list = getValues(key);
        if (list != null && !list.isEmpty())
            return list.get(0).toString();

        return "";
    }
}