package com.greenloop.service;

import com.greenloop.dao.OrderDAO;
import com.greenloop.model.EmailNotification;
import com.greenloop.model.Order;

public class NotificationService {
    private final EmailService emailService;
    private final OrderDAO orderDAO;

    public NotificationService() {
        this.emailService = new EmailService();
        this.orderDAO = new OrderDAO();
    }

    public void sendDispatchEmail(Order order) {
        String subject = "GreenLoop Order Dispatched - " + order.getOrderCode();

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
                order.getClient().getClientName(),
                order.getOrderCode(),
                order.getAddress(),
                order.getAmount(),
                order.getStatus()
        );

        try {
            emailService.sendEmail(order.getClient().getEmail(), subject, message);

            EmailNotification notification = new EmailNotification(
                    order.getOrderId(),
                    order.getClient().getClientId(),
                    order.getClient().getEmail(),
                    subject,
                    message,
                    "SENT"
            );

            orderDAO.saveEmailNotification(notification);

        } catch (RuntimeException e) {
            EmailNotification notification = new EmailNotification(
                    order.getOrderId(),
                    order.getClient().getClientId(),
                    order.getClient().getEmail(),
                    subject,
                    message,
                    "FAILED"
            );

            orderDAO.saveEmailNotification(notification);
            throw e;
        }
    }
}
