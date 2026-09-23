package view;

import model.User;
import service.AuthenticationException;
import service.AuthService;
import service.DatabaseException;
import util.SessionManager;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Split-Panel Login Screen supporting User and Admin credentials.
 */
public class LoginFrame extends BaseFrame {

    private final AuthService authService;
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        super("VoyageQuest Travel - Login", 860, 560);
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(1, 2));

        // 1. LEFT BRANDING PANEL
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.PRIMARY);
        brandPanel.setBorder(new EmptyBorder(40, 35, 40, 35));

        JLabel logoLbl = new JLabel("✈ VOYAGEQUEST");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLbl.setForeground(Color.WHITE);
        logoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandPanel.add(logoLbl);

        JLabel tagLbl = new JLabel("Online Travel & Tourism Booking System");
        tagLbl.setFont(UITheme.FONT_SUBTITLE);
        tagLbl.setForeground(UITheme.PRIMARY_LIGHT);
        tagLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandPanel.add(tagLbl);

        brandPanel.add(Box.createVerticalStrut(30));

        String[] highlights = {
                "✔ Discover 8+ Iconic Indian Destinations",
                "✔ Custom Tour Packages & Transparent Pricing",
                "✔ Luxury Hotels & Instant Room Availability",
                "✔ Real-Time Simulated Payments (UPI/Card/NetBanking)",
                "✔ Printable Reservation Receipts & History",
                "✔ Comprehensive Admin Control Center"
        };

        for (String h : highlights) {
            JLabel hLbl = new JLabel(h);
            hLbl.setFont(UITheme.FONT_REGULAR);
            hLbl.setForeground(Color.WHITE);
            hLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            brandPanel.add(hLbl);
            brandPanel.add(Box.createVerticalStrut(10));
        }

        brandPanel.add(Box.createVerticalGlue());

        // Quick Demo Fill Bar for Presentation
        JLabel demoTitle = new JLabel("Quick Viva Presentation Shortcuts:");
        demoTitle.setFont(UITheme.FONT_SMALL);
        demoTitle.setForeground(UITheme.PRIMARY_LIGHT);
        demoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandPanel.add(demoTitle);
        brandPanel.add(Box.createVerticalStrut(6));

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        demoBtns.setBackground(UITheme.PRIMARY);
        demoBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton adminPrefill = new JButton("Fill Admin");
        adminPrefill.setFont(UITheme.FONT_SMALL);
        adminPrefill.setBackground(Color.WHITE);
        adminPrefill.setFocusPainted(false);
        adminPrefill.addActionListener(e -> {
            emailField.setText("admin@travel.com");
            passwordField.setText("admin123");
        });

        JButton userPrefill = new JButton("Fill User");
        userPrefill.setFont(UITheme.FONT_SMALL);
        userPrefill.setBackground(Color.WHITE);
        userPrefill.setFocusPainted(false);
        userPrefill.addActionListener(e -> {
            emailField.setText("priya@example.com");
            passwordField.setText("user123");
        });

        demoBtns.add(adminPrefill);
        demoBtns.add(userPrefill);
        brandPanel.add(demoBtns);

        add(brandPanel);

        // 2. RIGHT LOGIN FORM PANEL
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(UITheme.BG_MAIN);
        formContainer.setBorder(new EmptyBorder(45, 40, 40, 40));

        JLabel loginTitle = new JLabel("Welcome Back");
        loginTitle.setFont(UITheme.FONT_TITLE);
        loginTitle.setForeground(UITheme.TEXT_DARK);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(loginTitle);

        JLabel loginSub = new JLabel("Please enter your account credentials to continue");
        loginSub.setFont(UITheme.FONT_REGULAR);
        loginSub.setForeground(UITheme.TEXT_MUTED);
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(loginSub);

        formContainer.add(Box.createVerticalStrut(25));

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridLayout(4, 1, 6, 8));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(new JLabel("Email Address / Username:"));
        emailField = UITheme.createTextField(20);
        formCard.add(emailField);

        formCard.add(new JLabel("Password:"));
        passwordField = UITheme.createPasswordField(20);
        formCard.add(passwordField);

        formContainer.add(formCard);
        formContainer.add(Box.createVerticalStrut(20));

        // Login Button
        JButton loginBtn = UITheme.createPrimaryButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> handleLogin());
        formContainer.add(loginBtn);

        formContainer.add(Box.createVerticalStrut(10));

        // Register Button
        JButton regBtn = UITheme.createSecondaryButton("New to VoyageQuest? Create Account");
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        regBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        formContainer.add(regBtn);

        formContainer.add(Box.createVerticalStrut(10));

        // Exit Button
        JButton exitBtn = new JButton("Exit Application");
        exitBtn.setFont(UITheme.FONT_SMALL);
        exitBtn.setForeground(UITheme.TEXT_MUTED);
        exitBtn.setBorderPainted(false);
        exitBtn.setContentAreaFilled(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitBtn.addActionListener(e -> System.exit(0));
        formContainer.add(exitBtn);

        add(formContainer);

        // Enter key submits login
        getRootPane().setDefaultButton(loginBtn);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(email, password);
            if (user != null) {
                if (user.isAdmin()) {
                    new AdminDashboard().setVisible(true);
                } else {
                    new UserDashboard().setVisible(true);
                }
                dispose();
            }
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Failed", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
