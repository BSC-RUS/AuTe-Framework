package ru.bsc.test.at.mock.wiremock.transformers;

import org.junit.Test;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UrlParametersExtractorTest {
  private final UrlParametersExtractor extractor = new UrlParametersExtractor();

  @Test
  public void testExtractPathSegmentsSuccess() throws Exception {
    URL url = new URL("http://localhost:1398/bsc-wire-mock/clients/42");
    List<String> pathSegments = extractor.extractPathSegments(url);
    assertEquals(3, pathSegments.size());
    assertEquals("bsc-wire-mock", pathSegments.get(0));
    assertEquals("clients", pathSegments.get(1));
    assertEquals("42", pathSegments.get(2));
  }

  @Test
  public void testExtractQueryParameterSuccess() throws Exception {
    URL url = new URL("http://localhost:1398/bsc-wire-mock/clients?id=42");
    Map<String, Object> parameters = extractor.extractQueryParameters(url);
    assertEquals(1, parameters.size());
    assertEquals("42", parameters.get("id"));
  }

  @Test
  public void testExtractUrlEncodedQueryParameter() throws Exception {
    String value = URLEncoder.encode("Иван", StandardCharsets.UTF_8.toString());
    URL url = new URL("http://localhost:1398/bsc-wire-mock/clients?name=" + value);
    Map<String, Object> parameters = extractor.extractQueryParameters(url);
    assertEquals(1, parameters.size());
    assertEquals("Иван", parameters.get("name"));
  }

  @Test
  public void testExtractArrayQueryParameter() throws Exception {
    URL url = new URL("http://localhost:1398/bsc-wire-mock/clients?values=1&values=2&values=3");
    Map<String, Object> parameters = extractor.extractQueryParameters(url);
    assertEquals(1, parameters.size());
    List values = (List) parameters.get("values");
    assertEquals(3, values.size());
    assertEquals("1",values.get(0));
    assertEquals("2",values.get(1));
    assertEquals("3",values.get(2));
  }

  @Test
  public void testExtractEmptyQueryParameter() throws Exception {
    URL url = new URL("http://localhost:1398/bsc-wire-mock/clients?vip");
    Map<String, Object> parameters = extractor.extractQueryParameters(url);
    assertEquals(1, parameters.size());
    assertNull(parameters.get("vip"));
  }
}