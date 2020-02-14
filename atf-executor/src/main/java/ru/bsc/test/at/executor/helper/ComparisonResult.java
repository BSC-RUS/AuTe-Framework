package ru.bsc.test.at.executor.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.xmlunit.diff.Diff;

@AllArgsConstructor
@Getter
public class ComparisonResult {

    private boolean hasDifferences;
    private String diff;
    private String expectedRequestBody;
    private String actualRequestBody;
}
