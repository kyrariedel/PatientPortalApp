package com.bridgecare.app.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import java.util.Map;
import com.bridgecare.app.models.User;

public interface IUserRepository {

    /**
     * Registers a user with the given user object, password, and user type.
     * Password is hashed and stored in Firebase, and the user object is stored in the database.
     * The user object doesn't have the password field, firebase handles that.
     *
     * @param user     the user object to register
     * @param password the password to register with, not related to the user object, firebase handles this.
     * @param listener the listener to call when the registration is complete
     * @param <T>      the type of user to register
     */
    <T extends User> void registerUser(T user, String password, OnCompleteListener<AuthResult> listener);

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
