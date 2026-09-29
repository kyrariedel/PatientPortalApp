package com.bridgecare.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bridgecare.app.fragments.PatientAppointmentsFragment;
import com.bridgecare.app.fragments.PatientOverviewFragment;
import com.bridgecare.app.fragments.PatientTasksFragment;
import com.bridgecare.app.utility.UserSessionHelper;

public class PatientDashboardAdapter extends FragmentStateAdapter {

    private final UserSessionHelper userSessionHelper;
    public PatientDashboardAdapter(@NonNull FragmentActivity fragmentActivity, UserSessionHelper userSessionHelper) {
        super(fragmentActivity);
        this.userSessionHelper = userSessionHelper;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PatientOverviewFragment();
            case 1:
                return new PatientTasksFragment(userSessionHelper);
            case 2:
                return new PatientAppointmentsFragment(userSessionHelper);
            default:
                return new PatientOverviewFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

