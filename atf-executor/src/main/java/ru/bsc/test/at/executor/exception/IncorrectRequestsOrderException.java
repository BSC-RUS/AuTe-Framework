package ru.bsc.test.at.executor.exception;

public class IncorrectRequestsOrderException extends RuntimeException {

    public IncorrectRequestsOrderException(){
        super("Incorrect request order");
    }
}
