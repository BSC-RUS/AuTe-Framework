package ru.bsc.test.at.util;

/**
 * Created by lenovo on 20.02.2019.
 */
public enum MultipartConstant {

    CONVERT_BASE64_IN_MULTIPART("CONVERT_BASE64_IN_MULTIPART");

    private final String value;

    MultipartConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
