package com.greenloop.ui;

import com.greenloop.dao.ClientDAO;
import com.greenloop.dao.DeliveryAgentDAO;
import com.greenloop.dao.InventoryDAO;
import com.greenloop.dao.ProductDAO;
import com.greenloop.models.Inventory;
import org.example.View.DeliveryAssignmentUI;
import org.example.View.OrderManagementForm;

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

    private final ProductDAO       productDAO       = new ProductDAO();
    private final ClientDAO        clientDAO        = new ClientDAO();
    private final InventoryDAO     inventoryDAO     = new InventoryDAO();
    private final DeliveryAgentDAO deliveryAgentDAO = new DeliveryAgentDAO();

    private JLabel productsVal, clientsVal, agentsVal, lowStockVal;
    private JLabel lastRefreshLabel;

    private final javax.swing.table.DefaultTableModel lowStockModel =
            new javax.swing.table.DefaultTableModel(
                    new String[]{"Inventory ID", "Product ID", "Qty", "Reorder Level"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

    public DashboardUI() {
        setTitle("GreenLoop - Dashboard");
        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        loadStats();
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(SIDEBAR);
        panel.setPreferredSize(new Dimension(230, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 18, 24, 18));

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
        panel.add(Box.createVerticalStrut(20));

        String[][] navItems = {
                {"Dashboard",          null},
                {"Products",           "products"},
                {"Clients",            "clients"},
                {"Inventory",          "inventory"},
                {"Delivery Agents",    "agents"},
                {"Orders",             "orders"},
                {"Assign Deliveries",  "deliveries"},
                {"Client Email",       "email"},
                {"Agent Email",        "notifyAgents"},
                {"Reports",            "reports"},
        };

        for (String[] item : navItems) {
            boolean active = item[1] == null;
            panel.add(buildNavItem(item[0], item[1], active));
            panel.add(Box.createVerticalStrut(2));
        }

        panel.add(Box.createVerticalGlue());

        JButton refreshBtn = new JButton("↻  Refresh Stats");
        refreshBtn.setBackground(new Color(50, 55, 50));
        refreshBtn.setForeground(GREEN);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        refreshBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshBtn.addActionListener(e -> loadStats());
        panel.add(refreshBtn);

        return panel;
    }

    private JLabel buildNavItem(String label, String action, boolean active) {
        JLabel item = new JLabel(label);
        item.setForeground(active ? GREEN : TEXT);
        item.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        item.setBorder(new EmptyBorder(10, 10, 10, 10));
        item.setOpaque(true);
        item.setBackground(active ? new Color(25, 50, 38) : SIDEBAR);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setAlignmentX(Component.LEFT_ALIGNMENT); // Fixed alignment

        if (action != null) {
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { item.setBackground(new Color(40, 45, 40)); }
                public void mouseExited(MouseEvent e) { item.setBackground(SIDEBAR); }
                public void mouseClicked(MouseEvent e) { openModule(action); }
            });
        }
        return item;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));
        main.add(buildHeader(), BorderLayout.NORTH);
        main.add(buildBody(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        JLabel title = new JLabel("Dashboard");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lastRefreshLabel = new JLabel("Last updated: -");
        lastRefreshLabel.setForeground(MUTED);
        panel.add(title, BorderLayout.WEST);
        panel.add(lastRefreshLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);

        body.add(sectionLabel("OVERVIEW"));
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setBackground(BG);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Fixed alignment

        productsVal = new JLabel("-"); clientsVal = new JLabel("-");
        agentsVal = new JLabel("-"); lowStockVal = new JLabel("-");

        kpiRow.add(buildKpiCard("Products", productsVal, "in catalogue", GREEN));
        kpiRow.add(buildKpiCard("Clients", clientsVal, "registered", GREEN));
        kpiRow.add(buildKpiCard("Delivery Agents", agentsVal, "active", GREEN));
        kpiRow.add(buildKpiCard("Low Stock", lowStockVal, "items to reorder", WARN));
        body.add(kpiRow);

        body.add(Box.createVerticalStrut(24));
        body.add(sectionLabel("LOW STOCK ALERTS"));
        body.add(Box.createVerticalStrut(10));

        JTable table = new JTable(lowStockModel);
        table.setBackground(CARD); table.setForeground(TEXT);
        table.setAlignmentX(Component.LEFT_ALIGNMENT); // Fixed alignment
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(scroll);

        body.add(Box.createVerticalStrut(20));
        body.add(sectionLabel("QUICK ACTIONS"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildQuickActions());
        return body;
    }

    private JPanel buildKpiCard(String title, JLabel val, String sub, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel t = new JLabel(title.toUpperCase()); t.setForeground(MUTED);
        val.setForeground(TEXT); val.setFont(new Font("Segoe UI", Font.BOLD, 30));
        JLabel s = new JLabel(sub); s.setForeground(accent);
        card.add(t); card.add(val); card.add(s);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setBackground(BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); // Fixed alignment
        panel.add(actionButton("+ Product", () -> openModule("products")));
        panel.add(actionButton("+ Client", () -> openModule("clients")));
        panel.add(actionButton("Inventory", () -> openModule("inventory")));
        panel.add(actionButton("Client Email", () -> openModule("email")));
        panel.add(actionButton("Agent Email", () -> openModule("notifyAgents")));
        return panel;
    }

    private JButton actionButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(35, 38, 35));
        btn.setForeground(GREEN);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); // Fixed alignment
        return lbl;
    }

    private void loadStats() {
        try {
            productsVal.setText(String.valueOf(productDAO.getAllProducts().size()));
            clientsVal.setText(String.valueOf(clientDAO.getAllClients().size()));
            agentsVal.setText(String.valueOf(deliveryAgentDAO.getAllAgents().size()));
            List<Inventory> lowStock = inventoryDAO.getLowStockItems();
            lowStockVal.setText(String.valueOf(lowStock.size()));
            lowStockModel.setRowCount(0);
            for (Inventory i : lowStock) lowStockModel.addRow(new Object[]{i.getInventoryId(), i.getProductId(), i.getQuantity(), i.getReorderLevel()});
            lastRefreshLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading stats");
        }
    }

    private void openModule(String module) {
        SwingUtilities.invokeLater(() -> {
            switch (module) {
                case "products"   -> new ProductCatalogueUI().setVisible(true);
                case "clients"    -> new ClientUI().setVisible(true);
                case "inventory"  -> new InventoryUI().setVisible(true);
                case "agents"     -> new DeliveryAgentUI().setVisible(true);
                case "orders"     -> new OrderManagementForm().setVisible(true);
                case "deliveries" -> new DeliveryAssignmentUI().setVisible(true);
                case "email"      -> new ClientDispatchEmailPanel().setVisible(true);
                case "notifyAgents" -> new AgentDispatchEmailPanel().setVisible(true);
                case "reports"    -> new ReportsUI().setVisible(true);
                default           -> {}
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DashboardUI::new);
    }
}