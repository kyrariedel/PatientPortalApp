package com.bridgecare.app.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import java.util.Map;
import com.bridgecare.app.models.User;

public interface IUserRepository {

    /**
     * Registers a user with Firebase Auth, optionally verifies a physician invite code
     * against hashed values in the database, then writes the profile and only then
     * reports completion so callers can sign out after the write finishes.
     *
     * @param user                 the user object to register
     * @param password             the password to register with
     * @param physicianInviteCode  required when the user role is Physician; ignored otherwise
     * @param listener             called after the profile write, or earlier on failure
     */
    <T extends User> void registerUser(T user, String password, String physicianInviteCode, OnCompleteListener<Void> listener);

    /**
     * Logs in a user with the given email and password.
     *
     * @param email    the email of the user to login
     * @param password the password of the user to login
     * @param listener the listener to call when the login is complete
     */
    void loginUser(String email, String password, OnCompleteListener<AuthResult> listener);

    /**
     * Logs out the current user. Check utility.UserSessionHelper.clearUser() to see how it is used.
     */
    void logoutUser();

    /**
     * Sends a Firebase password reset email to the given address.
     *
     * @param email    the account email
     * @param listener the listener to call when the request completes
     */
    void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener);

    /**
     * Gets the user with the given user id.
     *
     * @param userId   the id of the user to get
     * @param listener the listener to call when the user is retrieved
     */
    void getUser(String userId, OnCompleteListener<DataSnapshot> listener);

    void getAllUsers(ValueEventListener listener);

    void updateUser(String userId, Map<String, Object> updates, OnCompleteListener<Void> listener);

    void getPhysicians(ValueEventListener listener);

    void getPatients(ValueEventListener listener);
}
