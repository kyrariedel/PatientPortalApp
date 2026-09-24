package com.bridgecare.app;

import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bridgecare.app.models.Task;
import com.bridgecare.app.repositories.TaskRepository;
import com.bridgecare.app.utility.UserSessionHelper;

public class AddTaskActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 300;
    private static final int SELECT_PICTURE = 200;
    private EditText etTaskTitle, etTaskDescription, etStartDate, etEndDate;
    private Button btnCreateTask, btnTakePhoto, btnSelectImage;
    private ImageView IVPreviewImage;
    private Spinner spinnerPhysician;

    private StorageReference storageReference;
    private TaskRepository taskRepository;
    private UserSessionHelper userSessionHelper;

    private String selectedPhysicianId = null;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_task);

        initViews();
        setupDatabase();
        setupListeners();
        setupSpinners();
    }

    private void initViews() {
        etTaskTitle = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        spinnerPhysician = findViewById(R.id.spPhysicians);
        userSessionHelper = new UserSessionHelper(this);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
    }

    private void setupDatabase() {
        storageReference = FirebaseStorage.getInstance().getReference();
        taskRepository = new TaskRepository();
    }

    private void setupSpinners() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        List<String> physicianNames = new ArrayList<>();
        List<String> physicianIds = new ArrayList<>();

        physicianNames.add("Select Physician");
        physicianIds.add(null);

        ArrayAdapter<String> physicianAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                physicianNames
        );
        physicianAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhysician.setAdapter(physicianAdapter);

        // Get physicians from firebase
        usersRef.orderByChild("role").equalTo("Physician").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String fullName = userSnapshot.child("fullName").getValue(String.class);
                    String id = userSnapshot.child("id").getValue(String.class);

                    if (fullName != null && id != null) {
                        physicianNames.add(fullName);
                        physicianIds.add(id);
                    }
                }

                physicianAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AddTaskActivity.this, "Failed to load physicians: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // pick physician from list of physicians in database
        spinnerPhysician.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPhysicianId = physicianIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPhysicianId = null;
            }
        });
    }

    private void setupListeners() {
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
        btnCreateTask.setOnClickListener(v -> createTask());

        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnSelectImage.setOnClickListener(v -> openImageChooser());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE && data != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    IVPreviewImage.setImageURI(selectedImageUri);
                    IVPreviewImage.setVisibility(View.VISIBLE);
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedImageUri = getImageUri(photo);
                IVPreviewImage.setImageBitmap(photo);
                IVPreviewImage.setVisibility(View.VISIBLE);
            }
        }
    }

    private Uri getImageUri(Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "title", null);
        return Uri.parse(path);
    }

    // translate dates into correct format
    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                    editText.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void createTask() {
        if (!validateInputs()) {
            return;
        }

        // create task - images null for now
        Task newTask = new Task(
                null,
                etTaskTitle.getText().toString(),
                etTaskDescription.getText().toString(),
                null,
                userSessionHelper.getUserId(),
                selectedPhysicianId,
                LocalDate.parse(etStartDate.getText().toString()),
                LocalDate.parse(etEndDate.getText().toString()),
                null
        );

        saveTask(newTask);
    }

    private void saveTask(Task task) {
        DatabaseReference newTaskRef = FirebaseDatabase.getInstance().getReference("tasks").push();
        String newTaskId = newTaskRef.getKey();
        newTaskRef.setValue(task).addOnCompleteListener(taskCompletion -> {
            if (taskCompletion.isSuccessful()) {
                if (selectedImageUri != null) {
                    uploadImageToFirebase(newTaskId);
                } else {
                    IVPreviewImage.setVisibility(View.GONE);
                    Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to create task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(String taskId) {
        StorageReference imagesFolderRef = storageReference.child("patients/" + userSessionHelper.getUserId() + "/tasks/" + taskId + "/images");

        StorageReference fileRef = imagesFolderRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save image URL to task
                    taskRepository.addImageURLtoTask(taskId, uri.toString(), listener -> {
                        if (listener.isSuccessful()) {
                            Toast.makeText(this, "Task and image saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to save image URL to task", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private boolean validateInputs() {
        if (etTaskTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(AddTaskActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etStartDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(AddTaskActivity.this, "Start date is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etEndDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(AddTaskActivity.this, "End date is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        LocalDate startDate = LocalDate.parse(etStartDate.getText().toString());
        LocalDate endDate = LocalDate.parse(etEndDate.getText().toString());
        if (endDate.isBefore(startDate)) {
            Toast.makeText(AddTaskActivity.this, "Start date must be before end date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedPhysicianId == null) {
            Toast.makeText(AddTaskActivity.this, "Physician is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}