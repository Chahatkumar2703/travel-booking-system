package view;

import model.User;
import service.AuthenticationException;
import service.AuthService;
import service.DatabaseException;
import service.ValidationException;
import util.SessionManager;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for updating user account password securely.
 */
public class ChangePasswordDialog extends JDialog {

    private final AuthService authService;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePasswordDialog(Frame parent) {
        super(parent, "Change Account Password", true);
        this.authService = new AuthService();

        setSize(420, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel("CHANGE PASSWORD", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridLayout(3, 2, 8, 12));
        formCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        formCard.add(new JLabel("Current Password:"));
        oldPasswordField = UITheme.createPasswordField(15);
        formCard.add(oldPasswordField);

        formCard.add(new JLabel("New Password (6+ chars):"));
        newPasswordField = UITheme.createPasswordField(15);
        formCard.add(newPasswordField);

        formCard.add(new JLabel("Confirm New Password:"));
        confirmPasswordField = UITheme.createPasswordField(15);
        formCard.add(confirmPasswordField);

        add(formCard, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton saveBtn = UITheme.createPrimaryButton("Update Password");
        saveBtn.addActionListener(e -> handleChangePassword());

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        bottomBar.add(saveBtn);
        bottomBar.add(cancelBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void handleChangePassword() {
        String oldPass = new String(oldPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Session expired.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        try {
            boolean success = authService.changePassword(currentUser.getId(), oldPass, newPass, confirmPass);
            if (success) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Could not update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException | AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Notice", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
