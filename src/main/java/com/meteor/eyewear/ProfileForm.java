package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProfileForm {
    @NotNull
    @DecimalMin("-30.00")
    @DecimalMax("30.00")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal leftPower;

    public BigDecimal getLeftPower() {
        return leftPower;
    }

    public void setLeftPower(BigDecimal value) {
        this.leftPower = value;
    }

    @NotNull
    @DecimalMin("-30.00")
    @DecimalMax("30.00")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal rightPower;

    public BigDecimal getRightPower() {
        return rightPower;
    }

    public void setRightPower(BigDecimal value) {
        this.rightPower = value;
    }

    @Size(max = 40)
    private String prescription;

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String value) {
        this.prescription = value;
    }

    @Size(max = 3)
    private String astigmatism;

    public String getAstigmatism() {
        return astigmatism;
    }

    public void setAstigmatism(String value) {
        this.astigmatism = value;
    }

    @Size(max = 500)
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String value) {
        this.notes = value;
    }

    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long value) {
        this.version = value;
    }
}
