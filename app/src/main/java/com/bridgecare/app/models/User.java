package com.bridgecare.app.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dob;
    private String email;
    private String role;
    private LocalDateTime createdOn;

    public User() {}

    public User(String firstName, String lastName, LocalDate dob, String email, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.dob = dob;
        this.email = email;
        this.role = role;
        this.createdOn = LocalDateTime.now();
    }
    public User(String userId, String firstName, String lastName, LocalDate dob, String email, String role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.dob = dob;
        this.email = email;
        this.role = role;
        this.createdOn = LocalDateTime.now();
    }

    public String getId() {
        return userId;
    }

    public void setId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getFullName() {
        return fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Checks if the user is a patient.
     * @return true if the user role is a patient, false otherwise
     */
    public boolean isPatient() {
        return role.equals("Patient");
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }
}

