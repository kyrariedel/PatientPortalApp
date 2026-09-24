package com.bridgecare.app;

import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    private TextView titleTextView;
    private TextView physicianTextView;
    private TextView startDateTextView;
    private TextView endDateTextView;
    private Button editButton;
    private Button deleteButton;
    private Button detailsButton;
    private ImageView taskImageView;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.taskTitle);
        physicianTextView = itemView.findViewById(R.id.taskPhysician);
        startDateTextView = itemView.findViewById(R.id.startDate);
        endDateTextView = itemView.findViewById(R.id.endDate);
        taskImageView = itemView.findViewById(R.id.taskImage);
        editButton = itemView.findViewById(R.id.edit);

        titleTextView.setTypeface(null, Typeface.BOLD);
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public void setTitleTextView(TextView titleTextView) {
        this.titleTextView = titleTextView;
    }

    public TextView getPhysicianTextView() {
        return physicianTextView;
    }

    public void setPhysicianTextView(TextView assignedTextView) {
        this.physicianTextView = assignedTextView;
    }

    public TextView getStartDateTextView() {
        return startDateTextView;
    }

    public void setStartDateTextView(TextView startDateTextView) {
        this.startDateTextView = startDateTextView;
    }

    public TextView getEndDateTextView() {
        return endDateTextView;
    }

    public void setEndDateTextView(TextView endDateTextView) {
        this.endDateTextView = endDateTextView;
    }

    public Button getEditButton() {
        return editButton;
    }

    public void setEditButton(Button editButton) {
        this.editButton = editButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    public Button getDetailsButton() {
        return detailsButton;
    }

    public void setDetailsButton(Button detailsButton) {
        this.detailsButton = detailsButton;
    }
    
    public ImageView getTaskImageView() {
        return taskImageView;
    }

    public void setTaskImageView(ImageView taskImageView) {
        this.taskImageView = taskImageView;
    }
}