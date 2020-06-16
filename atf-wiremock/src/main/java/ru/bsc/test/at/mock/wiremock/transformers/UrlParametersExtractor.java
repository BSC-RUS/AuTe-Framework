package ru.bsc.test.at.mock.wiremock.transformers;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UrlParametersExtractor {
  List<String> extractPathSegments(URL url) {
    return Arrays.asList(url.getPath().substring(1).split("/"));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, Object> extractQueryParameters(URL url) {
    if (url == null) {
      return Collections.emptyMap();
    }
    List<NameValuePair> parameters = URLEncodedUtils.parse(url.getQuery(), StandardCharsets.UTF_8);
    Map<String, Object> result = new LinkedHashMap<>();
    for (NameValuePair parameter : parameters) {
      if (result.containsKey(parameter.getName())) {
        Object currentValue = result.get(parameter.getName());
        if (currentValue instanceof List) {
          ((List) currentValue).add(parameter.getValue());
        } else {
          List<Object> values = new ArrayList<>();
          values.add(currentValue);
          values.add(parameter.getValue());
          result.put(parameter.getName(), values);
        }
      } else {
        result.put(parameter.getName(), parameter.getValue());
      }
    }
    return result;
  }
}
