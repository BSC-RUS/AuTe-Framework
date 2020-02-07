/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.at.executor.validation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.util.List;
import java.util.Map;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.arrayOfJsonObjectToMap;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.formatUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.getKeys;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.isUsableAsUniqueKey;

/**
 * Created by rrudakov on 10/7/16.
 * Project name bcs-rest-at
 */
public class IgnoringComparator extends CustomComparator {
    private static final String IGNORE = "*ignore*";

    public IgnoringComparator(JSONCompareMode mode) {
        super(mode);
    }

    public IgnoringComparator(JSONCompareMode mode, List<Customization> customizations) {
        super(mode, customizations.toArray(new Customization[0]));
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        if (IGNORE.equals(expectedValue)) {
            return;
        }

        if (expectedValue instanceof String && actualValue instanceof String && ((String) expectedValue).contains(IGNORE)) {
            if (!MaskComparator.compare((String) expectedValue, (String) actualValue)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else {
            super.compareValues(prefix, expectedValue, actualValue, result);
        }
    }

    @Override
    public void compareJSONArrayOfJsonObjects(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        String uniqueKey = findUniqueKey(expected);
        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual)) {
            // An expensive last resort
            recursivelyCompareJSONArray(prefix, expected, actual, result);
            return;
        }
        Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected, uniqueKey);
        Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
        for (Object id : expectedValueMap.keySet()) {
            if (!actualValueMap.containsKey(id)) {
                result.missing(formatUniqueKey(prefix, uniqueKey, id), expectedValueMap.get(id));
                continue;
            }
            JSONObject expectedValue = expectedValueMap.get(id);
            JSONObject actualValue = actualValueMap.get(id);
            compareValues(formatUniqueKey(prefix, uniqueKey, id), expectedValue, actualValue, result);
        }
        for (Object id : actualValueMap.keySet()) {
            if (!expectedValueMap.containsKey(id)) {
                result.unexpected(formatUniqueKey(prefix, uniqueKey, id), actualValueMap.get(id));
            }
        }
    }

    private static String findUniqueKey(JSONArray expected) throws JSONException {
        // Find a unique key for the object (id, name, whatever)
        JSONObject o = (JSONObject)expected.get(0); // There's at least one at this point
        for(String candidate : getKeys(o)) {
            if (!IGNORE.equals(o.get(candidate)) && isUsableAsUniqueKey(candidate, expected)) {
                return candidate;
            }
        }
        // No usable unique key :-(
        return null;
    }
}
