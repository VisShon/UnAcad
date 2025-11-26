package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.auth.store.AuthStore;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;
import edu.univ.erp.ui.components.UIComponents;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        setTitle("University ERP - Login");
        setSize(450, 650);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel wrapper = new JPanel(
            new MigLayout(
                "fill", 
                "push[]push", 
                "push[]push"
            )
        );

        wrapper.setBackground(UIComponents.SIDEBAR_BG);
        add(wrapper);

        JPanel card = UIComponents.cardPanel();
        card.setLayout(
            new MigLayout(
                "wrap 1", 
                "[300]", 
                "20[]20[]10[]10[]20[]10[]"
            )
        );

        wrapper.add(card);

        card.add(
            UIComponents.titleLabel("ERP Login", new Color(40, 40, 40)), 
            "align center, gapbottom 10"
        );

        usernameField = UIComponents.textField();
        card.add(
            UIComponents.labeledField(
                "Username", 
                usernameField
            ), 
            "growx"
        );

        passwordField = UIComponents.passwordField();
        card.add(
            UIComponents.labeledField(
                "Password", 
                passwordField
            ), 
            "growx"
        );

        JButton loginButton = UIComponents.primaryButton(
            "Login", 
            UIComponents.PRIMARY_BG
        );

        card.add(
            loginButton, 
            "growx, gaptop 15"
        );

        JButton exitButton = UIComponents.primaryButton(
            "Exit",
            UIComponents.SECONDARY_BG
        );
        card.add(
            exitButton, 
            "growx"
        );

        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());// Press Enter
        exitButton.addActionListener(e -> System.exit(0));

    }

    /**
     * Handles Login Button / Enter Key
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Fields",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Authenticate via DB
        AuthStore.AuthResult result = AuthStore.login(username, password);

        if (result == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Store session information
        UserSession.createSession(result.userId, result.role);

        JOptionPane.showMessageDialog(this,
                "Login successful! Logged in as: " + result.role);

        // Close login window
        this.dispose();

        // Redirect user based on role
        switch (result.role.toUpperCase()) {
            case "ADMIN" -> new AdminDashboard().setVisible(true);
            case "STUDENT" -> new StudentDashboard().setVisible(true);
            case "INSTRUCTOR" -> new InstructorDashboard().setVisible(true);
            default -> JOptionPane.showMessageDialog(null,
                    "Unknown user role: " + result.role);
        }
    }
}