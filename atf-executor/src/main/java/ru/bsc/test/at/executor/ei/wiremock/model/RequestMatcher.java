package ru.bsc.test.at.executor.ei.wiremock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by lenovo on 28.11.2018.
 */

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestMatcher {
    private String contains;
    private String equalToJson;
    private String equalToXml;
    private String matchesXPath;
}
