package edu.univ.erp.ui.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import edu.univ.erp.api.auth.AuthAPI;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.ui.auth.LoginFrame;


public class NavigationBar extends JPanel {

    private final JLabel maintenanceLabel;
    private final JLabel userInfoLabel;
    private final JButton logoutBtn;
    private Timer refreshTimer;

    public NavigationBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Left panel - App title and maintenance banner
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("University ERP System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        leftPanel.add(titleLabel);

        maintenanceLabel = new JLabel();
        maintenanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        leftPanel.add(maintenanceLabel);

        add(leftPanel, BorderLayout.WEST);

        // Right panel - User info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);

        userInfoLabel = new JLabel();
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        updateUserInfo();
        rightPanel.add(userInfoLabel);

        logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutBtn.addActionListener(e -> performLogout());
        rightPanel.add(logoutBtn);

        // Change Password button (optional - add if needed)
        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.setFocusPainted(false);
        changePasswordBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        changePasswordBtn.addActionListener(e -> showChangePasswordDialog());
        rightPanel.add(changePasswordBtn);

        add(rightPanel, BorderLayout.EAST);

        // Initialize maintenance banner
        updateMaintenanceBanner();

        // Start auto-refresh timer (every 5 seconds)
        startAutoRefresh();
    }

    /**
     * Update maintenance banner dynamically
     * This can be called manually or by the timer
     */
    public final void updateMaintenanceBanner() {
        boolean isMaintenance = edu.univ.erp.api.maintenance.MaintenanceAPI.isReadOnly();
        if (isMaintenance) {
            maintenanceLabel.setText("⚠ MAINTENANCE MODE: View-only");
            maintenanceLabel.setForeground(Color.RED);
            maintenanceLabel.setBackground(new Color(255, 200, 200));
            maintenanceLabel.setOpaque(true);
            maintenanceLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.RED, 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        } else {
            maintenanceLabel.setText("");
            maintenanceLabel.setOpaque(false);
            maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }
        maintenanceLabel.revalidate();
        maintenanceLabel.repaint();
    }

    /**
     * Update user information display
     */
    private void updateUserInfo() {
        String role = UserSession.getRole();
        int userId = UserSession.getUserId();
        if (role != null) {
            userInfoLabel.setText(String.format("Logged in as: %s (ID: %d)",
                    role.toUpperCase(), userId));
        }
    }

    /**
     * Start a timer to auto-refresh maintenance status every 5 seconds
     * This ensures the banner updates even if changed from another session/admin
     */
    private void startAutoRefresh() {
        refreshTimer = new Timer(5000, e -> updateMaintenanceBanner());
        refreshTimer.start();
    }

    /**
     * Stop the refresh timer (call when window is closing)
     */
    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Perform logout action
     */
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Stop the refresh timer
        stopAutoRefresh();

        // Call logout API (clears session)
        AuthAPI.logout();

        // Dispose current window
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }

        // Open login frame
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private void showChangePasswordDialog() {
        JPasswordField oldPass = new JPasswordField();
        JPasswordField newPass1 = new JPasswordField();
        JPasswordField newPass2 = new JPasswordField();

        Object[] message = {
                "Old Password:", oldPass,
                "New Password:", newPass1,
                "Confirm New:", newPass2
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                "Change Password", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String oldPw = new String(oldPass.getPassword());
            String newPw1 = new String(newPass1.getPassword());
            String newPw2 = new String(newPass2.getPassword());

            if (!newPw1.equals(newPw2)) {
                JOptionPane.showMessageDialog(this, "Passwords don't match");
                return;
            }

            APIResponse<Void> r = AuthAPI.changePassword(oldPw, newPw1);
            JOptionPane.showMessageDialog(this, r.message);
        }
    }

    /**
     * Manual refresh method that can be called from outside
     * Useful after admin toggles maintenance mode
     */
    public void forceRefresh() {
        updateMaintenanceBanner();
    }
}