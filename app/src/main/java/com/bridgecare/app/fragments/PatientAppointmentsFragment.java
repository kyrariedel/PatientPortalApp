package com.bridgecare.app.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.bridgecare.app.R;
import com.bridgecare.app.models.Appointment;
import com.bridgecare.app.utility.UserSessionHelper;


public class PatientAppointmentsFragment extends AbstractUserAppointmentsFragment {

    private final UserSessionHelper userSessionHelper;
    private final String patientId;

    public PatientAppointmentsFragment(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
        patientId = userSessionHelper.getUserId();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerViewAppointments;
    }


    protected String getFirebaseAppointmentsPath() {
        return "appointments";
    }

    protected void fetchAppointments() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getFirebaseAppointmentsPath());
        // isolate task by id
        databaseReference.orderByChild("patientAssignedId").equalTo(patientId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Appointment appointmentItem = parseAppointment(dataSnapshot);
                            if (appointmentItem != null) {
                                appointmentList.add(appointmentItem);
                            }
                        }

                        // sort tasks by desc order
                        appointmentList.sort((appt1, appt2) -> {
                            long now = System.currentTimeMillis();
                            if (appt1.getAppointmentDateAndTime() == null || appt2.getAppointmentDateAndTime() == null) return 0;
                            boolean isAppt1Past = appt1.getAppointmentDateAndTime() < now;
                            boolean isAppt2Past = appt2.getAppointmentDateAndTime() < now;
                            if (isAppt1Past && !isAppt2Past) return 1;  // Past appointments go to the bottom
                            if (!isAppt1Past && isAppt2Past) return -1; // Future appointments go to the top
                            return appt1.getAppointmentDateAndTime().compareTo(appt2.getAppointmentDateAndTime());
                        });
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    protected Appointment parseAppointment(DataSnapshot snapshot) {
        String apptId = snapshot.getKey();
        String apptTitle = snapshot.child("title").getValue(String.class);
        String apptPhysician = snapshot.child("physicianAssignedId").getValue(String.class);
        String apptPatient = snapshot.child("patientAssignedId").getValue(String.class);
        String apptNotes = snapshot.child("notes").getValue(String.class);
        Long apptDandT = snapshot.child("appointmentDateAndTime").getValue(Long.class);

        if (apptTitle != null) {
            Appointment appt = new Appointment();
            appt.setAppointmentId(apptId);
            appt.setTitle(apptTitle);
            appt.setPhysicianAssignedId(apptPhysician);
            appt.setNotes(apptNotes);
            appt.setPatientAssignedId(apptPatient);
            appt.setAppointmentDateAndTime(apptDandT);
            return appt;
        }

        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_appointments, container, false);
        initRecyclerView(view);
        fetchAppointments();

        return view;
    }
}