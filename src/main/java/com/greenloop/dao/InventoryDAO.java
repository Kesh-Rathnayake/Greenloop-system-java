package com.greenloop.dao;

import com.greenloop.database.DatabaseConnection;
import com.greenloop.models.Inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public void addInventory(Inventory inventory) throws SQLException {
        String sql = "INSERT INTO inventory (product_id, quantity, reorder_level) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventory.getProductId());
            stmt.setInt(2, inventory.getQuantity());
            stmt.setInt(3, inventory.getReorderLevel());
            stmt.executeUpdate();
        }
    }

    public void updateInventory(Inventory inventory) throws SQLException {
        String sql = "UPDATE inventory SET product_id=?, quantity=?, reorder_level=? WHERE inventory_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventory.getProductId());
            stmt.setInt(2, inventory.getQuantity());
            stmt.setInt(3, inventory.getReorderLevel());
            stmt.setInt(4, inventory.getInventoryId());
            stmt.executeUpdate();
        }
    }

    public void deleteInventory(int inventoryId) throws SQLException {
        String sql = "DELETE FROM inventory WHERE inventory_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventoryId);
            stmt.executeUpdate();
        }
    }

    public List<Inventory> getAllInventory() throws SQLException {
        List<Inventory> inventoryList = new ArrayList<>();
        String sql = "SELECT i.*, p.name as product_name FROM inventory i JOIN products p ON i.product_id = p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Inventory inventory = new Inventory(
                        rs.getInt("inventory_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getInt("reorder_level")
                );
                inventoryList.add(inventory);
            }
        }
        return inventoryList;
    }

    public List<Inventory> getLowStockItems() throws SQLException {
        List<Inventory> lowStock = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity <= reorder_level";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lowStock.add(new Inventory(
                        rs.getInt("inventory_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getInt("reorder_level")
                ));
            }
        }
        return lowStock;
    }
}