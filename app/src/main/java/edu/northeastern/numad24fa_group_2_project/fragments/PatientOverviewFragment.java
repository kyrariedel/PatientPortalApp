package edu.northeastern.numad24fa_group_2_project.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.northeastern.numad24fa_group_2_project.R;
import edu.northeastern.numad24fa_group_2_project.utility.TaskPatientCreateDialogHelper;

public class PatientOverviewFragment extends AbstractUserOverviewFragment {

    // TODO: Implement latest appointment and task, also be able to add a an appointment.
    private TextView tvUpcomingAppointment, tvTaskName;
    private FloatingActionButton addTaskButton, addAppointmentButton;
    private ProgressBar taskProgressBar;

    public PatientOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    protected void initViews(View rootView) {
        tvUpcomingAppointment = rootView.findViewById(R.id.tvUpcomingAppointment);
        tvTaskName = rootView.findViewById(R.id.tvTaskName);
        addTaskButton = rootView.findViewById(R.id.OVRtaskFloatingActionButton);
        addAppointmentButton = rootView.findViewById(R.id.OVRappointmentFloatingActionButton);
        taskProgressBar = rootView.findViewById(R.id.taskProgressBar);
    }

    @Override
    protected void setupListeners() {
        addTaskButton.setOnClickListener(v -> {
            TaskPatientCreateDialogHelper dialogHelper = new TaskPatientCreateDialogHelper(requireContext(), taskRepository, userSessionHelper);
            dialogHelper.showPatientsCreateTaskDialog();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_overview, container, false);
        initViews(rootView);
        setupListeners();

        return rootView;
    }
}