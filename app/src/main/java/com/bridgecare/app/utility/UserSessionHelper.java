package com.bridgecare.app.utility;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

import com.bridgecare.app.repositories.UserRepository;

public class UserSessionHelper implements Serializable {
    private static final String PREF_NAME = "app_prefs";
    private static final String USER_ID_KEY = "USER_ID";
    private static final String ROLE_KEY = "ROLE";
    private final SharedPreferences preferences;
    private final UserRepository userRepository;

    public UserSessionHelper(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userRepository = new UserRepository();
    }

    /**
     * Save user id and role in shared preferences
     *
     * @param userId user id
     * @param role   user role
     */
    public void saveUser(String userId, String role) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID_KEY, userId);
        editor.putString(ROLE_KEY, role);
        editor.apply();
    }

    public String getUserId() {
        return preferences.getString(USER_ID_KEY, null);
    }

    public String getRole() {
        return preferences.getString(ROLE_KEY, null);
    }

    /**
     * Clear user id and role from shared preferences and logout user from firebase.
     */
    public void clearUser() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(USER_ID_KEY);
        editor.remove(ROLE_KEY);
        this.userRepository.logoutUser();
        editor.apply();
    }

    public boolean isUserLoggedIn() {
        return getUserId() != null;
    }
}
