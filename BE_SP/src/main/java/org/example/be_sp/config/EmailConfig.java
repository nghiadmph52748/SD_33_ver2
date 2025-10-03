package org.example.be_sp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Email Configuration
 * 
 * Reads email-related properties from application.properties
 * and provides them to the email service.
 */
@Configuration
@Getter
public class EmailConfig {
    
    @Value("${email.from.address}")
    private String fromAddress;
    
    @Value("${email.from.personal}")
    private String fromPersonal;
    
    @Value("${email.enabled}")
    private boolean enabled;
    
    @Value("${email.base-url}")
    private String baseUrl;
}
