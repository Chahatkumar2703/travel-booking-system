package service;

import dao.BookingDAO;
import dao.CarDAO;
import dao.FlightDAO;
import dao.HotelDAO;
import dao.PackageDAO;
import model.Booking;
import model.Car;
import model.Flight;
import model.Hotel;
import model.TravelPackage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service providing dedicated dashboard and listing management operations for Travel Agents.
 */
public class AgentService {

    private final FlightDAO flightDAO = new FlightDAO();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final CarDAO carDAO = new CarDAO();
    private final PackageDAO packageDAO = new PackageDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    public Map<String, Object> getAgentStats(int agentId) throws DatabaseException {
        try {
            List<Flight> flights = flightDAO.findByAgentId(agentId);
            List<Hotel> hotels = hotelDAO.findByAgentId(agentId);
            List<Car> cars = carDAO.findByAgentId(agentId);
            List<TravelPackage> pkgs = packageDAO.findByAgentId(agentId);
            List<Booking> bookings = bookingDAO.findByAgentId(agentId);

            int totalListings = flights.size() + hotels.size() + cars.size() + pkgs.size();
            long pendingApprovals = 0;
            for (Flight f : flights) if ("PENDING".equalsIgnoreCase(f.getApprovalStatus())) pendingApprovals++;
            for (Hotel h : hotels) if ("PENDING".equalsIgnoreCase(h.getApprovalStatus())) pendingApprovals++;
            for (Car c : cars) if ("PENDING".equalsIgnoreCase(c.getApprovalStatus())) pendingApprovals++;
            for (TravelPackage p : pkgs) if ("PENDING".equalsIgnoreCase(p.getApprovalStatus())) pendingApprovals++;

            double revenue = 0.0;
            int confirmedBookings = 0;
            for (Booking b : bookings) {
                if ("CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus())) {
                    revenue += b.getTotalAmount();
                    confirmedBookings++;
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalListings", totalListings);
            stats.put("totalFlights", flights.size());
            stats.put("totalHotels", hotels.size());
            stats.put("totalCars", cars.size());
            stats.put("totalPackages", pkgs.size());
            stats.put("pendingApprovals", pendingApprovals);
            stats.put("totalBookings", bookings.size());
            stats.put("confirmedBookings", confirmedBookings);
            stats.put("totalRevenue", revenue);

            return stats;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate agent stats: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAgentBookings(int agentId) throws DatabaseException {
        try {
            return bookingDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent bookings: " + e.getMessage(), e);
        }
    }

    public List<Flight> getAgentFlights(int agentId) throws DatabaseException {
        try {
            return flightDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent flights: " + e.getMessage(), e);
        }
    }

    public List<Hotel> getAgentHotels(int agentId) throws DatabaseException {
        try {
            return hotelDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent hotels: " + e.getMessage(), e);
        }
    }

    public List<Car> getAgentCars(int agentId) throws DatabaseException {
        try {
            return carDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent cars: " + e.getMessage(), e);
        }
    }

    public List<TravelPackage> getAgentPackages(int agentId) throws DatabaseException {
        try {
            return packageDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent packages: " + e.getMessage(), e);
        }
    }
}
