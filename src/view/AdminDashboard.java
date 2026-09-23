package view;

import model.*;
import service.*;
import util.SessionManager;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Admin Dashboard for system administrators.
 * Features 8 KPI summary metric cards and comprehensive management for
 * Users, Destinations, Packages, Hotels, Bookings, and Payments.
 */
public class AdminDashboard extends BaseFrame {

    private final AdminService adminService;
    private final DestinationService destinationService;
    private final PackageService packageService;
    private final HotelService hotelService;
    private final BookingService bookingService;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    // KPI Labels
    private JLabel totalUsersLbl;
    private JLabel totalDestinationsLbl;
    private JLabel totalPackagesLbl;
    private JLabel totalHotelsLbl;
    private JLabel totalBookingsLbl;
    private JLabel confirmedBookingsLbl;
    private JLabel cancelledBookingsLbl;
    private JLabel totalRevenueLbl;

    // Management Tables
    private JTable usersTable;
    private DefaultTableModel usersModel;
    private List<User> userList;

    private JTable destTable;
    private DefaultTableModel destModel;
    private List<Destination> destList;

    private JTable pkgTable;
    private DefaultTableModel pkgModel;
    private List<TravelPackage> pkgList;

    private JTable hotelTable;
    private DefaultTableModel hotelModel;
    private List<Hotel> hotelList;

    private JTable bookingTable;
    private DefaultTableModel bookingModel;
    private List<Booking> bookingList;

    private JTable paymentTable;
    private DefaultTableModel paymentModel;

    public AdminDashboard() {
        super("VoyageQuest Travel - Administrative Control Center", 1180, 750);
        this.adminService = new AdminService();
        this.destinationService = new DestinationService();
        this.packageService = new PackageService();
        this.hotelService = new HotelService();
        this.bookingService = new BookingService();

        initComponents();
        refreshAllData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // TOP HEADER
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(new Color(24, 32, 44)); // Darker slate navy
        topHeader.setBorder(new EmptyBorder(12, 22, 12, 22));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);
        JLabel logoLbl = new JLabel("🛡 VOYAGEQUEST ADMIN");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLbl.setForeground(Color.WHITE);
        titleBox.add(logoLbl);

        JLabel subLbl = new JLabel("| Operations & Control Management");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(new Color(175, 188, 205));
        titleBox.add(subLbl);
        topHeader.add(titleBox, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Admin Logout");
        logoutBtn.setFont(UITheme.FONT_BOLD);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());
        topHeader.add(logoutBtn, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // LEFT NAVIGATION BAR
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(18, 24, 34));
        sidebar.setPreferredSize(new Dimension(215, getHeight()));
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        String[] navLabels = {
                "📊 Overview & KPIs",
                "👥 Manage Users",
                "📍 Destinations",
                "🎒 Travel Packages",
                "🏨 Hotels & Rooms",
                "📋 All Bookings",
                "💳 Payment Audits"
        };

        String[] navKeys = {
                "OVERVIEW",
                "USERS",
                "DESTINATIONS",
                "PACKAGES",
                "HOTELS",
                "BOOKINGS",
                "PAYMENTS"
        };

        for (int i = 0; i < navLabels.length; i++) {
            final String key = navKeys[i];
            JButton navBtn = new JButton(navLabels[i]);
            navBtn.setFont(UITheme.FONT_BOLD);
            navBtn.setForeground(new Color(225, 230, 240));
            navBtn.setBackground(new Color(18, 24, 34));
            navBtn.setHorizontalAlignment(SwingConstants.LEFT);
            navBtn.setFocusPainted(false);
            navBtn.setBorder(new EmptyBorder(10, 14, 10, 14));
            navBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            navBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            navBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    navBtn.setBackground(UITheme.PRIMARY);
                    navBtn.setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    navBtn.setBackground(new Color(18, 24, 34));
                    navBtn.setForeground(new Color(225, 230, 240));
                }
            });

            navBtn.addActionListener(e -> switchView(key));
            sidebar.add(navBtn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh All Data");
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        refreshBtn.addActionListener(e -> {
            refreshAllData();
            showSuccess("System metrics and records refreshed.");
        });
        sidebar.add(refreshBtn);

        add(sidebar, BorderLayout.WEST);

        // MAIN CONTENT CONTAINER
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        contentPanel.add(buildOverviewPanel(), "OVERVIEW");
        contentPanel.add(buildUsersPanel(), "USERS");
        contentPanel.add(buildDestinationsPanel(), "DESTINATIONS");
        contentPanel.add(buildPackagesPanel(), "PACKAGES");
        contentPanel.add(buildHotelsPanel(), "HOTELS");
        contentPanel.add(buildBookingsPanel(), "BOOKINGS");
        contentPanel.add(buildPaymentsPanel(), "PAYMENTS");

        add(contentPanel, BorderLayout.CENTER);
    }

    private void switchView(String key) {
        cardLayout.show(contentPanel, key);
        if ("OVERVIEW".equals(key)) loadStats();
        else if ("USERS".equals(key)) loadUsers(null);
        else if ("DESTINATIONS".equals(key)) loadDestinations();
        else if ("PACKAGES".equals(key)) loadPackages();
        else if ("HOTELS".equals(key)) loadHotels();
        else if ("BOOKINGS".equals(key)) loadBookings();
        else if ("PAYMENTS".equals(key)) loadPayments();
    }

    // =========================================================================
    // 1. OVERVIEW & 8 KPI METRIC CARDS
    // =========================================================================
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel titleLbl = new JLabel("System Executive Summary");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_DARK);
        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(15));

        // 8 Metric Cards Grid (2 rows x 4 cols)
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
        grid.setBackground(UITheme.BG_MAIN);

        totalUsersLbl = new JLabel("0", JLabel.CENTER);
        totalDestinationsLbl = new JLabel("0", JLabel.CENTER);
        totalPackagesLbl = new JLabel("0", JLabel.CENTER);
        totalHotelsLbl = new JLabel("0", JLabel.CENTER);
        totalBookingsLbl = new JLabel("0", JLabel.CENTER);
        confirmedBookingsLbl = new JLabel("0", JLabel.CENTER);
        cancelledBookingsLbl = new JLabel("0", JLabel.CENTER);
        totalRevenueLbl = new JLabel("₹ 0.00", JLabel.CENTER);

        grid.add(createKpiCard("TOTAL USERS", totalUsersLbl, UITheme.PRIMARY));
        grid.add(createKpiCard("DESTINATIONS", totalDestinationsLbl, new Color(40, 167, 69)));
        grid.add(createKpiCard("PACKAGES", totalPackagesLbl, UITheme.ACCENT));
        grid.add(createKpiCard("PARTNER HOTELS", totalHotelsLbl, new Color(111, 66, 193)));

        grid.add(createKpiCard("TOTAL BOOKINGS", totalBookingsLbl, new Color(23, 162, 184)));
        grid.add(createKpiCard("CONFIRMED TRIPS", confirmedBookingsLbl, new Color(40, 167, 69)));
        grid.add(createKpiCard("CANCELLED TRIPS", cancelledBookingsLbl, UITheme.DANGER));
        grid.add(createKpiCard("TOTAL REVENUE", totalRevenueLbl, new Color(30, 126, 52)));

        panel.add(grid);
        panel.add(Box.createVerticalStrut(25));

        // Quick Actions Shortcut Panel
        JPanel shortcuts = UITheme.createCardPanel();
        shortcuts.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        shortcuts.add(new JLabel("Quick Admin Actions: "));

        JButton addDestBtn = UITheme.createPrimaryButton("+ Add Destination");
        addDestBtn.addActionListener(e -> new DestinationDialog(this, null, this::loadDestinations).setVisible(true));

        JButton addPkgBtn = UITheme.createAccentButton("+ Add Package");
        addPkgBtn.addActionListener(e -> new PackageDialog(this, null, this::loadPackages).setVisible(true));

        JButton addHtlBtn = UITheme.createSecondaryButton("+ Add Hotel");
        addHtlBtn.addActionListener(e -> new HotelDialog(this, null, this::loadHotels).setVisible(true));

        shortcuts.add(addDestBtn);
        shortcuts.add(addPkgBtn);
        shortcuts.add(addHtlBtn);
        panel.add(shortcuts);

        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valLbl, Color color) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridLayout(2, 1, 4, 4));

        JLabel tLbl = new JLabel(title, JLabel.CENTER);
        tLbl.setFont(UITheme.FONT_SMALL);
        tLbl.setForeground(UITheme.TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(color);

        card.add(tLbl);
        card.add(valLbl);
        return card;
    }

    private void loadStats() {
        try {
            AdminStats stats = adminService.getDashboardStats();
            totalUsersLbl.setText(String.valueOf(stats.getTotalUsers()));
            totalDestinationsLbl.setText(String.valueOf(stats.getTotalDestinations()));
            totalPackagesLbl.setText(String.valueOf(stats.getTotalPackages()));
            totalHotelsLbl.setText(String.valueOf(stats.getTotalHotels()));
            totalBookingsLbl.setText(String.valueOf(stats.getTotalBookings()));
            confirmedBookingsLbl.setText(String.valueOf(stats.getConfirmedBookings()));
            cancelledBookingsLbl.setText(String.valueOf(stats.getCancelledBookings()));
            totalRevenueLbl.setText(UITheme.formatCurrency(stats.getTotalRevenue()));
        } catch (DatabaseException e) {
            System.err.println("Error loading admin stats: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. USERS MANAGEMENT
    // =========================================================================
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBackground(UITheme.BG_MAIN);
        searchBar.add(new JLabel("Search Users:"));
        JTextField searchField = UITheme.createTextField(20);
        JButton searchBtn = UITheme.createPrimaryButton("Search");
        JButton resetBtn = UITheme.createSecondaryButton("Show All");

        searchBtn.addActionListener(e -> loadUsers(searchField.getText().trim()));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadUsers(null);
        });

        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(resetBtn);
        panel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"User ID", "Full Name", "Email Address", "Phone Number", "Role", "Account Status", "Registered On"};
        usersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        UITheme.styleTable(usersTable);

        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        // Actions
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton toggleBtn = UITheme.createSecondaryButton("Toggle Active / Disable Account");
        toggleBtn.addActionListener(e -> handleToggleUserStatus());

        bottomBar.add(toggleBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadUsers(String query) {
        usersModel.setRowCount(0);
        try {
            userList = adminService.searchUsers(query);
            for (User u : userList) {
                usersModel.addRow(new Object[]{
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getRole(),
                        u.getStatus(),
                        u.getCreatedAt() != null ? u.getCreatedAt().toString().replace("T", " ") : "N/A"
                });
            }
        } catch (DatabaseException e) {
            showError("Failed to fetch users: " + e.getMessage());
        }
    }

    private void handleToggleUserStatus() {
        int selected = usersTable.getSelectedRow();
        if (selected < 0 || userList == null || selected >= userList.size()) {
            showWarning("Please select a user to toggle status.");
            return;
        }

        User u = userList.get(selected);
        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            showWarning("Cannot deactivate the system administrator account.");
            return;
        }

        String newStatus = "ACTIVE".equalsIgnoreCase(u.getStatus()) ? "DISABLED" : "ACTIVE";
        boolean confirm = confirmAction("Are you sure you want to change user " + u.getFullName() + "'s status to " + newStatus + "?");
        if (!confirm) return;

        try {
            adminService.toggleUserStatus(u.getId(), newStatus);
            showSuccess("User status changed to " + newStatus);
            loadUsers(null);
            loadStats();
        } catch (DatabaseException e) {
            showError(e.getMessage());
        }
    }

    // =========================================================================
    // 3. DESTINATIONS MANAGEMENT
    // =========================================================================
    private JPanel buildDestinationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel titleLbl = new JLabel("Destination Catalog Management");
        titleLbl.setFont(UITheme.FONT_HEADER);
        panel.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "State", "Attractions", "Best Time to Visit"};
        destModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        destTable = new JTable(destModel);
        UITheme.styleTable(destTable);

        panel.add(new JScrollPane(destTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton addBtn = UITheme.createPrimaryButton("+ Add Destination");
        addBtn.addActionListener(e -> new DestinationDialog(this, null, this::loadDestinations).setVisible(true));

        JButton editBtn = UITheme.createSecondaryButton("Edit Selected");
        editBtn.addActionListener(e -> {
            int selected = destTable.getSelectedRow();
            if (selected >= 0 && destList != null && selected < destList.size()) {
                new DestinationDialog(this, destList.get(selected), this::loadDestinations).setVisible(true);
            } else {
                showWarning("Please select a destination to edit.");
            }
        });

        JButton deleteBtn = UITheme.createDangerButton("Delete Selected");
        deleteBtn.addActionListener(e -> handleDeleteDestination());

        bottomBar.add(addBtn);
        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadDestinations() {
        destModel.setRowCount(0);
        try {
            destList = destinationService.getAllDestinations();
            for (Destination d : destList) {
                destModel.addRow(new Object[]{
                        d.getId(), d.getName(), d.getState(), d.getAttractions(), d.getBestTime()
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading destinations: " + e.getMessage());
        }
    }

    private void handleDeleteDestination() {
        int selected = destTable.getSelectedRow();
        if (selected < 0 || destList == null || selected >= destList.size()) {
            showWarning("Please select a destination to delete.");
            return;
        }

        Destination d = destList.get(selected);
        boolean confirm = confirmAction("Delete destination " + d.getName() + "?\nAssociated packages and hotels will also be removed.");
        if (!confirm) return;

        try {
            destinationService.deleteDestination(d.getId());
            showSuccess("Destination deleted.");
            loadDestinations();
            loadStats();
        } catch (DatabaseException e) {
            showError(e.getMessage());
        }
    }

    // =========================================================================
    // 4. PACKAGES MANAGEMENT
    // =========================================================================
    private JPanel buildPackagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel titleLbl = new JLabel("Holiday & Tour Packages Management");
        titleLbl.setFont(UITheme.FONT_HEADER);
        panel.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Package Name", "Destination", "Duration", "Price/Person", "Hotel", "Food", "Transport", "Status"};
        pkgModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        pkgTable = new JTable(pkgModel);
        UITheme.styleTable(pkgTable);

        panel.add(new JScrollPane(pkgTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton addBtn = UITheme.createPrimaryButton("+ Add Package");
        addBtn.addActionListener(e -> new PackageDialog(this, null, this::loadPackages).setVisible(true));

        JButton editBtn = UITheme.createSecondaryButton("Edit Selected");
        editBtn.addActionListener(e -> {
            int selected = pkgTable.getSelectedRow();
            if (selected >= 0 && pkgList != null && selected < pkgList.size()) {
                new PackageDialog(this, pkgList.get(selected), this::loadPackages).setVisible(true);
            } else {
                showWarning("Please select a package to edit.");
            }
        });

        JButton deleteBtn = UITheme.createDangerButton("Delete Selected");
        deleteBtn.addActionListener(e -> handleDeletePackage());

        bottomBar.add(addBtn);
        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadPackages() {
        pkgModel.setRowCount(0);
        try {
            pkgList = packageService.getAllPackages();
            for (TravelPackage p : pkgList) {
                pkgModel.addRow(new Object[]{
                        p.getId(), p.getPackageName(), p.getDestinationName(),
                        p.getDurationSummary(), UITheme.formatCurrency(p.getPricePerPerson()),
                        p.isHotelIncluded() ? "Yes" : "No",
                        p.isFoodIncluded() ? "Yes" : "No",
                        p.isTransportIncluded() ? "Yes" : "No",
                        p.getStatus()
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading packages: " + e.getMessage());
        }
    }

    private void handleDeletePackage() {
        int selected = pkgTable.getSelectedRow();
        if (selected < 0 || pkgList == null || selected >= pkgList.size()) {
            showWarning("Please select a package to delete.");
            return;
        }

        TravelPackage p = pkgList.get(selected);
        boolean confirm = confirmAction("Delete package: " + p.getPackageName() + "?");
        if (!confirm) return;

        try {
            packageService.deletePackage(p.getId());
            showSuccess("Package deleted.");
            loadPackages();
            loadStats();
        } catch (DatabaseException e) {
            showError(e.getMessage());
        }
    }

    // =========================================================================
    // 5. HOTELS MANAGEMENT
    // =========================================================================
    private JPanel buildHotelsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel titleLbl = new JLabel("Hotel Accommodations & Room Availability");
        titleLbl.setFont(UITheme.FONT_HEADER);
        panel.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Hotel Name", "Destination", "Category", "Price / Night", "Available Rooms", "Rating", "Status"};
        hotelModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        hotelTable = new JTable(hotelModel);
        UITheme.styleTable(hotelTable);

        panel.add(new JScrollPane(hotelTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton addBtn = UITheme.createPrimaryButton("+ Add Hotel");
        addBtn.addActionListener(e -> new HotelDialog(this, null, this::loadHotels).setVisible(true));

        JButton editBtn = UITheme.createSecondaryButton("Edit Selected");
        editBtn.addActionListener(e -> {
            int selected = hotelTable.getSelectedRow();
            if (selected >= 0 && hotelList != null && selected < hotelList.size()) {
                new HotelDialog(this, hotelList.get(selected), this::loadHotels).setVisible(true);
            } else {
                showWarning("Please select a hotel to edit.");
            }
        });

        JButton adjustRoomsBtn = UITheme.createSecondaryButton("Update Rooms Count");
        adjustRoomsBtn.addActionListener(e -> handleAdjustRooms());

        JButton deleteBtn = UITheme.createDangerButton("Delete Selected");
        deleteBtn.addActionListener(e -> handleDeleteHotel());

        bottomBar.add(addBtn);
        bottomBar.add(editBtn);
        bottomBar.add(adjustRoomsBtn);
        bottomBar.add(deleteBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadHotels() {
        hotelModel.setRowCount(0);
        try {
            hotelList = hotelService.getAllHotels();
            for (Hotel h : hotelList) {
                hotelModel.addRow(new Object[]{
                        h.getId(), h.getHotelName(), h.getDestinationName(),
                        h.getRoomType(), UITheme.formatCurrency(h.getPricePerNight()),
                        h.getAvailableRooms(), h.getRating() + " ★", h.getStatus()
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading hotels: " + e.getMessage());
        }
    }

    private void handleAdjustRooms() {
        int selected = hotelTable.getSelectedRow();
        if (selected < 0 || hotelList == null || selected >= hotelList.size()) {
            showWarning("Please select a hotel from the table.");
            return;
        }

        Hotel h = hotelList.get(selected);
        String input = JOptionPane.showInputDialog(this,
                "Enter number of rooms to ADD or SUBTRACT for " + h.getHotelName() + " (e.g. 5 or -2):",
                "Adjust Room Availability", JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int delta = Integer.parseInt(input.trim());
            hotelService.updateRoomAvailability(h.getId(), delta);
            showSuccess("Room availability updated.");
            loadHotels();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid integer.");
        } catch (DatabaseException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleDeleteHotel() {
        int selected = hotelTable.getSelectedRow();
        if (selected < 0 || hotelList == null || selected >= hotelList.size()) {
            showWarning("Please select a hotel to delete.");
            return;
        }

        Hotel h = hotelList.get(selected);
        boolean confirm = confirmAction("Delete hotel: " + h.getHotelName() + "?");
        if (!confirm) return;

        try {
            hotelService.deleteHotel(h.getId());
            showSuccess("Hotel deleted.");
            loadHotels();
            loadStats();
        } catch (DatabaseException e) {
            showError(e.getMessage());
        }
    }

    // =========================================================================
    // 6. BOOKINGS MANAGEMENT
    // =========================================================================
    private JPanel buildBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel titleLbl = new JLabel("All System Reservations & Bookings");
        titleLbl.setFont(UITheme.FONT_HEADER);
        panel.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"Booking Code", "Customer", "Package Name", "Destination", "Date", "Guests", "Total Amount", "Status"};
        bookingModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        UITheme.styleTable(bookingTable);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(UITheme.BG_MAIN);

        JButton viewReceiptBtn = UITheme.createPrimaryButton("View Receipt");
        viewReceiptBtn.addActionListener(e -> {
            int selected = bookingTable.getSelectedRow();
            if (selected >= 0 && bookingList != null && selected < bookingList.size()) {
                new ReceiptDialog(this, bookingList.get(selected), null).setVisible(true);
            } else {
                showWarning("Please select a booking to view receipt.");
            }
        });

        JButton updateStatusBtn = UITheme.createSecondaryButton("Update Booking Status");
        updateStatusBtn.addActionListener(e -> handleUpdateBookingStatus());

        bottomBar.add(viewReceiptBtn);
        bottomBar.add(updateStatusBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadBookings() {
        bookingModel.setRowCount(0);
        try {
            bookingList = adminService.getAllBookings();
            for (Booking b : bookingList) {
                bookingModel.addRow(new Object[]{
                        b.getBookingCode(),
                        b.getUserName(),
                        b.getPackageName(),
                        b.getDestinationName(),
                        b.getTravelDate(),
                        b.getPersons(),
                        UITheme.formatCurrency(b.getTotalAmount()),
                        b.getBookingStatus()
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading bookings: " + e.getMessage());
        }
    }

    private void handleUpdateBookingStatus() {
        int selected = bookingTable.getSelectedRow();
        if (selected < 0 || bookingList == null || selected >= bookingList.size()) {
            showWarning("Please select a booking to update.");
            return;
        }

        Booking b = bookingList.get(selected);
        String[] options = {"CONFIRMED", "COMPLETED", "CANCELLED"};
        String selectedStatus = (String) JOptionPane.showInputDialog(this,
                "Select new status for booking " + b.getBookingCode() + ":",
                "Update Status", JOptionPane.QUESTION_MESSAGE, null, options, b.getBookingStatus());

        if (selectedStatus != null && !selectedStatus.equals(b.getBookingStatus())) {
            try {
                if ("CANCELLED".equals(selectedStatus)) {
                    bookingService.cancelBooking(b.getId(), b.getUserId(), true);
                } else {
                    adminService.updateBookingStatus(b.getId(), selectedStatus);
                }
                showSuccess("Booking status updated to " + selectedStatus);
                loadBookings();
                loadStats();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    // =========================================================================
    // 7. PAYMENTS AUDIT
    // =========================================================================
    private JPanel buildPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(UITheme.BG_MAIN);

        JLabel titleLbl = new JLabel("Financial Audit & Payment Transactions Log");
        titleLbl.setFont(UITheme.FONT_HEADER);
        panel.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"Txn Code", "Booking Code", "Customer", "Amount Paid", "Method", "Details", "Status", "Date & Time"};
        paymentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        paymentTable = new JTable(paymentModel);
        UITheme.styleTable(paymentTable);

        panel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadPayments() {
        paymentModel.setRowCount(0);
        try {
            List<Payment> list = adminService.getAllPayments();
            for (Payment p : list) {
                paymentModel.addRow(new Object[]{
                        p.getTransactionCode(),
                        p.getBookingCode(),
                        p.getUserName(),
                        UITheme.formatCurrency(p.getAmount()),
                        p.getPaymentMethod(),
                        p.getPaymentDetails(),
                        p.getPaymentStatus(),
                        p.getCreatedAt() != null ? p.getCreatedAt().toString().replace("T", " ") : "N/A"
                });
            }
        } catch (DatabaseException e) {
            showError("Error loading payments: " + e.getMessage());
        }
    }

    private void refreshAllData() {
        loadStats();
        loadUsers(null);
        loadDestinations();
        loadPackages();
        loadHotels();
        loadBookings();
        loadPayments();
    }

    private void handleLogout() {
        boolean confirm = confirmAction("Are you sure you want to log out of Admin Dashboard?");
        if (confirm) {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
