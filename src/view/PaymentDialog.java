package view;

import model.Booking;
import model.Payment;
import service.DatabaseException;
import service.PaymentResult;
import service.PaymentService;
import service.ValidationException;
import util.SessionManager;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Interactive simulated payment gateway supporting UPI, Card, Net Banking, and Cash on Arrival.
 */
public class PaymentDialog extends JDialog {

    private final Booking booking;
    private final PaymentService paymentService;
    private final Runnable onSuccessCallback;

    private JRadioButton upiRadio;
    private JRadioButton cardRadio;
    private JRadioButton netBankingRadio;
    private JRadioButton cashRadio;

    private JPanel methodCardPanel;
    private CardLayout methodCardLayout;

    // UPI fields
    private JTextField upiIdField;

    // Card fields
    private JTextField cardNumberField;
    private JTextField cardExpiryField;
    private JPasswordField cardCvvField;
    private JTextField cardHolderField;

    // Net Banking fields
    private JComboBox<String> bankCombo;

    private JButton payBtn;
    private JProgressBar progressBar;
    private JLabel statusLbl;

    public PaymentDialog(Frame parent, Booking booking, Runnable onSuccessCallback) {
        super(parent, "Simulated Secure Payment Gateway", true);
        this.booking = booking;
        this.paymentService = new PaymentService();
        this.onSuccessCallback = onSuccessCallback;

        setSize(580, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
    }

    private void initComponents() {
        // Top Summary Banner
        JPanel topBanner = new JPanel(new GridLayout(2, 1, 4, 4));
        topBanner.setBackground(UITheme.PRIMARY);
        topBanner.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titleLbl = new JLabel("SECURE CHECKOUT", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        topBanner.add(titleLbl);

        JLabel amountLbl = new JLabel("Payable Amount: " + UITheme.formatCurrency(booking.getTotalAmount()), JLabel.CENTER);
        amountLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLbl.setForeground(UITheme.PRIMARY_LIGHT);
        topBanner.add(amountLbl);

        add(topBanner, BorderLayout.NORTH);

        // Center Content: Method Select + Form Cards
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        centerPanel.setBackground(UITheme.BG_MAIN);

        // Booking Quick Overview
        JPanel overviewCard = UITheme.createCardPanel();
        overviewCard.setLayout(new GridLayout(2, 2, 8, 4));
        overviewCard.add(new JLabel("Booking ID: " + booking.getBookingCode()));
        overviewCard.add(new JLabel("Guests: " + booking.getPersons() + " Persons"));
        overviewCard.add(new JLabel("Package: " + booking.getPackageName()));
        overviewCard.add(new JLabel("Destination: " + booking.getDestinationName()));
        centerPanel.add(overviewCard);
        centerPanel.add(Box.createVerticalStrut(15));

        // Method Selector
        JLabel selectMethodLbl = new JLabel("Select Payment Method:");
        selectMethodLbl.setFont(UITheme.FONT_SUBTITLE);
        selectMethodLbl.setForeground(UITheme.TEXT_DARK);
        centerPanel.add(selectMethodLbl);
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel methodRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        methodRadioPanel.setBackground(UITheme.BG_MAIN);

        upiRadio = new JRadioButton("UPI (GPay/PhonePe)", true);
        cardRadio = new JRadioButton("Debit / Credit Card");
        netBankingRadio = new JRadioButton("Net Banking");
        cashRadio = new JRadioButton("Pay On Arrival");

        Font rFont = UITheme.FONT_BOLD;
        upiRadio.setFont(rFont);
        cardRadio.setFont(rFont);
        netBankingRadio.setFont(rFont);
        cashRadio.setFont(rFont);

        ButtonGroup group = new ButtonGroup();
        group.add(upiRadio);
        group.add(cardRadio);
        group.add(netBankingRadio);
        group.add(cashRadio);

        methodRadioPanel.add(upiRadio);
        methodRadioPanel.add(cardRadio);
        methodRadioPanel.add(netBankingRadio);
        methodRadioPanel.add(cashRadio);
        centerPanel.add(methodRadioPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Card Panel for Details
        methodCardLayout = new CardLayout();
        methodCardPanel = UITheme.createCardPanel();
        methodCardPanel.setLayout(methodCardLayout);

        // 1. UPI View
        JPanel upiPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        upiPanel.setBackground(Color.WHITE);
        upiPanel.add(new JLabel("Enter Virtual Payment Address (UPI ID):"));
        upiIdField = UITheme.createTextField(20);
        upiIdField.setText("customer@okhdfcbank");
        upiPanel.add(upiIdField);
        JLabel upiNote = new JLabel("Supports Google Pay, PhonePe, Paytm, BHIM");
        upiNote.setFont(UITheme.FONT_SMALL);
        upiNote.setForeground(UITheme.TEXT_MUTED);
        upiPanel.add(upiNote);
        methodCardPanel.add(upiPanel, "UPI");

        // 2. Card View
        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.add(new JLabel("Card Number (16 digits):"));
        cardNumberField = UITheme.createTextField(16);
        cardNumberField.setText("4532112233445566");
        cardPanel.add(cardNumberField);

        cardPanel.add(new JLabel("Cardholder Name:"));
        cardHolderField = UITheme.createTextField(20);
        cardHolderField.setText(booking.getUserName() != null ? booking.getUserName() : "Traveler");
        cardPanel.add(cardHolderField);

        cardPanel.add(new JLabel("Expiry Date (MM/YY):"));
        cardExpiryField = UITheme.createTextField(5);
        cardExpiryField.setText("08/28");
        cardPanel.add(cardExpiryField);

        cardPanel.add(new JLabel("CVV (3 digits):"));
        cardCvvField = UITheme.createPasswordField(3);
        cardCvvField.setText("789");
        cardPanel.add(cardCvvField);
        methodCardPanel.add(cardPanel, "CARD");

        // 3. Net Banking View
        JPanel nbPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        nbPanel.setBackground(Color.WHITE);
        nbPanel.add(new JLabel("Select Your Bank:"));
        bankCombo = new JComboBox<>(new String[]{
                "State Bank of India (SBI)",
                "HDFC Bank",
                "ICICI Bank",
                "Axis Bank",
                "Punjab National Bank (PNB)",
                "Bank of Baroda",
                "Kotak Mahindra Bank"
        });
        bankCombo.setFont(UITheme.FONT_REGULAR);
        nbPanel.add(bankCombo);
        JLabel nbNote = new JLabel("You will be redirected to the simulated bank authentication portal.");
        nbNote.setFont(UITheme.FONT_SMALL);
        nbNote.setForeground(UITheme.TEXT_MUTED);
        nbPanel.add(nbNote);
        methodCardPanel.add(nbPanel, "NET_BANKING");

        // 4. Cash / Pay Later View
        JPanel cashPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        cashPanel.setBackground(Color.WHITE);
        JLabel cashMsg = new JLabel("<html><b>Pay at Destination / On Arrival</b><br>You can pay the full amount via Cash or Card upon arrival at the destination hotel desk.</html>");
        cashMsg.setFont(UITheme.FONT_REGULAR);
        cashPanel.add(cashMsg);
        JLabel cashStatus = new JLabel("Status will remain PENDING until payment is verified upon check-in.");
        cashStatus.setFont(UITheme.FONT_SMALL);
        cashStatus.setForeground(UITheme.TEXT_MUTED);
        cashPanel.add(cashStatus);
        methodCardPanel.add(cashPanel, "CASH");

        centerPanel.add(methodCardPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Listeners for Switching Tabs
        upiRadio.addActionListener(e -> methodCardLayout.show(methodCardPanel, "UPI"));
        cardRadio.addActionListener(e -> methodCardLayout.show(methodCardPanel, "CARD"));
        netBankingRadio.addActionListener(e -> methodCardLayout.show(methodCardPanel, "NET_BANKING"));
        cashRadio.addActionListener(e -> methodCardLayout.show(methodCardPanel, "CASH"));

        // Progress bar for simulated delay
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 10));
        centerPanel.add(progressBar);

        statusLbl = new JLabel(" ", JLabel.CENTER);
        statusLbl.setFont(UITheme.FONT_BOLD);
        statusLbl.setForeground(UITheme.PRIMARY);
        centerPanel.add(statusLbl);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        bottomBar.setBackground(UITheme.BG_MAIN);

        payBtn = UITheme.createAccentButton("Authorize & Pay " + UITheme.formatCurrency(booking.getTotalAmount()));
        payBtn.addActionListener(e -> processSimulatedPayment());

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        bottomBar.add(payBtn);
        bottomBar.add(cancelBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void processSimulatedPayment() {
        String method;
        String details;

        if (upiRadio.isSelected()) {
            method = "UPI";
            details = upiIdField.getText().trim();
        } else if (cardRadio.isSelected()) {
            method = "CARD";
            String cardNum = cardNumberField.getText().trim();
            String exp = cardExpiryField.getText().trim();
            String cvv = new String(cardCvvField.getPassword()).trim();
            String holder = cardHolderField.getText().trim();
            details = cardNum + "|" + exp + "|" + cvv + "|" + holder;
        } else if (netBankingRadio.isSelected()) {
            method = "NET_BANKING";
            details = (String) bankCombo.getSelectedItem();
        } else {
            method = "CASH";
            details = "Pay on Arrival";
        }

        payBtn.setEnabled(false);
        progressBar.setVisible(true);
        statusLbl.setText("Contacting payment gateway... Please wait");

        // Simulated background processing delay (1.2 seconds)
        Timer timer = new Timer(1200, evt -> {
            ((Timer) evt.getSource()).stop();
            try {
                int userId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : booking.getUserId();
                PaymentResult result = paymentService.executePayment(
                        booking.getId(),
                        userId,
                        booking.getTotalAmount(),
                        method,
                        details
                );

                progressBar.setVisible(false);
                statusLbl.setText(result.getMessage());

                // Fetch newly recorded payment
                Payment payment = paymentService.getPaymentByBookingId(booking.getId());

                JOptionPane.showMessageDialog(this,
                        "Payment Completed Successfully!\n" +
                        "Transaction Code: " + result.getTransactionCode() + "\n" +
                        "Payment Status: " + result.getStatus(),
                        "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

                dispose();

                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }

                // Open Confirmation Receipt
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(PaymentDialog.this);
                new ReceiptDialog(parentFrame, booking, payment).setVisible(true);

            } catch (ValidationException ex) {
                progressBar.setVisible(false);
                payBtn.setEnabled(true);
                statusLbl.setText("Validation error");
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Validation Failed", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException ex) {
                progressBar.setVisible(false);
                payBtn.setEnabled(true);
                statusLbl.setText("Payment failed");
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
