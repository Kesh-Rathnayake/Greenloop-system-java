package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import org.example.View.DeliveryAssignmentUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // flatlaf dark theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // open UI in Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            DeliveryAssignmentUI frame = new DeliveryAssignmentUI();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("GreenLoop App");
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}