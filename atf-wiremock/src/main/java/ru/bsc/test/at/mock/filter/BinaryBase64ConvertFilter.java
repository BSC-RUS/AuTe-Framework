package ru.bsc.test.at.mock.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.mock.filter.utils.ConfigProperties;
import ru.bsc.test.at.mock.filter.utils.MultipartToBase64ConverterServletRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by lenovo on 05.02.2019.
 */
@Slf4j
@Component
public class BinaryBase64ConvertFilter implements Filter {

    @Value("${multipart.filter.convert.enabled:false}")
    private boolean filterLogicEnabled;
    @Value("${multipart.filter.boundary.static.enabled:false}")
    private boolean  staticBoundaryEnabled;
    @Value("${multipart.filter.threshold.tmpdir.file.size:1048576}")
    private int tmpThresholdSize;
    @Value("${multipart.filter.threshold.files.size:52428800}")
    private int filesThresholdSize;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String contentType = httpRequest.getContentType();

        if(isEnabledLogicConverter(contentType)) {
            log.info("It is request with multipart!");

            ConfigProperties configProperties = new ConfigProperties(staticBoundaryEnabled, tmpThresholdSize, filesThresholdSize);
            HttpServletRequest requestWrapper = new MultipartToBase64ConverterServletRequest(httpRequest, configProperties);

            filterChain.doFilter(requestWrapper, httpResponse);
        } else {
            filterChain.doFilter(httpRequest, httpResponse);
        }
    }


    private boolean isEnabledLogicConverter(String contentType) {
        return filterLogicEnabled &&
                contentType != null && contentType.startsWith(MultipartToBase64ConverterServletRequest.MULTIPART_TYPE);
    }

    @Override
    public void destroy() {

    }


}
