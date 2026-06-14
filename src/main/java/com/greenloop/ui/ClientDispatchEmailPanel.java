package com.greenloop.ui;

import com.greenloop.dao.OrderDAO;
import com.greenloop.models.Order;
import com.greenloop.service.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDispatchEmailPanel extends JFrame {
    private final JTable orderTable;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;

    private final OrderDAO orderDAO = new OrderDAO();
    private final NotificationService notificationService = new NotificationService();

    private List<Order> dispatchedOrders = new ArrayList<>();

    public ClientDispatchEmailPanel() {
        setTitle("GreenLoop - Client Email Notifications");
        setSize(950, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color background = new Color(18, 20, 18);
        Color card = new Color(45, 48, 45);
        Color green = new Color(21, 166, 117);
        Color text = new Color(235, 235, 235);
        Color muted = new Color(170, 170, 170);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(background);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(background);

        JLabel titleLabel = new JLabel("Client Dispatch Email Notifications");
        titleLabel.setForeground(text);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topButtonPanel.setBackground(background);

        JButton refreshButton = createButton("Refresh", green);
        refreshButton.addActionListener(e -> loadOrders());

        JButton closeButton = createButton("Close", new Color(90, 90, 90));
        closeButton.addActionListener(e -> dispose());

        topButtonPanel.add(refreshButton);
        topButtonPanel.add(closeButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(topButtonPanel, BorderLayout.EAST);

        String[] columns = {"Order ID", "Client", "Email", "Amount", "Status", "Delivery Address"};
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);

        orderTable.setBackground(card);
        orderTable.setForeground(text);
        orderTable.setGridColor(new Color(80, 80, 80));
        orderTable.setRowHeight(30);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        orderTable.getTableHeader().setBackground(new Color(58, 61, 58));
        orderTable.getTableHeader().setForeground(text);
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.getViewport().setBackground(card);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(background);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(muted);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(background);

        JButton sendButton = createButton("Send Dispatch Email", green);
        sendButton.addActionListener(e -> sendSelectedEmail());

        JButton sendAllButton = createButton("Send All Pending Emails", green);
        sendAllButton.addActionListener(e -> sendAllEmails());

        actionPanel.add(sendButton);
        actionPanel.add(sendAllButton);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadOrders();
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        return button;
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        dispatchedOrders = orderDAO.getDispatchedOrdersWithoutEmail();

        for (Order order : dispatchedOrders) {
            tableModel.addRow(new Object[]{
                    order.getDisplayOrderId(),
                    order.getClientName(),
                    order.getClientEmail(),
                    "Rs " + order.getAmount(),
                    order.getStatus(),
                    order.getDeliveryAddress()
            });
        }

        statusLabel.setText(dispatchedOrders.size() + " dispatched orders waiting for client email notification.");
    }

    private void sendSelectedEmail() {
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }

        Order selectedOrder = dispatchedOrders.get(selectedRow);

        try {
            notificationService.sendDispatchEmail(selectedOrder);
            JOptionPane.showMessageDialog(this, "Email sent to " + selectedOrder.getClientEmail());
            loadOrders();

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Email Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendAllEmails() {
        if (dispatchedOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending dispatched email notifications.");
            return;
        }

        int sentCount = 0;

        for (Order order : dispatchedOrders) {
            try {
                notificationService.sendDispatchEmail(order);
                sentCount++;
            } catch (RuntimeException e) {
                System.out.println("Failed for " + order.getDisplayOrderId() + ": " + e.getMessage());
            }
        }

        JOptionPane.showMessageDialog(this, sentCount + " emails sent successfully.");
        loadOrders();
    }
}