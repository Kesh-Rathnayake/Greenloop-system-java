package com.greenloop.dao;

import com.greenloop.database.DatabaseConnection;
import com.greenloop.models.DeliveryAgent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryAgentDAO {

    public List<DeliveryAgent> getAllAgents() throws SQLException {
        List<DeliveryAgent> agents = new ArrayList<>();
        String sql = "SELECT * FROM delivery_agents";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                DeliveryAgent agent = new DeliveryAgent(
                        rs.getInt("agent_id"),
                        rs.getString("name"),
                        rs.getString("vehicle_type"),
                        rs.getString("service_area"),
                        rs.getString("phone"),
                        rs.getString("email")
                );
                agents.add(agent);
            }
        }
        return agents;
    }

    public void addAgent(DeliveryAgent agent) throws SQLException {
        String sql = "INSERT INTO delivery_agents (name, vehicle_type, service_area, phone, email) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, agent.getName());
            stmt.setString(2, agent.getVehicleType());
            stmt.setString(3, agent.getServiceArea());
            stmt.setString(4, agent.getPhone());
            stmt.setString(5, agent.getEmail());
            stmt.executeUpdate();
        }
    }

    public void updateAgent(DeliveryAgent agent) throws SQLException {
        String sql = "UPDATE delivery_agents SET name=?, vehicle_type=?, service_area=?, phone=?, email=? WHERE agent_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, agent.getName());
            stmt.setString(2, agent.getVehicleType());
            stmt.setString(3, agent.getServiceArea());
            stmt.setString(4, agent.getPhone());
            stmt.setString(5, agent.getEmail());
            stmt.setInt(6, agent.getAgentId());
            stmt.executeUpdate();
        }
    }

    public void deleteAgent(int agentId) throws SQLException {
        String sql = "DELETE FROM delivery_agents WHERE agent_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, agentId);
            stmt.executeUpdate();
        }
    }

    public DeliveryAgent getAgentById(int agentId) throws SQLException {
        String sql = "SELECT * FROM delivery_agents WHERE agent_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, agentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new DeliveryAgent(
                        rs.getInt("agent_id"),
                        rs.getString("name"),
                        rs.getString("vehicle_type"),
                        rs.getString("service_area"),
                        rs.getString("phone"),
                        rs.getString("email")
                );
            }
        }
        return null;
    }
}