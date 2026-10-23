package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@org.hibernate.annotations.Check(
        constraints = "quantity >= 0 and price > 0 and type in ('FRAME','LENS')")
@Entity
@Table(name = "products")
public class Product {
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

    @Column(nullable = false, length = 10)
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    @Column(nullable = false, length = 100)
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    @Column(nullable = false, length = 80)
    private String brand;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String value) {
        this.brand = value;
    }

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal value) {
        this.price = value;
    }

    @Column(nullable = false)
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int value) {
        this.quantity = value;
    }

    @Column(length = 60)
    private String frameShape;

    public String getFrameShape() {
        return frameShape;
    }

    public void setFrameShape(String value) {
        this.frameShape = value;
    }

    @Column(length = 60)
    private String frameColour;

    public String getFrameColour() {
        return frameColour;
    }

    public void setFrameColour(String value) {
        this.frameColour = value;
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

    @Column(nullable = false)
    private boolean active = true;

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean value) {
        this.active = value;
    }
}
