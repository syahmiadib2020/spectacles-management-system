package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.Instant;

@org.hibernate.annotations.Check(constraints = "role in ('STAFF','CUSTOMER')")
@Entity
@Table(name = "accounts")
public class Account {
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

    @Column(nullable = false, length = 100)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    @Column(nullable = false)
    private String passwordHash;

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String value) {
        this.passwordHash = value;
    }

    @Column(nullable = false, length = 20)
    private String role = "CUSTOMER";

    public String getRole() {
        return role;
    }

    public void setRole(String value) {
        this.role = value;
    }

    @Column(length = 20)
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }

    @Column(unique = true, length = 12)
    private String ic;

    public String getIc() {
        return ic;
    }

    public void setIc(String value) {
        this.ic = value;
    }

    @Column(length = 500)
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        this.address = value;
    }

    @Column(length = 60)
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String value) {
        this.nickname = value;
    }

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        this.createdAt = value;
    }
}
