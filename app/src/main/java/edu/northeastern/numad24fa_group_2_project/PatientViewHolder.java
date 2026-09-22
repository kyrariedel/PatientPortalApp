package edu.northeastern.numad24fa_group_2_project;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PatientViewHolder extends RecyclerView.ViewHolder {
    public TextView patientFullname;
    public Button deatilsButton;

    public PatientViewHolder(@NonNull View itemView) {
        super(itemView);
        patientFullname = itemView.findViewById(R.id.patient_fullname);
        deatilsButton = itemView.findViewById(R.id.patient_details);
    }
}
