package view;

import model.User;
import service.AuthService;
import service.DatabaseException;
import service.ValidationException;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration window for new customers.
 */
public class RegisterFrame extends BaseFrame {

    private final AuthService authService;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passField;
    private JPasswordField confirmPassField;

    public RegisterFrame() {
        super("VoyageQuest - Create New Account", 520, 680);
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel titleLbl = new JLabel("CREATE AN ACCOUNT", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl);

        JLabel subLbl = new JLabel("Join VoyageQuest & Explore Beautiful Destinations", JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(UITheme.PRIMARY_LIGHT);
        headerPanel.add(subLbl);

        add(headerPanel, BorderLayout.NORTH);

        // Form Container
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(20, 35, 20, 35));
        bodyPanel.setBackground(UITheme.BG_MAIN);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridLayout(10, 1, 4, 6));

        card.add(new JLabel("Full Name:"));
        nameField = UITheme.createTextField(20);
        card.add(nameField);

        card.add(new JLabel("Email Address:"));
        emailField = UITheme.createTextField(20);
        card.add(emailField);

        card.add(new JLabel("Mobile Phone Number (10 digits):"));
        phoneField = UITheme.createTextField(20);
        card.add(phoneField);

        card.add(new JLabel("Password (min 6 characters):"));
        passField = UITheme.createPasswordField(20);
        card.add(passField);

        card.add(new JLabel("Confirm Password:"));
        confirmPassField = UITheme.createPasswordField(20);
        card.add(confirmPassField);

        bodyPanel.add(card);
        bodyPanel.add(Box.createVerticalStrut(15));

        // Action Buttons
        JButton registerBtn = UITheme.createPrimaryButton("Register Account");
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerBtn.addActionListener(e -> handleRegister());
        bodyPanel.add(registerBtn);

        bodyPanel.add(Box.createVerticalStrut(10));

        JButton backBtn = UITheme.createSecondaryButton("Already Have an Account? Sign In");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        bodyPanel.add(backBtn);

        add(bodyPanel, BorderLayout.CENTER);
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passField.getPassword());
        String confirmPassword = new String(confirmPassField.getPassword());

        try {
            User registered = authService.register(name, email, phone, password, confirmPassword);
            JOptionPane.showMessageDialog(this,
                    "Registration Successful! Welcome, " + registered.getFullName() + ".\nPlease login with your credentials.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            new LoginFrame().setVisible(true);
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
