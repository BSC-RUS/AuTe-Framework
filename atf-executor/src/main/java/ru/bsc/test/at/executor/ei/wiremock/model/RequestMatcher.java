package ru.bsc.test.at.executor.ei.wiremock.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lenovo on 28.11.2018.
 */

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestMatcher {

    public static final String TYPE_EQUAL_TO_XML = "equalToXml";
    public static final String XPATH = "XPath";
    public static final String TYPE_EQUAL_TO_JSON = "equalToJson";
    public static final String CONTAINS = "contains";

    private String contains;
    private String equalToJson;
    private String equalToXml;
    private String matchesXPath;

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
        }
        return newRequestMatcher;
    }

    @JsonIgnore
    public boolean isPresent() {
        return !StringUtils.isNoneEmpty(contains, equalToJson, equalToXml, matchesXPath);
    }
}
