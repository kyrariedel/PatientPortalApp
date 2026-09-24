package com.bridgecare.app.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Map;

import com.bridgecare.app.models.Task;

/**
 * The TaskRepository class is responsible for handling all the database operations related to tasks.
 */
public class TaskRepository implements ITaskRepository {

    private final DatabaseReference mDatabaseTasks;

    /**
     * Default constructor for the TaskRepository. Initializes the database reference to the tasks.
     */
    public TaskRepository() {
        this.mDatabaseTasks = FirebaseDatabase.getInstance().getReference("tasks");
    }

    @Override
    public void addTask(Task task, OnCompleteListener<Void> listener) {
        DatabaseReference newTaskRef = mDatabaseTasks.push();
        task.setTaskId(newTaskRef.getKey());
        newTaskRef.setValue(task).addOnCompleteListener(listener);
    }

    @Override
    public void removeTask(String taskId, OnCompleteListener<Void> listener) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        mDatabaseTasks.child(taskId).removeValue().addOnCompleteListener(listener);
    }

    @Override
    public void updateTask(String taskId, Map<String, Object> updates, OnCompleteListener<Void> listener) {
            if (taskId == null || taskId.isEmpty()) {
                throw new IllegalArgumentException("Task ID cannot be null or empty");
            }
        mDatabaseTasks.child(taskId).updateChildren(updates).addOnCompleteListener(listener);
    }

    @Override
    public void addImageURLtoTask(String taskId, String imageURL, OnCompleteListener<Void> listener) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        if (imageURL == null || imageURL.isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        mDatabaseTasks.child(taskId).child("taskImageUrl").setValue(imageURL).addOnCompleteListener(listener);
    }

    @Override
    public void getImageURLsFromTask(String taskId, ValueEventListener listener) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        mDatabaseTasks.child(taskId).child("taskImagesUrls").addValueEventListener(listener);
    }

    @Override
    public void getTask(String taskId, ValueEventListener listener) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }
        mDatabaseTasks.child(taskId).addValueEventListener(listener);
    }

    @Override
    public void getTasksAssignedFromPhysician(String physicianId, ValueEventListener listener) {
        if (physicianId == null || physicianId.isEmpty()) {
            throw new IllegalArgumentException("Physician ID cannot be null or empty");
        }
        mDatabaseTasks.orderByChild("physicianAssigned").equalTo(physicianId).addValueEventListener(listener);
    }

    @Override
    public void getTasksAssignedFromPatient(String patientId, ValueEventListener listener) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        mDatabaseTasks.orderByChild("patientAssigned").equalTo(patientId).addValueEventListener(listener);
    }

    @Override
    public void getAllTasks(ValueEventListener listener) {
        mDatabaseTasks.addValueEventListener(listener);
    }
}
