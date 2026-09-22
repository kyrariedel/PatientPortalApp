package edu.northeastern.numad24fa_group_2_project.fragments;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PhysicianOverviewFragment extends  AbstractUserOverviewFragment {

    private TextView tvUpcomingAppointment, tvTaskName;
    private FloatingActionButton addTaskButton, addAppointmentButton;
    private ProgressBar taskProgressBar;

    public PhysicianOverviewFragment() {}

    @Override
    protected void initViews(View rootView) {

    }

    @Override
    protected void setupListeners() {

    }
}
