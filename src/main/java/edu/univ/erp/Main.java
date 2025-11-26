package edu.univ.erp;
// Hello There General Kenobi

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;

import edu.univ.erp.data.DataSourceFactory;

public class Main {

    @SuppressWarnings("UseSpecificCatch")
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ignored) {}

        try {
            DataSourceFactory.getAuthDB().getConnection().close();
            DataSourceFactory.getErpDB().getConnection().close();
        } catch (Exception e) {  // catches Hikari + SQL + Runtime exceptions
            String errorMessage = """
            Cannot connect to database. Please check:
            1. MySQL is running
            2. Databases auth_db and erp_db exist
            3. Credentials are correct

            Error: %s
            """.formatted(e.getMessage());
            JOptionPane.showMessageDialog(null,
                    errorMessage,
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new edu.univ.erp.ui.auth.LoginFrame().setVisible(true));
    }
}
