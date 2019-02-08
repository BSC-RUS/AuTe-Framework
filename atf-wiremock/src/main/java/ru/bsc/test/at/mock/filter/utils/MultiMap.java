package ru.bsc.test.at.mock.filter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Map<String, List>
 */
public class MultiMap {

    Map<String, List> map = new HashMap();

    public Map getMap(){
        return map;
    }

    public void add(String key, Object value) {

        List list = map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        list.add(value);
    }

    public List getValues(String key){
        return map.get(key);
    }

    public String getValue(String key){

        List list = getValues(key);
        if (list != null && !list.isEmpty())
            return list.get(0).toString();

        return "";
    }
}