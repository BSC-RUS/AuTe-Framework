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

package ru.bsc.test.at.mock.filter.utils;

import javax.servlet.ServletRequest;
import java.util.Random;

import static ru.bsc.test.at.mock.filter.utils.MultipartToBase64ConverterServletRequest.BOUNDARY;
import static ru.bsc.test.at.mock.filter.utils.MultipartToBase64ConverterServletRequest.EQUAL_SIGN;
import static ru.bsc.test.at.mock.filter.utils.MultipartToBase64ConverterServletRequest.SEMICOLON;

/**
 * Created by lenovo on 08.02.2019.
 */
public class ConvertedRequestBody {

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private StringBuilder allDataBody = new StringBuilder();
    private String staticBoundary;
    private long staticBoundarySeed;
    private String initBoundary;
    private boolean isStaticBoundaryEnabled;

    public ConvertedRequestBody(ServletRequest initSeed, boolean isStaticBoundaryEnabled) {
        this.initBoundary = getCurrentBoundary(initSeed);
        this.isStaticBoundaryEnabled = isStaticBoundaryEnabled;
    }

    public StringBuilder getAllDataBody() {
        return allDataBody;
    }

    public void setAllDataBody(StringBuilder allDataBody) {
        this.allDataBody = allDataBody;
    }

    public String getStaticBoundary() {
        if(!isStaticBoundaryEnabled) {
            return this.initBoundary;
        }
        if(staticBoundary == null) {
            if(getStaticBoundarySeed() == 0) {
                staticBoundary = this.initBoundary;
            } else {
                staticBoundary = generateBoundary(getStaticBoundarySeed());
            }
        }
        return staticBoundary;
    }

    public void setStaticBoundary(String staticBoundary) {
        this.staticBoundary = staticBoundary;
    }

    public long getStaticBoundarySeed() {
        return staticBoundarySeed;
    }

    public void setStaticBoundarySeed(long staticBoundarySeed) {
        if(this.staticBoundarySeed == 0) {
            this.staticBoundarySeed = staticBoundarySeed;
        }
    }

    protected String generateBoundary(long seed) {
        final StringBuilder buffer = new StringBuilder();
        final Random rand = new Random(seed);
        final int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    private String getCurrentBoundary(ServletRequest request) {
        String[] partsContentType = request.getContentType().split(SEMICOLON);
        for(int i = 0; i < partsContentType.length; i++) {
            if(partsContentType[i].trim().startsWith(BOUNDARY)) {
                String[] boundaryStrArr = partsContentType[i].split(EQUAL_SIGN);
                return boundaryStrArr[1];
            }
        }
        return "";
    }
}
