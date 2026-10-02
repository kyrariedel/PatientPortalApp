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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;

import java.time.LocalDate;
import java.util.List;

import com.bridgecare.app.adapters.PhysicianDashboardAdapter;
import com.bridgecare.app.repositories.UserRepository;
import com.bridgecare.app.utility.UserSessionHelper;
import com.bridgecare.app.utility.DateUtils;

public class PhysicianDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PhysicianDashboardActivity";

    private TextView tvPhysicianName;
    private TextView tvPhysicianDob;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private UserRepository userRepository;
    private UserSessionHelper userSessionHelper;

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_physician_dashboard);
        userSessionHelper = new UserSessionHelper(this);

        if (!userSessionHelper.isUserLoggedIn() || !"Physician".equals(userSessionHelper.getRole())) {
            finish();
            return;
        }

        initDatabase();
        initViews();
        setupListeners();
        viewPager.setAdapter(new PhysicianDashboardAdapter(this, userSessionHelper));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(getTabTitle(position)) );
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    removePatientTasksFragment();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void removePatientTasksFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.getTag() != null && fragment.getTag().startsWith("TASKS_FRAGMENT_")) {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commit();
                break;
            }
        }
    }

    private void initViews() {
        tvPhysicianName = findViewById(R.id.tvPhysicianName);
        tvPhysicianDob = findViewById(R.id.tvPhysicianDob);
        logoutButton = findViewById(R.id.logoutButton);
        tabLayout = findViewById(R.id.physician_tab_layout);
        viewPager = findViewById(R.id.physician_view_pager);
    }

    private void initDatabase() {
        userRepository = new UserRepository();
        userRepository.getUser(userSessionHelper.getUserId(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                updatePhysicianDetails(task.getResult());
            } else {
                Log.e(TAG, "Error getting user details", task.getException());
            }
        });
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void updatePhysicianDetails(DataSnapshot dataSnapshot) {
        String fullName = dataSnapshot.child("fullName").getValue(String.class);
        if (fullName == null) {
            fullName = getString(R.string.unknown_name);
        }
        updateUI(fullName, parseDob(dataSnapshot.child("dob")));
    }

    private LocalDate parseDob(DataSnapshot dobSnapshot) {
        if (dobSnapshot == null || !dobSnapshot.exists()) {
            return null;
        }
        Integer year = dobSnapshot.child("year").getValue(Integer.class);
        Integer month = dobSnapshot.child("monthValue").getValue(Integer.class);
        Integer day = dobSnapshot.child("dayOfMonth").getValue(Integer.class);
        if (year != null && month != null && day != null) {
            return LocalDate.of(year, month, day);
        }
        return null;
    }

    private void updateUI(String name, LocalDate dob) {
        tvPhysicianName.setText(getString(R.string.physician_name_format, name));
        if (dob != null) {
            tvPhysicianDob.setText(getString(R.string.physician_dob_format, DateUtils.formatDate(dob)));
        } else {
            tvPhysicianDob.setText(R.string.physician_dob_unavailable);
        }
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 0: return getString(R.string.patients_tab);
            case 1: return getString(R.string.appointments_tab);
            default: return getString(R.string.appointments_tab);
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
        UserSessionHelper userSessionHelper = new UserSessionHelper(this);
        userSessionHelper.clearUser();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}