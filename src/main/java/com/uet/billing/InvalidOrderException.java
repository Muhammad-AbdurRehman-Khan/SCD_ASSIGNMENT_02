package com.uet.billing;

/** Thrown when an incoming order does not pass validation. The message is the text shown to the user. */
public class InvalidOrderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidOrderException(String message) {
        super(message);
    }
}
