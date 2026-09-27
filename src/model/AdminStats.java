package model;

/**
 * Data Transfer Object (DTO) for Admin Dashboard summary statistics.
 */
public class AdminStats {

    private int totalUsers;
    private int totalTravelers;
    private int totalAgents;
    private int totalDestinations;
    private int totalPackages;
    private int totalHotels;
    private int totalFlights;
    private int totalCars;
    private int pendingApprovals;
    private int totalBookings;
    private int confirmedBookings;
    private int cancelledBookings;
    private int flightBookings;
    private int hotelBookings;
    private int carBookings;
    private int packageBookings;
    private double totalRevenue;

    public AdminStats() {
    }

    public AdminStats(int totalUsers, int totalDestinations, int totalPackages, int totalHotels,
                      int totalBookings, int confirmedBookings, int cancelledBookings, double totalRevenue) {
        this.totalUsers = totalUsers;
        this.totalDestinations = totalDestinations;
        this.totalPackages = totalPackages;
        this.totalHotels = totalHotels;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalTravelers() {
        return totalTravelers;
    }

    public void setTotalTravelers(int totalTravelers) {
        this.totalTravelers = totalTravelers;
    }

    public int getTotalAgents() {
        return totalAgents;
    }

    public void setTotalAgents(int totalAgents) {
        this.totalAgents = totalAgents;
    }

    public int getTotalDestinations() {
        return totalDestinations;
    }

    public void setTotalDestinations(int totalDestinations) {
        this.totalDestinations = totalDestinations;
    }

    public int getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(int totalPackages) {
        this.totalPackages = totalPackages;
    }

    public int getTotalHotels() {
        return totalHotels;
    }

    public void setTotalHotels(int totalHotels) {
        this.totalHotels = totalHotels;
    }

    public int getTotalFlights() {
        return totalFlights;
    }

    public void setTotalFlights(int totalFlights) {
        this.totalFlights = totalFlights;
    }

    public int getTotalCars() {
        return totalCars;
    }

    public void setTotalCars(int totalCars) {
        this.totalCars = totalCars;
    }

    public int getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(int pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(int confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public int getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(int cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public int getFlightBookings() {
        return flightBookings;
    }

    public void setFlightBookings(int flightBookings) {
        this.flightBookings = flightBookings;
    }

    public int getHotelBookings() {
        return hotelBookings;
    }

    public void setHotelBookings(int hotelBookings) {
        this.hotelBookings = hotelBookings;
    }

    public int getCarBookings() {
        return carBookings;
    }

    public void setCarBookings(int carBookings) {
        this.carBookings = carBookings;
    }

    public int getPackageBookings() {
        return packageBookings;
    }

    public void setPackageBookings(int packageBookings) {
        this.packageBookings = packageBookings;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
