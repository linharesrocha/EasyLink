package br.com.easylink.easylinkservice.application.exceptions;

public class CustomKeyAlreadyExistsException extends RuntimeException {
    public CustomKeyAlreadyExistsException(String message) {
        super(message);
    }
}
