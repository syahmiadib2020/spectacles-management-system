package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductForm {
    @NotBlank
    @Pattern(regexp = "FRAME|LENS")
    private String type = "FRAME";

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = "[\\p{L}\\p{N} .'-]+",
            message = "Use letters, numbers, spaces, apostrophes, dots or hyphens.")
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    @NotBlank
    @Size(max = 80)
    @Pattern(
            regexp = "[\\p{L}\\p{N} .'-]+",
            message = "Use letters, numbers, spaces, apostrophes, dots or hyphens.")
    private String brand;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String value) {
        this.brand = value;
    }

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("999999.99")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal value) {
        this.price = value;
    }

    @NotNull
    @Min(0)
    @Max(100000)
    private Integer quantity = 0;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer value) {
        this.quantity = value;
    }

    @Size(max = 60)
    private String frameShape;

    public String getFrameShape() {
        return frameShape;
    }

    public void setFrameShape(String value) {
        this.frameShape = value;
    }

    @Size(max = 60)
    private String frameColour;

    public String getFrameColour() {
        return frameColour;
    }

    public void setFrameColour(String value) {
        this.frameColour = value;
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

    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long value) {
        this.version = value;
    }
}
