package org.example.be_sp.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiException extends RuntimeException {
    private final String code;

    public ApiException(String message, String code) {
        super(message);
        this.code = code;
    }
}
