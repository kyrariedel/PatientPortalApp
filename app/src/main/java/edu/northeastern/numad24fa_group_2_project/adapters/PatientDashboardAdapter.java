package edu.northeastern.numad24fa_group_2_project.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeastern.numad24fa_group_2_project.fragments.PatientAppointmentsFragment;
import edu.northeastern.numad24fa_group_2_project.fragments.PatientTasksFragment;
import edu.northeastern.numad24fa_group_2_project.utility.UserSessionHelper;

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
                return new PatientTasksFragment(userSessionHelper);
            case 1:
                return new PatientAppointmentsFragment(userSessionHelper);
            default:
                return new PatientTasksFragment(userSessionHelper);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

