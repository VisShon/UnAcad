package edu.univ.erp.ui.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.ui.components.UIComponents;

public class NavigationBar extends JPanel {

    private final JLabel maintenanceLabel;

    public NavigationBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 60));
        setBackground(UIComponents.PRIMARY_BG);

        // --- Left panel ---
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        leftPanel.setOpaque(false);

        JLabel titleLabel = UIComponents.titleLabel("UnAcad", Color.WHITE);
        leftPanel.add(titleLabel);

        maintenanceLabel = UIComponents.maintenanceLabel();
        leftPanel.add(maintenanceLabel);

        add(leftPanel, BorderLayout.WEST);

        // --- Right panel ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 12));
        rightPanel.setOpaque(false);

        String role = UserSession.getRole();
        int id = UserSession.getUserId();

        String bubbleLetter = role != null && !role.isEmpty() ? role.substring(0, 1).toUpperCase() : "?";
        String bubbleId = id >= 0 ? String.valueOf(id) : "?";

        JLabel profileBubble = UIComponents.profileBubble(bubbleLetter + bubbleId, getRoleColor(role));
        rightPanel.add(profileBubble);

        add(rightPanel, BorderLayout.EAST);

        // Initialize banner (empty if not in maintenance)
        updateMaintenanceBanner();
    }

    private Color getRoleColor(String role) {
        return switch (role) {
            case "ADMIN" -> new Color(128, 0, 128);
            case "INSTRUCTOR" -> new Color(255, 193, 7);
            case "STUDENT" -> new Color(76, 175, 80);
            default -> new Color(100, 100, 100);
        };
    }

    /**
     * Call this manually when maintenance mode may have changed
     */
    public void updateMaintenanceBanner() {
        boolean isMaintenance = edu.univ.erp.api.maintenance.MaintenanceAPI.isReadOnly();

        if (isMaintenance) {
            maintenanceLabel.setText("⚠ MAINTENANCE MODE: View-only");
            maintenanceLabel.setForeground(Color.RED);
            maintenanceLabel.setBackground(new Color(255, 200, 200));
            maintenanceLabel.setOpaque(true);
            Border border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.RED, 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            );
            maintenanceLabel.setBorder(border);
        } else {
            maintenanceLabel.setText("");
            maintenanceLabel.setOpaque(false);
            maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        maintenanceLabel.revalidate();
        maintenanceLabel.repaint();
    }

    /**
     * Shortcut to force a manual refresh from outside
     */
    public void forceRefresh() {
        updateMaintenanceBanner();
    }
}
