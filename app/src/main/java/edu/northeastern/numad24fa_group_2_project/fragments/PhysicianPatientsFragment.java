package edu.northeastern.numad24fa_group_2_project.fragments;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad24fa_group_2_project.R;
import edu.northeastern.numad24fa_group_2_project.adapters.PatientAdapter;
import edu.northeastern.numad24fa_group_2_project.models.Patient;
import edu.northeastern.numad24fa_group_2_project.utility.UserSessionHelper;

public class PhysicianPatientsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private List<Patient> patientList;
    private final UserSessionHelper userSessionHelper;
    private final String physicianId;

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
        fetchPatientsFromFirebase();

        return view;
    }

    private void fetchPatientsFromFirebase() {
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        taskRef.orderByChild("physicianAssignedId").equalTo(physicianId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        patientList.clear();
                        List<String> patientIds = new ArrayList<>();
                        for (DataSnapshot taskSnapshot: snapshot.getChildren()) {
                            String patientId = taskSnapshot.child("patientAssignedId").getValue(String.class);
                            if (patientId != null && !patientIds.contains(patientId)) {
                                patientIds.add(patientId);
                            }
                        }
                        fetchPatientDetails(userRef, patientIds);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPatientDetails(DatabaseReference userRef, List<String> patientIds) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String patientId : patientIds) {
                    DataSnapshot userSnapshot = snapshot.child(patientId);
                    if (userSnapshot.exists()) {
                        Patient patient = new Patient();
                        patient.setId(patientId);
                        patient.setFullName(userSnapshot.child("fullName").getValue(String.class));
                        patientList.add(patient);
                    }
                }
                patientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load patients: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
