package org.example.Controller;

import org.example.Models.Agent;
import org.example.Models.Order;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeliveryController {
    private Connection connection;

    public DeliveryController(Connection connection) {
        this.connection = connection;
    }

    // load all delivery agents
    public List<Agent> getAllAgents() throws SQLException {
        List<Agent> agents = new ArrayList<>();
        String sql = "SELECT a.agent_id, a.name, a.vehicle_type, a.service_area, a.phone, " + "COUNT(o.order_id) AS current_orders " +
                "FROM delivery_agents a " + "LEFT JOIN orders o ON a.agent_id = o.delivery_agent_id " +
                "AND o.status IN ('Assigned', 'Dispatched') " + "GROUP BY a.agent_id, a.name, a.vehicle_type, a.service_area, a.phone";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                agents.add(new Agent(
                        rs.getInt("agent_id"),
                        rs.getString("name"),
                        rs.getString("vehicle_type"),
                        rs.getString("service_area"),
                        rs.getString("phone"),
                        rs.getInt("current_orders")
                ));
            }
        }
        return agents;
    }

    // load pending orders
    public List<Order> getPendingOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();

        // FIX: Replaced the direct column selection with a LEFT JOIN to the clients table
        String sql = "SELECT o.order_id, c.name AS client_name, o.amount, o.status, o.delivery_address, " +
                "o.delivery_agent_id, o.scheduled_datetime, o.actual_delivery_date, o.delay_reason " +
                "FROM orders o " +
                "LEFT JOIN clients c ON o.client_id = c.client_id " +
                "WHERE o.status = 'Pending'";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        }
        return orders;
    }

    // assign order
    public boolean assignOrder(String orderId, int agentId, LocalDateTime scheduledDateTime) throws SQLException {
        String sql = "UPDATE orders SET delivery_agent_id = ?, scheduled_datetime = ?, status = 'Assigned' " +
                "WHERE order_id = ? AND status = 'Pending'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, agentId);
            pstmt.setTimestamp(2, Timestamp.valueOf(scheduledDateTime));
            pstmt.setString(3, orderId);
            int affected = pstmt.executeUpdate();
            return affected == 1;
        }
    }

    // map ResultSet to order
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        String orderId = rs.getString("order_id");
        String clientName = rs.getString("client_name");
        BigDecimal amount = rs.getBigDecimal("amount");
        String status = rs.getString("status");
        String address = rs.getString("delivery_address");
        Integer agentId = rs.getInt("delivery_agent_id");
        if (rs.wasNull()) agentId = null;
        Timestamp scheduledTs = rs.getTimestamp("scheduled_datetime");
        LocalDateTime scheduled = scheduledTs != null ? scheduledTs.toLocalDateTime() : null;
        Timestamp actualTs = rs.getTimestamp("actual_delivery_date");
        LocalDateTime actual = actualTs != null ? actualTs.toLocalDateTime() : null;
        String delayReason = rs.getString("delay_reason");
        return new Order(orderId, clientName, amount, status, address, agentId, scheduled, actual, delayReason);
    }

    public int getPendingOrdersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'Pending'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getDispatchedOrdersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'Dispatched'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getCompletedOrdersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'Delivered'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getDelayedOrdersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE delay_reason IS NOT NULL";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}