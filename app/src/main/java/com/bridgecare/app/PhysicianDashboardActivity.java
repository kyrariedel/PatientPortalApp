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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

import com.bridgecare.app.adapters.PhysicianDashboardAdapter;
import com.bridgecare.app.repositories.AppointmentRepository;
import com.bridgecare.app.repositories.UserRepository;
import com.bridgecare.app.utility.AppointmentPhysicianCreateDialogHelper;
import com.bridgecare.app.utility.TaskPatientCreateDialogHelper;
import com.bridgecare.app.utility.UserSessionHelper;
import com.bridgecare.app.utility.DateUtils;

public class PhysicianDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PhysicianDashboardActivity";

    private TextView tvPhysicianName;
    private FloatingActionButton addAppointmentButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private UserRepository userRepository;
    private UserSessionHelper userSessionHelper;
    private AppointmentRepository appointmentRepository;

    private Button logoutButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_physician_dashboard);
        userSessionHelper = new UserSessionHelper(this);

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
        logoutButton2 = findViewById(R.id.logoutButton2);
        tabLayout = findViewById(R.id.physician_tab_layout);
        viewPager = findViewById(R.id.physician_view_pager);
    }

    private void initDatabase() {
        userRepository = new UserRepository();
        appointmentRepository = new AppointmentRepository();
        userRepository.getUser(userSessionHelper.getUserId(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                updatePhysicianDetails(task.getResult());
            } else {
                Log.e(TAG, "Error getting user details", task.getException());
            }
        });
    }

    private void setupListeners() {
        logoutButton2.setOnClickListener(v -> showLogoutDialog());
    }

    // TODO: need to handle dob
    private void updatePhysicianDetails(DataSnapshot dataSnapshot) {
        String fullName = dataSnapshot.child("fullName").getValue(String.class);

        if (fullName == null) {
            fullName = "Unknown Name";
        }

        updateUI(fullName);
    }

    private void updateUI(String name) {
        tvPhysicianName.setText(String.format("Dr. %s", name));
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 0: return "Patients";
            case 1: return "Appointments";
            default: return "Tab " + position;
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
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