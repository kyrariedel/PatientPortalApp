package com.bridgecare.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;

import com.bridgecare.app.repositories.UserRepository;
import com.bridgecare.app.utility.DateUtils;
import com.bridgecare.app.utility.UserSessionHelper;
import com.bridgecare.app.adapters.PatientDashboardAdapter;

public class PatientDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PatientDashboardActivity";

    private TextView tvPatientName;
    private Button logoutButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private UserRepository userRepository;
    private UserSessionHelper userSessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_patient_dashboard);
        userSessionHelper = new UserSessionHelper(this);

        if (!userSessionHelper.isUserLoggedIn() || !"Patient".equals(userSessionHelper.getRole())) {
            finish();
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initDatabase();
        initViews();
        setupListeners();

        viewPager.setAdapter(new PatientDashboardAdapter(this, userSessionHelper));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(getTabTitle(position)));
        tabLayoutMediator.attach();
    }

    private void initViews() {
        tvPatientName = findViewById(R.id.tvPatientName);
        logoutButton = findViewById(R.id.logoutButton);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void initDatabase() {
        userRepository = new UserRepository();
        userRepository.getUser(userSessionHelper.getUserId(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                updatePatientDetails(task.getResult());
            } else {
                Log.e(TAG, "Error getting user details", task.getException());
            }
        });
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void updatePatientDetails(DataSnapshot dataSnapshot) {
        String fullName = dataSnapshot.child("fullName").getValue(String.class);
        if (fullName == null) {
            fullName = "Unknown Name";
        }

        updateUI(fullName);
    }

    private void updateUI(String name) {
        tvPatientName.setText(name);
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 0: return getString(R.string.overview_tab);
            case 1: return getString(R.string.tasks_tab);
            case 2: return getString(R.string.appointments_tab);
            default: return "Tab " + position;
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void logout() {
        userSessionHelper.clearUser();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}