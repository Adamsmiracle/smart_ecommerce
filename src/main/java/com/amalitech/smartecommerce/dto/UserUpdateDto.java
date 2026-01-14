package com.amalitech.smartecommerce.dto;

import com.amalitech.smartecommerce.constants.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing user.
 * Password is optional - only set if user wants to change it.
 */
public class UserUpdateDto {

    @NotNull(message = ValidationMessages.USER_ID_REQUIRED)
    private UUID id;

    @NotBlank(message = ValidationMessages.EMAIL_REQUIRED)
    @Email(message = ValidationMessages.EMAIL_INVALID)
    private String emailAddress;

    @NotBlank(message = ValidationMessages.FIRST_NAME_REQUIRED)
    @Size(min = 1, max = 100, message = ValidationMessages.FIRST_NAME_SIZE)
    private String firstName;

    @NotBlank(message = ValidationMessages.LAST_NAME_REQUIRED)
    @Size(min = 1, max = 100, message = ValidationMessages.LAST_NAME_SIZE)
    private String lastName;

    @Size(max = 20, message = ValidationMessages.PHONE_SIZE)
    private String phoneNumber;

    @Size(min = 8, message = ValidationMessages.PASSWORD_SIZE)
    private String password;

    public UserUpdateDto() {}

    public UserUpdateDto(UUID id, String emailAddress, String firstName, String lastName, String phoneNumber, String password) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // Getters and Setters
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

    /**
     * Check if password was provided for update.
     */
    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }
}
