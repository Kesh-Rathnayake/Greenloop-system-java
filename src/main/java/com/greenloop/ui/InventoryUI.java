package com.greenloop.ui;

import com.greenloop.dao.InventoryDAO;
import com.greenloop.dao.ProductDAO;
import com.greenloop.models.Inventory;
import com.greenloop.models.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class InventoryUI extends JFrame {

    // Theme Constants (Local to this file)
    private static final Color BG_DARK = new Color(18, 20, 18);
    private static final Color BG_CARD = new Color(48, 51, 48);
    private static final Color ACCENT_GREEN = new Color(21, 166, 117);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ProductDAO productDAO = new ProductDAO();
    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> productCombo;
    private List<Product> productList;
    private JTextField quantityField, reorderLevelField;
    private JButton addButton, updateButton, deleteButton, clearButton;

    public InventoryUI() {
        setTitle("GreenLoop - Inventory Management");
        setSize(980, 620); // Matched to collaborator's layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Wrapper
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TOP: Title
        JLabel titleLabel = new JLabel("Inventory Management");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FONT_TITLE);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // 2. CENTER: Table
        String[] columns = {"ID", "Product ID", "Product Name", "Quantity", "Reorder Level", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Styling
        inventoryTable.setBackground(BG_CARD);
        inventoryTable.setForeground(TEXT_PRIMARY);
        inventoryTable.setGridColor(new Color(80, 80, 80));
        inventoryTable.setRowHeight(30);
        inventoryTable.setFont(FONT_NORMAL);
        inventoryTable.setSelectionBackground(new Color(30, 80, 60));
        inventoryTable.setSelectionForeground(TEXT_PRIMARY);

        inventoryTable.getTableHeader().setBackground(new Color(58, 61, 58));
        inventoryTable.getTableHeader().setForeground(TEXT_PRIMARY);
        inventoryTable.getTableHeader().setFont(FONT_BOLD);
        inventoryTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. SOUTH: Form and Buttons
        JPanel southWrapper = new JPanel(new BorderLayout(10, 10));
        southWrapper.setBackground(BG_DARK);

        // Form Panel (2x4 Grid for wider layout)
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 15, 10));
        formPanel.setBackground(BG_DARK);

        TitledBorder formBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 75, 75)),
                "Inventory Details"
        );
        formBorder.setTitleColor(TEXT_MUTED);
        formBorder.setTitleFont(FONT_BOLD);
        formPanel.setBorder(BorderFactory.createCompoundBorder(formBorder, new EmptyBorder(10, 10, 10, 10)));

        productCombo = new JComboBox<>();
        productCombo.setBackground(BG_CARD);
        productCombo.setForeground(TEXT_PRIMARY);
        productCombo.setFont(FONT_NORMAL);
        loadProductCombo();

        quantityField = createStyledTextField();
        reorderLevelField = createStyledTextField();

        // Row 1
        formPanel.add(createStyledLabel("Product:"));
        formPanel.add(productCombo);
        formPanel.add(createStyledLabel("Quantity:"));
        formPanel.add(quantityField);

        // Row 2
        formPanel.add(createStyledLabel("Reorder Level:"));
        formPanel.add(reorderLevelField);
        formPanel.add(new JLabel("")); // Empty placeholder for grid
        formPanel.add(new JLabel("")); // Empty placeholder for grid

        southWrapper.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addButton = createStandardButton("Add Stock");
        updateButton = createStandardButton("Update Stock");
        deleteButton = createStandardButton("Delete");

        clearButton = createStandardButton("Clear");
        clearButton.setBackground(new Color(80, 80, 80));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        southWrapper.add(buttonPanel, BorderLayout.SOUTH);
        contentPanel.add(southWrapper, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Logic (Untouched)
        loadInventory();

        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && inventoryTable.getSelectedRow() != -1) {
                int row = inventoryTable.getSelectedRow();
                int productId = (int) tableModel.getValueAt(row, 1);
                for (int i = 0; i < productList.size(); i++) {
                    if (productList.get(i).getId() == productId) {
                        productCombo.setSelectedIndex(i);
                        break;
                    }
                }
                quantityField.setText(tableModel.getValueAt(row, 3).toString());
                reorderLevelField.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        addButton.addActionListener(e -> {
            try {
                int selectedIndex = productCombo.getSelectedIndex();
                int productId = productList.get(selectedIndex).getId();
                Inventory inventory = new Inventory(
                        0,
                        productId,
                        Integer.parseInt(quantityField.getText()),
                        Integer.parseInt(reorderLevelField.getText())
                );
                inventoryDAO.addInventory(inventory);
                loadInventory();
                clearForm();
                JOptionPane.showMessageDialog(this, "Stock added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an item to update.");
                return;
            }
            try {
                int inventoryId = (int) tableModel.getValueAt(selectedRow, 0);
                int selectedIndex = productCombo.getSelectedIndex();
                int productId = productList.get(selectedIndex).getId();
                Inventory inventory = new Inventory(
                        inventoryId,
                        productId,
                        Integer.parseInt(quantityField.getText()),
                        Integer.parseInt(reorderLevelField.getText())
                );
                inventoryDAO.updateInventory(inventory);
                loadInventory();
                clearForm();
                JOptionPane.showMessageDialog(this, "Stock updated successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an item to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?");
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int inventoryId = (int) tableModel.getValueAt(selectedRow, 0);
                    inventoryDAO.deleteInventory(inventoryId);
                    loadInventory();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Deleted successfully.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        clearButton.addActionListener(e -> clearForm());
    }

    // Helper Methods

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_PRIMARY);
        label.setFont(FONT_NORMAL);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setBackground(BG_CARD);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(Color.WHITE);
        field.setFont(FONT_NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 75, 75)),
                new EmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private JButton createStandardButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(FONT_BOLD);
        return button;
    }

    private void loadProductCombo() {
        try {
            productList = productDAO.getAllProducts();
            productCombo.removeAllItems();
            for (Product p : productList) {
                productCombo.addItem(p.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    private void loadInventory() {
        tableModel.setRowCount(0);
        try {
            List<Inventory> inventoryList = inventoryDAO.getAllInventory();
            for (Inventory i : inventoryList) {
                String status = i.getQuantity() <= i.getReorderLevel() ? "LOW STOCK" : "OK";
                String productName = "";
                for (Product p : productList) {
                    if (p.getId() == i.getProductId()) {
                        productName = p.getName();
                        break;
                    }
                }
                tableModel.addRow(new Object[]{
                        i.getInventoryId(),
                        i.getProductId(),
                        productName,
                        i.getQuantity(),
                        i.getReorderLevel(),
                        status
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage());
        }
    }

    private void clearForm() {
        quantityField.setText("");
        reorderLevelField.setText("");
        inventoryTable.clearSelection();
        if (!productList.isEmpty()) productCombo.setSelectedIndex(0);
    }
}