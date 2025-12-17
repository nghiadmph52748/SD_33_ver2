package org.example.be_sp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BeSpApplication {

    public static void main(String[] args) {
        // Load .env file BEFORE Spring Boot starts to ensure properties are available
        loadDotenv();
        
        SpringApplication.run(BeSpApplication.class, args);
    }
    
    private static void loadDotenv() {
        try {
            String userDir = System.getProperty("user.dir");
            String[] possiblePaths = {
                "./",
                "../",
                userDir,
                userDir + "/BE_SP",
                userDir + "/.."
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
                            
                            // Set as system properties so Spring Boot can use them
                            dotenv.entries().forEach(entry -> {
                                String key = entry.getKey();
                                String value = entry.getValue();
                                System.setProperty(key, value);
                                
                                // Map old variable names to new ones for backward compatibility
                                if ("DB_URL".equals(key)) {
                                    // Parse DB_URL: jdbc:sqlserver://127.0.0.1:1433;databaseName=GearUp;...
                                    try {
                                        String url = value;
                                        if (url.contains("jdbc:sqlserver://")) {
                                            String serverPart = url.substring(url.indexOf("//") + 2);
                                            String server = serverPart.split(":")[0];
                                            String portPart = serverPart.split(":")[1];
                                            String port = portPart.split(";")[0];
                                            String dbName = "";
                                            if (url.contains("databaseName=")) {
                                                dbName = url.substring(url.indexOf("databaseName=") + 13);
                                                dbName = dbName.split(";")[0];
                                            }
                                            System.setProperty("SQL_SERVER", server);
                                            System.setProperty("SQL_PORT", port);
                                            System.setProperty("SQL_DATABASE", dbName);
                                        }
                                    } catch (Exception e) {
                                        // If parsing fails, set defaults
                                        System.setProperty("SQL_SERVER", "localhost");
                                        System.setProperty("SQL_PORT", "1433");
                                        System.setProperty("SQL_DATABASE", "GearUp");
                                    }
                                } else if ("DB_USERNAME".equals(key)) {
                                    System.setProperty("SQL_USERNAME", value);
                                } else if ("DB_PASSWORD".equals(key)) {
                                    System.setProperty("SQL_PASSWORD", value);
                                } else if ("MAIL_USERNAME".equals(key)) {
                                    System.setProperty("SPRING_MAIL_USERNAME", value);
                                } else if ("MAIL_PASSWORD".equals(key)) {
                                    System.setProperty("SPRING_MAIL_PASSWORD", value);
                                } else if ("MAIL_HOST".equals(key)) {
                                    System.setProperty("SPRING_MAIL_HOST", value);
                                } else if ("MAIL_PORT".equals(key)) {
                                    System.setProperty("SPRING_MAIL_PORT", value);
                                } else if ("VNP_TMN_CODE".equals(key)) {
                                    System.setProperty("VNPAY_TMN_CODE", value);
                                } else if ("VNP_HASH_SECRET".equals(key)) {
                                    System.setProperty("VNPAY_HASH_SECRET", value);
                                } else if ("VNP_RETURN_URL".equals(key)) {
                                    System.setProperty("VNPAY_RETURN_URL", value);
                                } else if ("VNP_IPN_URL".equals(key)) {
                                    System.setProperty("VNPAY_IPN_URL", value);
                                } else if ("VNP_PAY_URL".equals(key)) {
                                    System.setProperty("VNPAY_URL", value);
                                } else if ("VNP_QR_API_URL".equals(key)) {
                                    System.setProperty("VIETQR_CHECKOUT_URL", value);
                                } else if ("GOOGLE_CLIENT_ID".equals(key)) {
                                    System.setProperty("OAUTH_GOOGLE_CLIENT_ID", value);
                                }
                            });
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Try next path
                }
            }
            
            if (dotenv == null || dotenv.entries().size() == 0) {
                System.out.println("WARNING: No .env file found. Searched in: " + java.util.Arrays.toString(possiblePaths));
            }
        } catch (Exception e) {
            System.out.println("Failed to load .env file: " + e.getMessage());
        }
    }

}
