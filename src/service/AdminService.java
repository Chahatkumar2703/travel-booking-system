package service;

import dao.*;
import model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service aggregating administrative reports, listing approvals, user moderation, and financial logs.
 */
public class AdminService {

    private final PaymentDAO paymentDAO;
    private final UserDAO userDAO;
    private final BookingDAO bookingDAO;
    private final FlightDAO flightDAO;
    private final HotelDAO hotelDAO;
    private final CarDAO carDAO;
    private final PackageDAO packageDAO;
    private final DestinationDAO destinationDAO;

    public AdminService() {
        this.paymentDAO = new PaymentDAO();
        this.userDAO = new UserDAO();
        this.bookingDAO = new BookingDAO();
        this.flightDAO = new FlightDAO();
        this.hotelDAO = new HotelDAO();
        this.carDAO = new CarDAO();
        this.packageDAO = new PackageDAO();
        this.destinationDAO = new DestinationDAO();
    }

    public AdminStats getDashboardStats() throws DatabaseException {
        try {
            AdminStats stats = paymentDAO.getAdminStats();

            List<User> users = userDAO.findAll();
            int travelers = 0;
            int agents = 0;
            for (User u : users) {
                if (u.isAgent()) agents++;
                else if (!u.isAdmin()) travelers++;
            }
            stats.setTotalTravelers(travelers);
            stats.setTotalAgents(agents);

            List<Flight> flights = flightDAO.findAll();
            List<Car> cars = carDAO.findAll();
            List<Hotel> hotels = hotelDAO.findAll();
            List<TravelPackage> pkgs = packageDAO.findAll();

            stats.setTotalFlights(flights.size());
            stats.setTotalCars(cars.size());
            stats.setTotalHotels(hotels.size());
            stats.setTotalPackages(pkgs.size());

            int pending = 0;
            for (Flight f : flights) if ("PENDING".equalsIgnoreCase(f.getApprovalStatus())) pending++;
            for (Car c : cars) if ("PENDING".equalsIgnoreCase(c.getApprovalStatus())) pending++;
            for (Hotel h : hotels) if ("PENDING".equalsIgnoreCase(h.getApprovalStatus())) pending++;
            for (TravelPackage p : pkgs) if ("PENDING".equalsIgnoreCase(p.getApprovalStatus())) pending++;
            stats.setPendingApprovals(pending);

            List<Booking> bookings = bookingDAO.findAll();
            int flightB = 0, hotelB = 0, carB = 0, pkgB = 0;
            for (Booking b : bookings) {
                String type = b.getBookingType();
                if ("FLIGHT".equalsIgnoreCase(type)) flightB++;
                else if ("HOTEL".equalsIgnoreCase(type)) hotelB++;
                else if ("CAR".equalsIgnoreCase(type)) carB++;
                else pkgB++;
            }
            stats.setFlightBookings(flightB);
            stats.setHotelBookings(hotelB);
            stats.setCarBookings(carB);
            stats.setPackageBookings(pkgB);

            return stats;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load dashboard metrics: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAllListings() throws DatabaseException {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            for (Flight f : flightDAO.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", f.getId());
                m.put("type", "FLIGHT");
                m.put("name", f.getAirline() + " " + f.getFlightNumber() + " (" + f.getOrigin() + " → " + f.getDestination() + ")");
                m.put("agentId", f.getAgentId());
                m.put("agentName", f.getAgentName() != null ? f.getAgentName() : "Agent #" + f.getAgentId());
                m.put("price", f.getPrice());
                m.put("status", f.getStatus());
                m.put("approvalStatus", f.getApprovalStatus());
                list.add(m);
            }
            for (Hotel h : hotelDAO.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", h.getId());
                m.put("type", "HOTEL");
                m.put("name", h.getHotelName() + " (" + h.getRoomType() + ")");
                m.put("agentId", h.getAgentId());
                m.put("agentName", h.getAgentName() != null ? h.getAgentName() : "Agent #" + h.getAgentId());
                m.put("price", h.getPricePerNight());
                m.put("status", h.getStatus());
                m.put("approvalStatus", h.getApprovalStatus());
                list.add(m);
            }
            for (Car c : carDAO.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("type", "CAR");
                m.put("name", c.getCarName() + " (" + c.getLocation() + ")");
                m.put("agentId", c.getAgentId());
                m.put("agentName", c.getAgentName() != null ? c.getAgentName() : "Agent #" + c.getAgentId());
                m.put("price", c.getPricePerDay());
                m.put("status", c.getStatus());
                m.put("approvalStatus", c.getApprovalStatus());
                list.add(m);
            }
            for (TravelPackage p : packageDAO.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("type", "PACKAGE");
                m.put("name", p.getPackageName());
                m.put("agentId", p.getAgentId());
                m.put("agentName", p.getAgentName() != null ? p.getAgentName() : "Agent #" + p.getAgentId());
                m.put("price", p.getPricePerPerson());
                m.put("status", p.getStatus());
                m.put("approvalStatus", p.getApprovalStatus());
                list.add(m);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch listings: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean approveListing(String type, int id) throws DatabaseException {
        try {
            if ("FLIGHT".equalsIgnoreCase(type)) {
                return flightDAO.updateApprovalStatus(id, "APPROVED");
            } else if ("HOTEL".equalsIgnoreCase(type)) {
                return hotelDAO.updateApprovalStatus(id, "APPROVED");
            } else if ("CAR".equalsIgnoreCase(type)) {
                return carDAO.updateApprovalStatus(id, "APPROVED");
            } else if ("PACKAGE".equalsIgnoreCase(type)) {
                return packageDAO.updateApprovalStatus(id, "APPROVED");
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed approving " + type + " #" + id + ": " + e.getMessage(), e);
        }
    }

    public boolean rejectListing(String type, int id) throws DatabaseException {
        try {
            if ("FLIGHT".equalsIgnoreCase(type)) {
                return flightDAO.updateApprovalStatus(id, "REJECTED");
            } else if ("HOTEL".equalsIgnoreCase(type)) {
                return hotelDAO.updateApprovalStatus(id, "REJECTED");
            } else if ("CAR".equalsIgnoreCase(type)) {
                return carDAO.updateApprovalStatus(id, "REJECTED");
            } else if ("PACKAGE".equalsIgnoreCase(type)) {
                return packageDAO.updateApprovalStatus(id, "REJECTED");
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed rejecting " + type + " #" + id + ": " + e.getMessage(), e);
        }
    }

    public boolean deleteListing(String type, int id) throws DatabaseException {
        try {
            if ("FLIGHT".equalsIgnoreCase(type)) {
                return flightDAO.delete(id);
            } else if ("HOTEL".equalsIgnoreCase(type)) {
                return hotelDAO.delete(id);
            } else if ("CAR".equalsIgnoreCase(type)) {
                return carDAO.delete(id);
            } else if ("PACKAGE".equalsIgnoreCase(type)) {
                return packageDAO.delete(id);
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed deleting " + type + " #" + id + ": " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() throws DatabaseException {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load users: " + e.getMessage(), e);
        }
    }

    public List<User> searchUsers(String query) throws DatabaseException {
        try {
            if (query == null || query.trim().isEmpty()) {
                return userDAO.findAll();
            }
            return userDAO.searchUsers(query.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Failed searching users: " + e.getMessage(), e);
        }
    }

    public boolean toggleUserStatus(int userId, String status) throws DatabaseException {
        try {
            return userDAO.toggleStatus(userId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Failed updating user status: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAllBookings() throws DatabaseException {
        try {
            return bookingDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load bookings: " + e.getMessage(), e);
        }
    }

    public boolean updateBookingStatus(int bookingId, String status) throws DatabaseException {
        try {
            return bookingDAO.updateStatus(bookingId, status);
        } catch (SQLException e) {
            throw new DatabaseException("Failed updating booking status: " + e.getMessage(), e);
        }
    }

    public List<Payment> getAllPayments() throws DatabaseException {
        try {
            return paymentDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load payments: " + e.getMessage(), e);
        }
    }
}
