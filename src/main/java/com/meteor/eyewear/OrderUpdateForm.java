package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

public class OrderUpdateForm {
    @NotBlank
    @Size(max = 30)
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    @NotBlank
    @Size(max = 500)
    private String shippingAddress;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String value) {
        this.shippingAddress = value;
    }

    @NotNull private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long value) {
        this.version = value;
    }
}
