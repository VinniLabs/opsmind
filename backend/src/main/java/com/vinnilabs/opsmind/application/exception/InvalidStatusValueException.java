package com.vinnilabs.opsmind.application.exception;

public class InvalidStatusValueException extends ApplicationException {

    public InvalidStatusValueException(String value) {
        super("INVALID_STATUS_VALUE", "Invalid incident status: " + value);
    }
}

