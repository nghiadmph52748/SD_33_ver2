package org.example.be_sp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminPasswordHashTest {

    @Test
    void adminHashMatchesAdmin() {
        // Hash from SQL seed for nhan_vien.ten_tai_khoan = 'admin'
        String adminHash = "$2a$20$DR4wdlvFMufQVuifTXHebO93J0wb6tK5fNjTAcVeX13yr3bvFaTvC";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("admin", adminHash), "Expected bcrypt hash to match plaintext 'admin'");
    }
}
