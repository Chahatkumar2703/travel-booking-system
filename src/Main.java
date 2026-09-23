import util.DatabaseInitializer;
import view.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for the Online Travel Booking System.
 */
public class Main {

    public static void main(String[] args) {
        // Set modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {
            }
        }

        // Initialize database tables and sample records if needed
        System.out.println("Initializing Travel Booking System...");
        DatabaseInitializer.initializeDatabase();

        // Launch Login window on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
