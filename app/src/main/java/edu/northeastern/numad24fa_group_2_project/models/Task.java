package edu.northeastern.numad24fa_group_2_project.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private List<String> taskImagesUrls = new ArrayList<>();
    private String patientAssigned;
    private String physicianAssigned;
    private transient LocalDate startDate;
    private transient LocalDate endDate;
    private transient LocalDateTime createdOn = LocalDateTime.now();
    private String taskImageUrl;

    public Task() {}

    public Task(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Task(String taskId, String taskTitle, String taskDescription, List<String> taskImages,
                String patientAssignedId, String physicianAssignedId, LocalDate startDate,
                LocalDate endDate, String taskImageUrl) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskImagesUrls = taskImages != null ? taskImages : new ArrayList<>();
        this.patientAssigned = patientAssignedId;
        this.physicianAssigned = physicianAssignedId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskImageUrl = taskImageUrl;
    }

    // constructor without taskImageUrl
    public Task(String taskTitle, String taskDescription, List<String> taskImages,
                String patientAssignedId, String physicianAssignedId,
                LocalDate startDate, LocalDate endDate) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskImagesUrls = taskImages != null ? taskImages : new ArrayList<>();
        this.patientAssigned = patientAssignedId;
        this.physicianAssigned = physicianAssignedId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public List<String> getTaskImagesUrls() {
        return taskImagesUrls;
    }

    public void setTaskImagesUrls(List<String> taskImagesUrls) {
        this.taskImagesUrls = taskImagesUrls;
    }

    public void addTaskImageUrl(String imageUrl) {
        this.taskImagesUrls.add(imageUrl);
    }

    public String getTaskImageUrl() {
        return taskImageUrl;
    }

    public void setTaskImageUrl(String taskImageUrl) {
        this.taskImageUrl = taskImageUrl;
    }

    public String getPatientAssignedId() {
        return patientAssigned;
    }

    public void setPatientAssignedId(String patientAssigned) {
        this.patientAssigned = patientAssigned;
    }

    public String getPhysicianAssignedId() {
        return physicianAssigned;
    }

    public void setPhysicianAssignedId(String physicianAssigned) {
        this.physicianAssigned = physicianAssigned;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

}

