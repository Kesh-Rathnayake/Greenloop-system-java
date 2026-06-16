package com.greenloop.ui;

import com.greenloop.dao.DeliveryAgentDAO;
import com.greenloop.models.DeliveryAgent;
import com.greenloop.service.EmailService; // Using your existing service

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AgentDispatchEmailPanel extends JFrame {
    private final JTable agentTable;
    private final DefaultTableModel tableModel;
    private final DeliveryAgentDAO agentDAO = new DeliveryAgentDAO();
    private final EmailService emailService = new EmailService();

    public AgentDispatchEmailPanel() {
        setTitle("GreenLoop - Agent Dispatch Notifications");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Styling
        Color background = new Color(18, 20, 18);
        Color card = new Color(45, 48, 45);
        Color green = new Color(21, 166, 117);
        Color text = new Color(235, 235, 235);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(background);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table
        String[] columns = {"Agent ID", "Name", "Vehicle", "Area", "Email"};
        tableModel = new DefaultTableModel(columns, 0);
        agentTable = new JTable(tableModel);
        agentTable.setBackground(card);
        agentTable.setForeground(text);

        JScrollPane scrollPane = new JScrollPane(agentTable);
        scrollPane.getViewport().setBackground(card);

        // Buttons
        JButton notifyButton = new JButton("Send Notification");
        notifyButton.setBackground(green);
        notifyButton.setForeground(Color.WHITE);
        notifyButton.addActionListener(e -> sendNotification());

        mainPanel.add(new JLabel("Available Agents"), BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(notifyButton, BorderLayout.SOUTH);

        add(mainPanel);
        loadAgents();
    }

    private void loadAgents() {
        tableModel.setRowCount(0);

        // This query joins agents with orders where status is 'Assigned'
        // and delivery_agent_id is not null.
        String sql = """
            SELECT DISTINCT a.agent_id, a.name, a.vehicle_type, a.service_area, a.email 
            FROM delivery_agents a
            INNER JOIN orders o ON a.agent_id = o.delivery_agent_id
            WHERE o.status = 'Assigned'
            """;

        try (java.sql.Connection conn = com.greenloop.database.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("agent_id"),
                        rs.getString("name"),
                        rs.getString("vehicle_type"),
                        rs.getString("service_area"),
                        rs.getString("email")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading active agents: " + e.getMessage());
        }
    }

    private void sendNotification() {
        int row = agentTable.getSelectedRow();
        if (row == -1) return;

        String email = (String) tableModel.getValueAt(row, 4);
        String name = (String) tableModel.getValueAt(row, 1);

        String subject = "New Delivery Assignment";
        String message = "Hello " + name + ", you have a new delivery assignment. Please check the system.";

        emailService.sendEmail(email, subject, message);
        JOptionPane.showMessageDialog(this, "Notification sent to " + name);
    }
}