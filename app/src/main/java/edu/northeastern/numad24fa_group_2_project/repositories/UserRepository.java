package edu.northeastern.numad24fa_group_2_project.repositories;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import edu.northeastern.numad24fa_group_2_project.models.User;

public class UserRepository implements IUserRepository {

    private final DatabaseReference mDatabase;
    private static final String TAG = "UserRepository";

    public UserRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public <T extends User> void registerUser(T user, String password, OnCompleteListener<AuthResult> listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                user.setId(firebaseUser.getUid());
                saveUserData(firebaseUser, user);
                Log.d(TAG, "User registered successfully with id: " + firebaseUser.getUid());
            } else {
                Log.e(TAG, "User registration failed", task.getException());
            }
            listener.onComplete(task);
        });
    }

    private <T extends User> void saveUserData(FirebaseUser firebaseUser, T user) {
        mDatabase.child(firebaseUser.getUid()).setValue(user);
    }

    @Override
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    @Override
    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void getUser(String userId, OnCompleteListener<DataSnapshot> listener) {
        mDatabase.child(userId).get().addOnCompleteListener(listener);
    }

    @Override
    public void getAllUsers(ValueEventListener listener) {
        mDatabase.addValueEventListener(listener);
    }

    @Override
    public void updateUser(String userId, Map<String, Object> updates, OnCompleteListener<Void> listener) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        mDatabase.child(userId).updateChildren(updates).addOnCompleteListener(listener);
    }

    @Override
    public void getPhysicians(ValueEventListener listener) {
        mDatabase.orderByChild("patient").equalTo(false).addValueEventListener(listener);
    }

    @Override
    public void getPatients(ValueEventListener listener) {
        mDatabase.orderByChild("patient").equalTo(true).addValueEventListener(listener);
    }
}
