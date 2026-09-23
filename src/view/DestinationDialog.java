package view;

import model.Destination;
import service.DatabaseException;
import service.DestinationService;
import service.ValidationException;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin dialog for adding or editing a Destination.
 */
public class DestinationDialog extends JDialog {

    private final Destination destination; // null if adding
    private final DestinationService destinationService;
    private final Runnable onSaved;

    private JTextField nameField;
    private JTextField stateField;
    private JTextArea descArea;
    private JTextArea attractionsArea;
    private JTextField bestTimeField;

    public DestinationDialog(Frame parent, Destination destination, Runnable onSaved) {
        super(parent, destination == null ? "Add New Destination" : "Edit Destination", true);
        this.destination = destination;
        this.destinationService = new DestinationService();
        this.onSaved = onSaved;

        setSize(500, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG_MAIN);

        initComponents();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(destination == null ? "ADD NEW DESTINATION" : "UPDATE DESTINATION", JLabel.CENTER);
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridLayout(5, 2, 8, 10));
        formCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        formCard.add(new JLabel("Destination Name:"));
        nameField = UITheme.createTextField(20);
        formCard.add(nameField);

        formCard.add(new JLabel("State / Region:"));
        stateField = UITheme.createTextField(20);
        formCard.add(stateField);

        formCard.add(new JLabel("Short Description:"));
        descArea = new JTextArea(2, 20);
        descArea.setLineWrap(true);
        formCard.add(new JScrollPane(descArea));

        formCard.add(new JLabel("Famous Attractions:"));
        attractionsArea = new JTextArea(2, 20);
        attractionsArea.setLineWrap(true);
        formCard.add(new JScrollPane(attractionsArea));

        formCard.add(new JLabel("Best Time to Visit:"));
        bestTimeField = UITheme.createTextField(20);
        formCard.add(bestTimeField);

        if (destination != null) {
            nameField.setText(destination.getName());
            stateField.setText(destination.getState());
            descArea.setText(destination.getDescription());
            attractionsArea.setText(destination.getAttractions());
            bestTimeField.setText(destination.getBestTime());
        }

        add(formCard, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton saveBtn = UITheme.createPrimaryButton("Save Destination");
        saveBtn.addActionListener(e -> saveDestination());

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        bottomBar.add(saveBtn);
        bottomBar.add(cancelBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void saveDestination() {
        String name = nameField.getText().trim();
        String state = stateField.getText().trim();
        String desc = descArea.getText().trim();
        String attractions = attractionsArea.getText().trim();
        String bestTime = bestTimeField.getText().trim();

        try {
            if (destination == null) {
                destinationService.addDestination(name, state, desc, attractions, bestTime);
                JOptionPane.showMessageDialog(this, "Destination added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                destinationService.updateDestination(destination.getId(), name, state, desc, attractions, bestTime);
                JOptionPane.showMessageDialog(this, "Destination updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
            if (onSaved != null) onSaved.run();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
