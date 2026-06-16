package com.greenloop.dao;

import com.greenloop.database.DatabaseConnection;
import com.greenloop.models.EmailNotification;
import com.greenloop.models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getDispatchedOrdersWithoutEmail() {
        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT 
                    o.order_id,
                    o.client_id,
                    o.amount,
                    o.status,
                    o.delivery_address,
                    c.name,
                    c.email,
                    c.phone
                FROM orders o
                INNER JOIN clients c ON o.client_id = c.client_id
                WHERE LOWER(o.status) = 'dispatched'
                AND o.order_id NOT IN (
                    SELECT order_id FROM email_notifications WHERE sent_status = 'SENT'
                )
                ORDER BY o.order_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("client_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDouble("amount"),
                        rs.getString("status"),
                        rs.getString("delivery_address")
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

        try (Connection connection = DatabaseConnection.getConnection();
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
    public List<String[]> getMonthlyRevenue() {
        List<String[]> data = new ArrayList<>();
        // This query groups sales by month
        String sql = "SELECT DATE_FORMAT(created_at, '%M') as month, COUNT(order_id) as count, SUM(amount) as total " +
                "FROM orders WHERE status = 'Delivered' GROUP BY month";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{ rs.getString("month"), rs.getString("count"), rs.getString("total") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    public List<Order> getOrdersByMonth(int month) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name, c.email, c.phone FROM orders o " +
                "INNER JOIN clients c ON o.client_id = c.client_id " +
                "WHERE MONTH(o.created_at) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, month);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(new Order(rs.getInt("order_id"), rs.getInt("client_id"),
                        rs.getString("name"), rs.getString("email"), rs.getString("phone"),
                        rs.getDouble("amount"), rs.getString("status"), rs.getString("delivery_address")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

}