package service;

import dao.DestinationDAO;
import model.Destination;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing destination operations and catalog searches.
 */
public class DestinationService {

    private final DestinationDAO destinationDAO;

    public DestinationService() {
        this.destinationDAO = new DestinationDAO();
    }

    public List<Destination> getAllDestinations() throws DatabaseException {
        try {
            return destinationDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch destinations: " + e.getMessage(), e);
        }
    }

    public Destination getDestinationById(int id) throws DatabaseException {
        try {
            return destinationDAO.findById(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch destination: " + e.getMessage(), e);
        }
    }

    public List<Destination> searchDestinations(String keyword) throws DatabaseException {
        try {
            if (!ValidationUtil.isNotEmpty(keyword)) {
                return destinationDAO.findAll();
            }
            return destinationDAO.search(keyword.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Failed searching destinations: " + e.getMessage(), e);
        }
    }

    public boolean addDestination(String name, String state, String description,
                                  String attractions, String bestTime)
            throws ValidationException, DatabaseException {
        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isNotEmpty(state) ||
            !ValidationUtil.isNotEmpty(description) || !ValidationUtil.isNotEmpty(attractions) ||
            !ValidationUtil.isNotEmpty(bestTime)) {
            throw new ValidationException("All destination fields are required.");
        }

        try {
            Destination existing = destinationDAO.findByName(name.trim());
            if (existing != null) {
                throw new ValidationException("A destination with this name already exists.");
            }

            Destination d = new Destination(0, name.trim(), state.trim(), description.trim(),
                    attractions.trim(), bestTime.trim());
            return destinationDAO.save(d);
        } catch (SQLException e) {
            throw new DatabaseException("Error saving destination: " + e.getMessage(), e);
        }
    }

    public boolean updateDestination(int id, String name, String state, String description,
                                     String attractions, String bestTime)
            throws ValidationException, DatabaseException {
        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isNotEmpty(state) ||
            !ValidationUtil.isNotEmpty(description) || !ValidationUtil.isNotEmpty(attractions) ||
            !ValidationUtil.isNotEmpty(bestTime)) {
            throw new ValidationException("All destination fields are required.");
        }

        try {
            Destination d = new Destination(id, name.trim(), state.trim(), description.trim(),
                    attractions.trim(), bestTime.trim());
            return destinationDAO.update(d);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating destination: " + e.getMessage(), e);
        }
    }

    public boolean deleteDestination(int id) throws DatabaseException {
        try {
            return destinationDAO.delete(id);
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting destination: " + e.getMessage(), e);
        }
    }
}
