package org.example.View;

import com.github.lgooddatepicker.components.DateTimePicker;
import org.example.Controller.DeliveryController;
import org.example.DatabaseConnection;
import org.example.Models.Agent;
import org.example.Models.Order;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DeliveryAssignmentUI extends JFrame {
    // UI components
    private JTable ordersTable;
    private JComboBox<Agent> agentsComboBox;
    private JButton assignButton;
    private JButton filterButton;
    private JButton assignOrdersButton;
    private JList agentsList;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JButton assignDeliveryButton;
    private JPanel mainPanel;
    private DateTimePicker dateTimePicker;
    private JLabel brandLabel;
    private JLabel pendingValueLabel;
    private JLabel dispatchValueLabel;
    private JLabel completedValueLabel;
    private JLabel delayedValueLabel;
    private JTextField filterTextField;

    private DeliveryController deliveryController;
    private DefaultTableModel tableModel;
    private List<Order> pendingOrders;
    private DefaultListModel<Agent> agentsListModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    public DeliveryAssignmentUI() {
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        agentsComboBox = (JComboBox<Agent>) comboBox2;
        assignButton = assignDeliveryButton;

        // initialize database connection
        try {
            Connection conn = DatabaseConnection.getConnection();
            deliveryController = new DeliveryController(conn);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setupDateTimePicker();

        // load data
        loadAgents();
        agentsList.setCellRenderer(new AgentListRenderer());
        loadPendingOrders();
        loadQuickAssignOrders();
        loadMetrics();

        assignButton.addActionListener(e -> assignOrder());
        assignOrdersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPendingOrders();
            }
        });
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = filterTextField.getText().trim();
                if (searchText.isEmpty()) {
                    tableSorter.setRowFilter(null);
                } else {
                    tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 0));
                }
            }
        });
    }

    private void loadMetrics() {
        try {
            int pending = deliveryController.getPendingOrdersCount();
            int dispatched = deliveryController.getDispatchedOrdersCount();
            int completed = deliveryController.getCompletedOrdersCount();
            int delayed = deliveryController.getDelayedOrdersCount();

            pendingValueLabel.setText(String.valueOf(pending));
            dispatchValueLabel.setText(String.valueOf(dispatched));
            completedValueLabel.setText(String.valueOf(completed));
            delayedValueLabel.setText(String.valueOf(delayed));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load metric counts");
        }
    }

    private void styleDateTimePicker() {
        dateTimePicker.getDatePicker().getComponentDateTextField().setBackground(new Color(26, 26, 26));
        dateTimePicker.getDatePicker().getComponentDateTextField().setForeground(Color.WHITE);
        dateTimePicker.getTimePicker().getComponentTimeTextField().setBackground(new Color(26, 26, 26));
        dateTimePicker.getTimePicker().getComponentTimeTextField().setForeground(Color.WHITE);
        dateTimePicker.getDatePicker().getComponentToggleCalendarButton().setBackground(new Color(54, 54, 54));
        dateTimePicker.getTimePicker().getComponentToggleTimeMenuButton().setBackground(new Color(54, 54, 54));
    }

    private void createUIComponents() {
        brandLabel = new JLabel();
        java.net.URL imgURL = getClass().getResource("/logoImg/logo.jpg");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            Image scaledImage = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            brandLabel.setIcon(new ImageIcon(scaledImage));
            brandLabel.setText("GreenLoop");
            brandLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
            brandLabel.setIconTextGap(8);
        } else {
            brandLabel.setIcon(null);
            brandLabel.setText("🌿 GreenLoop");
        }
    }

    private void setupDateTimePicker() {
        dateTimePicker.datePicker.getSettings().setFormatForDatesCommonEra("yyyy-MM-dd");
        dateTimePicker.datePicker.getSettings().setFormatForDatesBeforeCommonEra("yyyy-MM-dd");
        dateTimePicker.timePicker.getSettings().setFormatForDisplayTime("HH:mm");
        dateTimePicker.timePicker.getSettings().setFormatForMenuTimes("HH:mm");
        dateTimePicker.setDateTimeStrict(LocalDateTime.now());
        dateTimePicker.timePicker.getSettings().setAllowEmptyTimes(false);
        styleDateTimePicker();
    }

    private void loadAgents() {
        try {
            List<Agent> agents = deliveryController.getAllAgents();
            agentsComboBox.removeAllItems();
            for (Agent a : agents) {
                agentsComboBox.addItem(a);
            }
            agentsListModel = new DefaultListModel<>();
            for (Agent a : agents) {
                agentsListModel.addElement(a);
            }
            agentsList.setModel(agentsListModel);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load agents");
        }
    }

    private void loadPendingOrders() {
        try {
            pendingOrders = deliveryController.getPendingOrders();
            String[] columns = {"Order ID", "Client", "Amount", "Status", "Address", "Action"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 5;
                }
            };
            for (Order o : pendingOrders) {
                tableModel.addRow(new Object[]{
                        o.getOrderId(), o.getClientName(), o.getAmount(),
                        o.getStatus(), o.getDeliveryAddress(), "Assign"
                });
            }
            ordersTable.setModel(tableModel);
            tableSorter = new TableRowSorter<>(tableModel);
            ordersTable.setRowSorter(tableSorter);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load orders");
        }
    }

    private void assignOrder() {
        String orderId = null;
        Agent agent = (Agent) agentsComboBox.getSelectedItem();
        if (comboBox1.getSelectedIndex() != -1 && comboBox1.getSelectedItem() != null) {
            String selectedDisplay = (String) comboBox1.getSelectedItem();
            if (selectedDisplay != null && selectedDisplay.contains(" - ")) {
                orderId = selectedDisplay.split(" - ")[0];
            }
        }
        if (orderId == null) {
            int selectedRow = ordersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an order from the table or the dropdown");
                return;
            }
            orderId = (String) tableModel.getValueAt(selectedRow, 0);
        }
        if (agent == null) {
            JOptionPane.showMessageDialog(this, "Select an agent");
            return;
        }
        LocalDateTime scheduled = dateTimePicker.getDateTimeStrict();
        if (scheduled == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date and time");
            return;
        }
        try {
            boolean success = deliveryController.assignOrder(orderId, agent.getId(), scheduled);
            if (success) {
                JOptionPane.showMessageDialog(this, "Assigned successfully");
                loadPendingOrders();
                loadQuickAssignOrders();
                loadAgents();
                loadMetrics();
            } else {
                JOptionPane.showMessageDialog(this, "Assignment failed");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage());
        }
    }

    class AgentListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setBackground(isSelected ? new Color(42, 42, 42) : new Color(36, 36, 36));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JLabel avatar = new JLabel("👤");
            avatar.setOpaque(true);
            avatar.setBackground(new Color(16, 185, 129));
            avatar.setForeground(Color.WHITE);
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setPreferredSize(new Dimension(28, 28));
            avatar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setBackground(null);
            Agent agent = (Agent) value;
            JLabel nameLabel = new JLabel(agent.getName());
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String details = agent.getVehicleType() + " • " + agent.getServiceArea() + " • " + agent.getPhone() + " • " +
                    agent.getCurrentOrders() + " orders";
            JLabel detailsLabel = new JLabel(details);
            detailsLabel.setForeground(new Color(102, 102, 102));
            detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            textPanel.add(nameLabel);
            textPanel.add(detailsLabel);
            JLabel statusDot = new JLabel("●");
            statusDot.setForeground(new Color(16, 185, 129));
            statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            panel.add(avatar, BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);
            panel.add(statusDot, BorderLayout.EAST);
            return panel;
        }
    }

    private void loadQuickAssignOrders() {
        try {
            List<Order> orders = deliveryController.getPendingOrders();
            comboBox1.removeAllItems();
            for (Order o : orders) {
                String display = o.getOrderId() + " - " + o.getClientName();
                comboBox1.addItem(display);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load orders for quick assign");
        }
    }
}