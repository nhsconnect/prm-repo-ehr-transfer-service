package uk.nhs.prm.deductions.gp2gpmessagehandler.services;

public class HttpException extends Exception {
    public HttpException(String message, Exception cause) {
        super(message,cause);
    }

    public HttpException() {
        super();
    }

    public HttpException(String message) {
        super(message);
    }
}
