package com.bridgecare.app.utility;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bridgecare.app.R;
import com.bridgecare.app.models.Task;
import com.bridgecare.app.repositories.TaskRepository;

/**
 * Helper class to show dialog for creating a new task. It can be used by both patients and physicians.
 * I will also try to add an edit task dialog in the future to change the description and images of the task.
 */
public class TaskPatientCreateDialogHelper {
    private final Context context;
    private final TaskRepository taskRepository;
    private final UserSessionHelper userSessionHelper;
    private LocalDate startDate, endDate;
    private EditText etStartDate, etEndDate, etTaskName;
    private Spinner spPhysicians;
    private final Map<String, String> physicianIdMap = new HashMap<>();
    private String selectedPhysicianId;

    /**
     * Constructor to initialize task dialog helper class.
     *
     * @param context            The context of the activity or fragment.
     * @param taskRepository     The repository to interact with the task collection in the database.
     * @param userSessionHelper  The helper class to get the current user's role and id.
     */
    public TaskPatientCreateDialogHelper(Context context, TaskRepository taskRepository, UserSessionHelper userSessionHelper) {
        this.context = context;
        this.taskRepository = taskRepository;
        this.userSessionHelper = userSessionHelper;
    }

    /**
     * Show dialog to create a new task. It's super simple as of right now.
     */
    public void showPatientsCreateTaskDialog() {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_task, null);

        initViews(dialogView);
        setupDatePickers();
        populatePhysicianSpinner();

        new AlertDialog.Builder(context)
                .setTitle("Create New Task")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> createTask())
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void initViews(View dialogView) {
        etTaskName = dialogView.findViewById(R.id.etTaskName);
        etStartDate = dialogView.findViewById(R.id.etStartDate);
        etEndDate = dialogView.findViewById(R.id.etEndDate);
        spPhysicians = dialogView.findViewById(R.id.spPhysicians);
    }

    private void populatePhysicianSpinner() {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("role").equalTo("Physician") // <---- You can change this to get all patients and easily reverse the logic.
                .addListenerForSingleValueEvent(createPhysicianSpinnerListener());
    }

    private ValueEventListener createPhysicianSpinnerListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> physicianNames = new ArrayList<>();
                physicianIdMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String id = userSnapshot.child("id").getValue(String.class);
                    String name = "Dr. " + userSnapshot.child("fullName").getValue(String.class); // <---- You can change this to get the full name of the patient.
                    physicianNames.add(name);
                    physicianIdMap.put(name, id);
                }
                setupSpinnerAdapter(physicianNames);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load physicians.");
            }
        };
    }

    private void setupSpinnerAdapter(List<String> physicianNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, physicianNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPhysicians.setAdapter(adapter);
        spPhysicians.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPhysicianId = physicianIdMap.get(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPhysicianId = null;
            }
        });
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            if (isStartDate) {
                startDate = selectedDate;
                etStartDate.setText(DateUtils.formatDate(startDate));
            } else {
                endDate = selectedDate;
                etEndDate.setText(DateUtils.formatDate(endDate));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void createTask() {
        if (!isInputValid()) {
            showToast("Please fill all fields.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            showToast("End date cannot be before start date.");
            return;
        }
        String taskName = etTaskName.getText().toString().trim();
        String patientId = userSessionHelper.getUserId();
        Task task = new Task(taskName, "", null, patientId, selectedPhysicianId, startDate, endDate);
        saveTask(task);
    }

    private boolean isInputValid() {
        return !etTaskName.getText().toString().trim().isEmpty()
                && startDate != null
                && endDate != null
                && selectedPhysicianId != null;
    }

    private void saveTask(Task task) {
        taskRepository.addTask(task, listener -> showToast(listener.isSuccessful()
                ? "Task created successfully" : "Failed to create task"));
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
