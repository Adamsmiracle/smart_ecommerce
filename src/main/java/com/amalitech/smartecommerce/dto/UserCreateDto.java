package com.amalitech.smartecommerce.dto;

import com.amalitech.smartecommerce.constants.ValidationMessages;
import com.amalitech.smartecommerce.utils.InputValidator;
import com.amalitech.smartecommerce.utils.ValidationUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.intellij.lang.annotations.RegExp;

/**
 * DTO for creating a new user.
 * All required fields must be provided including password.
 */
public class UserCreateDto {

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
    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]{6,15}$",
            message = "Please enter a valid phone number (e.g., +1234567890, 123-456-7890)")
    private String phoneNumber;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    @Size(min = 8, message = ValidationMessages.PASSWORD_SIZE)
    private String password;

    public UserCreateDto() {}

    public UserCreateDto(String emailAddress, String firstName, String lastName, String phoneNumber, String password) {
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // Getters and Setters
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
