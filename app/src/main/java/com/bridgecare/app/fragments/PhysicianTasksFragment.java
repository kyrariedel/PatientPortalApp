package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.bridgecare.app.R;
import com.bridgecare.app.models.Task;

public class PhysicianTasksFragment extends AbstractUserTasksFragment {

    private boolean showingPatients = true;
    private List<String> patientList;
    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerViewTasks;
    }

    @Override
    protected String getFirebaseTaskPath() {
        return null;
    }

    @Override
    protected Task parseTask(DataSnapshot snapshot) {
        String taskTitle = snapshot.child("taskTitle").getValue(String.class);
        if (taskTitle != null) {
            Task task = new Task();
            task.setTaskTitle(taskTitle);
            return task;
        }
        return null;
    }

    private void fetchPatientsFromFirebase() {
        Query usersRef = FirebaseDatabase.getInstance().getReference("users");
        listenerRegistrar.removeAll();
        listenerRegistrar.add(usersRef, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList = new ArrayList<>();
                taskList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    Boolean isPatient = userSnapshot.child("patient").getValue(Boolean.class);

                    if ("Patient".equals(role) || Boolean.TRUE.equals(isPatient)) {
                        String patientId = userSnapshot.getKey();
                        String fullName = userSnapshot.child("fullName").getValue(String.class);

                        if (patientId != null) {
                            patientList.add(patientId);

                            Task task = new Task();
                            task.setTaskTitle(fullName != null ? fullName : "Unknown Patient");
                            taskList.add(task);
                        }
                    }
                }
                notifyTasksChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load patients: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onPatientSelected(String patientId) {
        showingPatients = false;
        fetchTasksForPatient(patientId);
    }

    private void fetchTasksForPatient(String patientId) {
        Query taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(patientId);
        listenerRegistrar.removeAll();
        listenerRegistrar.add(taskRef, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = parseTask(taskSnapshot);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                notifyTasksChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physician_tasks, container, false);
        initRecyclerView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (showingPatients) {
            fetchPatientsFromFirebase();
        }
    }
}
