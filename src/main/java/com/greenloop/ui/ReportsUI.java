package com.greenloop.ui;

import com.greenloop.dao.*;
import com.greenloop.models.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.io.File;

public class ReportsUI extends JFrame {
    private static final Color BG_DARK = new Color(18, 20, 18);
    private static final Color BG_CARD = new Color(48, 51, 48);
    private static final Color ACCENT_GREEN = new Color(21, 166, 117);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);

    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private JTable revenueTable, lowStockTable;
    private DefaultTableModel revenueModel, lowStockModel;
    private JComboBox<String> monthFilter;

    public ReportsUI() {
        setTitle("GreenLoop - System Reports");
        setSize(980, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header with Month Filter
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(BG_DARK);
        String[] months = {"All", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct"};
        monthFilter = new JComboBox<>(months);
        monthFilter.addActionListener(e -> loadRevenueData());
        headerPanel.add(new JLabel("Filter by Month: ")).setForeground(TEXT_PRIMARY);
        headerPanel.add(monthFilter);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Update this line in your constructor
        revenueModel = new DefaultTableModel(new String[]{"Order ID", "Client", "Amount", "Status", "Date"}, 0);
        revenueTable = createStyledTable(revenueModel);
        lowStockModel = new DefaultTableModel(new String[]{"ID", "Product", "Qty", "Deficit"}, 0);
        lowStockTable = createStyledTable(lowStockModel);

        JPanel reportsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        reportsPanel.setBackground(BG_DARK);
        reportsPanel.add(createScrollableTable(revenueTable, "Sales Report"));
        reportsPanel.add(createScrollableTable(lowStockTable, "Low Stock Alerts"));
        contentPanel.add(reportsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_DARK);
        JButton exportSales = createStandardButton("Export Sales CSV");
        exportSales.addActionListener(e -> exportTableToCSV(revenueModel, "Sales_Report"));
        JButton exportStock = createStandardButton("Export Stock CSV");
        exportStock.addActionListener(e -> exportTableToCSV(lowStockModel, "Low_Stock_Report"));

        buttonPanel.add(exportSales);
        buttonPanel.add(exportStock);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel);
        loadData();
    }

    private void loadData() {
        loadLowStockReport();
        loadRevenueData();
    }

    private void loadRevenueData() {
        revenueModel.setRowCount(0);
        int month = monthFilter.getSelectedIndex();
        List<Order> orders = (month == 0) ? orderDAO.getDispatchedOrdersWithoutEmail() : orderDAO.getOrdersByMonth(month);

        for (Order o : orders) {
            // Add a null check for the timestamp
            String dateString = (o.getCreatedAt() != null) ? o.getCreatedAt().toString() : "No Date";

            revenueModel.addRow(new Object[]{
                    o.getDisplayOrderId(),
                    o.getClientName(),
                    "Rs " + o.getAmount(),
                    o.getStatus(),
                    dateString // Safe to print
            });
        }
    }

    private void loadLowStockReport() {
        lowStockModel.setRowCount(0);
        try {
            List<Inventory> inv = inventoryDAO.getLowStockItems();
            List<Product> prod = productDAO.getAllProducts();
            for (Inventory i : inv) {
                String pName = prod.stream().filter(p -> p.getId() == i.getProductId()).map(Product::getName).findFirst().orElse("Unknown");
                lowStockModel.addRow(new Object[]{i.getInventoryId(), pName, i.getQuantity(), (i.getReorderLevel() - i.getQuantity())});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void exportTableToCSV(DefaultTableModel model, String fileName) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fc.getSelectedFile())) {
                for (int i = 0; i < model.getColumnCount(); i++) writer.write(model.getColumnName(i) + (i == model.getColumnCount()-1 ? "" : ","));
                writer.write("\n");
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) writer.write(model.getValueAt(i, j) + (j == model.getColumnCount()-1 ? "" : ","));
                    writer.write("Order ID,Client,Amount,Status,Date\n");
                }
                JOptionPane.showMessageDialog(this, "Exported successfully!");
            } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Error exporting."); }
        }
    }

    private JScrollPane createScrollableTable(JTable t, String title) {
        JScrollPane s = new JScrollPane(t);
        s.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title, 0, 0, null, TEXT_MUTED));
        s.getViewport().setBackground(BG_CARD);
        return s;
    }

    private JTable createStyledTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setBackground(BG_CARD); t.setForeground(TEXT_PRIMARY); t.setRowHeight(30);
        return t;
    }

    private JButton createStandardButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT_GREEN); b.setForeground(Color.WHITE);
        return b;
    }
}