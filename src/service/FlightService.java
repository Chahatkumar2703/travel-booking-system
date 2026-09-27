package service;

import dao.FlightDAO;
import model.Flight;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing flight schedules, search, and availability.
 */
public class FlightService {

    private final FlightDAO flightDAO;

    public FlightService() {
        this.flightDAO = new FlightDAO();
    }

    public List<Flight> getAllFlights() throws DatabaseException {
        try {
            return flightDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve flights: " + e.getMessage(), e);
        }
    }

    public List<Flight> searchFlights(String origin, String destination, String date, Double maxPrice, String airline) throws DatabaseException {
        try {
            return flightDAO.findApproved(origin, destination, date, maxPrice, airline);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search flights: " + e.getMessage(), e);
        }
    }

    public Flight getFlightById(int id) throws DatabaseException {
        try {
            return flightDAO.findById(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve flight #" + id + ": " + e.getMessage(), e);
        }
    }

    public List<Flight> getFlightsByAgent(int agentId) throws DatabaseException {
        try {
            return flightDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent flights: " + e.getMessage(), e);
        }
    }

    public Flight addFlight(int agentId, String airline, String flightNumber,
                            String origin, String destination, String departureDateStr,
                            String departureTime, String arrivalTime, double price,
                            int availableSeats, String imageUrl)
            throws ValidationException, DatabaseException {

        if (airline == null || airline.trim().isEmpty()) throw new ValidationException("Airline name is required.");
        if (flightNumber == null || flightNumber.trim().isEmpty()) throw new ValidationException("Flight number is required.");
        if (origin == null || origin.trim().isEmpty()) throw new ValidationException("Origin city is required.");
        if (destination == null || destination.trim().isEmpty()) throw new ValidationException("Destination city is required.");
        if (price <= 0) throw new ValidationException("Flight ticket price must be greater than 0.");
        if (availableSeats <= 0) throw new ValidationException("Available seats must be greater than 0.");

        LocalDate depDate;
        try {
            depDate = LocalDate.parse(departureDateStr.trim());
        } catch (Exception e) {
            throw new ValidationException("Invalid departure date format. Use YYYY-MM-DD.");
        }

        Flight flight = new Flight();
        flight.setAgentId(agentId);
        flight.setAirline(airline.trim());
        flight.setFlightNumber(flightNumber.trim());
        flight.setOrigin(origin.trim());
        flight.setDestination(destination.trim());
        flight.setDepartureDate(depDate);
        flight.setDepartureTime(departureTime != null ? departureTime.trim() : "10:00 AM");
        flight.setArrivalTime(arrivalTime != null ? arrivalTime.trim() : "12:00 PM");
        flight.setPrice(price);
        flight.setAvailableSeats(availableSeats);
        flight.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
        flight.setStatus("ACTIVE");
        flight.setApprovalStatus("PENDING"); // Pending admin approval

        try {
            flightDAO.save(flight);
            return flight;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save flight: " + e.getMessage(), e);
        }
    }

    public boolean updateFlight(int id, int agentId, String airline, String flightNumber,
                                String origin, String destination, String departureDateStr,
                                String departureTime, String arrivalTime, double price,
                                int availableSeats, String imageUrl, String status, String approvalStatus)
            throws ValidationException, DatabaseException {

        Flight flight = getFlightById(id);
        if (flight == null) throw new ValidationException("Flight not found.");

        if (airline != null && !airline.trim().isEmpty()) flight.setAirline(airline.trim());
        if (flightNumber != null && !flightNumber.trim().isEmpty()) flight.setFlightNumber(flightNumber.trim());
        if (origin != null && !origin.trim().isEmpty()) flight.setOrigin(origin.trim());
        if (destination != null && !destination.trim().isEmpty()) flight.setDestination(destination.trim());
        if (departureDateStr != null && !departureDateStr.trim().isEmpty()) {
            try { flight.setDepartureDate(LocalDate.parse(departureDateStr.trim())); } catch (Exception ignored) {}
        }
        if (departureTime != null) flight.setDepartureTime(departureTime.trim());
        if (arrivalTime != null) flight.setArrivalTime(arrivalTime.trim());
        if (price > 0) flight.setPrice(price);
        if (availableSeats >= 0) flight.setAvailableSeats(availableSeats);
        if (imageUrl != null) flight.setImageUrl(imageUrl.trim());
        if (status != null) flight.setStatus(status.trim());
        if (approvalStatus != null) flight.setApprovalStatus(approvalStatus.trim());

        try {
            return flightDAO.update(flight);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update flight: " + e.getMessage(), e);
        }
    }

    public boolean deleteFlight(int id) throws DatabaseException {
        try {
            return flightDAO.delete(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete flight: " + e.getMessage(), e);
        }
    }

    public boolean updateApprovalStatus(int id, String approvalStatus) throws DatabaseException {
        try {
            return flightDAO.updateApprovalStatus(id, approvalStatus);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update flight approval: " + e.getMessage(), e);
        }
    }
}
