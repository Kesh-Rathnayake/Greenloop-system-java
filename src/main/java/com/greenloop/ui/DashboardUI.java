package com.greenloop.ui;

import com.greenloop.dao.ClientDAO;
import com.greenloop.dao.DeliveryAgentDAO;
import com.greenloop.dao.InventoryDAO;
import com.greenloop.dao.ProductDAO;
import com.greenloop.models.Inventory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardUI extends JFrame {
    private static final Color BG       = new Color(18,  20,  18);
    private static final Color SIDEBAR  = new Color(35,  38,  35);
    private static final Color CARD     = new Color(48,  51,  48);
    private static final Color GREEN    = new Color(21,  166, 117);
    private static final Color TEXT     = new Color(235, 235, 235);
    private static final Color MUTED    = new Color(170, 170, 170);
    private static final Color DANGER   = new Color(220, 80,  80);
    private static final Color WARN     = new Color(220, 160, 40);

    // DAOs
    private final ProductDAO       productDAO       = new ProductDAO();
    private final ClientDAO        clientDAO        = new ClientDAO();
    private final InventoryDAO     inventoryDAO     = new InventoryDAO();
    private final DeliveryAgentDAO deliveryAgentDAO = new DeliveryAgentDAO();

    // KPI value labels (updated on refresh)
    private JLabel productsVal, clientsVal, agentsVal, lowStockVal;
    private JLabel lastRefreshLabel;

    // Low-stock table
    private final javax.swing.table.DefaultTableModel lowStockModel =
            new javax.swing.table.DefaultTableModel(
                    new String[]{"Inventory ID", "Product ID", "Qty", "Reorder Level"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

    public DashboardUI() {
        setTitle("GreenLoop — Dashboard");
        setSize(980, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        loadStats();
        setVisible(true);
    }


    //  SIDEBAR
    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(SIDEBAR);
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 18, 24, 18));

        // Logo
        JLabel logo = new JLabel("GreenLoop");
        logo.setForeground(TEXT);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Eco Packaging");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(logo);
        panel.add(sub);
        panel.add(Box.createVerticalStrut(8));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 65, 60));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(20));

        // Nav items: label → action
        String[][] navItems = {
                {"Dashboard",        null},
                {"Products",          "products"},
                {"Clients",           "clients"},
                {"Inventory",         "inventory"},
                {"Delivery Agents",   "agents"},
                {"Email Notify",      "email"},
        };

        for (String[] item : navItems) {
            boolean active = item[1] == null; // Dashboard = active
            panel.add(buildNavItem(item[0], item[1], active));
            panel.add(Box.createVerticalStrut(2));
        }

        panel.add(Box.createVerticalGlue());

        // Bottom: refresh button
        JButton refreshBtn = new JButton("↻  Refresh Stats");
        refreshBtn.setBackground(new Color(50, 55, 50));
        refreshBtn.setForeground(GREEN);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        refreshBtn.addActionListener(e -> loadStats());
        panel.add(refreshBtn);

        panel.add(Box.createVerticalStrut(12));

        // User tag
        JLabel user = new JLabel("● Admin");
        user.setForeground(GREEN);
        user.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        user.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(user);

        return panel;
    }

    private JLabel buildNavItem(String label, String action, boolean active) {
        JLabel item = new JLabel(label);
        item.setForeground(active ? GREEN : TEXT);
        item.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        item.setBorder(new EmptyBorder(9, 8, 9, 8));
        item.setOpaque(true);
        item.setBackground(active ? new Color(25, 50, 38) : SIDEBAR);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setCursor(action != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        if (action != null) {
            item.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    item.setBackground(new Color(40, 45, 40));
                }
                public void mouseExited(MouseEvent e) {
                    item.setBackground(SIDEBAR);
                }
                public void mouseClicked(MouseEvent e) {
                    openModule(action);
                }
            });
        }

        return item;
    }

    //  MAIN CONTENT
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        main.add(buildHeader(),   BorderLayout.NORTH);
        main.add(buildBody(),     BorderLayout.CENTER);

        return main;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JLabel title = new JLabel("Dashboard");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        lastRefreshLabel = new JLabel("Last updated: —");
        lastRefreshLabel.setForeground(MUTED);
        lastRefreshLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(title,            BorderLayout.WEST);
        panel.add(lastRefreshLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);


        // Section label
        body.add(sectionLabel("OVERVIEW"));
        body.add(Box.createVerticalStrut(10));

        // KPI row
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setBackground(BG);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        productsVal  = new JLabel("—");
        clientsVal   = new JLabel("—");
        agentsVal    = new JLabel("—");
        lowStockVal  = new JLabel("—");

        kpiRow.add(buildKpiCard("Products",        productsVal, "in catalogue",   GREEN));
        kpiRow.add(buildKpiCard("Clients",         clientsVal,  "registered",     GREEN));
        kpiRow.add(buildKpiCard("Delivery Agents", agentsVal,   "active",         GREEN));
        kpiRow.add(buildKpiCard("Low Stock",       lowStockVal, "items to reorder", WARN));

        body.add(kpiRow);
        body.add(Box.createVerticalStrut(24));

        // Section label
        body.add(sectionLabel("LOW STOCK ALERTS"));
        body.add(Box.createVerticalStrut(10));

        // Low stock table
        JTable table = new JTable(lowStockModel);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(60, 65, 60));
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(30, 80, 60));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setBackground(new Color(35, 38, 35));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 60)));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        body.add(scroll);

        // Quick actions row
        body.add(Box.createVerticalStrut(20));
        body.add(sectionLabel("QUICK ACTIONS"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildQuickActions());

        return body;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, String subtitle, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setForeground(MUTED);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setForeground(TEXT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setForeground(accent);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);

        return card;
    }

    private JPanel buildQuickActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setBackground(BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(actionButton("+ Add Product",        () -> openModule("products")));
        panel.add(actionButton("+ Add Client",         () -> openModule("clients")));
        panel.add(actionButton("+ Add Agent",          () -> openModule("agents")));
        panel.add(actionButton("View Inventory",       () -> openModule("inventory")));
        panel.add(actionButton("Send Notifications",   () -> openModule("email")));

        return panel;
    }

    private JButton actionButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(35, 38, 35));
        btn.setForeground(GREEN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(21, 166, 117, 80), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(25, 50, 38)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(35, 38, 35)); }
        });

        return btn;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(MUTED);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    //  DATA LOADING
    private void loadStats() {
        try {
            int products = productDAO.getAllProducts().size();
            int clients  = clientDAO.getAllClients().size();
            int agents   = deliveryAgentDAO.getAllAgents().size();

            List<Inventory> lowStock = inventoryDAO.getLowStockItems();
            int lowCount = lowStock.size();

            productsVal.setText(String.valueOf(products));
            clientsVal.setText(String.valueOf(clients));
            agentsVal.setText(String.valueOf(agents));
            lowStockVal.setText(String.valueOf(lowCount));

            // Colour the low stock card red if there are actual alerts
            lowStockVal.setForeground(lowCount > 0 ? DANGER : GREEN);

            // Refresh low stock table
            lowStockModel.setRowCount(0);
            if (lowStock.isEmpty()) {
                lowStockModel.addRow(new Object[]{"—", "—", "—", "All stock levels OK"});
            } else {
                for (Inventory i : lowStock) {
                    lowStockModel.addRow(new Object[]{
                            i.getInventoryId(),
                            i.getProductId(),
                            i.getQuantity(),
                            i.getReorderLevel()
                    });
                }
            }

            // Timestamp
            String ts = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm:ss"));
            lastRefreshLabel.setText("Last updated: " + ts);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load stats: " + e.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  NAVIGATION — opens each module as a standalone JFrame
    private void openModule(String module) {
        SwingUtilities.invokeLater(() -> {
            switch (module) {
                case "products"  -> new ProductCatalogueUI().setVisible(true);
                case "clients"   -> new ClientUI().setVisible(true);
                case "inventory" -> new InventoryUI().setVisible(true);
                case "agents"    -> new DeliveryAgentUI().setVisible(true);
                // case "email"  -> new ClientDispatchEmailPanel().setVisible(true); // uncomment after merge
                case "email"     -> JOptionPane.showMessageDialog(this,
                        "Email Notifications module coming after Thilini's PR is merged.",
                        "Not yet available", JOptionPane.INFORMATION_MESSAGE);
                default          -> {}
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DashboardUI::new);
    }
}