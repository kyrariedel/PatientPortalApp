package com.bridgecare.app.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.bridgecare.app.models.Appointment;

public class AppointmentRepository {

    private final DatabaseReference mDatabaseTasks;

    public AppointmentRepository() {
        this.mDatabaseTasks = FirebaseDatabase.getInstance().getReference("appointments");
    }

    public void addAppointment(Appointment appointment, OnCompleteListener<Void> listener) {
        DatabaseReference newAppointmentRef = mDatabaseTasks.push();
        appointment.setAppointmentId(newAppointmentRef.getKey());
        newAppointmentRef.setValue(appointment).addOnCompleteListener(listener);
    }

    public void removeAppointment(String appointmentId, OnCompleteListener<Void> listener) {
        if (appointmentId == null || appointmentId.isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        mDatabaseTasks.child(appointmentId).removeValue().addOnCompleteListener(listener);
    }

    public void getAppointment(String appointmentId, ValueEventListener listener) {
        if (appointmentId == null || appointmentId.isEmpty()) {
            throw new IllegalArgumentException("Appointment ID cannot be null or empty");
        }
        mDatabaseTasks.child(appointmentId).addValueEventListener(listener);
    }

    public void getAppointmentsFromPatient(String patientId, ValueEventListener listener) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        mDatabaseTasks.orderByChild("patientAssignedId").equalTo(patientId).addValueEventListener(listener);
    }

    public void getAppointmentsFromPhysician(String physicianId, ValueEventListener listener) {
        if (physicianId == null || physicianId.isEmpty()) {
            throw new IllegalArgumentException("Physician ID cannot be null or empty");
        }
        mDatabaseTasks.orderByChild("physicianAssignedId").equalTo(physicianId).addValueEventListener(listener);
    }

    public void getAllAppointments(ValueEventListener listener) {
        mDatabaseTasks.addValueEventListener(listener);
    }
}