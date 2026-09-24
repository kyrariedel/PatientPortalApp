package com.bridgecare.app.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ValueEventListener;
import java.util.Map;
import com.bridgecare.app.models.Task;

public interface ITaskRepository {

    /**
     * Add a task to the database. It generates a task ID and sets it to the task object.
     * @param task the task to add.
     * @param listener the listener to call when the addition is complete.
     */
    void addTask(Task task, OnCompleteListener<Void> listener);

    /**
     * Remove a task from the database.
     * @param taskId the task ID to remove.
     * @param listener the listener to call when the removal is complete.
     */
    void removeTask(String taskId, OnCompleteListener<Void> listener);

    /**
     * Update a task with the given updates.
     * @param taskId the task ID to update.
     * @param updates the updates to apply to the task.
     * @param listener the listener to call when the update is complete.
     * <dt><span class="strong">Example Below:</span> <pre>
     * {@code
     * Map<String, Object> updates = new HashMap<>();
     * updates.put("taskTitle", "New Task Title");
     * updates.put("taskDescription", "New Task Description");
     * }</pre></dt>
     * <dt></span> <pre>
     * {@code
     * taskRepository.updateTask("taskId1", updates, task -> {
     *     if (task.isSuccessful()) {
     *         // Something here
     *     } else {
     *         // Something here
     *     }
     * }
     * }</dt>
     */
    void updateTask(String taskId, Map<String, Object> updates, OnCompleteListener<Void> listener);

    /**
     * Add an image URL to a task. MUST be a url.
     * @param taskId the task ID to add the image URL to.
     * @param imageURL the image URL to add as a string. MUST be a URL.
     */
    void addImageURLtoTask(String taskId, String imageURL, OnCompleteListener<Void> listener);

    /**
     * Get all image URLs for a task.
     * @param taskId the task ID to get images for.
     * @param listener the listener to call when the data is retrieved.
     */
    void getImageURLsFromTask(String taskId, ValueEventListener listener);

    /**
     * Get a task from the database.
     * @param taskId the task ID to get.
     * @param listener the listener to call when the data is retrieved.
     */
    void getTask(String taskId, ValueEventListener listener);

    /**
     * Get all tasks assigned to a physician.
     * @param physicianId the physician ID to get tasks for.
     * @param listener the listener to call when the data is retrieved.
     */
    void getTasksAssignedFromPhysician(String physicianId, ValueEventListener listener);

    /**
     * Get all tasks assigned to a patient.
     * @param patientId the patient ID to get tasks for.
     * @param listener the listener to call when the data is retrieved.
     */
    void getTasksAssignedFromPatient(String patientId, ValueEventListener listener);

    /**
     * Get all tasks from the database. This is to show every task in the database. USE WITH CAUTION.
     */
    void getAllTasks(ValueEventListener listener);
}
