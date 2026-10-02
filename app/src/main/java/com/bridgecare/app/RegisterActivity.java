package com.bridgecare.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.util.Calendar;
import com.bridgecare.app.models.Patient;
import com.bridgecare.app.models.Physician;
import com.bridgecare.app.models.User;
import com.bridgecare.app.repositories.UserRepository;
import com.bridgecare.app.utility.AuthValidator;
import com.bridgecare.app.utility.DateUtils;
import com.bridgecare.app.utility.UserSessionHelper;

public class RegisterActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private EditText editTextFirstName, editTextLastName, editTextDob, editTextEmail, editTextPassword, editTextPhysicianInviteCode;
    private Spinner spinnerRole;
    private Button buttonRegister, toLoginButton;
    private ProgressBar authProgressBar;
    private LocalDate dob;
    private UserSessionHelper userSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        userSessionHelper = new UserSessionHelper(this);

        if (userSessionHelper.getUserId() != null) {
            String role = userSessionHelper.getRole();
            Class<?> targetActivity = role.equals("Patient") ? PatientDashboardActivity.class : PhysicianDashboardActivity.class;
            startActivity(new Intent(RegisterActivity.this, targetActivity));
            finish();
        } else {
            initViews();
            userRepository = new UserRepository();
            setupListeners();
            setupRoleSpinner();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextDob = findViewById(R.id.editTextDob);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        buttonRegister = findViewById(R.id.buttonRegister);
        toLoginButton = findViewById(R.id.toLoginPage);
        authProgressBar = findViewById(R.id.authProgressBar);
        editTextPhysicianInviteCode = findViewById(R.id.editTextPhysicianInviteCode);
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isPhysician = "Physician".equals(parent.getItemAtPosition(position).toString());
                editTextPhysicianInviteCode.setVisibility(isPhysician ? View.VISIBLE : View.GONE);
                if (!isPhysician) {
                    editTextPhysicianInviteCode.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextPhysicianInviteCode.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> registerUser());
        editTextDob.setOnClickListener(v -> showDatePickerDialog());
        toLoginButton.setOnClickListener(v -> toLoginPage());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dob = LocalDate.of(year, month + 1, dayOfMonth);
            editTextDob.setText(DateUtils.formatDate(dob));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void registerUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String dobString = editTextDob.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String selectedRole = spinnerRole.getSelectedItem().toString();

        if (!validateInputs(firstName, lastName, dobString, email, password, selectedRole)) {
            return;
        }
        if ("Patient".equals(selectedRole)) {
            createUser(new Patient(firstName, lastName, dob, email), password);
        } else if ("Physician".equals(selectedRole)) {
            createUser(new Physician(firstName, lastName, dob, email), password);
        }
    }

    private boolean validateInputs(String firstName, String lastName, String dobString, String email, String password, String selectedRole) {
        if (firstName.isEmpty() || lastName.isEmpty() || dobString.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!AuthValidator.isValidEmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!AuthValidator.isValidPassword(password)) {
            Toast.makeText(this,
                    getString(R.string.error_password_too_short, AuthValidator.MIN_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dob == null) {
            Toast.makeText(this, R.string.error_select_dob, Toast.LENGTH_SHORT).show();
            return false;
        }
        if ("Physician".equals(selectedRole)) {
            String inviteCode = editTextPhysicianInviteCode.getText().toString().trim();
            if (!AuthValidator.isValidPhysicianInviteCode(inviteCode)) {
                Toast.makeText(this, R.string.error_invalid_physician_invite, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void toLoginPage() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void createUser(User user, String password) {
        setBusy(true);
        userRepository.registerUser(user, password, task -> {
            if (task.isSuccessful()) {
                userSessionHelper.clearUser();
                Toast.makeText(this,
                        getString(R.string.register_success, user.getRole(), user.getFullName()),
                        Toast.LENGTH_SHORT).show();
                navigateToLogin();
            } else {
                setBusy(false);
                Toast.makeText(this, R.string.error_register_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setBusy(boolean busy) {
        if (authProgressBar != null) {
            authProgressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        }
        if (buttonRegister != null) {
            buttonRegister.setEnabled(!busy);
        }
        if (toLoginButton != null) {
            toLoginButton.setEnabled(!busy);
        }
    }
}
