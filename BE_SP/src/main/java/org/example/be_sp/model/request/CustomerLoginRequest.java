package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerLoginRequest {

    /**
     * Allows login using either username or email.
     */
    private String identifier;

    /**
     * Optional explicit email field.
     */
    private String email;

    /**
     * Optional explicit username field.
     */
    private String username;

    private String password;

    public String resolveIdentifier() {
        if (username != null && !username.isBlank()) {
            return username;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return identifier;
    }
}


