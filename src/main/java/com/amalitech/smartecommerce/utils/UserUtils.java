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
         try {
                return BCrypt.checkpw(password, storedHash);
            } catch (IllegalArgumentException e) {
                return false;
            }

    }

}

