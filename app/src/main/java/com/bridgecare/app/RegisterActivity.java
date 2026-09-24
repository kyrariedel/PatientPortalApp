package com.bridgecare.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.bridgecare.app.utility.DateUtils;
import com.bridgecare.app.utility.UserSessionHelper;

public class RegisterActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private EditText editTextFirstName, editTextLastName, editTextDob, editTextEmail, editTextPassword;
    private Spinner spinnerRole;
    private Button buttonRegister, toLoginButton;
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
    }

    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
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
        String password = editTextPassword.getText().toString().trim();
        String selectedRole = spinnerRole.getSelectedItem().toString();

        if (validateInputs(firstName, lastName, dobString, email, password)) {
            if ("Patient".equals(selectedRole)) {
                createUser(new Patient(firstName, lastName, dob, email), password);
            } else if ("Physician".equals(selectedRole)) {
                createUser(new Physician(firstName, lastName, dob, email), password);
            }
        }
    }

    private boolean validateInputs(String firstName, String lastName, String dobString, String email, String password) {
        if (firstName.isEmpty() || lastName.isEmpty() || dobString.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dob == null) {
            Toast.makeText(this, "Please select a valid date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void toLoginPage() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void createUser(User user, String password) {
        userRepository.registerUser(user, password, task -> {
            if (task.isSuccessful()) {
                String message = user.getRole() + " registered: Please Login." + user.getFullName();
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                navigateToLogin();
            } else {
                Toast.makeText(this, "Failed to register (Email can be already used) ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}