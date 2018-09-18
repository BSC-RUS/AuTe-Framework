package ru.bsc.test.at.executor.exception;

public class JsonParsingException extends RuntimeException {

    public JsonParsingException(Exception e) {
        super(e);
    }

}
