package service;

import dao.PackageDAO;
import model.TravelPackage;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing travel packages catalog, search, and administrative controls.
 */
public class PackageService {

    private final PackageDAO packageDAO;

    public PackageService() {
        this.packageDAO = new PackageDAO();
    }

    public List<TravelPackage> getAllPackages() throws DatabaseException {
        try {
            return packageDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch packages: " + e.getMessage(), e);
        }
    }

    public TravelPackage getPackageById(int id) throws DatabaseException {
        try {
            return packageDAO.findById(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch package: " + e.getMessage(), e);
        }
    }

    public List<TravelPackage> getPackagesByDestination(int destinationId) throws DatabaseException {
        try {
            return packageDAO.findByDestination(destinationId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch packages for destination: " + e.getMessage(), e);
        }
    }

    public List<TravelPackage> searchPackages(String keyword, Integer destinationId, Double maxPrice) throws DatabaseException {
        try {
            return packageDAO.search(keyword, destinationId, maxPrice);
        } catch (SQLException e) {
            throw new DatabaseException("Failed searching packages: " + e.getMessage(), e);
        }
    }

    public boolean addPackage(String name, int destinationId, int days, int nights,
                              double price, String places, boolean hotelInc,
                              boolean foodInc, boolean transInc, String desc)
            throws ValidationException, DatabaseException {

        if (!ValidationUtil.isNotEmpty(name)) {
            throw new ValidationException("Package Name is required.");
        }
        if (destinationId <= 0) {
            throw new ValidationException("Please select a destination.");
        }
        if (days <= 0 || nights < 0) {
            throw new ValidationException("Duration days must be > 0 and nights >= 0.");
        }
        if (price <= 0) {
            throw new ValidationException("Price per person must be greater than 0.");
        }
        if (!ValidationUtil.isNotEmpty(places)) {
            throw new ValidationException("Places covered cannot be empty.");
        }

        try {
            TravelPackage p = new TravelPackage(0, name.trim(), destinationId, null,
                    days, nights, price, places.trim(), hotelInc, foodInc, transInc,
                    desc != null ? desc.trim() : "", "ACTIVE");
            return packageDAO.save(p);
        } catch (SQLException e) {
            throw new DatabaseException("Error saving package: " + e.getMessage(), e);
        }
    }

    public boolean updatePackage(int id, String name, int destinationId, int days, int nights,
                                 double price, String places, boolean hotelInc,
                                 boolean foodInc, boolean transInc, String desc, String status)
            throws ValidationException, DatabaseException {

        if (!ValidationUtil.isNotEmpty(name)) {
            throw new ValidationException("Package Name is required.");
        }
        if (destinationId <= 0) {
            throw new ValidationException("Please select a destination.");
        }
        if (days <= 0 || nights < 0) {
            throw new ValidationException("Duration days must be > 0 and nights >= 0.");
        }
        if (price <= 0) {
            throw new ValidationException("Price per person must be greater than 0.");
        }

        try {
            TravelPackage p = new TravelPackage(id, name.trim(), destinationId, null,
                    days, nights, price, places.trim(), hotelInc, foodInc, transInc,
                    desc != null ? desc.trim() : "", status);
            return packageDAO.update(p);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating package: " + e.getMessage(), e);
        }
    }

    public boolean deletePackage(int id) throws DatabaseException {
        try {
            return packageDAO.delete(id);
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting package: " + e.getMessage(), e);
        }
    }
}
