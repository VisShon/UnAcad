package edu.univ.erp.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

import edu.univ.erp.api.auth.AuthAPI;
import edu.univ.erp.api.common.APIResponse;
import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.ui.auth.LoginFrame;

public class SideBar extends JPanel {

    private final List<JButton> buttons;
    private final List<String> keys;
    private final BiConsumer<String, JButton> tabHandler;
    private final JButton logoutButton;
    private final JButton changePasswordButton;

    public SideBar(
        List<String[]> entries,
        BiConsumer<String, JButton> tabHandler,
        Runnable maintenanceToggleHandler,
        boolean includeMaintenanceButton
    ) {
        this.tabHandler = tabHandler;
        this.buttons = new java.util.ArrayList<>();
        this.keys = new java.util.ArrayList<>();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(180, 0));
        setBackground(UIComponents.SIDEBAR_BG);
        setBorder(
            BorderFactory.createMatteBorder(
                1, 0, 1, 1, 
                UIComponents.SIDEBAR_BORDER
            )
        );

        for (String[] entry : entries) {
            String key = entry[0];
            String label = entry[1];
            JButton button = UIComponents.emojiButton(label);
            
            buttons.add(button);
            keys.add(key);
            add(button);

            button.addActionListener(e -> switchTab(key, button));
        }

        if (includeMaintenanceButton) {
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setForeground(UIComponents.SIDEBAR_BORDER);

            add(sep);

            JButton maintenanceButton = UIComponents.emojiButton("🛠️  Maintenance");
            maintenanceButton.setBackground(UIComponents.SIDEBAR_INACTIVE_BG);
            maintenanceButton.setForeground(UIComponents.SIDEBAR_INACTIVE_FG);
            add(maintenanceButton);

            maintenanceButton.addActionListener(e -> maintenanceToggleHandler.run());
        }

        if (!buttons.isEmpty()) {
            highlight(buttons.get(0));
            tabHandler.accept(keys.get(0), buttons.get(0));
        }

        add(Box.createVerticalGlue());

        logoutButton = UIComponents.primaryButton(
            "Logout", 
            new Color(220, 53, 69)
        );

        changePasswordButton = UIComponents.primaryButton(
            "Password", 
            UIComponents.SECONDARY_BG
        );

        Dimension maxSize = new Dimension(
            Integer.MAX_VALUE, 
            logoutButton.getPreferredSize().height
        );
        
        logoutButton.setMaximumSize(maxSize);
        changePasswordButton.setMaximumSize(maxSize);

        add(logoutButton);
        add(Box.createVerticalStrut(10));
        add(changePasswordButton);

        logoutButton.addActionListener(e -> performLogout());
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());

    }

    private void switchTab(String key, JButton button) {
        highlight(button);
        tabHandler.accept(key, button);
    }

    private void highlight(JButton button) {
        for (JButton b : buttons) {
            if (b == button) {
                b.setBackground(UIComponents.SIDEBAR_ACTIVE_BG);
                b.setForeground(UIComponents.SIDEBAR_ACTIVE_FG);
            } else {
                b.setBackground(UIComponents.SIDEBAR_INACTIVE_BG);
                b.setForeground(UIComponents.SIDEBAR_INACTIVE_FG);
            }
        }
    }

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

            APIResponse r = AuthAPI.changePassword(oldPw, newPw1);
            JOptionPane.showMessageDialog(this, r.message);
        }
    }
}
