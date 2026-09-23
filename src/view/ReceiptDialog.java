package view;

import model.Booking;
import model.Payment;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Printable and exportable booking confirmation receipt dialog.
 */
public class ReceiptDialog extends JDialog {

    private final Booking booking;
    private final Payment payment;
    private JTextArea receiptArea;

    public ReceiptDialog(Frame parent, Booking booking, Payment payment) {
        super(parent, "Booking Confirmation & Receipt", true);
        this.booking = booking;
        this.payment = payment;

        setSize(550, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
    }

    private void initComponents() {
        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("BOOKING CONFIRMATION RECEIPT", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.CENTER);

        JLabel subLbl = new JLabel("Official Travel Reservation Voucher", JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.PRIMARY_LIGHT);
        headerPanel.add(subLbl, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Receipt Content Box
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setBorder(new EmptyBorder(15, 20, 15, 20));
        receiptArea.setText(generateReceiptText());

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons Bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.BG_MAIN);

        JButton printBtn = UITheme.createPrimaryButton("Print Receipt");
        printBtn.addActionListener(e -> printReceipt());

        JButton saveBtn = UITheme.createAccentButton("Save Receipt to File");
        saveBtn.addActionListener(e -> saveReceiptToFile());

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(printBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("             VOYAGEQUEST TRAVEL BOOKINGS                \n");
        sb.append("         Certified Tour Operator & Travel Agency         \n");
        sb.append("========================================================\n\n");

        sb.append(String.format("BOOKING REFERENCE  : %s\n", booking.getBookingCode()));
        sb.append(String.format("BOOKING STATUS     : %s\n", booking.getBookingStatus()));
        if (booking.getCreatedAt() != null) {
            sb.append(String.format("BOOKED ON          : %s\n", booking.getCreatedAt().toString().replace("T", " ")));
        }
        sb.append("--------------------------------------------------------\n");
        sb.append("CUSTOMER INFORMATION\n");
        sb.append(String.format("Name               : %s\n", booking.getUserName()));
        if (booking.getUserEmail() != null) {
            sb.append(String.format("Email              : %s\n", booking.getUserEmail()));
        }
        sb.append("--------------------------------------------------------\n");
        sb.append("ITINERARY DETAILS\n");
        sb.append(String.format("Destination        : %s\n", booking.getDestinationName()));
        sb.append(String.format("Package Name       : %s\n", booking.getPackageName()));
        sb.append(String.format("Departure Date     : %s\n", booking.getTravelDate()));
        sb.append(String.format("Number of Guests   : %d Persons\n", booking.getPersons()));
        sb.append(String.format("Hotel Accommodated : %s\n", booking.getHotelName() != null ? booking.getHotelName() : "Standard Package Inclusions"));
        if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty()) {
            sb.append(String.format("Special Requests   : %s\n", booking.getSpecialRequests()));
        }
        sb.append("--------------------------------------------------------\n");
        sb.append("FARE BREAKDOWN\n");
        sb.append(String.format("Package Base Cost  : %s\n", UITheme.formatCurrency(booking.getPackageCost())));
        sb.append(String.format("Hotel Stay Cost    : %s\n", UITheme.formatCurrency(booking.getHotelCost())));
        sb.append(String.format("Taxes & Service    : %s\n", "Included"));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("TOTAL FARE PAID    : %s\n", UITheme.formatCurrency(booking.getTotalAmount())));
        sb.append("--------------------------------------------------------\n");
        sb.append("PAYMENT INFORMATION\n");
        if (payment != null) {
            sb.append(String.format("Transaction Code   : %s\n", payment.getTransactionCode()));
            sb.append(String.format("Payment Method     : %s\n", payment.getPaymentMethod()));
            sb.append(String.format("Payment Status     : %s\n", payment.getPaymentStatus()));
            sb.append(String.format("Payment Details    : %s\n", payment.getPaymentDetails()));
        } else {
            sb.append(String.format("Payment Status     : %s\n", booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "CONFIRMED"));
        }
        sb.append("========================================================\n");
        sb.append("  Thank you for choosing VoyageQuest! Have a safe trip. \n");
        sb.append("  Helpline: +91 1800-VOYAGE | support@travelbooking.com \n");
        sb.append("========================================================\n");
        return sb.toString();
    }

    private void printReceipt() {
        try {
            boolean done = receiptArea.print();
            if (done) {
                JOptionPane.showMessageDialog(this, "Receipt sent to printer.", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Printing error: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveReceiptToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Receipt-" + booking.getBookingCode() + ".txt"));
        int option = chooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(receiptArea.getText());
                JOptionPane.showMessageDialog(this, "Receipt saved successfully to: " + file.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
