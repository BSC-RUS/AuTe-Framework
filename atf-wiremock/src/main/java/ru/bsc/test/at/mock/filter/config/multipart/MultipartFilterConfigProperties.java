package ru.bsc.test.at.mock.filter.config.multipart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by lenovo on 07.02.2019.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties
@Component
public class MultipartFilterConfigProperties {
    @Value("${multipart.filter.convert.enabled:false}")
    private boolean filterLogicEnabled;
    @Value("${multipart.filter.boundary.static.enabled:false}")
    private boolean  staticBoundaryEnabled;
    @Value("${multipart.filter.threshold.tmpdir.file.size:1048576}")
    private int tmpThresholdSize;
    @Value("${multipart.filter.threshold.files.size:52428800}")
    private int filesThresholdSize;
}
