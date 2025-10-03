package org.example.be_sp.controller;

import org.example.be_sp.model.response.ResponseObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public API không cần xác thực token
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {

    @GetMapping("/health")
    public ResponseObject<?> healthCheck() {
        return new ResponseObject<>(true, "API is running", "Health check successful");
    }

    @GetMapping("/info")
    public ResponseObject<?> systemInfo() {

        Object info = new Object() {
            public final String version = "1.0.0";
            public final String status = "running";
            public final long timestamp = System.currentTimeMillis();
        };

        return new ResponseObject<>(true, info, "System information");
    }
}
