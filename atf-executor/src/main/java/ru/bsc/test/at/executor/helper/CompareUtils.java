package ru.bsc.test.at.executor.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;
import ru.bsc.test.at.executor.exception.ComparisonException;
import ru.bsc.test.at.executor.exception.JsonParsingException;
import ru.bsc.test.at.executor.validation.IgnoringComparator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Slf4j
public class CompareUtils {

    private static final String IGNORE = "\\u002A" + "ignore" + "\\u002A";
    private static final String STR_SPLIT = "(\r\n|\n\r|\r|\n|" + IGNORE + ")";
    private static final String CLEAR_STR_PATTERN = "(\r\n|\n\r|\r|\n)";
    private static final String NBS_PATTERN = "[\\s\\u00A0]";

    private CompareUtils() {
    }

    static ComparisonResult compareRequestAsXml(String expectedRequestBody, String actualRequestBody, Set<String> ignoredTags) {
        Diff diff = DiffBuilder.compare(StringUtils.defaultString(expectedRequestBody))
                               .withTest(StringUtils.defaultString(actualRequestBody))
                               .checkForIdentical()
                               .ignoreComments()
                               .ignoreWhitespace()
                               .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                               .withDifferenceEvaluator(new IgnoreTagsDifferenceEvaluator(ignoredTags))
                               .build();

        return new ComparisonResult(diff.hasDifferences(), diff.toString(), expectedRequestBody, actualRequestBody);
    }

    static ComparisonResult compareRequestAsJson(String expectedRequestBody, String actualBody, Set<String> ignoringPaths, String mode) {
        String defaultExpectedRequestBody = StringUtils.defaultString(expectedRequestBody);
        String defaultActualBody = StringUtils.defaultString(actualBody);
        ObjectMapper om = new ObjectMapper();
        try {
            om.readValue(defaultExpectedRequestBody, Object.class);
            om.readValue(defaultActualBody, Object.class);
        } catch (Exception e) {
            throw new JsonParsingException(e);
        }

        ComparisonResult comparisonResult;
        try {
            List<Customization> customizations = ignoringPaths.stream()
                                                              .map(p -> new Customization(p, (o1, o2) -> true))
                                                              .collect(Collectors.toList());

            JSONCompareMode compareMode = StringUtils.isEmpty(mode) ? JSONCompareMode.STRICT : JSONCompareMode.valueOf(mode);
            JSONAssert.assertEquals(defaultExpectedRequestBody.replaceAll("\\s", " "),
                    defaultActualBody.replaceAll("\\s", " "),
                    new IgnoringComparator(compareMode, customizations));

            comparisonResult = new ComparisonResult(false, "", expectedRequestBody, actualBody);
        } catch (Throwable assertionError) {
            if (!(assertionError instanceof AssertionError)) {
              log.error("Exception while parse json {} {} {} {}", expectedRequestBody, actualBody, ignoringPaths, mode, assertionError);
            }
            comparisonResult = new ComparisonResult(true, assertionError.getMessage(), expectedRequestBody, actualBody);
        }

        return comparisonResult;
    }

    static ComparisonResult compareRequestAsString(String expectedRequest, String actualRequest) {
        String defaultExpectedRequest = StringUtils.defaultString(expectedRequest);
        String defaultActualRequest = StringUtils.defaultString(actualRequest);

        String[] split = defaultExpectedRequest.split(STR_SPLIT);
        defaultActualRequest = defaultActualRequest.replaceAll(CLEAR_STR_PATTERN, "").replaceAll(NBS_PATTERN, " ");

        if (split.length == 1 && !Objects.equals(defaultIfNull(split[0], ""), defaultIfNull(defaultActualRequest, ""))) {
            throw new ComparisonException("", defaultExpectedRequest, defaultActualRequest);
        }

        int i = 0;
        boolean notEquals = false;
        StringBuilder diff = new StringBuilder("\n");
        for (String s : split) {
            i = defaultActualRequest.indexOf(s.trim(), i);
            if (i < 0) {
                notEquals = true;
                diff.append(s.trim()).append("\n");
            }
        }

        return new ComparisonResult(notEquals, diff.toString(), expectedRequest, actualRequest);
    }

}
