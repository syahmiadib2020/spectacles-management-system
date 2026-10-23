package com.meteor.eyewear;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

public class OrderForm {
    @NotNull private Long frameId;

    public Long getFrameId() {
        return frameId;
    }

    public void setFrameId(Long value) {
        this.frameId = value;
    }

    @NotNull private Long lensId;

    public Long getLensId() {
        return lensId;
    }

    public void setLensId(Long value) {
        this.lensId = value;
    }

    @NotNull
    @Min(1)
    @Max(100)
    private Integer quantity = 1;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer value) {
        this.quantity = value;
    }

    @NotBlank
    @Pattern(regexp = "OUTLET|CUSTOMER")
    private String delivery;

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String value) {
        this.delivery = value;
    }

    @Size(max = 60)
    private String outlet;

    public String getOutlet() {
        return outlet;
    }

    public void setOutlet(String value) {
        this.outlet = value;
    }

    @Size(max = 500)
    private String shippingAddress;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String value) {
        this.shippingAddress = value;
    }
}
