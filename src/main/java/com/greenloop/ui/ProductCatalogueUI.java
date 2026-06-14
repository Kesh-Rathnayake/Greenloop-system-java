package com.greenloop.ui;

import com.greenloop.dao.ProductDAO;
import com.greenloop.models.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ProductCatalogueUI extends JFrame {

    // --- Theme Constants (Local to this file) ---
    private static final Color BG_DARK = new Color(18, 20, 18);
    private static final Color BG_CARD = new Color(48, 51, 48);
    private static final Color ACCENT_GREEN = new Color(21, 166, 117);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private ProductDAO productDAO = new ProductDAO();
    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField nameField, typeField, priceField, ecoRatingField;
    private JTextArea descriptionArea;
    private JButton addButton, updateButton, deleteButton, clearButton;

    public ProductCatalogueUI() {
        setTitle("GreenLoop - Product Catalogue");
        setSize(980, 620); // Matched to collaborator's layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Wrapper
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. TOP: Title ---
        JLabel titleLabel = new JLabel("Product Catalogue");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FONT_TITLE);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // --- 2. CENTER: Table ---
        String[] columns = {"ID", "Name", "Type", "Price", "Eco Rating", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Styling
        productTable.setBackground(BG_CARD);
        productTable.setForeground(TEXT_PRIMARY);
        productTable.setGridColor(new Color(80, 80, 80));
        productTable.setRowHeight(30);
        productTable.setFont(FONT_NORMAL);
        productTable.setSelectionBackground(new Color(30, 80, 60));
        productTable.setSelectionForeground(TEXT_PRIMARY);

        productTable.getTableHeader().setBackground(new Color(58, 61, 58));
        productTable.getTableHeader().setForeground(TEXT_PRIMARY);
        productTable.getTableHeader().setFont(FONT_BOLD);
        productTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 3. SOUTH: Form and Buttons ---
        JPanel southWrapper = new JPanel(new BorderLayout(10, 10));
        southWrapper.setBackground(BG_DARK);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 10));
        formPanel.setBackground(BG_DARK);

        TitledBorder formBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 75, 75)),
                "Product Details"
        );
        formBorder.setTitleColor(TEXT_MUTED);
        formBorder.setTitleFont(FONT_BOLD);
        formPanel.setBorder(BorderFactory.createCompoundBorder(formBorder, new EmptyBorder(10, 10, 10, 10)));

        nameField = createStyledTextField();
        typeField = createStyledTextField();
        priceField = createStyledTextField();
        ecoRatingField = createStyledTextField();

        descriptionArea = new JTextArea(2, 20);
        descriptionArea.setBackground(BG_CARD);
        descriptionArea.setForeground(TEXT_PRIMARY);
        descriptionArea.setCaretColor(Color.WHITE);
        descriptionArea.setFont(FONT_NORMAL);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));

        formPanel.add(createStyledLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(createStyledLabel("Type:"));
        formPanel.add(typeField);
        formPanel.add(createStyledLabel("Price:"));
        formPanel.add(priceField);
        formPanel.add(createStyledLabel("Eco Rating:"));
        formPanel.add(ecoRatingField);
        formPanel.add(createStyledLabel("Description:"));
        formPanel.add(descScroll);

        southWrapper.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addButton = createStandardButton("Add Product");
        updateButton = createStandardButton("Update Product");
        deleteButton = createStandardButton("Delete Product");

        clearButton = createStandardButton("Clear");
        clearButton.setBackground(new Color(80, 80, 80));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        southWrapper.add(buttonPanel, BorderLayout.SOUTH);
        contentPanel.add(southWrapper, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // --- Logic (Untouched) ---
        loadProducts();

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                typeField.setText(tableModel.getValueAt(row, 2).toString());
                priceField.setText(tableModel.getValueAt(row, 3).toString());
                ecoRatingField.setText(tableModel.getValueAt(row, 4).toString());
                descriptionArea.setText(tableModel.getValueAt(row, 5).toString());
            }
        });

        addButton.addActionListener(e -> {
            try {
                Product product = new Product(0, nameField.getText(), typeField.getText(),
                        Double.parseDouble(priceField.getText()), descriptionArea.getText(),
                        Double.parseDouble(ecoRatingField.getText()));
                productDAO.addProduct(product);
                loadProducts();
                clearForm();
                JOptionPane.showMessageDialog(this, "Product added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to update.");
                return;
            }
            try {
                int productId = (int) tableModel.getValueAt(selectedRow, 0);
                Product product = new Product(productId, nameField.getText(), typeField.getText(),
                        Double.parseDouble(priceField.getText()), descriptionArea.getText(),
                        Double.parseDouble(ecoRatingField.getText()));
                productDAO.updateProduct(product);
                loadProducts();
                clearForm();
                JOptionPane.showMessageDialog(this, "Product updated successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?");
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int productId = (int) tableModel.getValueAt(selectedRow, 0);
                    productDAO.deleteProduct(productId);
                    loadProducts();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        clearButton.addActionListener(e -> clearForm());
    }

    // --- Helper Methods ---

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

    private void loadProducts() {
        tableModel.setRowCount(0);
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product p : products) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getName(), p.getType(),
                        p.getPrice(), p.getEcoRating(), p.getDescription()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        typeField.setText("");
        priceField.setText("");
        ecoRatingField.setText("");
        descriptionArea.setText("");
        productTable.clearSelection();
    }
}