package com.greenloop.dao;

import com.greenloop.db.DBConnection;
import com.greenloop.model.Client;
import com.greenloop.model.EmailNotification;
import com.greenloop.model.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getDispatchedOrdersWithoutEmail() {
        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT o.order_id, o.order_code, o.amount, o.status, o.address,
                       c.client_id, c.client_name, c.email, c.phone
                FROM orders o
                INNER JOIN clients c ON o.client_id = c.client_id
                WHERE o.status = 'Dispatched'
                AND o.order_id NOT IN (
                    SELECT order_id FROM email_notifications WHERE sent_status = 'SENT'
                )
                ORDER BY o.order_id DESC
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getString("email"),
                        rs.getString("phone")
                );

                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getString("order_code"),
                        client,
                        rs.getDouble("amount"),
                        rs.getString("status"),
                        rs.getString("address")
                );

                orders.add(order);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading dispatched orders: " + e.getMessage());
        }

        return orders;
    }

    public void saveEmailNotification(EmailNotification notification) {
        String sql = """
                INSERT INTO email_notifications
                (order_id, client_id, recipient_email, subject, message, sent_status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, notification.getOrderId());
            statement.setInt(2, notification.getClientId());
            statement.setString(3, notification.getRecipientEmail());
            statement.setString(4, notification.getSubject());
            statement.setString(5, notification.getMessage());
            statement.setString(6, notification.getSentStatus());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving email notification: " + e.getMessage());
        }
    }
}