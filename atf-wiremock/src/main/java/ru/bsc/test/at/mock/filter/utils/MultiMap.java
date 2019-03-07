/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

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