package com.bridgecare.app.repositories;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import com.bridgecare.app.models.User;
import com.bridgecare.app.utility.AuthValidator;

public class UserRepository implements IUserRepository {

    private final DatabaseReference mDatabase;
    private static final String TAG = "UserRepository";

    public UserRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public <T extends User> void registerUser(T user, String password, String physicianInviteCode,
                                             OnCompleteListener<Void> listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                Log.e(TAG, "User registration failed", task.getException());
                listener.onComplete(Tasks.forException(
                        task.getException() != null ? task.getException() : new IllegalStateException("Registration failed")));
                return;
            }
            FirebaseUser firebaseUser = auth.getCurrentUser();
            user.setId(firebaseUser.getUid());
            if ("Physician".equals(user.getRole())) {
                verifyPhysicianInviteThenSave(firebaseUser, user, physicianInviteCode, listener);
            } else {
                saveUserData(firebaseUser, user, listener);
            }
        });
    }

    private <T extends User> void verifyPhysicianInviteThenSave(FirebaseUser firebaseUser, T user,
                                                               String physicianInviteCode,
                                                               OnCompleteListener<Void> listener) {
        if (physicianInviteCode == null || physicianInviteCode.trim().isEmpty()) {
            abortRegistration(firebaseUser, new SecurityException("Physician invite code required"), listener);
            return;
        }
        String codeHash = AuthValidator.hashPhysicianInviteCode(physicianInviteCode);
        FirebaseDatabase.getInstance().getReference("physicianInviteCodes").child(codeHash)
                .get()
                .addOnCompleteListener(inviteTask -> {
                    if (!inviteTask.isSuccessful() || inviteTask.getResult() == null || !inviteTask.getResult().exists()) {
                        abortRegistration(firebaseUser, new SecurityException("Invalid physician invite code"), listener);
                        return;
                    }
                    saveUserData(firebaseUser, user, listener);
                });
    }

    private <T extends User> void saveUserData(FirebaseUser firebaseUser, T user, OnCompleteListener<Void> listener) {
        mDatabase.child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(saveTask -> {
            if (saveTask.isSuccessful()) {
                Log.d(TAG, "User registered successfully with id: " + firebaseUser.getUid());
                listener.onComplete(saveTask);
            } else {
                Log.e(TAG, "User profile write failed", saveTask.getException());
                abortRegistration(firebaseUser, saveTask.getException() != null
                        ? saveTask.getException()
                        : new IllegalStateException("Failed to save user profile"), listener);
            }
        });
    }

    private void abortRegistration(FirebaseUser firebaseUser, Exception error, OnCompleteListener<Void> listener) {
        if (firebaseUser != null) {
            firebaseUser.delete();
        }
        FirebaseAuth.getInstance().signOut();
        listener.onComplete(Tasks.forException(error != null ? error : new IllegalStateException("Registration aborted")));
    }

    @Override
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    @Override
    public void sendPasswordResetEmail(String email, OnCompleteListener<Void> listener) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(listener);
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
