package com.greenloop.service;

import com.greenloop.dao.OrderDAO;
import com.greenloop.models.EmailNotification;
import com.greenloop.models.Order;

public class NotificationService {
    private final EmailService emailService;
    private final OrderDAO orderDAO;

    public NotificationService() {
        this.emailService = new EmailService();
        this.orderDAO = new OrderDAO();
    }

    public void sendDispatchEmail(Order order) {
        String subject = "GreenLoop Order Dispatched - " + order.getDisplayOrderId();

        String message = """
                Dear %s,

                Good news! Your GreenLoop order has been dispatched.

                Order ID: %s
                Delivery Address: %s
                Total Amount: Rs %.2f
                Current Status: %s

                Thank you for choosing GreenLoop Eco Packaging.

                Best regards,
                GreenLoop Team
                """.formatted(
                order.getClientName(),
                order.getDisplayOrderId(),
                order.getDeliveryAddress(),
                order.getAmount(),
                order.getStatus()
        );

        try {
            emailService.sendEmail(order.getClientEmail(), subject, message);

            EmailNotification notification = new EmailNotification(
                    order.getOrderId(),
                    order.getClientId(),
                    order.getClientEmail(),
                    subject,
                    message,
                    "SENT"
            );

            orderDAO.saveEmailNotification(notification);

        } catch (RuntimeException e) {
            EmailNotification notification = new EmailNotification(
                    order.getOrderId(),
                    order.getClientId(),
                    order.getClientEmail(),
                    subject,
                    message,
                    "FAILED"
            );

            orderDAO.saveEmailNotification(notification);
            throw e;
        }
    }
}