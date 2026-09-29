package com.bridgecare.app.utility;

import android.util.Patterns;

public final class AuthValidator {
    public static final int MIN_PASSWORD_LENGTH = 6;

    private AuthValidator() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
}
