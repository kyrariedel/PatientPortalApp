package com.bridgecare.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.bridgecare.app.utility.AuthValidator;
import com.bridgecare.app.utility.UserSessionHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton, toRegisterButton;
    private TextView forgotPasswordButton;
    private ProgressBar authProgressBar;
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
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        authProgressBar = findViewById(R.id.authProgressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> login());
        toRegisterButton.setOnClickListener(v -> toRegisterPage());
        forgotPasswordButton.setOnClickListener(v -> sendPasswordReset());
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AuthValidator.isValidEmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        setBusy(true);
        userRepository.loginUser(email, password, this::onLoginComplete);
    }

    private void sendPasswordReset() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, R.string.error_reset_email_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AuthValidator.isValidEmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        setBusy(true);
        userRepository.sendPasswordResetEmail(email, task -> {
            setBusy(false);
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.reset_email_sent, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.reset_email_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onLoginComplete(Task<AuthResult> task) {
        if (!task.isSuccessful()) {
            setBusy(false);
            Toast.makeText(LoginActivity.this, R.string.error_login_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            fetchUserRole(firebaseUser.getUid());
        } else {
            setBusy(false);
            Toast.makeText(LoginActivity.this, R.string.error_user_data, Toast.LENGTH_SHORT).show();
        }
    }

    public void toRegisterPage() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void fetchUserRole(String userId) {
        userRepository.getUser(userId, task -> {
            if (task.isSuccessful()) {
                DataSnapshot userSnapshot = task.getResult();
                String role = userSnapshot.child("role").getValue(String.class);
                if (role != null) {
                    handleSuccessfulLogin(userId, role);
                    return;
                }
            }
            setBusy(false);
            Toast.makeText(LoginActivity.this, R.string.error_user_data, Toast.LENGTH_SHORT).show();
        });
    }

    private void handleSuccessfulLogin(String userId, String role) {
        userSessionHelper.saveUser(userId, role);
        Class<?> targetActivity = role.equals("Patient") ? PatientDashboardActivity.class : PhysicianDashboardActivity.class;
        startActivity(new Intent(LoginActivity.this, targetActivity));
        finish();
    }

    private void setBusy(boolean busy) {
        if (authProgressBar != null) {
            authProgressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        }
        if (loginButton != null) {
            loginButton.setEnabled(!busy);
        }
        if (toRegisterButton != null) {
            toRegisterButton.setEnabled(!busy);
        }
        if (forgotPasswordButton != null) {
            forgotPasswordButton.setEnabled(!busy);
            forgotPasswordButton.setClickable(!busy);
        }
    }
}
