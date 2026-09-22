package edu.northeastern.numad24fa_group_2_project.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.numad24fa_group_2_project.fragments.PhysicianAppointmentsFragment;
import edu.northeastern.numad24fa_group_2_project.fragments.PhysicianOverviewFragment;
import edu.northeastern.numad24fa_group_2_project.fragments.PhysicianPatientsFragment;
import edu.northeastern.numad24fa_group_2_project.fragments.PhysicianTasksFragment;
import edu.northeastern.numad24fa_group_2_project.utility.UserSessionHelper;

public class PhysicianDashboardAdapter extends FragmentStateAdapter {

    private final UserSessionHelper userSessionHelper;
    public PhysicianDashboardAdapter(@NonNull FragmentActivity fragmentActivity, UserSessionHelper userSessionHelper) {
        super(fragmentActivity);
        this.userSessionHelper = userSessionHelper;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PhysicianPatientsFragment(userSessionHelper);
            case 1:
                return new PhysicianAppointmentsFragment(userSessionHelper);
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
