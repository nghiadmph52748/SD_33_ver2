package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLoginRequest {
    private String token;
    private String provider; // "google" or "facebook"
}

