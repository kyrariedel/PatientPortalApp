package com.bridgecare.app.utility;

import android.util.Patterns;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AuthValidator {
    public static final int MIN_PASSWORD_LENGTH = 6;

    private AuthValidator() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidPhysicianInviteCode(String inviteCode) {
        return inviteCode != null && !inviteCode.trim().isEmpty();
    }

    public static String hashPhysicianInviteCode(String inviteCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(inviteCode.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte value : hashed) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required to verify invite codes", e);
        }
    }
}
