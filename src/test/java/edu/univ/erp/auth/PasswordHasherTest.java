package edu.univ.erp.auth;

import edu.univ.erp.auth.hash.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Password Hasher Tests")
class PasswordHasherTest {

    @Test
    @DisplayName("Should hash password successfully")
    void testPasswordHashing() {
        String password = "mySecurePassword123";
        String hash = PasswordHasher.hash(password);
        
        assertNotNull(hash);
        assertThat(hash, not(equalTo(password)));
        assertThat(hash.length(), greaterThan(50));
    }

    @Test
    @DisplayName("Should verify correct password")
    void testPasswordVerification() {
        String password = "mySecurePassword123";
        String hash = PasswordHasher.hash(password);
        
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testIncorrectPasswordRejection() {
        String password = "mySecurePassword123";
        String wrongPassword = "wrongPassword";
        String hash = PasswordHasher.hash(password);
        
        assertFalse(PasswordHasher.verify(wrongPassword, hash));
    }

    @RepeatedTest(3)
    @DisplayName("Should generate different hashes for same password")
    void testHashUniqueness() {
        String password = "mySecurePassword123";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);
        
        assertNotEquals(hash1, hash2, "BCrypt should generate unique salts");
        assertTrue(PasswordHasher.verify(password, hash1));
        assertTrue(PasswordHasher.verify(password, hash2));
    }

    @Test
    @DisplayName("Should handle empty password")
    void testEmptyPassword() {
        String password = "";
        String hash = PasswordHasher.hash(password);
        
        assertNotNull(hash);
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testSpecialCharacters() {
        String password = "P@ssw0rd!#$%^&*()";
        String hash = PasswordHasher.hash(password);
        
        assertTrue(PasswordHasher.verify(password, hash));
    }
}
