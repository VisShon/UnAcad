package edu.univ.erp.auth.hash;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles password hashing and verification using BCrypt.
 */

public class PasswordHasher {

    // Generate a BCrypt hash from a plain password
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Verify plain password against stored hash
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}