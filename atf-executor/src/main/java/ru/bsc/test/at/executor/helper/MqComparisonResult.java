package ru.bsc.test.at.executor.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.xmlunit.diff.Diff;

@AllArgsConstructor
@Getter
public class MqComparisonResult {

    private Diff diff;
    private String expectedRequestBody;
    private String actualRequestBody;

    public boolean hasDifferences(){
        return diff.hasDifferences();
    }

}
