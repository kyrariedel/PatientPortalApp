package com.bridgecare.app.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient extends User {

    List<Appointment> appointments;
    List<Task> tasks;

    public Patient() {
        super();
    }

    /**
     * Constructor for Patient,
     * @param firstName first name of the patient
     * @param lastName last name of the patient
     * @param dob date of birth of the patient
     * @param email email of the patient
     */
    public Patient(String firstName, String lastName, LocalDate dob, String email) {
        super(firstName, lastName, dob, email, "Patient");
        this.appointments = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    /**
     * Constructor for Patient,
     * @param patientId id of the patient
     * @param firstName first name of the patient
     * @param lastName last name of the patient
     * @param dob date of birth of the patient
     * @param email email of the patient
     */
    public Patient(String patientId, String firstName, String lastName, LocalDate dob, String email) {
        super(patientId, firstName, lastName, dob, email, "Patient");
        this.appointments = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    // Add an appointment to the list of appointments... I don't think this will work,
    // you would need to use a repository to add an appointment to the database.
    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    // Add a Task to the list of Tasks... I don't think this will work,
    // you would need to use a repository to add a Task to the database.
    public void addTask(Task task) {
        this.tasks.add(task);
    }
}
