package org.example.be_sp.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // Create JavaTimeModule with custom LocalDateTime serializer/deserializer
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            
            // Configure LocalDateTime serializer (for responses)
            javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DATETIME_FORMATTER));
            
            // Configure LocalDateTime deserializer (for requests)
            javaTimeModule.addDeserializer(LocalDateTime.class, 
                new LocalDateTimeDeserializer(DATETIME_FORMATTER));
            
            // Apply the module to the builder
            builder.modules(javaTimeModule);
            
            // Disable writing dates as timestamps
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
