/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

package ru.bsc.test.at.mock.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.mock.filter.config.multipart.MultipartFilterConfigProperties;
import ru.bsc.test.at.mock.filter.utils.MultipartToBase64ConverterServletRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by lenovo on 05.02.2019.
 */
@Slf4j
@Component
public class BinaryBase64ConvertFilter implements Filter {

    private static final String MULTIPART_TYPE    = "multipart/";

    @Autowired
    private MultipartFilterConfigProperties configProperties;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();

        if(isEnabledLogicConverter(contentType)) {
            log.info("It is request with multipart!");
            HttpServletRequest requestWrapper = new MultipartToBase64ConverterServletRequest(httpRequest, configProperties);
            filterChain.doFilter(requestWrapper, response);
        } else {
            filterChain.doFilter(httpRequest,  response);
        }
    }


    private boolean isEnabledLogicConverter(String contentType) {
        return configProperties.isConvertEnabled() &&
                contentType != null && contentType.startsWith(MULTIPART_TYPE);
    }

    @Override
    public void destroy() {

    }


}
