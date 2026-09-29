package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.bridgecare.app.R;
import com.bridgecare.app.models.Appointment;
import com.bridgecare.app.models.Task;
import com.bridgecare.app.repositories.AppointmentRepository;
import com.bridgecare.app.utility.AppointmentPhysicianCreateDialogHelper;
import com.bridgecare.app.utility.DateUtils;
import com.bridgecare.app.utility.FirebaseListenerRegistrar;
import com.bridgecare.app.utility.TaskPatientCreateDialogHelper;

public class PatientOverviewFragment extends AbstractUserOverviewFragment {

    private TextView tvUpcomingAppointment, tvTaskName;
    private FloatingActionButton addTaskButton, addAppointmentButton;
    private ProgressBar taskProgressBar;
    private AppointmentRepository appointmentRepository;
    private final FirebaseListenerRegistrar listenerRegistrar = new FirebaseListenerRegistrar();

    public PatientOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    protected void initDatabase() {
        super.initDatabase();
        appointmentRepository = new AppointmentRepository();
    }

    @Override
    protected void initViews(View rootView) {
        tvUpcomingAppointment = rootView.findViewById(R.id.tvUpcomingAppointment);
        tvTaskName = rootView.findViewById(R.id.tvTaskName);
        addTaskButton = rootView.findViewById(R.id.OVRtaskFloatingActionButton);
        addAppointmentButton = rootView.findViewById(R.id.OVRappointmentFloatingActionButton);
        taskProgressBar = rootView.findViewById(R.id.taskProgressBar);
        tvUpcomingAppointment.setText(R.string.no_upcoming_appointment);
        tvTaskName.setText(R.string.no_tasks_yet);
        taskProgressBar.setProgress(0);
    }

    @Override
    protected void setupListeners() {
        addTaskButton.setOnClickListener(v -> {
            TaskPatientCreateDialogHelper dialogHelper = new TaskPatientCreateDialogHelper(requireContext(), taskRepository, userSessionHelper);
            dialogHelper.showPatientsCreateTaskDialog();
        });
        addAppointmentButton.setOnClickListener(v -> {
            AppointmentPhysicianCreateDialogHelper dialogHelper =
                    new AppointmentPhysicianCreateDialogHelper(requireContext(), appointmentRepository, userSessionHelper);
            dialogHelper.showCreateAppointmentDialog();
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

    @Override
    public void onStart() {
        super.onStart();
        loadOverviewData();
    }

    @Override
    public void onStop() {
        listenerRegistrar.removeAll();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        listenerRegistrar.removeAll();
        super.onDestroyView();
    }

    private void loadOverviewData() {
        if (userSessionHelper == null || userSessionHelper.getUserId() == null) {
            return;
        }
        listenerRegistrar.removeAll();
        String patientId = userSessionHelper.getUserId();

        Query appointmentsQuery = FirebaseDatabase.getInstance()
                .getReference("appointments")
                .orderByChild("patientAssignedId")
                .equalTo(patientId);
        listenerRegistrar.add(appointmentsQuery, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Appointment upcoming = findUpcomingAppointment(snapshot);
                updateUpcomingAppointment(upcoming);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvUpcomingAppointment != null) {
                    tvUpcomingAppointment.setText(R.string.no_upcoming_appointment);
                }
            }
        });

        Query tasksQuery = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .orderByChild("patientAssignedId")
                .equalTo(patientId);
        listenerRegistrar.add(tasksQuery, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Task> tasks = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = parseTask(child);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
                updateTaskProgress(tasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvTaskName != null) {
                    tvTaskName.setText(R.string.no_tasks_yet);
                }
                if (taskProgressBar != null) {
                    taskProgressBar.setProgress(0);
                }
            }
        });
    }

    private Appointment findUpcomingAppointment(DataSnapshot snapshot) {
        Appointment upcoming = null;
        long now = System.currentTimeMillis();
        for (DataSnapshot child : snapshot.getChildren()) {
            Appointment appointment = parseAppointment(child);
            if (appointment == null || appointment.getAppointmentDateAndTime() == null) {
                continue;
            }
            long time = appointment.getAppointmentDateAndTime();
            if (time < now) {
                continue;
            }
            if (upcoming == null || time < upcoming.getAppointmentDateAndTime()) {
                upcoming = appointment;
            }
        }
        return upcoming;
    }

    private void updateUpcomingAppointment(Appointment upcoming) {
        if (tvUpcomingAppointment == null) {
            return;
        }
        if (upcoming == null) {
            tvUpcomingAppointment.setText(R.string.no_upcoming_appointment);
            return;
        }
        String when = DateUtils.formatDateDayTime(upcoming.getAppointmentDateAndTime());
        String physicianId = upcoming.getPhysicianAssignedId();
        if (physicianId == null) {
            tvUpcomingAppointment.setText(when);
            return;
        }
        FirebaseDatabase.getInstance().getReference("users").child(physicianId)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (tvUpcomingAppointment == null || !isAdded()) {
                        return;
                    }
                    String fullName = userSnapshot.child("fullName").getValue(String.class);
                    String prefix = fullName != null ? "Dr. " + fullName + " " : "";
                    tvUpcomingAppointment.setText(prefix + when);
                })
                .addOnFailureListener(e -> {
                    if (tvUpcomingAppointment != null && isAdded()) {
                        tvUpcomingAppointment.setText(when);
                    }
                });
    }

    private void updateTaskProgress(List<Task> tasks) {
        if (tvTaskName == null || taskProgressBar == null) {
            return;
        }
        if (tasks.isEmpty()) {
            tvTaskName.setText(R.string.no_tasks_yet);
            taskProgressBar.setProgress(0);
            return;
        }

        LocalDate today = LocalDate.now();
        int completed = 0;
        Task currentTask = null;
        for (Task task : tasks) {
            if (isTaskComplete(task, today)) {
                completed++;
            } else if (currentTask == null || compareTasks(task, currentTask) < 0) {
                currentTask = task;
            }
        }

        int progress = (int) Math.round(completed * 100.0 / tasks.size());
        taskProgressBar.setMax(100);
        taskProgressBar.setProgress(progress);

        if (currentTask != null && currentTask.getTaskTitle() != null) {
            tvTaskName.setText(getString(R.string.task_progress_summary, completed, tasks.size())
                    + "\n" + currentTask.getTaskTitle());
        } else {
            tvTaskName.setText(getString(R.string.task_progress_summary, completed, tasks.size())
                    + "\n" + getString(R.string.all_tasks_complete));
        }
    }

    private boolean isTaskComplete(Task task, LocalDate today) {
        return task.getEndDate() != null && task.getEndDate().isBefore(today);
    }

    private int compareTasks(Task left, Task right) {
        LocalDate leftDate = left.getStartDate() != null ? left.getStartDate() : left.getEndDate();
        LocalDate rightDate = right.getStartDate() != null ? right.getStartDate() : right.getEndDate();
        if (leftDate == null && rightDate == null) {
            return 0;
        }
        if (leftDate == null) {
            return 1;
        }
        if (rightDate == null) {
            return -1;
        }
        return leftDate.compareTo(rightDate);
    }

    private Appointment parseAppointment(DataSnapshot snapshot) {
        String title = snapshot.child("title").getValue(String.class);
        if (title == null) {
            return null;
        }
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(snapshot.getKey());
        appointment.setTitle(title);
        appointment.setPhysicianAssignedId(snapshot.child("physicianAssignedId").getValue(String.class));
        appointment.setPatientAssignedId(snapshot.child("patientAssignedId").getValue(String.class));
        appointment.setNotes(snapshot.child("notes").getValue(String.class));
        appointment.setAppointmentDateAndTime(snapshot.child("appointmentDateAndTime").getValue(Long.class));
        return appointment;
    }

    private Task parseTask(DataSnapshot snapshot) {
        String taskTitle = snapshot.child("taskTitle").getValue(String.class);
        if (taskTitle == null) {
            return null;
        }
        Task task = new Task();
        task.setTaskId(snapshot.getKey());
        task.setTaskTitle(taskTitle);
        task.setPhysicianAssignedId(snapshot.child("physicianAssignedId").getValue(String.class));
        task.setTaskImageUrl(snapshot.child("taskImageUrl").getValue(String.class));
        task.setTaskDescription(snapshot.child("taskDescription").getValue(String.class));
        task.setStartDate(parseFirebaseDate(snapshot.child("startDate")));
        task.setEndDate(parseFirebaseDate(snapshot.child("endDate")));
        return task;
    }

    private LocalDate parseFirebaseDate(DataSnapshot dateSnapshot) {
        if (!dateSnapshot.exists()) {
            return null;
        }
        Integer year = dateSnapshot.child("year").getValue(Integer.class);
        Integer month = dateSnapshot.child("monthValue").getValue(Integer.class);
        Integer day = dateSnapshot.child("dayOfMonth").getValue(Integer.class);
        if (year != null && month != null && day != null) {
            return LocalDate.of(year, month, day);
        }
        return null;
    }
}
