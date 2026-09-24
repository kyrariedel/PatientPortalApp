package com.bridgecare.app;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentViewHolder extends RecyclerView.ViewHolder {
    private TextView idTextView;
    private TextView statusTextView;
    private TextView physicianTextView;
    private TextView patientTextView;
    private TextView dateTextView;
    private TextView notesTextView;
    private TextView timeTextView;

    public AppointmentViewHolder(@NonNull View itemView) {
        super(itemView);
        idTextView = itemView.findViewById(R.id.appointmentId);
        statusTextView = itemView.findViewById(R.id.appointmentStatus);
        physicianTextView = itemView.findViewById(R.id.appointmentPhysician);
        dateTextView = itemView.findViewById(R.id.appointmentDate);
        notesTextView = itemView.findViewById(R.id.appointmentNotes);
        patientTextView = itemView.findViewById(R.id.appointmentPatient);
        timeTextView = itemView.findViewById(R.id.appointmentTime);
    }

    public TextView getIdTextView() {
        return idTextView;
    }

    public void setIdTextView(TextView idTextView) {
        this.idTextView = idTextView;
    }

    public TextView getStatusTextView() {
        return statusTextView;
    }

    public void setStatusTextView(TextView statusTextView) {
        this.statusTextView = statusTextView;
    }

    public TextView getPhysicianTextView() {
        return physicianTextView;
    }


    public void setPhysicianTextView(TextView physicianTextView) {
        this.physicianTextView = physicianTextView;
    }

    public TextView getPatientTextView() {
        return patientTextView;
    }

    public void setPatientTextView(TextView patientTextView) {
        this.patientTextView = patientTextView;
    }

    public TextView getDateTextView() {
        return dateTextView;
    }

    public void setDateTextView(TextView dateTextView) {
        this.dateTextView = dateTextView;
    }

    public TextView getNotesTextView() {
        return notesTextView;
    }

    public void setNotesTextView(TextView notesTextView) {
        this.notesTextView = notesTextView;
    }

    public TextView getTimeTextView() {
        return timeTextView;
    }

    public void setTimeTextView(TextView timeTextView) {
        this.timeTextView = timeTextView;
    }
}
