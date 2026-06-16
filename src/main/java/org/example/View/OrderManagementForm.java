package org.example.View;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class OrderManagementForm extends JFrame {
    private JPanel mainPanel;
    private JComboBox<String> comboBox2;          // client selection
    private JComboBox<String> comboBox1;         // product selection
    private JTextField txtPrice;
    private JTextField txtQuantity;
    private JButton btnAddItem;
    private JTable tblOrderItems;
    private JButton btnPlaceOrder;
    private JLabel lblTotal;
    private JLabel lblTotalAmount;

    private DefaultTableModel tableModel;
    private double grandTotal = 0.0;

    // Maps for products
    private Map<String, Integer> productIdMap = new HashMap<>();
    private Map<String, Double> productPriceMap = new HashMap<>();

    // Maps for clients
    private Map<String, Integer> clientIdMap = new HashMap<>();

    public OrderManagementForm() {
        setContentPane(mainPanel);
        setTitle("GreenLoop - Process Client Orders");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Table model
        tableModel = new DefaultTableModel(new String[]{"Product", "Price", "Qty", "Sub Total"}, 0);
        tblOrderItems.setModel(tableModel);

        // Load data into combo boxes
        loadClients();
        loadProducts();

        // When a product is selected, auto fill price
        comboBox1.addActionListener(e -> {
            String selected = (String) comboBox1.getSelectedItem();
            if (selected != null && productPriceMap.containsKey(selected)) {
                txtPrice.setText(String.valueOf(productPriceMap.get(selected)));
            } else {
                txtPrice.setText("");
            }
        });

        // Add Item button
        btnAddItem.addActionListener(e -> {
            try {
                String selectedProduct = (String) comboBox1.getSelectedItem();
                if (selectedProduct == null) {
                    JOptionPane.showMessageDialog(this, "Please select a product");
                    return;
                }

                double price = Double.parseDouble(txtPrice.getText().trim());
                int qty = Integer.parseInt(txtQuantity.getText().trim());

                // Check inventory
                int availableQty = getAvailableQuantity(selectedProduct);
                if (availableQty == -1) {
                    JOptionPane.showMessageDialog(this, "Product not found in inventory!");
                    return;
                }
                if (qty > availableQty) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + availableQty);
                    return;
                }

                // Add to table
                double subTotal = price * qty;
                grandTotal += subTotal;
                tableModel.addRow(new Object[]{selectedProduct, price, qty, subTotal});
                lblTotalAmount.setText(String.valueOf(grandTotal));

                // Clear quantity field
                txtQuantity.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for Price and Quantity!");
            }
        });

        // Place Order button
        btnPlaceOrder.addActionListener(e -> {
            if (tableModel.getRowCount() == 0 || comboBox2.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a client and add at least one item!");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                // Get selected client name and ID
                String clientName = (String) comboBox2.getSelectedItem();
                Integer clientId = clientIdMap.get(clientName);

                if (clientId == null) {
                    JOptionPane.showMessageDialog(this, "Invalid client selected");
                    return;
                }

                String orderSql = "INSERT INTO orders (client_id, amount, status, created_at) VALUES (?, ?, 'Pending', NOW())";
                try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    orderStmt.setInt(1, clientId);
                    orderStmt.setDouble(2, grandTotal);
                    orderStmt.executeUpdate();

                    ResultSet rs = orderStmt.getGeneratedKeys();
                    int newOrderId = 0;
                    if (rs.next()) {
                        newOrderId = rs.getInt(1);
                    }

                    // Insert order details and update inventory
                    String detailSql = "INSERT INTO order_details (order_id, product_id, quantity, sub_total) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement detailStmt = conn.prepareStatement(detailSql)) {
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            String productName = tableModel.getValueAt(i, 0).toString();
                            int qty = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                            double subTotal = Double.parseDouble(tableModel.getValueAt(i, 3).toString());

                            detailStmt.setInt(1, newOrderId);
                            Integer productId = productIdMap.get(productName);
                            detailStmt.setInt(2, productId);
                            detailStmt.setInt(3, qty);
                            detailStmt.setDouble(4, subTotal);
                            detailStmt.executeUpdate();

                            // Update inventory
                            updateInventory(conn, productName, qty);
                        }
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Order Placed Successfully! Order ID: " + newOrderId);

                    // Reset form
                    comboBox2.setSelectedIndex(0);
                    comboBox1.setSelectedIndex(0);
                    txtPrice.setText("");
                    txtQuantity.setText("");
                    tableModel.setRowCount(0);
                    grandTotal = 0.0;
                    lblTotalAmount.setText("0.0");

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        });
    }

    // Load clients into combo box
    private void loadClients() {
        String sql = "SELECT client_id, name FROM clients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("client_id");
                String name = rs.getString("name");
                clientIdMap.put(name, id);
                comboBox2.addItem(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load clients: " + e.getMessage());
        }
    }

    // Load products into combo box and store ID & price maps
    private void loadProducts() {
        String sql = "SELECT product_id, name, price FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                productIdMap.put(name, id);
                productPriceMap.put(name, price);
                comboBox1.addItem(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load products: " + e.getMessage());
        }
    }

    // Get available quantity from inventory using product name
    private int getAvailableQuantity(String productName) {
        String sql = "SELECT i.quantity FROM inventory i " +
                "JOIN products p ON i.product_id = p.product_id " +
                "WHERE p.name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Update inventory after order
    private void updateInventory(Connection conn, String productName, int orderedQty) throws SQLException {
        Integer productId = productIdMap.get(productName);
        if (productId == null) return;
        String updateSql = "UPDATE inventory SET quantity = quantity - ? WHERE product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, orderedQty);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(() -> {
            OrderManagementForm form = new OrderManagementForm();
            form.setLocationRelativeTo(null);
            form.setVisible(true);
        });
    }
}