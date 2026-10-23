package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

@org.hibernate.annotations.Check(
        constraints =
                "quantity > 0 and unit_price > 0 and total > 0 and delivery in"
                        + " ('OUTLET','CUSTOMER')")
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
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

    @Column(nullable = false)
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long value) {
        this.customerId = value;
    }

    @Column(nullable = false)
    private Long staffId;

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long value) {
        this.staffId = value;
    }

    @Column(nullable = false)
    private Long frameId;

    public Long getFrameId() {
        return frameId;
    }

    public void setFrameId(Long value) {
        this.frameId = value;
    }

    @Column(nullable = false)
    private Long lensId;

    public Long getLensId() {
        return lensId;
    }

    public void setLensId(Long value) {
        this.lensId = value;
    }

    @Column(nullable = false, length = 100)
    private String frameName;

    public String getFrameName() {
        return frameName;
    }

    public void setFrameName(String value) {
        this.frameName = value;
    }

    @Column(nullable = false, length = 100)
    private String lensName;

    public String getLensName() {
        return lensName;
    }

    public void setLensName(String value) {
        this.lensName = value;
    }

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal value) {
        this.unitPrice = value;
    }

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal value) {
        this.total = value;
    }

    @Column(nullable = false)
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int value) {
        this.quantity = value;
    }

    @Column(nullable = false, length = 12)
    private String delivery;

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String value) {
        this.delivery = value;
    }

    @Column(length = 60)
    private String outlet;

    public String getOutlet() {
        return outlet;
    }

    public void setOutlet(String value) {
        this.outlet = value;
    }

    @Column(nullable = false, length = 500)
    private String shippingAddress;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String value) {
        this.shippingAddress = value;
    }

    @Column(nullable = false, length = 30)
    private String status = "Pending";

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        this.createdAt = value;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customerId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_customerorder_customer"))
    private Account customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "staffId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_customerorder_staff"))
    private Account staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "frameId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_customerorder_frame"))
    private Product frame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "lensId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_customerorder_lens"))
    private Product lens;
}
