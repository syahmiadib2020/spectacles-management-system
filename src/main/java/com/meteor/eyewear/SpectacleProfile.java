package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "spectacle_profiles")
public class SpectacleProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        this.id = value;
    }

    @Version private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long value) {
        this.version = value;
    }

    @Column(nullable = false, unique = true)
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long value) {
        this.customerId = value;
    }

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal leftPower;

    public BigDecimal getLeftPower() {
        return leftPower;
    }

    public void setLeftPower(BigDecimal value) {
        this.leftPower = value;
    }

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rightPower;

    public BigDecimal getRightPower() {
        return rightPower;
    }

    public void setRightPower(BigDecimal value) {
        this.rightPower = value;
    }

    @Column(length = 40)
    private String prescription;

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String value) {
        this.prescription = value;
    }

    @Column(length = 3)
    private String astigmatism;

    public String getAstigmatism() {
        return astigmatism;
    }

    public void setAstigmatism(String value) {
        this.astigmatism = value;
    }

    @Column(length = 500)
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String value) {
        this.notes = value;
    }

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant value) {
        this.updatedAt = value;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customerId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_spectacleprofile_customer"))
    private Account customer;
}
