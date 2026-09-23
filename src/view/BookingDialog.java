package view;

import model.Booking;
import model.Hotel;
import model.TravelPackage;
import model.User;
import service.BookingService;
import service.DatabaseException;
import service.HotelService;
import service.ValidationException;
import util.SessionManager;
import util.UITheme;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Booking form dialog that allows selecting travelers, travel date,
 * optional hotel upgrade, and live-calculates total fare.
 */
public class BookingDialog extends JDialog {

    private final TravelPackage travelPackage;
    private final BookingService bookingService;
    private final HotelService hotelService;
    private final Runnable onBookingCompleted;

    private JTextField userField;
    private JTextField packageField;
    private JTextField destinationField;
    private JTextField dateField;
    private JSpinner personsSpinner;
    private JComboBox<HotelOption> hotelCombo;
    private JTextArea requestsArea;

    private JLabel packageCostLbl;
    private JLabel hotelCostLbl;
    private JLabel totalAmountLbl;

    public BookingDialog(Frame parent, TravelPackage travelPackage, Runnable onBookingCompleted) {
        super(parent, "Book Travel Package: " + travelPackage.getPackageName(), true);
        this.travelPackage = travelPackage;
        this.bookingService = new BookingService();
        this.hotelService = new HotelService();
        this.onBookingCompleted = onBookingCompleted;

        setSize(650, 720);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
        loadHotels();
        recalculateTotals();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titleLbl = new JLabel("RESERVATION DETAILS", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl);

        JLabel subLbl = new JLabel(travelPackage.getPackageName() + " • " + travelPackage.getDurationSummary(), JLabel.CENTER);
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(UITheme.PRIMARY_LIGHT);
        headerPanel.add(subLbl);

        add(headerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(15, 25, 15, 25));
        formContainer.setBackground(UITheme.BG_MAIN);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        User currentUser = SessionManager.getCurrentUser();
        String userName = currentUser != null ? currentUser.getFullName() : "Guest User";

        // Row 0: User Name & Destination
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formCard.add(new JLabel("Traveler Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        userField = UITheme.createTextField(20);
        userField.setText(userName);
        userField.setEditable(false);
        userField.setBackground(new Color(245, 245, 245));
        formCard.add(userField, gbc);

        // Row 1: Package Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formCard.add(new JLabel("Selected Package:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        packageField = UITheme.createTextField(20);
        packageField.setText(travelPackage.getPackageName() + " (" + UITheme.formatCurrency(travelPackage.getPricePerPerson()) + "/person)");
        packageField.setEditable(false);
        packageField.setBackground(new Color(245, 245, 245));
        formCard.add(packageField, gbc);

        // Row 2: Destination
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        formCard.add(new JLabel("Destination:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        destinationField = UITheme.createTextField(20);
        destinationField.setText(travelPackage.getDestinationName());
        destinationField.setEditable(false);
        destinationField.setBackground(new Color(245, 245, 245));
        formCard.add(destinationField, gbc);

        // Row 3: Travel Date
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.2;
        formCard.add(new JLabel("Travel Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        dateField = UITheme.createTextField(15);
        // Default travel date: 14 days from today
        dateField.setText(LocalDate.now().plusDays(14).toString());
        formCard.add(dateField, gbc);

        // Row 4: Number of Persons
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.2;
        formCard.add(new JLabel("Number of Persons:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        personsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
        personsSpinner.setFont(UITheme.FONT_BOLD);
        personsSpinner.addChangeListener(e -> recalculateTotals());
        formCard.add(personsSpinner, gbc);

        // Row 5: Hotel Selection
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.2;
        formCard.add(new JLabel("Hotel Accommodation:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        hotelCombo = new JComboBox<>();
        hotelCombo.setFont(UITheme.FONT_REGULAR);
        hotelCombo.addActionListener(e -> recalculateTotals());
        formCard.add(hotelCombo, gbc);

        // Row 6: Special Requests
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.2;
        formCard.add(new JLabel("Special Requests:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        requestsArea = new JTextArea(3, 20);
        requestsArea.setFont(UITheme.FONT_REGULAR);
        requestsArea.setLineWrap(true);
        requestsArea.setWrapStyleWord(true);
        formCard.add(new JScrollPane(requestsArea), gbc);

        formContainer.add(formCard);
        formContainer.add(Box.createVerticalStrut(15));

        // Cost Calculation Summary Card
        JPanel calcCard = UITheme.createCardPanel();
        calcCard.setLayout(new GridLayout(3, 2, 10, 6));
        calcCard.setBackground(UITheme.PRIMARY_LIGHT);

        JLabel pkgCostTitle = new JLabel("Package Cost (Price × Persons):");
        pkgCostTitle.setFont(UITheme.FONT_REGULAR);
        packageCostLbl = new JLabel("₹ 0.00", JLabel.RIGHT);
        packageCostLbl.setFont(UITheme.FONT_BOLD);

        JLabel htlCostTitle = new JLabel("Hotel Cost (Rate × Nights):");
        htlCostTitle.setFont(UITheme.FONT_REGULAR);
        hotelCostLbl = new JLabel("₹ 0.00", JLabel.RIGHT);
        hotelCostLbl.setFont(UITheme.FONT_BOLD);

        JLabel totalTitle = new JLabel("Total Fare Payable:");
        totalTitle.setFont(UITheme.FONT_HEADER);
        totalTitle.setForeground(UITheme.PRIMARY_DARK);

        totalAmountLbl = new JLabel("₹ 0.00", JLabel.RIGHT);
        totalAmountLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalAmountLbl.setForeground(UITheme.ACCENT);

        calcCard.add(pkgCostTitle);
        calcCard.add(packageCostLbl);
        calcCard.add(htlCostTitle);
        calcCard.add(hotelCostLbl);
        calcCard.add(totalTitle);
        calcCard.add(totalAmountLbl);

        formContainer.add(calcCard);

        add(formContainer, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton proceedBtn = UITheme.createAccentButton("Confirm & Proceed to Payment");
        proceedBtn.addActionListener(e -> handleProceedToPayment());

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        bottomBar.add(proceedBtn);
        bottomBar.add(cancelBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadHotels() {
        hotelCombo.removeAllItems();
        // Option 1: Standard included hotel
        hotelCombo.addItem(new HotelOption(null, "Standard Stay (Included in Package - ₹0 extra)"));

        try {
            List<Hotel> hotels = hotelService.getHotelsByDestination(travelPackage.getDestinationId());
            for (Hotel h : hotels) {
                if ("ACTIVE".equalsIgnoreCase(h.getStatus()) && h.getAvailableRooms() > 0) {
                    hotelCombo.addItem(new HotelOption(h, h.getHotelName() + " [" + h.getRoomType() + "] (₹" + h.getPricePerNight() + "/night)"));
                }
            }
        } catch (DatabaseException e) {
            System.err.println("Could not load hotels for package: " + e.getMessage());
        }
    }

    private void recalculateTotals() {
        int persons = (Integer) personsSpinner.getValue();
        HotelOption selectedOpt = (HotelOption) hotelCombo.getSelectedItem();
        Hotel hotel = selectedOpt != null ? selectedOpt.hotel : null;

        double[] costs = bookingService.calculateCost(travelPackage, hotel, persons);
        packageCostLbl.setText(UITheme.formatCurrency(costs[0]));
        hotelCostLbl.setText(UITheme.formatCurrency(costs[1]));
        totalAmountLbl.setText(UITheme.formatCurrency(costs[2]));
    }

    private void handleProceedToPayment() {
        String travelDateStr = dateField.getText().trim();
        int persons = (Integer) personsSpinner.getValue();
        String requests = requestsArea.getText().trim();
        HotelOption opt = (HotelOption) hotelCombo.getSelectedItem();
        Integer hotelId = (opt != null && opt.hotel != null) ? opt.hotel.getId() : null;

        if (!ValidationUtil.isValidTravelDate(travelDateStr)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid travel date! Date cannot be in the past and must follow format YYYY-MM-DD.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User currentUser = SessionManager.getCurrentUser();
            int userId = currentUser != null ? currentUser.getId() : 2;

            Booking booking = bookingService.createBooking(
                    userId,
                    travelPackage.getId(),
                    hotelId,
                    travelDateStr,
                    persons,
                    requests
            );

            // Hide booking dialog
            setVisible(false);

            // Open Simulated Payment Dialog
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            PaymentDialog payDialog = new PaymentDialog(parentFrame, booking, () -> {
                dispose();
                if (onBookingCompleted != null) {
                    onBookingCompleted.run();
                }
            });
            payDialog.setVisible(true);

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database failure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class HotelOption {
        final Hotel hotel;
        final String label;

        HotelOption(Hotel hotel, String label) {
            this.hotel = hotel;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
