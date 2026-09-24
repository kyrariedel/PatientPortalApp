package com.bridgecare.app.utility;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bridgecare.app.R;
import com.bridgecare.app.models.Appointment;
import com.bridgecare.app.repositories.AppointmentRepository;

public class AppointmentPhysicianCreateDialogHelper {
    private EditText etAppointmentName, etAppointmentDate, etAppointmentTime, etNotes;
    private Spinner spPatients;
    private final Context context;
    private final AppointmentRepository appointmentRepository;
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private final UserSessionHelper userSessionHelper;
    private final Map<String, String> patientIdMap = new HashMap<>();
    private String selectedPatientId;

    public AppointmentPhysicianCreateDialogHelper(Context context, AppointmentRepository appointmentRepository,
                                                  UserSessionHelper userSessionHelper) {
        this.context = context;
        this.appointmentRepository = appointmentRepository;
        this.userSessionHelper = userSessionHelper;
    }

    public void showCreateAppointmentDialog() {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_appointment, null);

        initViews(dialogView);
        setupListeners();
        populatePatientSpinner();

        new AlertDialog.Builder(context)
                .setTitle("Create New Appointment")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> createAppointment())
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

    private void populatePatientSpinner() {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("role").equalTo("Patient")
                .addListenerForSingleValueEvent(createPatientSpinnerListener());
    }

    private ValueEventListener createPatientSpinnerListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> patientNames = new ArrayList<>();
                patientIdMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String id = userSnapshot.child("id").getValue(String.class);
                    String name = userSnapshot.child("fullName").getValue(String.class); // <---- You can change this to get the full name of the patient.
                    patientNames.add(name);
                    patientIdMap.put(name, id);
                }
                setupSpinnerAdapter(patientNames);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load patients.");
            }
        };
    }

    private void setupSpinnerAdapter(List<String> physicianNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, physicianNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPatients.setAdapter(adapter);
        spPatients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPatientId = patientIdMap.get(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPatientId = null;
            }
        });
    }

    private void setupListeners() {
        etAppointmentDate.setOnClickListener(v -> showDatePickerDialog());
        etAppointmentTime.setOnClickListener(v -> showTimePickerDialog());
    }

    private void initViews(View dialogView) {
        etAppointmentName = dialogView.findViewById(R.id.etAppointmentName);
        etAppointmentDate = dialogView.findViewById(R.id.etAppointmentDate);
        etAppointmentTime = dialogView.findViewById(R.id.etAppointmentTime);
        etNotes = dialogView.findViewById(R.id.etNotes);
        spPatients = dialogView.findViewById(R.id.spPatients);
    }

    private void showTimePickerDialog() {
        int hour = 12;
        int minute = 0;

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minuteOfDay) -> {
            selectedTime = LocalTime.of(hourOfDay, minuteOfDay);
            String time;
            time = String.format("%d:%02d %s",
                    (hourOfDay % 12 == 0 ? 12 : hourOfDay % 12),
                    minuteOfDay,
                    hourOfDay >= 12 ? "PM" : "AM");
            etAppointmentTime.setText(time);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            etAppointmentDate.setText(DateUtils.formatDate(selectedDate));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void createAppointment() {
        if (!isInputValid()) {
            showToast("Please fill all fields.");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            showToast("Appointment date cannot be in the past.");
            return;
        }

        String appointmentName = etAppointmentName.getText().toString().trim();
        String physicianId = userSessionHelper.getUserId();
        String notes = etNotes.getText().toString();
        LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
        Long combinedDateTimeInMillis  = appointmentDateTime
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Appointment appointment = new Appointment(combinedDateTimeInMillis, appointmentName, notes, physicianId, selectedPatientId);
        saveAppointment(appointment);

    }

    private void saveAppointment(Appointment appointment) {
        appointmentRepository.addAppointment(appointment, listener -> showToast(listener.isSuccessful()
                ? "Appointment created successfully" : "Failed to create appointment"));
    }

    private boolean isInputValid() {
        return !etAppointmentName.getText().toString().trim().isEmpty()
                && etAppointmentDate != null
                && etAppointmentTime != null
                && selectedPatientId != null;
    }


    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
