package com.greenloop;

import com.greenloop.ui.DashboardUI;
import com.greenloop.ui.ProductCatalogueUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DashboardUI::new);
    }
}
