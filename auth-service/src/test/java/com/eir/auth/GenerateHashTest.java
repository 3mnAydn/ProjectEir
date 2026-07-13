package com.eir.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class GenerateHashTest {

    @Test
    void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String password = "Admin123!";
        String hash = encoder.encode(password);
        System.out.println("Hash for 'Admin123!': " + hash);
        System.out.println("Matches: " + encoder.matches("Admin123!", hash));
        
        // Also check the existing hash
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        System.out.println("Existing hash matches 'Admin123!': " + encoder.matches("Admin123!", existingHash));
        System.out.println("Existing hash matches 'admin': " + encoder.matches("admin", existingHash));
        System.out.println("Existing hash matches 'Admin123': " + encoder.matches("Admin123", existingHash));
        System.out.println("Existing hash matches 'admin123': " + encoder.matches("admin123", existingHash));
    }
}