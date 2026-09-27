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
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service managing universal booking workflows for Packages, Flights, Hotels, and Cars,
 * fare calculations, cancellations, and organized travel itinerary generation.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final PackageDAO packageDAO;
    private final HotelDAO hotelDAO;
    private final FlightDAO flightDAO;
    private final CarDAO carDAO;
    private static final Random RANDOM = new Random();

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.packageDAO = new PackageDAO();
        this.hotelDAO = new HotelDAO();
        this.flightDAO = new FlightDAO();
        this.carDAO = new CarDAO();
    }

    public double[] calculateCost(TravelPackage pkg, Hotel hotel, int persons) {
        double packagePrice = pkg != null ? pkg.getPricePerPerson() : 0.0;
        int nights = pkg != null ? pkg.getDurationNights() : 1;
        double hotelPrice = hotel != null ? hotel.getPricePerNight() : 0.0;

        double packageCost = packagePrice * Math.max(1, persons);
        double hotelCost = hotel != null ? (hotelPrice * nights) : 0.0;
        double total = packageCost + hotelCost;

        return new double[]{packageCost, hotelCost, total};
    }

    public String generateBookingCode() {
        int currentYear = Year.now().getValue();
        int randomSuffix = 1000 + RANDOM.nextInt(9000);
        return String.format("TB-%d-%d", currentYear, randomSuffix);
    }

    /**
     * Creates a tour package booking (with optional hotel).
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
            booking.setBookingType("PACKAGE");
            booking.setItemId(pkg.getId());
            booking.setItemName(pkg.getPackageName());
            booking.setPackageId(packageId);
            booking.setAgentId(pkg.getAgentId());
            booking.setHotelId(hotel != null ? hotel.getId() : null);
            booking.setTravelDate(travelDate);
            booking.setStartDate(travelDate);
            booking.setEndDate(travelDate.plusDays(pkg.getDurationDays()));
            booking.setPersons(persons);
            booking.setQuantity(persons);
            booking.setPackageCost(packageCost);
            booking.setHotelCost(hotelCost);
            booking.setTotalAmount(totalAmount);
            booking.setSpecialRequests(specialRequests);
            booking.setBookingStatus("CONFIRMED");

            boolean saved = bookingDAO.save(booking);
            if (!saved) {
                throw new DatabaseException("Failed to save booking.", null);
            }

            if (hotel != null) {
                hotelDAO.updateRoomAvailability(hotel.getId(), -1);
            }

            return bookingDAO.findById(booking.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Database error creating package booking: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a flight booking.
     */
    public Booking createFlightBooking(int userId, int flightId, String travelDateStr, int passengers, String specialRequests)
            throws ValidationException, DatabaseException {

        if (passengers <= 0) {
            throw new ValidationException("Number of passengers must be at least 1.");
        }

        try {
            Flight flight = flightDAO.findById(flightId);
            if (flight == null) throw new ValidationException("Flight not found.");
            if (flight.getAvailableSeats() < passengers) {
                throw new ValidationException("Only " + flight.getAvailableSeats() + " seats remaining on this flight.");
            }

            LocalDate travelDate = flight.getDepartureDate();
            if (travelDateStr != null && !travelDateStr.trim().isEmpty()) {
                try { travelDate = LocalDate.parse(travelDateStr.trim()); } catch (Exception ignored) {}
            }

            double totalAmount = flight.getPrice() * passengers;
            String bookingCode = generateBookingCode();

            Booking booking = new Booking();
            booking.setBookingCode(bookingCode);
            booking.setUserId(userId);
            booking.setBookingType("FLIGHT");
            booking.setItemId(flight.getId());
            booking.setItemName(flight.getAirline() + " " + flight.getFlightNumber() + " (" + flight.getOrigin() + " → " + flight.getDestination() + ")");
            booking.setFlightId(flight.getId());
            booking.setAgentId(flight.getAgentId());
            booking.setTravelDate(travelDate);
            booking.setStartDate(travelDate);
            booking.setEndDate(travelDate);
            booking.setPersons(passengers);
            booking.setQuantity(passengers);
            booking.setPackageCost(totalAmount);
            booking.setHotelCost(0.0);
            booking.setTotalAmount(totalAmount);
            booking.setSpecialRequests(specialRequests);
            booking.setBookingStatus("CONFIRMED");

            boolean saved = bookingDAO.save(booking);
            if (!saved) throw new DatabaseException("Failed to save flight booking.", null);

            flightDAO.decrementSeats(flight.getId(), passengers);
            return bookingDAO.findById(booking.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Database error creating flight booking: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a standalone hotel room reservation.
     */
    public Booking createHotelBooking(int userId, int hotelId, String checkInStr, String checkOutStr, int rooms, int guests, String specialRequests)
            throws ValidationException, DatabaseException {

        if (rooms <= 0) throw new ValidationException("Number of rooms must be at least 1.");
        if (checkInStr == null || checkOutStr == null) throw new ValidationException("Check-in and check-out dates are required.");

        LocalDate checkIn = LocalDate.parse(checkInStr.trim());
        LocalDate checkOut = LocalDate.parse(checkOutStr.trim());

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new ValidationException("Check-out date must be after check-in date.");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) nights = 1;

        try {
            Hotel hotel = hotelDAO.findById(hotelId);
            if (hotel == null) throw new ValidationException("Hotel not found.");
            if (hotel.getAvailableRooms() < rooms) {
                throw new ValidationException("Only " + hotel.getAvailableRooms() + " rooms available.");
            }

            double totalAmount = hotel.getPricePerNight() * nights * rooms;
            String bookingCode = generateBookingCode();

            Booking booking = new Booking();
            booking.setBookingCode(bookingCode);
            booking.setUserId(userId);
            booking.setBookingType("HOTEL");
            booking.setItemId(hotel.getId());
            booking.setItemName(hotel.getHotelName() + " (" + hotel.getRoomType() + ")");
            booking.setHotelId(hotel.getId());
            booking.setAgentId(hotel.getAgentId());
            booking.setTravelDate(checkIn);
            booking.setStartDate(checkIn);
            booking.setEndDate(checkOut);
            booking.setPersons(guests > 0 ? guests : rooms * 2);
            booking.setQuantity(rooms);
            booking.setPackageCost(0.0);
            booking.setHotelCost(totalAmount);
            booking.setTotalAmount(totalAmount);
            booking.setSpecialRequests(specialRequests);
            booking.setBookingStatus("CONFIRMED");

            boolean saved = bookingDAO.save(booking);
            if (!saved) throw new DatabaseException("Failed to save hotel reservation.", null);

            hotelDAO.updateRoomAvailability(hotel.getId(), -rooms);
            return bookingDAO.findById(booking.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Database error creating hotel reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a car rental booking.
     */
    public Booking createCarBooking(int userId, int carId, String pickupDateStr, String dropDateStr, int units, String specialRequests)
            throws ValidationException, DatabaseException {

        if (units <= 0) throw new ValidationException("Number of cars must be at least 1.");
        if (pickupDateStr == null || dropDateStr == null) throw new ValidationException("Pickup and return dates are required.");

        LocalDate pickup = LocalDate.parse(pickupDateStr.trim());
        LocalDate drop = LocalDate.parse(dropDateStr.trim());

        if (drop.isBefore(pickup)) {
            throw new ValidationException("Drop-off date cannot be earlier than pickup date.");
        }

        long days = ChronoUnit.DAYS.between(pickup, drop);
        if (days <= 0) days = 1;

        try {
            Car car = carDAO.findById(carId);
            if (car == null) throw new ValidationException("Rental car not found.");
            if (car.getAvailableUnits() < units) {
                throw new ValidationException("Only " + car.getAvailableUnits() + " units available.");
            }

            double totalAmount = car.getPricePerDay() * days * units;
            String bookingCode = generateBookingCode();

            Booking booking = new Booking();
            booking.setBookingCode(bookingCode);
            booking.setUserId(userId);
            booking.setBookingType("CAR");
            booking.setItemId(car.getId());
            booking.setItemName(car.getCarName() + " (" + car.getBrand() + " " + car.getModel() + ")");
            booking.setCarId(car.getId());
            booking.setAgentId(car.getAgentId());
            booking.setTravelDate(pickup);
            booking.setStartDate(pickup);
            booking.setEndDate(drop);
            booking.setPersons(units);
            booking.setQuantity(units);
            booking.setPackageCost(totalAmount);
            booking.setHotelCost(0.0);
            booking.setTotalAmount(totalAmount);
            booking.setSpecialRequests(specialRequests);
            booking.setBookingStatus("CONFIRMED");

            boolean saved = bookingDAO.save(booking);
            if (!saved) throw new DatabaseException("Failed to save car rental booking.", null);

            carDAO.decrementUnits(car.getId(), units);
            return bookingDAO.findById(booking.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Database error creating car rental: " + e.getMessage(), e);
        }
    }

    /**
     * Cancels an existing booking without deleting historical record and restores inventory.
     */
    public boolean cancelBooking(int bookingId, int userId, boolean isAdmin)
            throws ValidationException, DatabaseException {
        try {
            Booking booking = bookingDAO.findById(bookingId);
            if (booking == null) {
                throw new ValidationException("Booking not found.");
            }

            if (!isAdmin && booking.getUserId() != userId) {
                throw new ValidationException("You are not authorized to cancel this booking.");
            }

            if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
                throw new ValidationException("This booking is already cancelled.");
            }

            boolean cancelled = bookingDAO.cancelBooking(bookingId);
            if (cancelled) {
                // Restore inventory according to booking type
                if ("HOTEL".equalsIgnoreCase(booking.getBookingType()) && booking.getHotelId() != null) {
                    hotelDAO.updateRoomAvailability(booking.getHotelId(), booking.getQuantity() > 0 ? booking.getQuantity() : 1);
                } else if ("FLIGHT".equalsIgnoreCase(booking.getBookingType()) && booking.getFlightId() != null) {
                    flightDAO.incrementSeats(booking.getFlightId(), booking.getQuantity() > 0 ? booking.getQuantity() : 1);
                } else if ("CAR".equalsIgnoreCase(booking.getBookingType()) && booking.getCarId() != null) {
                    carDAO.incrementUnits(booking.getCarId(), booking.getQuantity() > 0 ? booking.getQuantity() : 1);
                } else if ("PACKAGE".equalsIgnoreCase(booking.getBookingType()) && booking.getHotelId() != null && booking.getHotelId() > 0) {
                    hotelDAO.updateRoomAvailability(booking.getHotelId(), 1);
                }
            }
            return cancelled;
        } catch (SQLException e) {
            throw new DatabaseException("Error cancelling booking: " + e.getMessage(), e);
        }
    }

    /**
     * Compiles an organized Travel Itinerary for a user from their active confirmed bookings.
     */
    public Map<String, Object> getItinerary(int userId) throws DatabaseException {
        try {
            List<Booking> bookings = bookingDAO.findByUserId(userId);
            List<Map<String, Object>> items = new ArrayList<>();

            for (Booking b : bookings) {
                if ("CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus())) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("bookingId", b.getId());
                    item.put("bookingCode", b.getBookingCode());
                    item.put("type", b.getBookingType());
                    item.put("title", b.getItemName());
                    item.put("travelDate", b.getTravelDate() != null ? b.getTravelDate().toString() : "");
                    item.put("startDate", b.getStartDate() != null ? b.getStartDate().toString() : "");
                    item.put("endDate", b.getEndDate() != null ? b.getEndDate().toString() : "");
                    item.put("quantity", b.getQuantity());
                    item.put("amount", b.getTotalAmount());
                    item.put("status", b.getBookingStatus());
                    item.put("specialRequests", b.getSpecialRequests() != null ? b.getSpecialRequests() : "");
                    items.add(item);
                }
            }

            // Sort chronologically by startDate or travelDate
            items.sort((a, b) -> {
                String d1 = (String) a.get("startDate");
                String d2 = (String) b.get("startDate");
                return d1.compareTo(d2);
            });

            Map<String, Object> itinerary = new HashMap<>();
            itinerary.put("userId", userId);
            itinerary.put("totalSegments", items.size());
            itinerary.put("segments", items);

            return itinerary;
        } catch (SQLException e) {
            throw new DatabaseException("Error generating itinerary: " + e.getMessage(), e);
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
