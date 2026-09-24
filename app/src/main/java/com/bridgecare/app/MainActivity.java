package com.bridgecare.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bridgecare.app.utility.UserSessionHelper;

public class MainActivity extends AppCompatActivity {

    private Button toRegisterButton, toLoginButton;
    private TextView textView;
    private UserSessionHelper userSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        toRegisterButton = findViewById(R.id.toRegisterButton);
        toLoginButton = findViewById(R.id.toLoginButton);
        textView = findViewById(R.id.textView);
        userSessionHelper = new UserSessionHelper(this);
        textView.setText("Signed in as: " + userSessionHelper.getUserId()); // <-- This is buggy, when logging out, it doesn't update the text view.
        // The text view should be updated to "Signed in as: null" when the user logs out.
    }

    private void setupListeners() {
        toRegisterButton.setOnClickListener(v -> toRegisterActivity());
        toLoginButton.setOnClickListener(v -> toLoginActivity());
    }

    private void toRegisterActivity() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void toPatientDashboardActivity(View v) {
        Intent intent = new Intent(MainActivity.this, PatientDashboardActivity.class);
        startActivity(intent);
    }

    public void toLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}