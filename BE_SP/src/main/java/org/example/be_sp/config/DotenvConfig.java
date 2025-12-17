package org.example.be_sp.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Try multiple locations: current directory, parent directory, and user home
            String userDir = System.getProperty("user.dir");
            String[] possiblePaths = {
                "./",
                "../",
                userDir,
                userDir + "/BE_SP",
                userDir + "/..",
                System.getProperty("user.home")
            };
            
            Dotenv dotenv = null;
            String loadedFrom = null;
            for (String path : possiblePaths) {
                try {
                    java.io.File envFile = new java.io.File(path, ".env");
                    if (envFile.exists() && envFile.isFile()) {
                    dotenv = Dotenv.configure()
                            .directory(path)
                            .ignoreIfMissing()
                            .load();
                    if (dotenv != null && dotenv.entries().size() > 0) {
                            loadedFrom = envFile.getAbsolutePath();
                            System.out.println("Loaded .env file from: " + loadedFrom);
                        break;
                        }
                    }
                } catch (Exception e) {
                    // Try next path
                    System.out.println("Tried path " + path + ": " + e.getMessage());
                }
            }
            
            if (dotenv == null || dotenv.entries().size() == 0) {
                System.out.println("WARNING: No .env file found or .env file is empty. Using default configuration.");
                System.out.println("Searched in: " + java.util.Arrays.toString(possiblePaths));
                return;
            }

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> dotenvMap = new HashMap<>();
            
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                dotenvMap.put(key, value);
                // Set as system property (for System.getProperty() access)
                System.setProperty(key, value);
            });

            // Add dotenv properties FIRST so they override everything
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenvProperties", dotenvMap));
                    
            // Also add them to system environment map for System.getenv() fallback
            // Note: We can't modify actual environment variables, but we can ensure
            // Spring property resolution works by having dotenvProperties first
                    
        } catch (Exception e) {
            // If .env file doesn't exist, continue without it
            System.out.println("No .env file found, using default configuration: " + e.getMessage());
        }
    }
}

