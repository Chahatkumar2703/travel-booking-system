package service;

import dao.HotelDAO;
import model.Hotel;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing hotel listings and room availability.
 */
public class HotelService {

    private final HotelDAO hotelDAO;

    public HotelService() {
        this.hotelDAO = new HotelDAO();
    }

    public List<Hotel> getAllHotels() throws DatabaseException {
        try {
            return hotelDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch hotels: " + e.getMessage(), e);
        }
    }

    public Hotel getHotelById(int id) throws DatabaseException {
        try {
            return hotelDAO.findById(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch hotel: " + e.getMessage(), e);
        }
    }

    public List<Hotel> getHotelsByDestination(int destinationId) throws DatabaseException {
        try {
            return hotelDAO.findByDestination(destinationId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch hotels for destination: " + e.getMessage(), e);
        }
    }

    public boolean addHotel(String name, int destinationId, String address, String roomType,
                            double price, int rooms, double rating, String desc)
            throws ValidationException, DatabaseException {

        if (!ValidationUtil.isNotEmpty(name)) {
            throw new ValidationException("Hotel Name is required.");
        }
        if (destinationId <= 0) {
            throw new ValidationException("Please select a destination.");
        }
        if (!ValidationUtil.isNotEmpty(address)) {
            throw new ValidationException("Hotel address cannot be empty.");
        }
        if (price < 0) {
            throw new ValidationException("Price per night cannot be negative.");
        }
        if (rooms < 0) {
            throw new ValidationException("Available rooms cannot be negative.");
        }

        try {
            Hotel h = new Hotel(0, name.trim(), destinationId, null, address.trim(),
                    roomType != null ? roomType.trim() : "Deluxe",
                    price, rooms, rating, desc != null ? desc.trim() : "", "ACTIVE");
            return hotelDAO.save(h);
        } catch (SQLException e) {
            throw new DatabaseException("Error saving hotel: " + e.getMessage(), e);
        }
    }

    public boolean updateHotel(int id, String name, int destinationId, String address, String roomType,
                               double price, int rooms, double rating, String desc, String status)
            throws ValidationException, DatabaseException {

        if (!ValidationUtil.isNotEmpty(name)) {
            throw new ValidationException("Hotel Name is required.");
        }
        if (destinationId <= 0) {
            throw new ValidationException("Please select a destination.");
        }
        if (!ValidationUtil.isNotEmpty(address)) {
            throw new ValidationException("Hotel address cannot be empty.");
        }

        try {
            Hotel h = new Hotel(id, name.trim(), destinationId, null, address.trim(),
                    roomType != null ? roomType.trim() : "Deluxe",
                    price, rooms, rating, desc != null ? desc.trim() : "", status);
            return hotelDAO.update(h);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating hotel: " + e.getMessage(), e);
        }
    }

    public boolean deleteHotel(int id) throws DatabaseException {
        try {
            return hotelDAO.delete(id);
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting hotel: " + e.getMessage(), e);
        }
    }

    public boolean updateRoomAvailability(int id, int delta) throws DatabaseException {
        try {
            return hotelDAO.updateRoomAvailability(id, delta);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating room availability: " + e.getMessage(), e);
        }
    }
}
