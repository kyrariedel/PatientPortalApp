package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.bridgecare.app.R;
import com.bridgecare.app.adapters.PatientAdapter;
import com.bridgecare.app.models.Patient;
import com.bridgecare.app.utility.FirebaseListenerRegistrar;
import com.bridgecare.app.utility.UserSessionHelper;

public class PhysicianPatientsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private List<Patient> patientList;
    private final UserSessionHelper userSessionHelper;
    private final String physicianId;
    private final FirebaseListenerRegistrar listenerRegistrar = new FirebaseListenerRegistrar();
    private final List<String> assignedPatientIds = new ArrayList<>();
    private DataSnapshot latestUsersSnapshot;

    public PhysicianPatientsFragment(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
        physicianId = userSessionHelper.getUserId();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physician_patients, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPatients);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        patientList = new ArrayList<>();
        patientAdapter = new PatientAdapter(patientList, patient -> showTaskDetails(patient));

        recyclerView.setAdapter(patientAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchPatientsFromFirebase();
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

    private void fetchPatientsFromFirebase() {
        listenerRegistrar.removeAll();
        Query tasksQuery = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .orderByChild("physicianAssignedId")
                .equalTo(physicianId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        listenerRegistrar.add(tasksQuery, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignedPatientIds.clear();
                for (DataSnapshot taskSnapshot: snapshot.getChildren()) {
                    String patientId = taskSnapshot.child("patientAssignedId").getValue(String.class);
                    if (patientId != null && !assignedPatientIds.contains(patientId)) {
                        assignedPatientIds.add(patientId);
                    }
                }
                rebuildPatientList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        listenerRegistrar.add(userRef, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                latestUsersSnapshot = snapshot;
                rebuildPatientList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load patients: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void rebuildPatientList() {
        if (patientList == null || patientAdapter == null || latestUsersSnapshot == null) {
            return;
        }
        patientList.clear();
        for (String patientId : assignedPatientIds) {
            DataSnapshot userSnapshot = latestUsersSnapshot.child(patientId);
            if (userSnapshot.exists()) {
                Patient patient = new Patient();
                patient.setId(patientId);
                patient.setFullName(userSnapshot.child("fullName").getValue(String.class));
                patientList.add(patient);
            }
        }
        patientAdapter.notifyDataSetChanged();
    }

    private void showTaskDetails(Patient patient) {
        String fragmentTag = "TASKS_FRAGMENT_" + patient.getId();
        Fragment existingFragment = requireActivity().getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (existingFragment != null && existingFragment.isVisible()) {
            return;
        }

        Fragment patientTasksFragment = new PatientTasksFragment(userSessionHelper);
        Bundle bundle = new Bundle();
        bundle.putString("patientId", patient.getId());
        bundle.putBoolean("isPhysician", true);
        patientTasksFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, patientTasksFragment, fragmentTag)
                .addToBackStack(null)
                .commit();
    }
}
