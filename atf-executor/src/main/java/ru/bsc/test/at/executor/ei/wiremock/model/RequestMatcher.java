/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the ATF project
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

package ru.bsc.test.at.executor.ei.wiremock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by lenovo on 28.11.2018.
 */

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestMatcher {

    public static final String TYPE_EQUAL_TO_JSON = "equalToJson";
    public static final String TYPE_EQUAL_TO_XML = "equalToXml";
    public static final String XPATH = "XPath";
    public static final String CONTAINS = "contains";
    public static final String MATCHES = "matches";
    public static final String ABSENT = "absent";

    private String equalToJson;
    private String equalToXml;
    private String matchesXPath;
    private String absent;
    private String contains;
    @JsonRawValue
    private String matches;

    @JsonIgnore
    public static RequestMatcher build(String type, String value) {
        RequestMatcher newRequestMatcher = new RequestMatcher();
        if (TYPE_EQUAL_TO_XML.equalsIgnoreCase(type)) {
            newRequestMatcher.setEqualToXml(value);
        } else if (TYPE_EQUAL_TO_JSON.equalsIgnoreCase(type)) {
            newRequestMatcher.setEqualToJson(value);
        } else if (CONTAINS.equalsIgnoreCase(type)) {
            newRequestMatcher.setContains(value);
        } else if (XPATH.equalsIgnoreCase(type)) {
            newRequestMatcher.setMatchesXPath(value);
        } else if (MATCHES.equalsIgnoreCase(type)) {
            newRequestMatcher.setMatches(StringUtils.wrap(value, "\""));
        } else if (ABSENT.equalsIgnoreCase(type)){
            newRequestMatcher.setAbsent("absent_pattern");
        }
        return newRequestMatcher;
    }

    @JsonIgnore
    public boolean isPresent() {
        return isNotEmpty(equalToJson) || isNotEmpty(equalToXml) ||
                isNotEmpty(matchesXPath) || isNotEmpty(contains) ||
                isNotEmpty(matches) || isNotEmpty(absent);
    }
}
