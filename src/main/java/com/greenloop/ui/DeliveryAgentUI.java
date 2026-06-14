package com.greenloop.ui;

import com.greenloop.dao.DeliveryAgentDAO;
import com.greenloop.models.DeliveryAgent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DeliveryAgentUI extends JFrame {

    // Theme Constants (Local to this file)
    private static final Color BG_DARK = new Color(18, 20, 18);
    private static final Color BG_CARD = new Color(48, 51, 48);
    private static final Color ACCENT_GREEN = new Color(21, 166, 117);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private DeliveryAgentDAO agentDAO = new DeliveryAgentDAO();
    private JTable agentTable;
    private DefaultTableModel tableModel;

    private JTextField nameField, vehicleTypeField, serviceAreaField, phoneField, emailField;
    private JButton addButton, updateButton, deleteButton, clearButton;

    public DeliveryAgentUI() {
        setTitle("GreenLoop - Delivery Agent Management");
        setSize(980, 620); // Matched to collaborator's layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Wrapper
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TOP: Title
        JLabel titleLabel = new JLabel("Delivery Agent Management");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FONT_TITLE);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // 2. CENTER: Table
        String[] columns = {"ID", "Name", "Vehicle Type", "Service Area", "Phone", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        agentTable = new JTable(tableModel);
        agentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Styling
        agentTable.setBackground(BG_CARD);
        agentTable.setForeground(TEXT_PRIMARY);
        agentTable.setGridColor(new Color(80, 80, 80));
        agentTable.setRowHeight(30);
        agentTable.setFont(FONT_NORMAL);
        agentTable.setSelectionBackground(new Color(30, 80, 60));
        agentTable.setSelectionForeground(TEXT_PRIMARY);

        agentTable.getTableHeader().setBackground(new Color(58, 61, 58));
        agentTable.getTableHeader().setForeground(TEXT_PRIMARY);
        agentTable.getTableHeader().setFont(FONT_BOLD);
        agentTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(agentTable);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. SOUTH: Form and Buttons
        JPanel southWrapper = new JPanel(new BorderLayout(10, 10));
        southWrapper.setBackground(BG_DARK);

        // Form Panel (3x4 Grid for wider layout)
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 10));
        formPanel.setBackground(BG_DARK);

        TitledBorder formBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 75, 75)),
                "Agent Details"
        );
        formBorder.setTitleColor(TEXT_MUTED);
        formBorder.setTitleFont(FONT_BOLD);
        formPanel.setBorder(BorderFactory.createCompoundBorder(formBorder, new EmptyBorder(10, 10, 10, 10)));

        nameField = createStyledTextField();
        vehicleTypeField = createStyledTextField();
        serviceAreaField = createStyledTextField();
        phoneField = createStyledTextField();
        emailField = createStyledTextField();

        // Row 1
        formPanel.add(createStyledLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(createStyledLabel("Vehicle Type:"));
        formPanel.add(vehicleTypeField);

        // Row 2
        formPanel.add(createStyledLabel("Service Area:"));
        formPanel.add(serviceAreaField);
        formPanel.add(createStyledLabel("Phone:"));
        formPanel.add(phoneField);

        // Row 3
        formPanel.add(createStyledLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("")); // Empty placeholder for grid
        formPanel.add(new JLabel("")); // Empty placeholder for grid

        southWrapper.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addButton = createStandardButton("Add Agent");
        updateButton = createStandardButton("Update Agent");
        deleteButton = createStandardButton("Delete Agent");

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
        loadAgents();

        agentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && agentTable.getSelectedRow() != -1) {
                int row = agentTable.getSelectedRow();
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                vehicleTypeField.setText(tableModel.getValueAt(row, 2).toString());
                serviceAreaField.setText(tableModel.getValueAt(row, 3).toString());
                phoneField.setText(tableModel.getValueAt(row, 4).toString());
                emailField.setText(tableModel.getValueAt(row, 5).toString());
            }
        });

        addButton.addActionListener(e -> {
            try {
                DeliveryAgent agent = new DeliveryAgent(
                        0,
                        nameField.getText(),
                        vehicleTypeField.getText(),
                        serviceAreaField.getText(),
                        phoneField.getText(),
                        emailField.getText()
                );
                agentDAO.addAgent(agent);
                loadAgents();
                clearForm();
                JOptionPane.showMessageDialog(this, "Agent added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = agentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an agent to update.");
                return;
            }
            try {
                int agentId = (int) tableModel.getValueAt(selectedRow, 0);
                DeliveryAgent agent = new DeliveryAgent(
                        agentId,
                        nameField.getText(),
                        vehicleTypeField.getText(),
                        serviceAreaField.getText(),
                        phoneField.getText(),
                        emailField.getText()
                );
                agentDAO.updateAgent(agent);
                loadAgents();
                clearForm();
                JOptionPane.showMessageDialog(this, "Agent updated successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = agentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an agent to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?");
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int agentId = (int) tableModel.getValueAt(selectedRow, 0);
                    agentDAO.deleteAgent(agentId);
                    loadAgents();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Agent deleted successfully.");
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

    private void loadAgents() {
        tableModel.setRowCount(0);
        try {
            List<DeliveryAgent> agents = agentDAO.getAllAgents();
            for (DeliveryAgent a : agents) {
                tableModel.addRow(new Object[]{
                        a.getAgentId(),
                        a.getName(),
                        a.getVehicleType(),
                        a.getServiceArea(),
                        a.getPhone(),
                        a.getEmail()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading agents: " + e.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        vehicleTypeField.setText("");
        serviceAreaField.setText("");
        phoneField.setText("");
        emailField.setText("");
        agentTable.clearSelection();
    }
}