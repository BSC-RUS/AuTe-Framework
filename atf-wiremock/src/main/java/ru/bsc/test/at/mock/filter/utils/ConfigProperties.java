package ru.bsc.test.at.mock.filter.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by lenovo on 07.02.2019.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfigProperties {
    private boolean staticBoundaryEnabled;
    private int tmpThresholdSize;
    private int filesThresholdSize;
}
