package com.bridgecare.app.models;

import java.time.LocalDate;

public class Physician extends User {

    /**
     * Constructor for Physician.
     * @param firstName first name of the physician
     * @param lastName last name of the physician
     * @param dob date of birth of the physician
     * @param email email of the physician
     */
    public Physician(String firstName, String lastName, LocalDate dob, String email) {
        super(firstName, lastName, dob, email, "Physician");
    }

    /**
     * Constructor for Physician.
     * @param physicianId id of the physician
     * @param firstName first name of the physician
     * @param lastName last name of the physician
     * @param dob date of birth of the physician
     * @param email email of the physician
     */
    public Physician(String physicianId, String firstName, String lastName, LocalDate dob, String email) {
        super(physicianId, firstName, lastName, dob, email, "Physician");
    }
}
