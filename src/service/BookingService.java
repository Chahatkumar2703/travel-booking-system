package service;

import dao.BookingDAO;
import dao.HotelDAO;
import dao.PackageDAO;
import model.Booking;
import model.Hotel;
import model.TravelPackage;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Random;

/**
 * Service managing package booking workflows, calculations, and cancellations.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final PackageDAO packageDAO;
    private final HotelDAO hotelDAO;
    private static final Random RANDOM = new Random();

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.packageDAO = new PackageDAO();
        this.hotelDAO = new HotelDAO();
    }

    /**
     * Calculates costs:
     * Package Cost = package.pricePerPerson * persons
     * Hotel Cost = hotel.pricePerNight * package.durationNights (if hotel selected)
     * Total = Package Cost + Hotel Cost
     */
    public double[] calculateCost(TravelPackage pkg, Hotel hotel, int persons) {
        double packagePrice = pkg != null ? pkg.getPricePerPerson() : 0.0;
        int nights = pkg != null ? pkg.getDurationNights() : 1;
        double hotelPrice = hotel != null ? hotel.getPricePerNight() : 0.0;

        double packageCost = packagePrice * Math.max(1, persons);
        double hotelCost = hotel != null ? (hotelPrice * nights) : 0.0;
        double total = packageCost + hotelCost;

        return new double[]{packageCost, hotelCost, total};
    }

    /**
     * Generates a unique, professional Booking Code: TB-2026-XXXX
     */
    public String generateBookingCode() {
        int currentYear = Year.now().getValue();
        int randomSuffix = 1000 + RANDOM.nextInt(9000);
        return String.format("TB-%d-%d", currentYear, randomSuffix);
    }

    /**
     * Creates a new booking record after validation.
     */
    public Booking createBooking(int userId, int packageId, Integer hotelId,
                                 String travelDateStr, int persons, String specialRequests)
            throws ValidationException, DatabaseException {

        if (persons <= 0) {
            throw new ValidationException("Number of persons must be greater than 0.");
        }

        if (!ValidationUtil.isValidTravelDate(travelDateStr)) {
            throw new ValidationException("Travel date cannot be empty, in the past, or invalid format (YYYY-MM-DD).");
        }

        LocalDate travelDate = ValidationUtil.parseDate(travelDateStr);

        try {
            TravelPackage pkg = packageDAO.findById(packageId);
            if (pkg == null) {
                throw new ValidationException("Selected travel package was not found.");
            }

            Hotel hotel = null;
            if (hotelId != null && hotelId > 0) {
                hotel = hotelDAO.findById(hotelId);
                if (hotel == null) {
                    throw new ValidationException("Selected hotel not found.");
                }
                if (hotel.getAvailableRooms() <= 0) {
                    throw new ValidationException("The selected hotel has no rooms currently available.");
                }
            }

            double[] costs = calculateCost(pkg, hotel, persons);
            double packageCost = costs[0];
            double hotelCost = costs[1];
            double totalAmount = costs[2];

            String bookingCode = generateBookingCode();

            Booking booking = new Booking();
            booking.setBookingCode(bookingCode);
            booking.setUserId(userId);
            booking.setPackageId(packageId);
            booking.setHotelId(hotel != null ? hotel.getId() : null);
            booking.setTravelDate(travelDate);
            booking.setPersons(persons);
            booking.setPackageCost(packageCost);
            booking.setHotelCost(hotelCost);
            booking.setTotalAmount(totalAmount);
            booking.setSpecialRequests(specialRequests);
            booking.setBookingStatus("CONFIRMED");

            boolean saved = bookingDAO.save(booking);
            if (!saved) {
                throw new DatabaseException("Failed to save booking.", null);
            }

            // Decrement available hotel rooms if booked
            if (hotel != null) {
                hotelDAO.updateRoomAvailability(hotel.getId(), -1);
            }

            return bookingDAO.findById(booking.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Database error creating booking: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels an existing booking without deleting historical record.
     */
    public boolean cancelBooking(int bookingId, int userId, boolean isAdmin)
            throws ValidationException, DatabaseException {
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                throw new ValidationException("Booking not found.");
            }

            // Permission check: regular user can only cancel their own booking
            if (!isAdmin && booking.getUserId() != userId) {
                throw new ValidationException("You are not authorized to cancel this booking.");
            }

            if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
                throw new ValidationException("This booking is already cancelled.");
            }

            boolean cancelled = bookingDAO.cancelBooking(bookingId);
            if (cancelled) {
                // Restore hotel room availability if hotel was reserved
                if (booking.getHotelId() != null && booking.getHotelId() > 0) {
                    hotelDAO.updateRoomAvailability(booking.getHotelId(), 1);
                }
            }
            return cancelled;
        } catch (SQLException e) {
            throw new DatabaseException("Error cancelling booking: " + e.getMessage(), e);
        }
    }

    public List<Booking> getUserBookings(int userId) throws DatabaseException {
        try {
            return bookingDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching bookings for user: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAllBookings() throws DatabaseException {
        try {
            return bookingDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all bookings: " + e.getMessage(), e);
        }
    }

    public Booking getBookingById(int id) throws DatabaseException {
        try {
            return bookingDAO.findById(id);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving booking details: " + e.getMessage(), e);
        }
    }

    public boolean updateBookingStatus(int bookingId, String newStatus) throws DatabaseException {
        try {
            return bookingDAO.updateStatus(bookingId, newStatus);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating booking status: " + e.getMessage(), e);
        }
    }
}
