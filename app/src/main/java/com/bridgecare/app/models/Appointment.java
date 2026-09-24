package com.bridgecare.app.models;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class Appointment {
    private String appointmentId;
    private Long appointmentDateAndTime;
    private String title;
    private String notes;
    private String physicianAssignedId;
    private String patientAssignedId;
    private Long createdOn;

    public Appointment() {}

    public Appointment(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Appointment(String appointmentId, Long appointmentDateAndTime, String title,
                       String notes, String physicianAssignedId, String patientAssignedId) {
        this.title = title;
        this.appointmentDateAndTime = appointmentDateAndTime;
        this.appointmentId = appointmentId;
        this.notes = notes;
        this.physicianAssignedId = physicianAssignedId;
        this.patientAssignedId = patientAssignedId;
        this.createdOn = System.currentTimeMillis();
    }

    public Appointment(Long appointmentDateAndTime, String title, String notes,
                       String physicianAssignedId, String patientAssignedId) {
        this.appointmentDateAndTime = appointmentDateAndTime;
        this.title = title;
        this.notes = notes;
        this.physicianAssignedId = physicianAssignedId;
        this.patientAssignedId = patientAssignedId;
        this.createdOn = System.currentTimeMillis();
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long appointmentDateAndTime() {
        return appointmentDateAndTime;
    }

    public void setAppointmentDateAndTime(Long appointmentDateAndTime) {
        this.appointmentDateAndTime = appointmentDateAndTime;
    }

    public Long getAppointmentDateAndTime() {
        return appointmentDateAndTime;
    }

    public String getAppointmentDateFormatted() {
        if (appointmentDateAndTime == null) {
            return "No Date";
        }
        LocalDate appointmentDate = Instant.ofEpochMilli(appointmentDateAndTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return appointmentDate.format(formatter);
    }

    public String getAppointmentTimeFormatted() {
        if (appointmentDateAndTime == null) {
            return "No Time";
        }
        LocalTime appointmentTime = Instant.ofEpochMilli(appointmentDateAndTime)
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return appointmentTime.format(formatter);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhysicianAssignedId() {
        return physicianAssignedId;
    }

    public void setPhysicianAssignedId(String physicianAssignedId) {
        this.physicianAssignedId = physicianAssignedId;
    }

    public String getPatientAssignedId() {
        return patientAssignedId;
    }

    public void setPatientAssignedId(String patientAssignedId) {
        this.patientAssignedId = patientAssignedId;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }
}