package com.bridgecare.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bridgecare.app.PatientViewHolder;
import com.bridgecare.app.R;
import com.bridgecare.app.models.Patient;

public class PatientAdapter extends RecyclerView.Adapter<PatientViewHolder> {
    private final List<Patient> patientList;
    private final OnPatientClickListener onPatientClickListener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientAdapter(List<Patient> patientList, OnPatientClickListener onPatientClickListener) {
        this.patientList = patientList;
        this.onPatientClickListener = onPatientClickListener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.patientFullname.setText(patient.getFullName());
        holder.deatilsButton.setOnClickListener(v -> onPatientClickListener.onPatientClick(patient));
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }
}
