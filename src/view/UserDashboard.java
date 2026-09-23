package view;

import model.Booking;
import model.Destination;
import model.Hotel;
import model.TravelPackage;
import model.User;
import service.AuthService;
import service.BookingService;
import service.DatabaseException;
import service.DestinationService;
import service.HotelService;
import service.PackageService;
import service.ValidationException;
import util.SessionManager;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Modern User Dashboard for travelers.
 * Provides destination discovery, package browsing, hotel checks, booking workflows, and profile management.
 */
public class UserDashboard extends BaseFrame {

    private final User currentUser;
    private final DestinationService destinationService;
    private final PackageService packageService;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final AuthService authService;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Tables
    private JTable destTable;
    private DefaultTableModel destModel;

    private JTable pkgTable;
    private DefaultTableModel pkgModel;
    private List<TravelPackage> currentPackages;

    private JTable hotelTable;
    private DefaultTableModel hotelModel;

    private JTable bookingTable;
    private DefaultTableModel bookingModel;
    private List<Booking> userBookings;

    // Profile fields
    private JTextField profileNameField;
    private JTextField profileEmailField;
    private JTextField profilePhoneField;
    private JLabel totalBookingsLbl;
    private JLabel activeBookingsLbl;
    private JLabel cancelledBookingsLbl;
    private JLabel totalSpentLbl;

    // Filters
    private JComboBox<String> pkgDestFilterCombo;
    private JTextField pkgSearchField;
    private JComboBox<String> hotelDestFilterCombo;

    public UserDashboard() {
        super("VoyageQuest Travel - Customer Portal", 1120, 720);
        this.currentUser = SessionManager.getCurrentUser();
        this.destinationService = new DestinationService();
        this.packageService = new PackageService();
        this.hotelService = new HotelService();
        this.bookingService = new BookingService();
        this.authService = new AuthService();

        initComponents();
        loadAllData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // TOP HEADER
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.PRIMARY);
        topHeader.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBox.setOpaque(false);
        JLabel logoLbl = new JLabel("✈ VOYAGEQUEST");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLbl.setForeground(Color.WHITE);
        titleBox.add(logoLbl);

        JLabel welcomeLbl = new JLabel("| Welcome, " + (currentUser != null ? currentUser.getFullName() : "Traveler") + "!");
        welcomeLbl.setFont(UITheme.FONT_SUBTITLE);
        welcomeLbl.setForeground(UITheme.PRIMARY_LIGHT);
        titleBox.add(welcomeLbl);
        topHeader.add(titleBox, BorderLayout.WEST);

        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userBox.setOpaque(false);
        JLabel dateLbl = new JLabel("Today: " + LocalDate.now());
        dateLbl.setFont(UITheme.FONT_SMALL);
        dateLbl.setForeground(UITheme.PRIMARY_LIGHT);
        userBox.add(dateLbl);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.FONT_BOLD);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());
        userBox.add(logoutBtn);
        topHeader.add(userBox, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // LEFT SIDEBAR
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(210, getHeight()));
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        String[] navItems = {
                "🏠 Overview",
                "📍 Destinations",
                "🎒 Travel Packages",
                "🏨 Hotels",
                "📋 My Bookings",
                "👤 My Profile"
        };

        String[] navKeys = {
                "OVERVIEW",
                "DESTINATIONS",
                "PACKAGES",
                "HOTELS",
                "BOOKINGS",
                "PROFILE"
        };

        for (int i = 0; i < navItems.length; i++) {
            final String key = navKeys[i];
            JButton navBtn = new JButton(navItems[i]);
            navBtn.setFont(UITheme.FONT_BOLD);
            navBtn.setForeground(Color.WHITE);
            navBtn.setBackground(UITheme.PRIMARY_DARK);
            navBtn.setHorizontalAlignment(SwingConstants.LEFT);
            navBtn.setFocusPainted(false);
            navBtn.setBorder(new EmptyBorder(10, 15, 10, 15));
            navBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            navBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            navBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    navBtn.setBackground(UITheme.PRIMARY);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    navBtn.setBackground(UITheme.PRIMARY_DARK);
                }
            });

            navBtn.addActionListener(e -> switchView(key));
            sidebar.add(navBtn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Quick book button in sidebar
        JButton bookDirectBtn = UITheme.createAccentButton("✨ Book a Tour");
        bookDirectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookDirectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bookDirectBtn.addActionListener(e -> switchView("PACKAGES"));
        sidebar.add(bookDirectBtn);

        add(sidebar, BorderLayout.WEST);

        // MAIN CONTENT (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(buildOverviewPanel(), "OVERVIEW");
        contentPanel.add(buildDestinationsPanel(), "DESTINATIONS");
        contentPanel.add(buildPackagesPanel(), "PACKAGES");
        contentPanel.add(buildHotelsPanel(), "HOTELS");
        contentPanel.add(buildBookingsPanel(), "BOOKINGS");
        contentPanel.add(buildProfilePanel(), "PROFILE");

        add(contentPanel, BorderLayout.CENTER);
    }

    private void switchView(String cardName) {
        cardLayout.show(contentPanel, cardName);
        if ("BOOKINGS".equals(cardName)) {
            loadUserBookings();
        } else if ("PROFILE".equals(cardName)) {
            loadProfileStats();
        }
    }

    // =========================================================================
    // 1. OVERVIEW PANEL
    // =========================================================================
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        // Hero Banner
        JPanel hero = UITheme.createCardPanel();
        hero.setLayout(new BorderLayout());
        hero.setBackground(new Color(230, 243, 250));
        hero.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel hTitle = new JLabel("Plan Your Next Unforgettable Journey", JLabel.LEFT);
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        hTitle.setForeground(UITheme.PRIMARY);
        hero.add(hTitle, BorderLayout.NORTH);

        JLabel hSub = new JLabel("Explore mountains, beaches, deserts, and backwaters across India with verified hotels and tailored itineraries.");
        hSub.setFont(UITheme.FONT_REGULAR);
        hSub.setForeground(UITheme.TEXT_DARK);
        hero.add(hSub, BorderLayout.CENTER);

        panel.add(hero);
        panel.add(Box.createVerticalStrut(20));

        // Quick KPI Metric Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setBackground(UITheme.BG_MAIN);

        statsRow.add(createMetricCard("Curated Packages", "12+ Options", "From ₹3,999 to ₹19,999", UITheme.PRIMARY));
        statsRow.add(createMetricCard("Top Destinations", "8 Destinations", "Goa, Kashmir, Kerala & more", new Color(40, 167, 69)));
        statsRow.add(createMetricCard("Partner Hotels", "10+ Verified", "Ratings 4.2+ to 4.9★", UITheme.ACCENT));

        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(25));

        // Featured Shortcut Card
        JPanel featureCard = UITheme.createCardPanel();
        featureCard.setLayout(new BorderLayout(10, 10));

        JLabel fTitle = new JLabel("Popular Holiday Packages Ready to Book");
        fTitle.setFont(UITheme.FONT_HEADER);
        fTitle.setForeground(UITheme.TEXT_DARK);
        featureCard.add(fTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Package Name", "Destination", "Duration", "Price/Person", "Hotel Stay", "Meals"};
        DefaultTableModel featureModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable fTable = new JTable(featureModel);
        UITheme.styleTable(fTable);

        featureCard.add(new JScrollPane(fTable), BorderLayout.CENTER);

        JButton viewAllBtn = UITheme.createPrimaryButton("Browse All Packages & Book Now");
        viewAllBtn.addActionListener(e -> switchView("PACKAGES"));
        featureCard.add(viewAllBtn, BorderLayout.SOUTH);

        panel.add(featureCard);

        // Fill top 4 packages asynchronously
        SwingUtilities.invokeLater(() -> {
            try {
                List<TravelPackage> list = packageService.getAllPackages();
                for (int i = 0; i < Math.min(4, list.size()); i++) {
                    TravelPackage p = list.get(i);
                    featureModel.addRow(new Object[]{
                            p.getId(), p.getPackageName(), p.getDestinationName(),
                            p.getDurationSummary(), UITheme.formatCurrency(p.getPricePerPerson()),
                            p.isHotelIncluded() ? "Included" : "Optional",
                            p.isFoodIncluded() ? "Included" : "Self"
                    });
                }
            } catch (Exception ignored) {}
        });

        return panel;
    }

    private JPanel createMetricCard(String title, String val, String subtitle, Color color) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridLayout(3, 1, 4, 4));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(UITheme.FONT_SUBTITLE);
        tLbl.setForeground(UITheme.TEXT_MUTED);

        JLabel vLbl = new JLabel(val);
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        vLbl.setForeground(color);

        JLabel sLbl = new JLabel(subtitle);
        sLbl.setFont(UITheme.FONT_SMALL);
        sLbl.setForeground(UITheme.TEXT_MUTED);

        card.add(tLbl);
        card.add(vLbl);
        card.add(sLbl);
        return card;
    }

    // =========================================================================
    // 2. DESTINATIONS PANEL
    // =========================================================================
    private JPanel buildDestinationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBackground(UITheme.BG_MAIN);

        JLabel searchLbl = new JLabel("Search Destination:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        JTextField searchField = UITheme.createTextField(22);
        JButton searchBtn = UITheme.createPrimaryButton("Search");
        JButton resetBtn = UITheme.createSecondaryButton("Reset");

        searchBtn.addActionListener(e -> loadDestinations(searchField.getText().trim()));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadDestinations(null);
        });

        searchBar.add(searchLbl);
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(resetBtn);
        panel.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Destination", "State", "Famous Attractions", "Best Time to Visit"};
        destModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        destTable = new JTable(destModel);
        UITheme.styleTable(destTable);
        destTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        destTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        destTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        destTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        destTable.getColumnModel().getColumn(4).setPreferredWidth(140);

        panel.add(new JScrollPane(destTable), BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton viewPackagesBtn = UITheme.createAccentButton("View Packages for Selected Destination");
        viewPackagesBtn.addActionListener(e -> {
            int selectedRow = destTable.getSelectedRow();
            if (selectedRow >= 0) {
                String destName = (String) destModel.getValueAt(selectedRow, 1);
                pkgDestFilterCombo.setSelectedItem(destName);
                switchView("PACKAGES");
            } else {
                JOptionPane.showMessageDialog(this, "Please select a destination from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        bottomBar.add(viewPackagesBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadDestinations(String keyword) {
        destModel.setRowCount(0);
        try {
            List<Destination> list = destinationService.searchDestinations(keyword);
            for (Destination d : list) {
                destModel.addRow(new Object[]{
                        d.getId(), d.getName(), d.getState(), d.getAttractions(), d.getBestTime()
                });
            }
        } catch (DatabaseException e) {
            showError("Failed to load destinations: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. PACKAGES PANEL
    // =========================================================================
    private JPanel buildPackagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(UITheme.BG_MAIN);

        filterBar.add(new JLabel("Search:"));
        pkgSearchField = UITheme.createTextField(15);
        filterBar.add(pkgSearchField);

        filterBar.add(new JLabel("Destination:"));
        pkgDestFilterCombo = new JComboBox<>();
        pkgDestFilterCombo.setFont(UITheme.FONT_REGULAR);
        filterBar.add(pkgDestFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        JButton resetBtn = UITheme.createSecondaryButton("Show All");

        filterBtn.addActionListener(e -> applyPackageFilters());
        resetBtn.addActionListener(e -> {
            pkgSearchField.setText("");
            pkgDestFilterCombo.setSelectedIndex(0);
            applyPackageFilters();
        });

        filterBar.add(filterBtn);
        filterBar.add(resetBtn);
        panel.add(filterBar, BorderLayout.NORTH);

        // Packages Table
        String[] cols = {"ID", "Package Name", "Destination", "Duration", "Price/Person", "Hotel", "Food", "Transport", "Places Covered"};
        pkgModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        pkgTable = new JTable(pkgModel);
        UITheme.styleTable(pkgTable);
        pkgTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        pkgTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        pkgTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        pkgTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        pkgTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        pkgTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        pkgTable.getColumnModel().getColumn(6).setPreferredWidth(50);
        pkgTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        pkgTable.getColumnModel().getColumn(8).setPreferredWidth(200);

        panel.add(new JScrollPane(pkgTable), BorderLayout.CENTER);

        // Action Buttons
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton detailsBtn = UITheme.createSecondaryButton("View Full Package Details");
        detailsBtn.addActionListener(e -> showSelectedPackageDetails());

        JButton bookBtn = UITheme.createAccentButton("Book Selected Package");
        bookBtn.addActionListener(e -> openBookingDialogForSelected());

        bottomBar.add(detailsBtn);
        bottomBar.add(bookBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadPackageDestFilters() {
        pkgDestFilterCombo.removeAllItems();
        pkgDestFilterCombo.addItem("All Destinations");
        try {
            List<Destination> list = destinationService.getAllDestinations();
            for (Destination d : list) {
                pkgDestFilterCombo.addItem(d.getName());
            }
        } catch (Exception ignored) {}
    }

    private void applyPackageFilters() {
        String keyword = pkgSearchField.getText().trim();
        String selectedDest = (String) pkgDestFilterCombo.getSelectedItem();
        Integer destId = null;

        if (selectedDest != null && !"All Destinations".equals(selectedDest)) {
            try {
                List<Destination> dList = destinationService.getAllDestinations();
                for (Destination d : dList) {
                    if (d.getName().equalsIgnoreCase(selectedDest)) {
                        destId = d.getId();
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        pkgModel.setRowCount(0);
        try {
            currentPackages = packageService.searchPackages(keyword, destId, null);
            for (TravelPackage p : currentPackages) {
                pkgModel.addRow(new Object[]{
                        p.getId(), p.getPackageName(), p.getDestinationName(),
                        p.getDurationSummary(), UITheme.formatCurrency(p.getPricePerPerson()),
                        p.isHotelIncluded() ? "Yes" : "No",
                        p.isFoodIncluded() ? "Yes" : "No",
                        p.isTransportIncluded() ? "Yes" : "No",
                        p.getPlacesCovered()
                });
            }
        } catch (DatabaseException e) {
            showError("Error searching packages: " + e.getMessage());
        }
    }

    private void showSelectedPackageDetails() {
        int selected = pkgTable.getSelectedRow();
        if (selected < 0 || currentPackages == null || selected >= currentPackages.size()) {
            showWarning("Please select a package from the table.");
            return;
        }

        TravelPackage p = currentPackages.get(selected);
        String msg = String.format(
                "Package Name: %s\n" +
                "Destination : %s\n" +
                "Duration    : %s\n" +
                "Price/Person: %s\n" +
                "Places      : %s\n\n" +
                "Inclusions  :\n" +
                "- Hotel Stay   : %s\n" +
                "- Daily Meals  : %s\n" +
                "- Transport/Cab: %s\n\n" +
                "Overview:\n%s",
                p.getPackageName(), p.getDestinationName(), p.getDurationSummary(),
                UITheme.formatCurrency(p.getPricePerPerson()), p.getPlacesCovered(),
                p.isHotelIncluded() ? "Included" : "Not Included",
                p.isFoodIncluded() ? "Included" : "Not Included",
                p.isTransportIncluded() ? "Included" : "Not Included",
                p.getDescription()
        );

        JOptionPane.showMessageDialog(this, msg, "Package Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openBookingDialogForSelected() {
        int selected = pkgTable.getSelectedRow();
        if (selected < 0 || currentPackages == null || selected >= currentPackages.size()) {
            showWarning("Please select a package to book.");
            return;
        }

        TravelPackage p = currentPackages.get(selected);
        new BookingDialog(this, p, () -> {
            loadUserBookings();
            switchView("BOOKINGS");
        }).setVisible(true);
    }

    // =========================================================================
    // 4. HOTELS PANEL
    // =========================================================================
    private JPanel buildHotelsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(UITheme.BG_MAIN);

        filterBar.add(new JLabel("Destination:"));
        hotelDestFilterCombo = new JComboBox<>();
        hotelDestFilterCombo.setFont(UITheme.FONT_REGULAR);
        filterBar.add(hotelDestFilterCombo);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        JButton resetBtn = UITheme.createSecondaryButton("Show All");

        filterBtn.addActionListener(e -> applyHotelFilters());
        resetBtn.addActionListener(e -> {
            hotelDestFilterCombo.setSelectedIndex(0);
            applyHotelFilters();
        });

        filterBar.add(filterBtn);
        filterBar.add(resetBtn);
        panel.add(filterBar, BorderLayout.NORTH);

        // Hotel Table
        String[] cols = {"ID", "Hotel Name", "Destination", "Room Category", "Price / Night", "Available Rooms", "Rating", "Address"};
        hotelModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        hotelTable = new JTable(hotelModel);
        UITheme.styleTable(hotelTable);

        panel.add(new JScrollPane(hotelTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadHotelDestFilters() {
        hotelDestFilterCombo.removeAllItems();
        hotelDestFilterCombo.addItem("All Destinations");
        try {
            List<Destination> list = destinationService.getAllDestinations();
            for (Destination d : list) {
                hotelDestFilterCombo.addItem(d.getName());
            }
        } catch (Exception ignored) {}
    }

    private void applyHotelFilters() {
        hotelModel.setRowCount(0);
        String selected = (String) hotelDestFilterCombo.getSelectedItem();
        try {
            List<Hotel> list;
            if (selected == null || "All Destinations".equals(selected)) {
                list = hotelService.getAllHotels();
            } else {
                int destId = 0;
                for (Destination d : destinationService.getAllDestinations()) {
                    if (d.getName().equalsIgnoreCase(selected)) {
                        destId = d.getId();
                        break;
                    }
                }
                list = hotelService.getHotelsByDestination(destId);
            }

            for (Hotel h : list) {
                hotelModel.addRow(new Object[]{
                        h.getId(), h.getHotelName(), h.getDestinationName(),
                        h.getRoomType(), UITheme.formatCurrency(h.getPricePerNight()),
                        h.getAvailableRooms() + " Rooms",
                        h.getRating() + " ★",
                        h.getAddress()
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading hotels: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. MY BOOKINGS PANEL
    // =========================================================================
    private JPanel buildBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        // Header Title
        JLabel titleLbl = new JLabel("My Reservations & Booking History");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.TEXT_DARK);
        panel.add(titleLbl, BorderLayout.NORTH);

        // Table
        String[] cols = {"Booking ID", "Destination", "Package Name", "Travel Date", "Guests", "Total Amount", "Payment Status", "Booking Status"};
        bookingModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        UITheme.styleTable(bookingTable);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        // Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton detailsBtn = UITheme.createPrimaryButton("View Details & Receipt");
        detailsBtn.addActionListener(e -> viewSelectedBookingReceipt());

        JButton cancelBtn = UITheme.createDangerButton("Cancel Booking");
        cancelBtn.addActionListener(e -> handleCancelBooking());

        bottomBar.add(detailsBtn);
        bottomBar.add(cancelBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadUserBookings() {
        if (currentUser == null) return;
        bookingModel.setRowCount(0);
        try {
            userBookings = bookingService.getUserBookings(currentUser.getId());
            for (Booking b : userBookings) {
                bookingModel.addRow(new Object[]{
                        b.getBookingCode(),
                        b.getDestinationName(),
                        b.getPackageName(),
                        b.getTravelDate(),
                        b.getPersons() + " Guests",
                        UITheme.formatCurrency(b.getTotalAmount()),
                        b.getPaymentStatus() != null ? b.getPaymentStatus() : "PAID",
                        b.getBookingStatus()
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading bookings: " + e.getMessage());
        }
    }

    private void viewSelectedBookingReceipt() {
        int selected = bookingTable.getSelectedRow();
        if (selected < 0 || userBookings == null || selected >= userBookings.size()) {
            showWarning("Please select a booking to view its receipt.");
            return;
        }

        Booking b = userBookings.get(selected);
        new ReceiptDialog(this, b, null).setVisible(true);
    }

    private void handleCancelBooking() {
        int selected = bookingTable.getSelectedRow();
        if (selected < 0 || userBookings == null || selected >= userBookings.size()) {
            showWarning("Please select a booking to cancel.");
            return;
        }

        Booking b = userBookings.get(selected);
        if ("CANCELLED".equalsIgnoreCase(b.getBookingStatus())) {
            showWarning("This booking is already cancelled.");
            return;
        }

        boolean confirm = confirmAction("Are you sure you want to cancel booking " + b.getBookingCode() + "?");
        if (!confirm) return;

        try {
            boolean success = bookingService.cancelBooking(b.getId(), currentUser.getId(), false);
            if (success) {
                showSuccess("Booking " + b.getBookingCode() + " has been cancelled.");
                loadUserBookings();
            } else {
                showError("Could not cancel booking.");
            }
        } catch (ValidationException | DatabaseException ex) {
            showError(ex.getMessage());
        }
    }

    // =========================================================================
    // 6. PROFILE PANEL
    // =========================================================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel pTitle = new JLabel("User Profile & Account Statistics");
        pTitle.setFont(UITheme.FONT_TITLE);
        pTitle.setForeground(UITheme.TEXT_DARK);
        panel.add(pTitle);
        panel.add(Box.createVerticalStrut(20));

        // Profile Stats Card
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setBackground(UITheme.BG_MAIN);

        totalBookingsLbl = new JLabel("0", JLabel.CENTER);
        activeBookingsLbl = new JLabel("0", JLabel.CENTER);
        cancelledBookingsLbl = new JLabel("0", JLabel.CENTER);
        totalSpentLbl = new JLabel("₹ 0.00", JLabel.CENTER);

        statsRow.add(createProfileStatBox("Total Bookings", totalBookingsLbl, UITheme.PRIMARY));
        statsRow.add(createProfileStatBox("Active Bookings", activeBookingsLbl, new Color(40, 167, 69)));
        statsRow.add(createProfileStatBox("Cancelled Trips", cancelledBookingsLbl, UITheme.DANGER));
        statsRow.add(createProfileStatBox("Total Spent", totalSpentLbl, UITheme.ACCENT));

        panel.add(statsRow);
        panel.add(Box.createVerticalStrut(25));

        // Edit Profile Card
        JPanel editCard = UITheme.createCardPanel();
        editCard.setLayout(new GridLayout(4, 2, 10, 12));

        editCard.add(new JLabel("Full Name:"));
        profileNameField = UITheme.createTextField(20);
        editCard.add(profileNameField);

        editCard.add(new JLabel("Email Address (Registered):"));
        profileEmailField = UITheme.createTextField(20);
        profileEmailField.setEditable(false);
        profileEmailField.setBackground(new Color(245, 245, 245));
        editCard.add(profileEmailField);

        editCard.add(new JLabel("Mobile Phone Number:"));
        profilePhoneField = UITheme.createTextField(20);
        editCard.add(profilePhoneField);

        JButton saveProfileBtn = UITheme.createPrimaryButton("Save Profile Changes");
        saveProfileBtn.addActionListener(e -> saveProfileChanges());
        editCard.add(saveProfileBtn);

        JButton changePassBtn = UITheme.createSecondaryButton("Change Account Password");
        changePassBtn.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        editCard.add(changePassBtn);

        panel.add(editCard);

        if (currentUser != null) {
            profileNameField.setText(currentUser.getFullName());
            profileEmailField.setText(currentUser.getEmail());
            profilePhoneField.setText(currentUser.getPhone());
        }

        return panel;
    }

    private JPanel createProfileStatBox(String title, JLabel valLbl, Color color) {
        JPanel box = UITheme.createCardPanel();
        box.setLayout(new GridLayout(2, 1, 4, 4));

        JLabel tLbl = new JLabel(title, JLabel.CENTER);
        tLbl.setFont(UITheme.FONT_SMALL);
        tLbl.setForeground(UITheme.TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(color);

        box.add(tLbl);
        box.add(valLbl);
        return box;
    }

    private void loadProfileStats() {
        if (currentUser == null) return;
        try {
            double[] stats = authService.getUserStats(currentUser.getId());
            totalBookingsLbl.setText(String.valueOf((int) stats[0]));
            activeBookingsLbl.setText(String.valueOf((int) stats[1]));
            cancelledBookingsLbl.setText(String.valueOf((int) stats[2]));
            totalSpentLbl.setText(UITheme.formatCurrency(stats[3]));
        } catch (DatabaseException e) {
            System.err.println("Could not load user stats: " + e.getMessage());
        }
    }

    private void saveProfileChanges() {
        if (currentUser == null) return;
        String name = profileNameField.getText().trim();
        String phone = profilePhoneField.getText().trim();

        try {
            boolean success = authService.updateProfile(currentUser.getId(), name, phone);
            if (success) {
                showSuccess("Profile details updated successfully!");
            }
        } catch (ValidationException | DatabaseException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadAllData() {
        loadDestinations(null);
        loadPackageDestFilters();
        applyPackageFilters();
        loadHotelDestFilters();
        applyHotelFilters();
        loadUserBookings();
        loadProfileStats();
    }

    private void handleLogout() {
        boolean confirm = confirmAction("Are you sure you want to log out?");
        if (confirm) {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
