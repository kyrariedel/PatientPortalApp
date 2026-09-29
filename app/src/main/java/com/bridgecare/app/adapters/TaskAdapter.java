package com.bridgecare.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.content.Intent;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.bridgecare.app.EditTaskActivity;
import com.bridgecare.app.R;
import com.bridgecare.app.TaskViewHolder;
import com.bridgecare.app.models.Task;
import com.bridgecare.app.utility.DateUtils;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private List<Task> taskList;
    private Context context;
    private DatabaseReference databaseReference;
    private boolean isPhysician;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public TaskAdapter(List<Task> taskList, boolean isPhysician, Context context) {
        this.taskList = taskList;
        this.context = context;
        this.isPhysician = isPhysician;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        if (task.getTaskTitle() != null) {
            String taskTitle = task.getTaskTitle();
            holder.getTitleTextView().setText(taskTitle);
        } else {
            holder.getTitleTextView().setText("No Task Title");
        }

        // get fullName from Physician user from ID
        if (task.getPhysicianAssignedId() != null) {
            getPhysicianName(task.getPhysicianAssignedId(), holder.getPhysicianTextView());
        } else {
            holder.getPhysicianTextView().setText("No Physician Assigned");
        }

        if (task.getStartDate() != null) {
            String formattedStartDate = DateUtils.formatDate(task.getStartDate());
            holder.getStartDateTextView().setText(formattedStartDate);
        } else {
            holder.getStartDateTextView().setText("No Start Date");
        }

        if (task.getEndDate() != null) {
            String formattedEndDate = DateUtils.formatDate(task.getEndDate());
            holder.getEndDateTextView().setText(formattedEndDate);
        } else {
            holder.getEndDateTextView().setText("No End Date");
        }
        ImageView taskImageView = holder.getTaskImageView();
        String taskImage = task.getTaskImageUrl();
        if (taskImage != null && !taskImage.isEmpty()) {
            loadTaskImage(taskImage, taskImageView);
            taskImageView.setVisibility(View.VISIBLE);
        } else {
            taskImageView.setVisibility(View.GONE);
        }

        holder.getEditButton().setOnClickListener(v -> goToEditTaskActivity(task.getTaskId()));
        holder.getEditButton().setVisibility(isPhysician ? View.GONE : View.VISIBLE);
    }

    // uses ID to get name for taskList
    private void getPhysicianName(String physicianId, TextView physicianTextView) {
        databaseReference.child(physicianId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    if (fullName != null) {
                        physicianTextView.setText(fullName);
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

    private void goToEditTaskActivity(String taskId) {
        Log.d("TaskAdapter", "Edit button clicked for task ID: " + taskId);
        Intent intent = new Intent(context, EditTaskActivity.class);
        intent.putExtra("taskId", taskId);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void loadTaskImage(String image, final ImageView imageView) {
        if (image.startsWith("http://") || image.startsWith("https://")) {
            Glide.with(imageView.getContext())
                    .load(image)
                    .override(600, 200)
                    .fitCenter()
                    .into(imageView);
            return;
        }

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(image);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(imageView.getContext())
                    .load(uri.toString())
                    .override(600, 200)
                    .fitCenter()
                    .into(imageView);
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Error fetching download URL", e);
        });
    }
}
