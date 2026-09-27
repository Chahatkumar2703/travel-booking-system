package service;

import dao.DestinationDAO;
import dao.HotelDAO;
import model.Destination;
import model.Hotel;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing hotel listings and room availability.
 */
public class HotelService {

    private final HotelDAO hotelDAO;
    private final DestinationDAO destinationDAO;

    public HotelService() {
        this.hotelDAO = new HotelDAO();
        this.destinationDAO = new DestinationDAO();
    }

    public List<Hotel> getAllHotels() throws DatabaseException {
        try {
            return hotelDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch hotels: " + e.getMessage(), e);
        }
    }

    public List<Hotel> getApprovedHotels(String location, Double maxPrice, Double minRating, Integer destId) throws DatabaseException {
        try {
            return hotelDAO.findApproved(location, maxPrice, minRating, destId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch approved hotels: " + e.getMessage(), e);
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

    /**
     * Creates a new Hotel listing under an Agent account.
     * Validates input, resolves destination ID gracefully, and sets approval_status to PENDING.
     */
    public Hotel addHotel(int agentId, String hotelName, String location, String address,
                          Integer destinationId, String roomType, double pricePerNight,
                          int availableRooms, double rating, String description, String imageUrl)
            throws ValidationException, DatabaseException {

        if (hotelName == null || hotelName.trim().isEmpty()) {
            throw new ValidationException("Hotel Name is required.");
        }
        if (location == null || location.trim().isEmpty()) {
            if (address != null && !address.trim().isEmpty()) {
                location = address.trim();
            } else {
                throw new ValidationException("Destination / Location is required.");
            }
        }
        if (address == null || address.trim().isEmpty()) {
            address = location.trim();
        }
        if (pricePerNight <= 0) {
            throw new ValidationException("Price per night must be greater than 0.");
        }
        if (availableRooms <= 0) {
            throw new ValidationException("Available rooms must be greater than 0.");
        }

        // Gracefully resolve a valid destinationId
        int resolvedDestId = 0;
        try {
            if (destinationId != null && destinationId > 0) {
                Destination existing = destinationDAO.findById(destinationId);
                if (existing != null) {
                    resolvedDestId = existing.getId();
                }
            }

            if (resolvedDestId <= 0 && location != null && !location.trim().isEmpty()) {
                Destination existing = destinationDAO.findByName(location.trim());
                if (existing != null) {
                    resolvedDestId = existing.getId();
                } else {
                    List<Destination> allDest = destinationDAO.findAll();
                    String locLower = location.trim().toLowerCase();
                    for (Destination d : allDest) {
                        String dNameLower = d.getName().toLowerCase();
                        if (locLower.contains(dNameLower) || dNameLower.contains(locLower)) {
                            resolvedDestId = d.getId();
                            break;
                        }
                    }

                    if (resolvedDestId <= 0) {
                        if (!allDest.isEmpty()) {
                            resolvedDestId = allDest.get(0).getId();
                        } else {
                            Destination newDest = new Destination(0, location.trim(), location.trim(),
                                    "Destination: " + location.trim(), "Attractions", "Year-round");
                            destinationDAO.save(newDest);
                            resolvedDestId = newDest.getId() > 0 ? newDest.getId() : 1;
                        }
                    }
                }
            }

            if (resolvedDestId <= 0) {
                resolvedDestId = 1;
            }
        } catch (SQLException e) {
            resolvedDestId = 1;
        }

        Hotel h = new Hotel();
        h.setAgentId(agentId > 0 ? agentId : 2);
        h.setHotelName(hotelName.trim());
        h.setDestinationId(resolvedDestId);
        h.setLocation(location.trim());
        h.setAddress(address.trim());
        h.setRoomType(roomType != null && !roomType.trim().isEmpty() ? roomType.trim() : "Deluxe");
        h.setPricePerNight(pricePerNight);
        h.setAvailableRooms(availableRooms);
        h.setRating(rating > 0 ? rating : 4.5);
        h.setDescription(description != null ? description.trim() : "");
        h.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
        h.setStatus("ACTIVE");
        h.setApprovalStatus("PENDING"); // Pending Admin approval

        try {
            boolean saved = hotelDAO.save(h);
            if (!saved) {
                throw new DatabaseException("Failed to save hotel record into database.", null);
            }
            return h;
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating hotel: " + e.getMessage(), e);
        }
    }

    public boolean addHotel(String name, int destinationId, String address, String roomType,
                            double price, int rooms, double rating, String desc)
            throws ValidationException, DatabaseException {
        Hotel h = addHotel(1, name, address, address, destinationId, roomType, price, rooms, rating, desc, "");
        return h != null && h.getId() > 0;
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
