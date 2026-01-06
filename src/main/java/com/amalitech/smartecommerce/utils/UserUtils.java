package com.amalitech.smartecommerce.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for user-related operations like password hashing.
 */
public class UserUtils {

    /**
     * Hash a password using BCrypt.
     * BCrypt automatically handles salt generation and includes it in the hash.
     *
     * @param password the plain text password
     * @return the BCrypt hashed password
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verify a password against a stored hash.
     * Supports both BCrypt and legacy SHA-256 hashes for backward compatibility.
     *
     * @param password the plain text password to verify
     * @param storedHash the stored hash to compare against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        // Check if it's a BCrypt hash (starts with $2a$, $2b$, or $2y$)
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, storedHash);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // Try SHA-256 (legacy) - hash is 64 hex characters
        if (storedHash.length() == 64 && storedHash.matches("[a-fA-F0-9]+")) {
            String sha256Hash = hashPasswordSHA256(password);
            return sha256Hash.equals(storedHash);
        }

        // Try plain text comparison (for development/testing only - not recommended)
        return password.equals(storedHash);
    }

    /**
     * Hash a password using SHA-256 (legacy method for backward compatibility).
     *
     * @param password the plain text password
     * @return the SHA-256 hashed password as a hex string
     */
    private static String hashPasswordSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    /**
     * Check if a stored hash is using the old format and needs migration.
     *
     * @param storedHash the stored hash to check
     * @return true if the hash needs migration to BCrypt
     */
    public static boolean needsHashMigration(String storedHash) {
        if (storedHash == null) {
            return false;
        }
        // BCrypt hashes start with $2a$, $2b$, or $2y$
        return !storedHash.startsWith("$2a$") &&
               !storedHash.startsWith("$2b$") &&
               !storedHash.startsWith("$2y$");
    }

    /**
     * Validate email format.
     *
     * @param email the email to validate
     * @return true if the email format is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}

