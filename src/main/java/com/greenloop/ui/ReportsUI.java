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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ReportsUI extends JFrame {

    // Theme Constants
    private static final Color BG_DARK = new Color(18, 20, 18);
    private static final Color BG_CARD = new Color(48, 51, 48);
    private static final Color ACCENT_GREEN = new Color(21, 166, 117);
    private static final Color TEXT_PRIMARY = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(170, 170, 170);
    private static final Color DANGER_RED = new Color(220, 80, 80);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ProductDAO productDAO = new ProductDAO();

    private JTable revenueTable;
    private DefaultTableModel revenueModel;

    private JTable lowStockTable;
    private DefaultTableModel lowStockModel;

    private JLabel totalRevenueLabel;

    public ReportsUI() {
        setTitle("GreenLoop - System Reports");
        setSize(980, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. TOP: Title and Global KPI
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_DARK);

        JLabel titleLabel = new JLabel("Monthly Sales & Inventory Reports");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(FONT_TITLE);

        totalRevenueLabel = new JLabel("Total YTD Revenue: Pending Data...");
        totalRevenueLabel.setForeground(ACCENT_GREEN);
        totalRevenueLabel.setFont(FONT_BOLD);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(totalRevenueLabel, BorderLayout.EAST);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER: Split view for the two reports
        JPanel reportsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        reportsPanel.setBackground(BG_DARK);

        // --- REVENUE REPORT SECTION (Placeholder) ---
        JPanel revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBackground(BG_DARK);
        revenuePanel.setBorder(createCustomTitledBorder("Monthly Revenue Summary"));

        String[] revColumns = {"Month", "Total Orders Completed", "Gross Revenue (Rs)"};
        revenueModel = new DefaultTableModel(revColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        revenueTable = createStyledTable(revenueModel);

        JScrollPane revScroll = new JScrollPane(revenueTable);
        revScroll.getViewport().setBackground(BG_CARD);
        revScroll.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        revenuePanel.add(revScroll, BorderLayout.CENTER);

        // --- LOW STOCK REPORT SECTION (Active) ---
        JPanel stockPanel = new JPanel(new BorderLayout());
        stockPanel.setBackground(BG_DARK);
        stockPanel.setBorder(createCustomTitledBorder("Action Required: Low Stock Alerts"));

        String[] stockColumns = {"Inventory ID", "Product Name", "Current Qty", "Reorder Level", "Deficit"};
        lowStockModel = new DefaultTableModel(stockColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        lowStockTable = createStyledTable(lowStockModel);

        JScrollPane stockScroll = new JScrollPane(lowStockTable);
        stockScroll.getViewport().setBackground(BG_CARD);
        stockScroll.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 75)));
        stockPanel.add(stockScroll, BorderLayout.CENTER);

        reportsPanel.add(revenuePanel);
        reportsPanel.add(stockPanel);

        contentPanel.add(reportsPanel, BorderLayout.CENTER);

        // 3. SOUTH: Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton refreshButton = createStandardButton("Refresh Reports");
        refreshButton.addActionListener(e -> {
            loadLowStockReport();
            loadRevenueReportPlaceholder();
        });

        JButton closeButton = createStandardButton("Close");
        closeButton.setBackground(new Color(80, 80, 80));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Initial Data Load
        loadLowStockReport();
        loadRevenueReportPlaceholder();
    }

    // --- Data Loading Methods ---

    private void loadLowStockReport() {
        lowStockModel.setRowCount(0);
        try {
            List<Inventory> allInventory = inventoryDAO.getAllInventory();
            List<Product> allProducts = productDAO.getAllProducts();

            for (Inventory item : allInventory) {
                // Logic for finding Low Stock
                if (item.getQuantity() <= item.getReorderLevel()) {

                    String productName = "Unknown Product";
                    for (Product p : allProducts) {
                        if (p.getId() == item.getProductId()) {
                            productName = p.getName();
                            break;
                        }
                    }

                    int deficit = item.getReorderLevel() - item.getQuantity();

                    lowStockModel.addRow(new Object[]{
                            item.getInventoryId(),
                            productName,
                            item.getQuantity(),
                            item.getReorderLevel(),
                            deficit + " units needed"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading low stock report: " + e.getMessage());
        }
    }

    private void loadRevenueReportPlaceholder() {
        revenueModel.setRowCount(0);

        // TODO: Replace this entire block once Order and OrderDAO are merged
        /* * Example Future Implementation:
         * OrderDAO tempOrderDAO = new OrderDAO();
         * List<MonthlySummary> summaries = tempOrderDAO.getMonthlyRevenue();
         * for(MonthlySummary s : summaries) {
         * revenueModel.addRow(new Object[]{s.getMonth(), s.getOrderCount(), s.getTotal()});
         * }
         */

        revenueModel.addRow(new Object[]{"January", "Pending PR Merge", "0.00"});
        revenueModel.addRow(new Object[]{"February", "Pending PR Merge", "0.00"});
        revenueModel.addRow(new Object[]{"March", "Pending PR Merge", "0.00"});
    }

    // --- UI Helper Methods ---

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(80, 80, 80));
        table.setRowHeight(30);
        table.setFont(FONT_NORMAL);
        table.setSelectionBackground(new Color(30, 80, 60));
        table.setSelectionForeground(TEXT_PRIMARY);

        table.getTableHeader().setBackground(new Color(58, 61, 58));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        return table;
    }

    private TitledBorder createCustomTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 75, 75)), title
        );
        border.setTitleColor(TEXT_MUTED);
        border.setTitleFont(FONT_BOLD);
        return border;
    }

    private JButton createStandardButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(FONT_BOLD);
        return button;
    }
}