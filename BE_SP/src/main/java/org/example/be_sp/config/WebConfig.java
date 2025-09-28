package org.example.be_sp.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.example.be_sp.interceptor.TokenValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenValidationInterceptor tokenValidationInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // đường dẫn thư mục vật lý
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // map URL /uploads/** tới thư mục uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenValidationInterceptor)
                .addPathPatterns("/api/**") // Áp dụng cho tất cả API paths
                .excludePathPatterns(
                        "/api/auth/**", // Exclude auth endpoints
                        "/api/public/**", // Exclude public endpoints
                        "/api/health/**" // Exclude health check endpoints
                );
    }
}
