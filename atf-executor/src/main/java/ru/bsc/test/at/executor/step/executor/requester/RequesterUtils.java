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

package ru.bsc.test.at.executor.step.executor.requester;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.util.Assert;
import ru.bsc.test.at.executor.helper.client.api.ClientResponse;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.validation.IgnoringComparator;
import ru.bsc.test.at.executor.validation.MaskComparator;

import java.util.Map;
import java.util.Random;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Pavel Golovkin
 */
@Slf4j
public class RequesterUtils {

    static final int DEFAULT_POLLING_RETRY_COUNT = 50;
    static final int MIN_POLLING_DELAY_MS = 100;
    private static final int MAX_POLLING_DELAY_MS = 1000 * 30;

    /**
     * Коэффициент экспоненциального нарастания задержки
     */
    private static final double POLLING_DELAY_MULTIPLIER = 1.5;
    /**
     * Коэффициент для определения случайности задержки
     */
    private static final double POLLING_DELAY_JITTER = 0.1;

    protected RequesterUtils() {
        //to avoid init
    }

    static void saveValuesByJsonXPath(Step step, String responseContent, Map<String, Object> scenarioVariables) {
        log.debug("Save values by json xpath {} {} {}", step, responseContent, scenarioVariables);
        if (isNotEmpty(responseContent) && isNotEmpty(step.getJsonXPath())) {
            String[] lines = step.getJsonXPath().split("\\r?\\n");
            for (String line : lines) {
                String[] lineParts = line.split("=", 2);
                String parameterName = lineParts[0].trim();
                String jsonXPath = lineParts[1];
                scenarioVariables.put(parameterName, JsonPath.read(responseContent, jsonXPath).toString());
            }
        }
    }

    static void compareResponse(Step step, String expectedResponse, ClientResponse responseData) throws Exception {
        if (step.getExpectedResponseIgnore()) {
            return;
        }

        if (step.getResponseCompareMode() == null) {
            jsonComparing(expectedResponse, responseData, step.getJsonCompareMode());
        } else {
            log.debug("Response compare mode {}, ", step.getResponseCompareMode());
            switch (step.getResponseCompareMode()) {
                case FULL_MATCH:
                    if (!StringUtils.equals(expectedResponse, responseData.getContent())) {
                        throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + responseData.getContent());
                    }
                    break;
                case IGNORE_MASK:
                    if (!MaskComparator.compare(expectedResponse, responseData.getContent())) {
                        throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + responseData.getContent());
                    }
                    break;
                default:
                    jsonComparing(expectedResponse, responseData, step.getJsonCompareMode());
                    break;
            }
        }
    }

    static void checkScenarioVariables(Step step, Map<String, Object> scenarioVariables) throws Exception {
        if (step.getSavedValuesCheck() != null) {
            for (Map.Entry<String, String> entry : step.getSavedValuesCheck().entrySet()) {
                String valueExpected = entry.getValue() == null ? "" : entry.getValue();
                for (Map.Entry<String, Object> savedVal : scenarioVariables.entrySet()) {
                    String key = String.format("%%%s%%", savedVal.getKey());
                    valueExpected = valueExpected.replaceAll(key, String.valueOf(savedVal.getValue()));
                }
                String valueActual = String.valueOf(scenarioVariables.get(entry.getKey()));
                if (!valueExpected.equals(valueActual)) {
                    throw new Exception("Saved value " + entry.getKey() + " = " + valueActual + ". Expected: " + valueExpected);
                }
            }
        }
    }

    static void checkResponseBody(Step step, String expectedResponse, String actualResponse) throws Exception {
        if (!step.getExpectedResponseIgnore()) {
            if (step.getResponseCompareMode() == null) {
                jsonComparing(expectedResponse, actualResponse, step.getJsonCompareMode());
            } else {
                switch (step.getResponseCompareMode()) {
                    case FULL_MATCH:
                        if (!StringUtils.equals(expectedResponse, actualResponse)) {
                            throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + actualResponse);
                        }
                        break;
                    case IGNORE_MASK:
                        if (!MaskComparator.compare(expectedResponse, actualResponse)) {
                            throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + actualResponse);
                        }
                        break;
                    default:
                        jsonComparing(expectedResponse, actualResponse, step.getJsonCompareMode());
                        break;
                }
            }
        }
    }

    static boolean tryUsePolling(Step step, ClientResponse clientResponse) {
        Assert.notNull(clientResponse, "client response must not be null");
        String content = clientResponse.getContent();
        log.debug("trying use polling {} {}", step, content);
        if (!step.getUsePolling()) {
            return false;
        }
        boolean retry = true;
        try {
            if (StringUtils.isNotEmpty(content) && JsonPath.read(content, step.getPollingJsonXPath()) != null) {
                log.info("Required attribute for polling found in path {}. Stop polling", step.getPollingJsonXPath());
                retry = false;
            }
        } catch (PathNotFoundException | IllegalArgumentException e) {
            log.info("Required attribute for polling not found in path {}. Continue polling", step.getPollingJsonXPath());
            retry = true;
        }
        log.debug("trying use polling? Is - {}", retry);
        return retry;
    }

    /**
     * Расчет задержки по алгоритму экспоненциальной выдержки. Задержка увеличивается
     * в зависимости от коэффициента POLLING_DELAY_MULTIPLIER и является случайной.
     *
     * @param prevDelay Задержка перед предыдущим запросом
     * @return Задержка перед следующим запросом
     */
    static long calculateNextPollingDelay(long prevDelay) {
        long nextDelay = (long) Math.min(prevDelay * POLLING_DELAY_MULTIPLIER, MAX_POLLING_DELAY_MS);
        Random random = new Random();
        nextDelay += random.nextInt((int) (nextDelay * POLLING_DELAY_JITTER));
        return nextDelay;
    }

    private static void jsonComparing(String expectedResponse, String responseContent, String jsonCompareMode) throws Exception {
        log.debug("Json comparing {} {} {}", expectedResponse, responseContent, jsonCompareMode);
        if ((StringUtils.isNotEmpty(expectedResponse) || StringUtils.isNotEmpty(responseContent)) &&
                (!responseContent.equals(expectedResponse))) {
            try {
                JSONAssert.assertEquals(
                        expectedResponse == null ? "" : expectedResponse.replaceAll(" ", " "),
                        responseContent.replaceAll(" ", " "), // Fix broken space in response
                        new IgnoringComparator(StringUtils.isEmpty(jsonCompareMode) ?
                                JSONCompareMode.NON_EXTENSIBLE :
                                JSONCompareMode.valueOf(jsonCompareMode))
                );
            } catch (Error assertionError) {
                throw new Exception(assertionError);
            }
        }
    }

    private static void jsonComparing(String expectedResponse, ClientResponse responseData, String jsonCompareMode) throws Exception {
            jsonComparing(expectedResponse, responseData.getContent(), jsonCompareMode);
    }
}
