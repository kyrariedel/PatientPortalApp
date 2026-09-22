package edu.northeastern.numad24fa_group_2_project.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

import edu.northeastern.numad24fa_group_2_project.AppointmentViewHolder;
import edu.northeastern.numad24fa_group_2_project.R;
import edu.northeastern.numad24fa_group_2_project.models.Appointment;
import edu.northeastern.numad24fa_group_2_project.utility.DateUtils;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
    private List<Appointment> appointmentList;
    private DatabaseReference databaseReference;

    public AppointmentAdapter(List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        // changed from id to title
        if (appointment.getTitle() != null) {
            String title = appointment.getTitle();
            holder.getIdTextView().setText(title);
        } else {
            holder.getIdTextView().setText("No Appointment Title");
        }

        if (appointment.getAppointmentDateAndTime() != null) {
            long now = System.currentTimeMillis();
            long appointmentTime = appointment.getAppointmentDateAndTime();
            Calendar calendarNow = Calendar.getInstance();
            Calendar calendarAppointment = Calendar.getInstance();
            calendarAppointment.setTimeInMillis(appointmentTime);
            boolean isSameDay = calendarNow.get(Calendar.YEAR) == calendarAppointment.get(Calendar.YEAR) &&
                    calendarNow.get(Calendar.DAY_OF_YEAR) == calendarAppointment.get(Calendar.DAY_OF_YEAR);
            String dateStatus;
            int color;
            if (isSameDay && appointmentTime >= now) {
                dateStatus = "Today";
                color = Color.BLUE;
            } else if (appointmentTime < now) {
                dateStatus = "Past";
                color = Color.parseColor("#FFA500");
            } else {
                dateStatus = "Upcoming";
                color = Color.parseColor("#006400");
            }

            holder.getStatusTextView().setText(dateStatus);
            holder.getStatusTextView().setTextColor(color);

            // date + time
            holder.getDateTextView().setText(appointment.getAppointmentDateFormatted());
            holder.getTimeTextView().setText(appointment.getAppointmentTimeFormatted());
        } else {
            holder.getStatusTextView().setText("Appointment Status: Unknown");
            holder.getDateTextView().setText("No Date");
            holder.getTimeTextView().setText("No Time");
        }

        if (appointment.getPhysicianAssignedId() != null) {
            getPhysicianName(appointment.getPhysicianAssignedId(), holder.getPhysicianTextView());
        } else {
            holder.getPhysicianTextView().setText("No Physician Assigned");
        }

        if (appointment.getPatientAssignedId() != null) {
            getPatientName(appointment.getPatientAssignedId(), holder.getPatientTextView());
        } else {
            holder.getPatientTextView().setText("No Patient Assigned");
        }

        if (appointment.getNotes() != null) {
            String notes = appointment.getNotes();
            holder.getNotesTextView().setText(notes);
        } else {
            holder.getNotesTextView().setText("No Notes");
        }
    }

    private void getPhysicianName(String physicianId, TextView physicianTextView) {
        databaseReference.child(physicianId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    if (fullName != null) {
                        physicianTextView.setText("Dr. " + fullName);
                    } else {
                        physicianTextView.setText("No Name Found");
                    }
                } else {
                    physicianTextView.setText("No Name Found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                physicianTextView.setText("Error Fetching Name");
                Log.e("TaskAdapter", "Error fetching name", error.toException());
            }
        });
    }

    private void getPatientName(String patientId, TextView patientTextView) {
        databaseReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    if (fullName != null) {
                        patientTextView.setText(fullName);
                    } else {
                        patientTextView.setText("No Name Found");
                    }
                } else {
                    patientTextView.setText("No Name Found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                patientTextView.setText("Error Fetching Name");
                Log.e("TaskAdapter", "Error fetching name", error.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }
}