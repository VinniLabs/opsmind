package com.vinnilabs.opsmind.application.exception;

public class InvalidDiagnosisAccuracyValueException extends ApplicationException {

    public InvalidDiagnosisAccuracyValueException(String value) {
        super("INVALID_DIAGNOSIS_ACCURACY_VALUE", "Invalid diagnosis accuracy: " + value);
    }
}

