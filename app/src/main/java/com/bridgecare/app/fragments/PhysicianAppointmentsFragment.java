package com.bridgecare.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.bridgecare.app.R;
import com.bridgecare.app.models.Appointment;
import com.bridgecare.app.repositories.AppointmentRepository;
import com.bridgecare.app.utility.AppointmentPhysicianCreateDialogHelper;
import com.bridgecare.app.utility.UserSessionHelper;

public class PhysicianAppointmentsFragment extends AbstractUserAppointmentsFragment {

    private final String physicianId;
    private final UserSessionHelper userSessionHelper;
    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    public PhysicianAppointmentsFragment(UserSessionHelper userSessionHelper) {
        physicianId = userSessionHelper.getUserId();
        this.userSessionHelper = userSessionHelper;
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

    @Override
    protected void fetchAppointments() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(getFirebaseAppointmentsPath())
                .orderByChild("physicianAssignedId")
                .equalTo(physicianId);
        listenerRegistrar.removeAll();
        listenerRegistrar.add(query, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Appointment appointmentItem = parseAppointment(dataSnapshot);
                    if (appointmentItem != null) {
                        appointmentList.add(appointmentItem);
                    }
                }

                appointmentList.sort((appt1, appt2) -> {
                    long now = System.currentTimeMillis();
                    if (appt1.getAppointmentDateAndTime() == null || appt2.getAppointmentDateAndTime() == null) return 0;
                    boolean isAppt1Past = appt1.getAppointmentDateAndTime() < now;
                    boolean isAppt2Past = appt2.getAppointmentDateAndTime() < now;
                    if (isAppt1Past && !isAppt2Past) return 1;
                    if (!isAppt1Past && isAppt2Past) return -1;
                    return appt1.getAppointmentDateAndTime().compareTo(appt2.getAppointmentDateAndTime());
                });
                notifyAppointmentsChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load appointments: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected Appointment parseAppointment(DataSnapshot snapshot) {
        String apptId = snapshot.getKey();
        String apptTitle = snapshot.child("title").getValue(String.class);
        String apptPatient = snapshot.child("patientAssignedId").getValue(String.class);
        String apptPhysician = snapshot.child("physicianAssignedId").getValue(String.class);
        String apptNotes = snapshot.child("notes").getValue(String.class);
        Long apptDandT = snapshot.child("appointmentDateAndTime").getValue(Long.class);

        if (apptTitle != null) {
            Appointment appt = new Appointment();
            appt.setAppointmentId(apptId);
            appt.setTitle(apptTitle);
            appt.setPatientAssignedId(apptPatient);
            appt.setPhysicianAssignedId(apptPhysician);
            appt.setNotes(apptNotes);
            appt.setAppointmentDateAndTime(apptDandT);
            return appt;
        }

        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physician_appointments, container, false);
        initRecyclerView(view);
        FloatingActionButton button = view.findViewById(R.id.addAppointmentButton);
        button.setOnClickListener(v -> {
            AppointmentPhysicianCreateDialogHelper dialogHelper = new AppointmentPhysicianCreateDialogHelper(requireContext(),
                    appointmentRepository , userSessionHelper);
            dialogHelper.showCreateAppointmentDialog();
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchAppointments();
    }
}
