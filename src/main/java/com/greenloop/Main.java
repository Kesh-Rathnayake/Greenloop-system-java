package com.greenloop;

import com.greenloop.ui.ClientDispatchEmailPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientDispatchEmailPanel panel = new ClientDispatchEmailPanel();
            panel.setVisible(true);
        });
    }
}
