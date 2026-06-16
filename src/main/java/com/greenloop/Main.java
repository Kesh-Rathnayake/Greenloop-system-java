package com.greenloop;

import com.greenloop.ui.DashboardUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to load theme");
        }
        SwingUtilities.invokeLater(DashboardUI::new);

    }
}

