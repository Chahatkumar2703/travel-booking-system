package view;

import model.Destination;
import model.Hotel;
import service.DatabaseException;
import service.DestinationService;
import service.HotelService;
import service.ValidationException;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Admin dialog for adding or editing Hotels and updating room capacity.
 */
public class HotelDialog extends JDialog {

    private final Hotel hotel;
    private final HotelService hotelService;
    private final DestinationService destinationService;
    private final Runnable onSaved;

    private JTextField nameField;
    private JComboBox<DestinationItem> destCombo;
    private JTextField addressField;
    private JComboBox<String> roomTypeCombo;
    private JTextField priceField;
    private JSpinner roomsSpinner;
    private JSpinner ratingSpinner;
    private JTextArea descArea;
    private JComboBox<String> statusCombo;

    public HotelDialog(Frame parent, Hotel hotel, Runnable onSaved) {
        super(parent, hotel == null ? "Add New Hotel" : "Edit Hotel: " + hotel.getHotelName(), true);
        this.hotel = hotel;
        this.hotelService = new HotelService();
        this.destinationService = new DestinationService();
        this.onSaved = onSaved;

        setSize(540, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
        loadDestinations();

        if (hotel != null) {
            populateFields();
        }
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(hotel == null ? "ADD NEW HOTEL" : "UPDATE HOTEL DETAILS", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Row 0: Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formCard.add(new JLabel("Hotel Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        nameField = UITheme.createTextField(20);
        formCard.add(nameField, gbc);

        // Row 1: Destination
        gbc.gridx = 0; gbc.gridy = 1;
        formCard.add(new JLabel("Destination:"), gbc);
        gbc.gridx = 1;
        destCombo = new JComboBox<>();
        destCombo.setFont(UITheme.FONT_REGULAR);
        formCard.add(destCombo, gbc);

        // Row 2: Address
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Address / Area:"), gbc);
        gbc.gridx = 1;
        addressField = UITheme.createTextField(20);
        formCard.add(addressField, gbc);

        // Row 3: Room Type
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Room Category:"), gbc);
        gbc.gridx = 1;
        roomTypeCombo = new JComboBox<>(new String[]{"Deluxe", "Standard AC", "Luxury Suite", "Royal Suite", "Cottage"});
        roomTypeCombo.setFont(UITheme.FONT_REGULAR);
        formCard.add(roomTypeCombo, gbc);

        // Row 4: Price
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Price Per Night (₹):"), gbc);
        gbc.gridx = 1;
        priceField = UITheme.createTextField(15);
        priceField.setText("2500.00");
        formCard.add(priceField, gbc);

        // Row 5: Rooms Available
        gbc.gridx = 0; gbc.gridy = 5;
        formCard.add(new JLabel("Available Rooms:"), gbc);
        gbc.gridx = 1;
        roomsSpinner = new JSpinner(new SpinnerNumberModel(15, 0, 500, 1));
        roomsSpinner.setFont(UITheme.FONT_REGULAR);
        formCard.add(roomsSpinner, gbc);

        // Row 6: Rating
        gbc.gridx = 0; gbc.gridy = 6;
        formCard.add(new JLabel("Guest Rating (1.0 to 5.0):"), gbc);
        gbc.gridx = 1;
        ratingSpinner = new JSpinner(new SpinnerNumberModel(4.5, 1.0, 5.0, 0.1));
        ratingSpinner.setFont(UITheme.FONT_REGULAR);
        formCard.add(ratingSpinner, gbc);

        // Row 7: Description
        gbc.gridx = 0; gbc.gridy = 7;
        formCard.add(new JLabel("Description / Amenities:"), gbc);
        gbc.gridx = 1;
        descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        formCard.add(new JScrollPane(descArea), gbc);

        // Row 8: Status
        gbc.gridx = 0; gbc.gridy = 8;
        formCard.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        statusCombo.setFont(UITheme.FONT_REGULAR);
        formCard.add(statusCombo, gbc);

        add(formCard, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton saveBtn = UITheme.createPrimaryButton("Save Hotel");
        saveBtn.addActionListener(e -> saveHotel());

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        bottomBar.add(saveBtn);
        bottomBar.add(cancelBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadDestinations() {
        destCombo.removeAllItems();
        try {
            List<Destination> list = destinationService.getAllDestinations();
            for (Destination d : list) {
                destCombo.addItem(new DestinationItem(d.getId(), d.getName() + ", " + d.getState()));
            }
        } catch (DatabaseException e) {
            System.err.println("Could not load destinations: " + e.getMessage());
        }
    }

    private void populateFields() {
        nameField.setText(hotel.getHotelName());
        addressField.setText(hotel.getAddress());
        roomTypeCombo.setSelectedItem(hotel.getRoomType());
        priceField.setText(String.valueOf(hotel.getPricePerNight()));
        roomsSpinner.setValue(hotel.getAvailableRooms());
        ratingSpinner.setValue(hotel.getRating());
        descArea.setText(hotel.getDescription());
        statusCombo.setSelectedItem(hotel.getStatus());

        for (int i = 0; i < destCombo.getItemCount(); i++) {
            if (destCombo.getItemAt(i).id == hotel.getDestinationId()) {
                destCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveHotel() {
        String name = nameField.getText().trim();
        DestinationItem destItem = (DestinationItem) destCombo.getSelectedItem();
        int destId = destItem != null ? destItem.id : 0;
        String address = addressField.getText().trim();
        String roomType = (String) roomTypeCombo.getSelectedItem();
        int rooms = (Integer) roomsSpinner.getValue();
        double rating = ((Double) ratingSpinner.getValue());
        String desc = descArea.getText().trim();
        String status = (String) statusCombo.getSelectedItem();

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (hotel == null) {
                hotelService.addHotel(name, destId, address, roomType, price, rooms, rating, desc);
                JOptionPane.showMessageDialog(this, "Hotel added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                hotelService.updateHotel(hotel.getId(), name, destId, address, roomType, price, rooms, rating, desc, status);
                JOptionPane.showMessageDialog(this, "Hotel updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
            if (onSaved != null) onSaved.run();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class DestinationItem {
        final int id;
        final String label;

        DestinationItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
