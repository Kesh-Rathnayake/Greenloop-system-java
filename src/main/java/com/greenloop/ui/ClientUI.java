package com.greenloop.ui;

import com.greenloop.dao.ClientDAO;
import com.greenloop.models.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClientUI extends JFrame {

    // Theme Constants (Local to this file)
    private static final Color BG_DARK = new Color(18, 20, 18);
    private static final Color BG_CARD = new Color(48, 51, 48);
    private static final Color ACCENT_GREEN = new Color(21, 166, 117);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private ClientDAO clientDAO = new ClientDAO();
    private JTable clientTable;
    private DefaultTableModel tableModel;

    private JTextField nameField, emailField, phoneField;
    private JTextArea addressArea;
    private JButton addButton, updateButton, deleteButton, clearButton;

    public ClientUI() {
        setTitle("GreenLoop - Client Management");
        setSize(980, 620); // Matched to collaborator's layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Wrapper
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TOP: Title
        JLabel titleLabel = new JLabel("Client Management");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FONT_TITLE);
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // 2. CENTER: Table
        String[] columns = {"ID", "Client Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        clientTable = new JTable(tableModel);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Table Styling
        clientTable.setBackground(BG_CARD);
        clientTable.setForeground(TEXT_PRIMARY);
        clientTable.setGridColor(new Color(80, 80, 80));
        clientTable.setRowHeight(30);
        clientTable.setFont(FONT_NORMAL);
        clientTable.setSelectionBackground(new Color(30, 80, 60));
        clientTable.setSelectionForeground(TEXT_PRIMARY);

        clientTable.getTableHeader().setBackground(new Color(58, 61, 58));
        clientTable.getTableHeader().setForeground(TEXT_PRIMARY);
        clientTable.getTableHeader().setFont(FONT_BOLD);
        clientTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. SOUTH: Form and Buttons
        JPanel southWrapper = new JPanel(new BorderLayout(10, 10));
        southWrapper.setBackground(BG_DARK);

        // Form Panel (Adjusted to 2x4 Grid for wider layout)
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 15, 10));
        formPanel.setBackground(BG_DARK);

        TitledBorder formBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 75, 75)),
                "Client Details"
        );
        formBorder.setTitleColor(TEXT_MUTED);
        formBorder.setTitleFont(FONT_BOLD);
        formPanel.setBorder(BorderFactory.createCompoundBorder(formBorder, new EmptyBorder(10, 10, 10, 10)));

        nameField = createStyledTextField();
        emailField = createStyledTextField();
        phoneField = createStyledTextField();

        addressArea = new JTextArea(2, 20);
        addressArea.setBackground(BG_CARD);
        addressArea.setForeground(TEXT_PRIMARY);
        addressArea.setCaretColor(Color.WHITE);
        addressArea.setFont(FONT_NORMAL);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));

        formPanel.add(createStyledLabel("Client Name:"));
        formPanel.add(nameField);
        formPanel.add(createStyledLabel("Email:"));
        formPanel.add(emailField);

        formPanel.add(createStyledLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(createStyledLabel("Address:"));
        formPanel.add(addressScroll);

        southWrapper.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addButton = createStandardButton("Add Client");
        updateButton = createStandardButton("Update Client");
        deleteButton = createStandardButton("Delete Client");

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
        loadClients();

        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && clientTable.getSelectedRow() != -1) {
                int row = clientTable.getSelectedRow();
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                emailField.setText(tableModel.getValueAt(row, 2).toString());
                phoneField.setText(tableModel.getValueAt(row, 3).toString());
                addressArea.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        addButton.addActionListener(e -> {
            try {
                Client client = new Client(
                        0,
                        nameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        addressArea.getText()
                );
                clientDAO.addClient(client);
                loadClients();
                clearForm();
                JOptionPane.showMessageDialog(this, "Client added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = clientTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a client to update.");
                return;
            }
            try {
                int clientId = (int) tableModel.getValueAt(selectedRow, 0);
                Client client = new Client(
                        clientId,
                        nameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        addressArea.getText()
                );
                clientDAO.updateClient(client);
                loadClients();
                clearForm();
                JOptionPane.showMessageDialog(this, "Client updated successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = clientTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a client to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this client?");
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int clientId = (int) tableModel.getValueAt(selectedRow, 0);
                    clientDAO.deleteClient(clientId);
                    loadClients();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Client deleted successfully.");
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

    private void loadClients() {
        tableModel.setRowCount(0);
        try {
            List<Client> clients = clientDAO.getAllClients();
            for (Client c : clients) {
                tableModel.addRow(new Object[]{
                        c.getClientId(),
                        c.getClientName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getAddress()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading clients: " + e.getMessage());
        }
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        clientTable.clearSelection();
    }
}