package com.bridgecare.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.bridgecare.app.repositories.UserRepository;
import com.bridgecare.app.utility.UserSessionHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton, toRegisterButton;
    private UserRepository userRepository;
    private UserSessionHelper userSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        userSessionHelper = new UserSessionHelper(this);

        if (userSessionHelper.getUserId() != null) {
            String role = userSessionHelper.getRole();
            Class<?> targetActivity = role.equals("Patient") ? PatientDashboardActivity.class : PhysicianDashboardActivity.class;
            startActivity(new Intent(LoginActivity.this, targetActivity));
            finish();
        } else {
            initViews();
            setupListeners();
            userRepository = new UserRepository();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        toRegisterButton = findViewById(R.id.toRegisterButton);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> login());
        toRegisterButton.setOnClickListener(v -> toRegisterPage());
    }

    private void login() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        userRepository.loginUser(email, password, this::onLoginComplete);
    }


    private void onLoginComplete(Task<AuthResult> task) {
        if (!task.isSuccessful()) {
            Toast.makeText(LoginActivity.this, "Login failed: Invalid Email or Password", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            fetchUserRole(firebaseUser.getUid());
        }
    }

    public void toRegisterPage() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Fetches the user role from the database and saves it to the user session using handleSuccessfulLogin.
     *
     * @param userId The user ID of the user to fetch the role for.
     */
    private void fetchUserRole(String userId) {
        userRepository.getUser(userId, task -> {
            if (task.isSuccessful()) {
                DataSnapshot userSnapshot = task.getResult();
                String role = userSnapshot.child("role").getValue(String.class);
                if (role != null) {
                    handleSuccessfulLogin(userId, role);
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves the user ID and role to the user session and starts the appropriate dashboard activity.
     *
     * @param userId The user ID of the user that logged in.
     * @param role   The role of the user that logged in.
     */
    private void handleSuccessfulLogin(String userId, String role) {
        userSessionHelper.saveUser(userId, role);
        Class<?> targetActivity = role.equals("Patient") ? PatientDashboardActivity.class : PhysicianDashboardActivity.class;
        startActivity(new Intent(LoginActivity.this, targetActivity));
        finish();
    }
}