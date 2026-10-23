package com.meteor.eyewear;

import jakarta.validation.constraints.*;

public class RegistrationForm extends AccountForm {
    @NotBlank
    @Size(min = 10, max = 72, message = "Use a password between 10 and 72 characters.")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        password = value;
    }
}
