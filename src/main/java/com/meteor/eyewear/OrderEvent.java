package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.Instant;

@Entity
@Table(name = "order_events")
public class OrderEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long value) {
        this.id = value;
    }

    @Column(nullable = false)
    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long value) {
        this.orderId = value;
    }

    @Column(nullable = false, length = 30)
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
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
    private Instant createdAt = Instant.now();

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        this.createdAt = value;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "orderId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_orderevent_order"))
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "staffId",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_orderevent_staff"))
    private Account staff;
}
