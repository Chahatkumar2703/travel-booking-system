package view;

import model.Destination;
import model.TravelPackage;
import service.DatabaseException;
import service.DestinationService;
import service.PackageService;
import service.ValidationException;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Admin dialog for creating and updating Travel Packages.
 */
public class PackageDialog extends JDialog {

    private final TravelPackage travelPackage;
    private final PackageService packageService;
    private final DestinationService destinationService;
    private final Runnable onSaved;

    private JTextField nameField;
    private JComboBox<DestinationItem> destCombo;
    private JSpinner daysSpinner;
    private JSpinner nightsSpinner;
    private JTextField priceField;
    private JTextField placesField;
    private JCheckBox hotelCheck;
    private JCheckBox foodCheck;
    private JCheckBox transportCheck;
    private JTextArea descArea;
    private JComboBox<String> statusCombo;

    public PackageDialog(Frame parent, TravelPackage travelPackage, Runnable onSaved) {
        super(parent, travelPackage == null ? "Add Travel Package" : "Edit Travel Package", true);
        this.travelPackage = travelPackage;
        this.packageService = new PackageService();
        this.destinationService = new DestinationService();
        this.onSaved = onSaved;

        setSize(560, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
        loadDestinations();

        if (travelPackage != null) {
            populateFields();
        }
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(travelPackage == null ? "ADD TRAVEL PACKAGE" : "EDIT TRAVEL PACKAGE", JLabel.CENTER);
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
        formCard.add(new JLabel("Package Name:"), gbc);
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

        // Row 2: Duration (Days & Nights)
        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Duration (Days / Nights):"), gbc);
        gbc.gridx = 1;
        JPanel durPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        durPanel.setBackground(Color.WHITE);
        daysSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 30, 1));
        nightsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 30, 1));
        durPanel.add(new JLabel("Days:"));
        durPanel.add(daysSpinner);
        durPanel.add(new JLabel("Nights:"));
        durPanel.add(nightsSpinner);
        formCard.add(durPanel, gbc);

        // Row 3: Price
        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Price Per Person (₹):"), gbc);
        gbc.gridx = 1;
        priceField = UITheme.createTextField(15);
        priceField.setText("9999.00");
        formCard.add(priceField, gbc);

        // Row 4: Places Covered
        gbc.gridx = 0; gbc.gridy = 4;
        formCard.add(new JLabel("Places Covered:"), gbc);
        gbc.gridx = 1;
        placesField = UITheme.createTextField(20);
        formCard.add(placesField, gbc);

        // Row 5: Inclusions
        gbc.gridx = 0; gbc.gridy = 5;
        formCard.add(new JLabel("Inclusions:"), gbc);
        gbc.gridx = 1;
        JPanel incPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        incPanel.setBackground(Color.WHITE);
        hotelCheck = new JCheckBox("Hotel", true);
        foodCheck = new JCheckBox("Food", true);
        transportCheck = new JCheckBox("Transport", true);
        incPanel.add(hotelCheck);
        incPanel.add(foodCheck);
        incPanel.add(transportCheck);
        formCard.add(incPanel, gbc);

        // Row 6: Description
        gbc.gridx = 0; gbc.gridy = 6;
        formCard.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        formCard.add(new JScrollPane(descArea), gbc);

        // Row 7: Status
        gbc.gridx = 0; gbc.gridy = 7;
        formCard.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        statusCombo.setFont(UITheme.FONT_REGULAR);
        formCard.add(statusCombo, gbc);

        add(formCard, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton saveBtn = UITheme.createPrimaryButton("Save Package");
        saveBtn.addActionListener(e -> savePackage());

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
        nameField.setText(travelPackage.getPackageName());
        daysSpinner.setValue(travelPackage.getDurationDays());
        nightsSpinner.setValue(travelPackage.getDurationNights());
        priceField.setText(String.valueOf(travelPackage.getPricePerPerson()));
        placesField.setText(travelPackage.getPlacesCovered());
        hotelCheck.setSelected(travelPackage.isHotelIncluded());
        foodCheck.setSelected(travelPackage.isFoodIncluded());
        transportCheck.setSelected(travelPackage.isTransportIncluded());
        descArea.setText(travelPackage.getDescription());
        statusCombo.setSelectedItem(travelPackage.getStatus());

        for (int i = 0; i < destCombo.getItemCount(); i++) {
            if (destCombo.getItemAt(i).id == travelPackage.getDestinationId()) {
                destCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void savePackage() {
        String name = nameField.getText().trim();
        DestinationItem destItem = (DestinationItem) destCombo.getSelectedItem();
        int destId = destItem != null ? destItem.id : 0;
        int days = (Integer) daysSpinner.getValue();
        int nights = (Integer) nightsSpinner.getValue();
        String places = placesField.getText().trim();
        boolean hotelInc = hotelCheck.isSelected();
        boolean foodInc = foodCheck.isSelected();
        boolean transInc = transportCheck.isSelected();
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
            if (travelPackage == null) {
                packageService.addPackage(name, destId, days, nights, price, places, hotelInc, foodInc, transInc, desc);
                JOptionPane.showMessageDialog(this, "Package created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                packageService.updatePackage(travelPackage.getId(), name, destId, days, nights, price, places, hotelInc, foodInc, transInc, desc, status);
                JOptionPane.showMessageDialog(this, "Package updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
