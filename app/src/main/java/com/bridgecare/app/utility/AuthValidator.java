package com.bridgecare.app.utility;

import android.util.Patterns;

public final class AuthValidator {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final String PHYSICIAN_INVITE_CODE = "CARE-MD-ACCESS";

    private AuthValidator() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidPhysicianInviteCode(String inviteCode) {
        return PHYSICIAN_INVITE_CODE.equals(inviteCode);
    }
}
