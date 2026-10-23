package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

public class AccountForm {
    @NotBlank
    @Size(max = 100)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    @NotBlank
    @Pattern(regexp = "[0-9+() -]{8,20}", message = "Enter a valid phone number (8–20 characters).")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    @NotBlank
    @Pattern(regexp = "[0-9]{12}", message = "Enter a 12-digit IC number.")
    private String ic;

    public String getIc() {
        return ic;
    }

    public void setIc(String value) {
        this.ic = value;
    }

    @NotBlank
    @Size(max = 500)
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        this.address = value;
    }

    @Size(max = 60)
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String value) {
        this.nickname = value;
    }

    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long value) {
        this.version = value;
    }
}
