package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.session.UserSession;
import edu.univ.erp.auth.store.AuthStore;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        setTitle("University ERP - Login");
        setSize(400, 300);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout
        setLayout(new MigLayout("wrap 2", "[right][250]", "[]20[]20[]20[]"));

        // Title
        JLabel title = new JLabel("ERP Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, "span 2, align center, gapbottom 20");

        // Username field
        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField, "growx");

        // Password field
        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField, "growx");

        // Login button
        JButton loginButton = new JButton("Login");
        add(new JLabel());
        add(loginButton, "growx");

        // Exit button
        JButton exitButton = new JButton("Exit");
        add(new JLabel());
        add(exitButton, "growx");

        // Event handlers
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