package service;

import dao.CarDAO;
import model.Car;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for managing car rental catalog, search, and availability.
 */
public class CarService {

    private final CarDAO carDAO;

    public CarService() {
        this.carDAO = new CarDAO();
    }

    public List<Car> getAllCars() throws DatabaseException {
        try {
            return carDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve cars: " + e.getMessage(), e);
        }
    }

    public List<Car> searchCars(String location, String carType, Double maxPrice) throws DatabaseException {
        try {
            return carDAO.findApproved(location, carType, maxPrice);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search cars: " + e.getMessage(), e);
        }
    }

    public Car getCarById(int id) throws DatabaseException {
        try {
            return carDAO.findById(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve car #" + id + ": " + e.getMessage(), e);
        }
    }

    public List<Car> getCarsByAgent(int agentId) throws DatabaseException {
        try {
            return carDAO.findByAgentId(agentId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve agent cars: " + e.getMessage(), e);
        }
    }

    public Car addCar(int agentId, String carName, String brand, String model,
                      String location, String carType, double pricePerDay,
                      int availableUnits, String imageUrl)
            throws ValidationException, DatabaseException {

        if (carName == null || carName.trim().isEmpty()) throw new ValidationException("Car name is required.");
        if (brand == null || brand.trim().isEmpty()) throw new ValidationException("Brand name is required.");
        if (location == null || location.trim().isEmpty()) throw new ValidationException("Pickup location is required.");
        if (pricePerDay <= 0) throw new ValidationException("Daily rental price must be greater than 0.");
        if (availableUnits <= 0) throw new ValidationException("Available units must be greater than 0.");

        Car car = new Car();
        car.setAgentId(agentId);
        car.setCarName(carName.trim());
        car.setBrand(brand.trim());
        car.setModel(model != null ? model.trim() : "");
        car.setLocation(location.trim());
        car.setCarType(carType != null ? carType.trim() : "Sedan");
        car.setPricePerDay(pricePerDay);
        car.setAvailableUnits(availableUnits);
        car.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
        car.setStatus("ACTIVE");
        car.setApprovalStatus("PENDING"); // Pending admin approval

        try {
            carDAO.save(car);
            return car;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save rental car: " + e.getMessage(), e);
        }
    }

    public boolean updateCar(int id, int agentId, String carName, String brand, String model,
                             String location, String carType, double pricePerDay,
                             int availableUnits, String imageUrl, String status, String approvalStatus)
            throws ValidationException, DatabaseException {

        Car car = getCarById(id);
        if (car == null) throw new ValidationException("Car not found.");

        if (carName != null && !carName.trim().isEmpty()) car.setCarName(carName.trim());
        if (brand != null && !brand.trim().isEmpty()) car.setBrand(brand.trim());
        if (model != null) car.setModel(model.trim());
        if (location != null && !location.trim().isEmpty()) car.setLocation(location.trim());
        if (carType != null) car.setCarType(carType.trim());
        if (pricePerDay > 0) car.setPricePerDay(pricePerDay);
        if (availableUnits >= 0) car.setAvailableUnits(availableUnits);
        if (imageUrl != null) car.setImageUrl(imageUrl.trim());
        if (status != null) car.setStatus(status.trim());
        if (approvalStatus != null) car.setApprovalStatus(approvalStatus.trim());

        try {
            return carDAO.update(car);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update car: " + e.getMessage(), e);
        }
    }

    public boolean deleteCar(int id) throws DatabaseException {
        try {
            return carDAO.delete(id);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete car: " + e.getMessage(), e);
        }
    }

    public boolean updateApprovalStatus(int id, String approvalStatus) throws DatabaseException {
        try {
            return carDAO.updateApprovalStatus(id, approvalStatus);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update car approval: " + e.getMessage(), e);
        }
    }
}
