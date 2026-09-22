package edu.northeastern.numad24fa_group_2_project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.numad24fa_group_2_project.repositories.TaskRepository;
import edu.northeastern.numad24fa_group_2_project.utility.UserSessionHelper;

/**
 * This class is used to edit the task by uploading the image to the task.
 */
public class EditTaskActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 300;
    private static final int SELECT_PICTURE = 200;
    private Button BSelectImage, BUploadImage, BOpenCamera;
    private ImageView IVPreviewImage;
    private StorageReference storageReference;
    private TaskRepository taskRepository;
    private UserSessionHelper userSessionHelper;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        initViews();
        setupDatabase();

        taskId = getIntent().getStringExtra("taskId");

        setupListeners();
        requestPermissions();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            openCamera(); // Call your method to open the camera
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void initViews() {
        BSelectImage = findViewById(R.id.BSelectImage);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
        BUploadImage = findViewById(R.id.BUploadImage);
        userSessionHelper = new UserSessionHelper(this);
        BOpenCamera = findViewById(R.id.BOpenCamera);

    }

    private void setupDatabase() {
        storageReference = FirebaseStorage.getInstance().getReference();
        taskRepository = new TaskRepository();
    }

    private void setupListeners() {
        BSelectImage.setOnClickListener(v -> openImageChooser());
        BUploadImage.setOnClickListener(v -> {
            Uri imageUri = (Uri) IVPreviewImage.getTag();
            if (imageUri != null) uploadImageToFirebase(imageUri);
            else Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        });
        BOpenCamera.setOnClickListener(v -> openCamera());
    }

    /**
     * This function is triggered when user selects the image from the imageChooser.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("EditTaskActivity", "onActivityResult");
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE && data != null) {
                // Handle image chooser result
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    IVPreviewImage.setImageURI(selectedImageUri);
                    IVPreviewImage.setTag(selectedImageUri);
                    BUploadImage.setVisibility(View.VISIBLE);
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                // Handle camera capture result
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                IVPreviewImage.setImageBitmap(photo);
                Uri imageUri = getImageUri(photo);
                IVPreviewImage.setTag(imageUri);
                BUploadImage.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e("EditTaskActivity", "Result code not OK");
        }
    }

    private Uri getImageUri(Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "title", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (taskId == null) {
            showToast("Task ID is null");
            return;
        }

        StorageReference imagesFolderRef = storageReference.child("patients/" + userSessionHelper.getUserId() + "/tasks/" + taskId + "/images");
        imagesFolderRef.listAll().addOnSuccessListener(listResult -> {
            deleteExistingImages(listResult);
            uploadNewImage(imagesFolderRef, imageUri);
        }).addOnFailureListener(e -> showToast("Failed to list images: " + e.getMessage()));
    }

    private void deleteExistingImages(ListResult listResult) {
        for (StorageReference item : listResult.getItems()) {
            item.delete().addOnFailureListener(e -> Log.e("Upload", "Failed to delete: " + item.getName(), e));
        }
    }

    private void uploadNewImage(StorageReference folderRef, Uri imageUri) {
        StorageReference fileRef = folderRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveImageToTask(uri.toString()))
        ).addOnFailureListener(e -> showToast("Failed to upload image: " + e.getMessage()));
    }

    private void saveImageToTask(String imageUrl) {
        taskRepository.addImageURLtoTask(taskId, imageUrl, listener -> {
            if (listener.isSuccessful()) {
                showToast("Image saved successfully");
                finish();
            } else {
                showToast("Failed to save image URL to task");
            }
        });
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

