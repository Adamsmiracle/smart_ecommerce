package com.amalitech.smartecommerce.model;

import java.util.UUID;

public class User {
    private UUID id;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;

    public User() {}

    public User(UUID id, String emailAddress, String firstName, String lastName, String phoneNumber, String password) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
