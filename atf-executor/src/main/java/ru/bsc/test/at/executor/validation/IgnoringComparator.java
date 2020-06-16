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
import org.skyscreamer.jsonassert.*;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.*;
import static ru.bsc.test.at.executor.utils.StreamUtils.nullSafeStream;

/**
 * Created by rrudakov on 10/7/16.
 * Project name bcs-rest-at
 */
public class IgnoringComparator extends CustomComparator {
    private static final String IGNORE = "*ignore*";
    //Ignored tags in Mock requests
    private List<Customization> customizations;

    public IgnoringComparator(JSONCompareMode mode) {
        super(mode);
    }

    public IgnoringComparator(JSONCompareMode mode, List<Customization> customizations) {
        super(mode, customizations.toArray(new Customization[0]));
        this.customizations = customizations;
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
        if (IGNORE.equals(expectedValue)) {
            return;
        }

        Customization customization = getCustomization(prefix);
        if (expectedValue instanceof String && actualValue instanceof String && ((String) expectedValue).contains(IGNORE)) {
            if (!MaskComparator.compare((String) expectedValue, (String) actualValue)) {
                result.fail(prefix, expectedValue, actualValue);
            }
        } else if (customization != null) {
            try {
                if (!customization.matches(prefix, actualValue, expectedValue, result)) {
                    result.fail(prefix, expectedValue, actualValue);
                }
            }
            catch (ValueMatcherException e) {
                result.fail(prefix, e);
            }
        } else {
            super.compareValues(prefix, expectedValue, actualValue, result);
        }
    }

    @Override
    public void compareJSONArrayOfJsonObjects(String prefix, JSONArray expected, JSONArray actual, JSONCompareResult result) throws JSONException {
        String uniqueKey = findUniqueKeyWithSupportIgnore(expected, prefix);
        if (uniqueKey == null && isAllObjectEquals(expected, prefix)) {
            compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
            return;
        }
        if (uniqueKey == null || !isUsableAsUniqueKeyWithSupportIgnore(uniqueKey, actual, prefix)) {
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

    private boolean isAllObjectEquals(JSONArray array, String key) throws JSONException {
        JSONObject firstObject = (JSONObject) array.get(0);
        Map<String, Object> values = nullSafeStream(getKeys(firstObject)).collect(toMap(field -> field, firstObject::get));
        for (int i = 1; i < array.length(); i++) {
            JSONObject item = (JSONObject) array.get(i);
            if (!compareObjects(values, item, key)) {
                return false;
            }
        }
        return true;
    }

    private boolean compareObjects(Map<String, Object> values, JSONObject object, String key) {
        return values.entrySet().stream().allMatch(field -> {
            if (object.has(field.getKey())) {
                Object value = object.get(field.getKey());
                if (isSimpleValue(value)) {
                    return getCustomization(key + "." + field.getKey()) != null || Objects.equals(field.getValue(), object.get(field.getKey()));
                }
            }
            return true;
        });
    }

    private String findUniqueKeyWithSupportIgnore(JSONArray expected, String key) throws JSONException {
        return getKeys((JSONObject) expected.get(0)).stream()
            .filter(candidate -> isUsableAsUniqueKeyWithSupportIgnore(candidate, expected, key))
            .findFirst().orElse(null);
    }

    private boolean isUsableAsUniqueKeyWithSupportIgnore(String candidate, JSONArray array, String key) throws JSONException {
        if (getCustomization(key + "." + candidate) != null) {
            return false;
        }

        Set<Object> seenValues = new HashSet<>();
        for (int i = 0 ; i < array.length() ; i++) {
            JSONObject o = (JSONObject) array.get(i);
            if (o.has(candidate)) {
                Object value = o.get(candidate);
                if (isSimpleValue(value) && !seenValues.contains(value) && !IGNORE.equals(value)) {
                    seenValues.add(value);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private Customization getCustomization(String path) {
        StringBuilder correctPath = new StringBuilder(path.replaceAll("\\[.*?]", ""));
        if (correctPath.indexOf(".") == 0) {
            correctPath.deleteCharAt(0);
        }
        if (correctPath.length() != 0 && correctPath.lastIndexOf(".") == correctPath.length() - 1) {
            correctPath.deleteCharAt(correctPath.length() - 1);
        }
        return nullSafeStream(customizations)
            .filter(c -> c.appliesToPath(correctPath.toString()))
            .findFirst().orElse(null);
    }
}
