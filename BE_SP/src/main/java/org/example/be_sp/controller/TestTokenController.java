package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.security.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Token Controller - FOR DEVELOPMENT/TESTING ONLY
 * Generates test JWT tokens without authentication
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TestTokenController {
    
    private final JwtUtils jwtUtils;
    
    /**
     * Generate a test token for any username
     * GET /api/test/generate-token?username=admin
     * 
     * WARNING: This endpoint should be disabled in production!
     * Remove or secure this endpoint before deploying to production.
     */
    @GetMapping("/generate-token")
    public ResponseObject<Map<String, String>> generateTestToken(
            @RequestParam(defaultValue = "admin") String username) {
        
        String token = jwtUtils.generateTestToken(username);
        
        Map<String, String> result = new HashMap<>();
        result.put("username", username);
        result.put("token", token);
        result.put("expiresIn", "24 hours");
        result.put("curlExample", String.format(
            "curl -X POST http://localhost:8080/api/message/test-notification " +
            "-H \"Authorization: Bearer %s\" " +
            "-H \"Content-Type: application/json\" " +
            "-d '{\"type\":\"notice\",\"title\":\"Test\",\"content\":\"Hello!\"}'",
            token
        ));
        
        return new ResponseObject<>(result, "Test token generated successfully");
    }
    
    /**
     * Quick token - Just returns the token as plain text (easiest to copy)
     * GET /api/test/token
     * GET /api/test/token?username=admin
     */
    @GetMapping(value = "/token", produces = "text/plain")
    public String quickToken(@RequestParam(defaultValue = "admin") String username) {
        return jwtUtils.generateTestToken(username);
    }
    
    /**
     * Token page - Returns a simple HTML page with the token and copy button
     * GET /api/test/token-page
     */
    @GetMapping(value = "/token-page", produces = "text/html")
    public String tokenPage(@RequestParam(defaultValue = "admin") String username) {
        String token = jwtUtils.generateTestToken(username);
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Token Generator</title>
            </head>
            <body>
                <h1>Test Token Generator</h1>
                
                <p><strong>WARNING:</strong> This is for DEVELOPMENT ONLY! Disable this endpoint before deploying to production.</p>
                
                <hr>
                
                <p><strong>Username:</strong> %s</p>
                <p><strong>Expires:</strong> 24 hours</p>
                <p><strong>Type:</strong> Test Token</p>
                
                <hr>
                
                <h2>Your Token:</h2>
                <p id="tokenBox">%s</p>
                <button onclick="copyToken()">Copy Token</button>
                
                <hr>
                
                <h2>Quick Copy Commands:</h2>
                
                <h3>Export as Environment Variable:</h3>
                <pre id="exportCmd">export TOKEN="%s"</pre>
                <button onclick="copyExport()">Copy</button>
                
                <h3>Use in cURL:</h3>
                <pre id="curlCmd">curl -X POST http://localhost:8080/api/message/test-notification \\
  -H "Authorization: Bearer %s" \\
  -H "Content-Type: application/json" \\
  -d '{"type":"notice","title":"Test","content":"Hello!"}'</pre>
                <button onclick="copyCurl()">Copy</button>
                
                <h3>Set in Browser Console:</h3>
                <pre id="browserCmd">localStorage.setItem('token', '%s')</pre>
                <button onclick="copyBrowser()">Copy</button>
                
                <hr>
                
                <h2>Generate New Token:</h2>
                <form method="GET">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username" value="%s" placeholder="Enter username">
                    <button type="submit">Generate</button>
                </form>
                
                <script>
                    function copyToken() {
                        copyToClipboard(document.getElementById('tokenBox').textContent);
                    }
                    
                    function copyExport() {
                        copyToClipboard(document.getElementById('exportCmd').textContent);
                    }
                    
                    function copyCurl() {
                        copyToClipboard(document.getElementById('curlCmd').textContent);
                    }
                    
                    function copyBrowser() {
                        copyToClipboard(document.getElementById('browserCmd').textContent);
                    }
                    
                    function copyToClipboard(text) {
                        navigator.clipboard.writeText(text).then(function() {
                            alert('Copied to clipboard!');
                        });
                    }
                </script>
            </body>
            </html>
            """, username, token, token, token, token, username);
    }
    
    /**
     * Generate multiple test tokens at once
     * GET /api/test/generate-tokens
     */
    @GetMapping("/generate-tokens")
    public ResponseObject<Map<String, Object>> generateMultipleTokens() {
        Map<String, Object> result = new HashMap<>();
        
        String[] usernames = {"admin", "user1", "user2", "testuser"};
        Map<String, String> tokens = new HashMap<>();
        
        for (String username : usernames) {
            tokens.put(username, jwtUtils.generateTestToken(username));
        }
        
        result.put("tokens", tokens);
        result.put("expiresIn", "24 hours");
        result.put("note", "Copy any token and use it in Authorization header");
        
        return new ResponseObject<>(result, "Multiple test tokens generated");
    }
}
