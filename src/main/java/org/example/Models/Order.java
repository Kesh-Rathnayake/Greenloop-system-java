package org.example.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private String orderId;
    private String clientName;
    private BigDecimal amount;
    private String status;
    private String deliveryAddress;
    private Integer deliveryAgentId;
    private LocalDateTime scheduledDateTime;
    private LocalDateTime actualDeliveryDate;
    private String delayReason;

    // constructor
    public Order(String orderId, String clientName, BigDecimal amount, String status, String deliveryAddress, Integer deliveryAgentId,
                 LocalDateTime scheduledDateTime, LocalDateTime actualDeliveryDate, String delayReason) {
        this.orderId = orderId;
        this.clientName = clientName;
        this.amount = amount;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
        this.deliveryAgentId = deliveryAgentId;
        this.scheduledDateTime = scheduledDateTime;
        this.actualDeliveryDate = actualDeliveryDate;
        this.delayReason = delayReason;
    }

    // getters and setters
    public String getOrderId() {
        return orderId;
    }

    public String getClientName() {
        return clientName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public Integer getDeliveryAgentId() {
        return deliveryAgentId;
    }

    public void setDeliveryAgentId(Integer id) {
        this.deliveryAgentId = id;
    }

    public LocalDateTime getScheduledDateTime() {
        return scheduledDateTime;
    }

    public void setScheduledDateTime(LocalDateTime dt) {
        this.scheduledDateTime = dt;
    }
}