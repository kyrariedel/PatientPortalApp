package com.bridgecare.app.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;

import com.bridgecare.app.R;
import com.bridgecare.app.adapters.TaskAdapter;
import com.bridgecare.app.models.Task;
import com.bridgecare.app.AddTaskActivity;
import com.bridgecare.app.utility.UserSessionHelper;


public class PatientTasksFragment extends AbstractUserTasksFragment {

    private final UserSessionHelper userSessionHelper;
    private String patientId;

    public PatientTasksFragment(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
        patientId = userSessionHelper.getUserId();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
            if (patientId != null) {
                fetchTasksForPatient(patientId);
            }
            isPhysician = getArguments().getBoolean("isPhysician", false);
        }
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerViewTasks;
    }

    // TODO: Need to change the path like "tasks/patient"
    @Override
    protected String getFirebaseTaskPath() {
        return "tasks";
    }

    protected void fetchTasksFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getFirebaseTaskPath());
        // isolate task by id
        databaseReference.orderByChild("patientAssignedId").equalTo(patientId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task taskItem = parseTask(dataSnapshot);
                    if (taskItem != null) {
                        taskList.add(taskItem);
                    }
                }

                // sort tasks by desc order
                        taskList.sort((task1, task2) -> {
                            if (task1.getStartDate() == null || task2.getStartDate() == null) return 0;
                            return task2.getStartDate().compareTo(task1.getStartDate());
                        });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void fetchTasksForPatient(String patientId) {
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        taskRef.orderByChild("patientAssignedId").equalTo(patientId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        taskList.clear();
                        for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                            String taskId = taskSnapshot.child("taskId").getValue(String.class);
                            String patientAssignedId = taskSnapshot.child("patientAssignedId").getValue(String.class);
                            String physicianAssignedId = taskSnapshot.child("physicianAssignedId").getValue(String.class);

                            Task task = new Task();
                            task.setTaskId(taskId);
                            task.setPatientAssignedId(patientAssignedId);
                            task.setPhysicianAssignedId(physicianAssignedId);

                            taskList.add(task);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected Task parseTask(DataSnapshot snapshot) {
        String taskId = snapshot.getKey();
        String taskTitle = snapshot.child("taskTitle").getValue(String.class);
        String taskPhysician = snapshot.child("physicianAssignedId").getValue(String.class);
        String taskImageUrl = snapshot.child("taskImageUrl").getValue(String.class);
        String taskDescription = snapshot.child("taskDescription").getValue(String.class);
        LocalDate taskStart = parseFirebaseDate(snapshot.child("startDate"));
        LocalDate taskEnd = parseFirebaseDate(snapshot.child("endDate"));

        if (taskTitle != null) {
            Task task = new Task();
            task.setTaskId(taskId);
            task.setTaskTitle(taskTitle);
            task.setPhysicianAssignedId(taskPhysician);
            task.setTaskImageUrl(taskImageUrl);
            task.setTaskDescription(taskDescription);
            task.setStartDate(taskStart);
            task.setEndDate(taskEnd);
            return task;
        }

        return null;
    }

    private LocalDate parseFirebaseDate(DataSnapshot dateSnapshot) {
        if (dateSnapshot.exists()) {
            Integer year = dateSnapshot.child("year").getValue(Integer.class);
            Integer month = dateSnapshot.child("monthValue").getValue(Integer.class);
            Integer day = dateSnapshot.child("dayOfMonth").getValue(Integer.class);

            if (year != null && month != null && day != null) {
                return LocalDate.of(year, month, day);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_tasks, container, false);
        initRecyclerView(view);

        if (getArguments() != null) {
            patientId = getArguments().getString("patientId");
        }

        fetchTasksFromFirebase();

        FloatingActionButton fabNewTask = view.findViewById(R.id.addTaskBtn);
//        fabNewTask.setOnClickListener(v -> {
//            // Navigate to create new task activity or show dialog
//            Intent intent = new Intent(getContext(), AddTaskActivity.class);
//            startActivity(intent);
//        });

        if (isPhysician) {
            // remove btn if physician viewing
            fabNewTask.setVisibility(View.GONE);
        } else {
            fabNewTask.setVisibility(View.VISIBLE);
            fabNewTask.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddTaskActivity.class);
                startActivity(intent);
            });
        }
        return view;
    }
}