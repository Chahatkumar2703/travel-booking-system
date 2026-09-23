package model;

/**
 * Data Transfer Object (DTO) for Admin Dashboard summary statistics.
 */
public class AdminStats {

    private int totalUsers;
    private int totalDestinations;
    private int totalPackages;
    private int totalHotels;
    private int totalBookings;
    private int confirmedBookings;
    private int cancelledBookings;
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

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
