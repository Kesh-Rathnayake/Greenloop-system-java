package com.greenloop.ui;

import com.greenloop.dao.OrderDAO;
import com.greenloop.model.Order;
import com.greenloop.service.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientDispatchEmailPanel extends JFrame {
    private final JTable orderTable;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;

    private final OrderDAO orderDAO = new OrderDAO();
    private final NotificationService notificationService = new NotificationService();

    private List<Order> dispatchedOrders;

    public ClientDispatchEmailPanel() {
        setTitle("GreenLoop App - Client Email Notifications");
        setSize(980, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color background = new Color(18, 20, 18);
        Color sidebar = new Color(35, 38, 35);
        Color card = new Color(48, 51, 48);
        Color green = new Color(21, 166, 117);
        Color text = new Color(235, 235, 235);
        Color muted = new Color(170, 170, 170);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(background);

        JPanel sidePanel = createSidebar(sidebar, green, text, muted);
        mainPanel.add(sidePanel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(background);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(background);

        JLabel titleLabel = new JLabel("Client Dispatch Email Notifications");
        titleLabel.setForeground(text);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton refreshButton = createButton("Refresh", green);
        refreshButton.addActionListener(e -> loadOrders());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        kpiPanel.setBackground(background);

        kpiPanel.add(createKpiCard("Pending Emails", "0", "dispatch alerts", card, text, green));
        kpiPanel.add(createKpiCard("Email Status", "Ready", "SMTP connected after send", card, text, green));
        kpiPanel.add(createKpiCard("Module", "Topic 8", "client notification", card, text, green));

        String[] columns = {"Order ID", "Client", "Email", "Amount", "Status", "Address"};
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

        JButton sendButton = createButton("Send Dispatch Email", green);
        sendButton.addActionListener(e -> sendSelectedEmail());

        JButton sendAllButton = createButton("Send All Pending Emails", green);
        sendAllButton.addActionListener(e -> sendAllEmails());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(background);
        buttonPanel.add(sendButton);
        buttonPanel.add(sendAllButton);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(muted);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(kpiPanel, BorderLayout.CENTER);
        contentPanel.add(scrollPane, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setBackground(background);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        wrapper.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(wrapper, BorderLayout.CENTER);
        add(mainPanel);

        loadOrders();
    }

    private JPanel createSidebar(Color sidebar, Color green, Color text, Color muted) {
        JPanel panel = new JPanel();
        panel.setBackground(sidebar);
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 18, 20, 18));

        JLabel logo = new JLabel("GreenLoop");
        logo.setForeground(text);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel("Eco Packaging");
        sub.setForeground(muted);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(logo);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(30));

        String[] menu = {
                "Dashboard",
                "Products",
                "Clients",
                "Inventory",
                "Delivery Agents",
                "Orders",
                "Email Notifications",
                "Reports"
        };

        for (String item : menu) {
            JLabel label = new JLabel(item);
            label.setForeground(item.equals("Email Notifications") ? green : text);
            label.setFont(new Font("Segoe UI", Font.BOLD, item.equals("Email Notifications") ? 14 : 13));
            label.setBorder(new EmptyBorder(10, 5, 10, 5));
            panel.add(label);
        }

        panel.add(Box.createVerticalGlue());

        JLabel user = new JLabel("AS  Admin Staff");
        user.setForeground(text);
        user.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(user);

        return panel;
    }

    private JPanel createKpiCard(String title, String value, String subtitle,
                                 Color card, Color text, Color green) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(card);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(180, 180, 180));
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(text);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(green);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private JButton createButton(String text, Color green) {
        JButton button = new JButton(text);
        button.setBackground(green);
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
                    order.getOrderCode(),
                    order.getClient().getClientName(),
                    order.getClient().getEmail(),
                    "Rs " + order.getAmount(),
                    order.getStatus(),
                    order.getAddress()
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
            JOptionPane.showMessageDialog(this, "Email sent to " + selectedOrder.getClient().getEmail());
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
                System.out.println("Failed for " + order.getOrderCode() + ": " + e.getMessage());
            }
        }

        JOptionPane.showMessageDialog(this, sentCount + " emails sent successfully.");
        loadOrders();
    }
}
