package org.example.be_sp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this class to generate hashed passwords for database updates
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hashes for common passwords
        String[] passwords = {"admin", "123456", "password", "user123"};
        
        System.out.println("BCrypt Password Hashes:");
        System.out.println("========================");
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("\nPlain text: " + password);
            System.out.println("BCrypt hash: " + hash);
            System.out.println("SQL Update: UPDATE nhan_vien SET mat_khau = '" + hash + "' WHERE ten_tai_khoan = 'YOUR_USERNAME';");
        }
        
        System.out.println("\n========================");
        System.out.println("Copy the SQL statements above and run them in your database");
        System.out.println("to update passwords to BCrypt format.");
    }
}
