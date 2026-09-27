package service;

import dao.SystemSettingsDAO;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service for platform system settings.
 */
public class SettingsService {

    private final SystemSettingsDAO settingsDAO;

    public SettingsService() {
        this.settingsDAO = new SystemSettingsDAO();
    }

    public Map<String, String> getAllSettings() throws DatabaseException {
        try {
            return settingsDAO.getAllSettings();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve system settings: " + e.getMessage(), e);
        }
    }

    public String getSetting(String key, String defaultValue) {
        return settingsDAO.getSetting(key, defaultValue);
    }

    public boolean updateSetting(String key, String value) throws DatabaseException {
        try {
            return settingsDAO.updateSetting(key, value);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update setting: " + e.getMessage(), e);
        }
    }

    public boolean isBookingEnabled() {
        String val = getSetting("booking_enabled", "true");
        return !"false".equalsIgnoreCase(val);
    }

    public boolean isMaintenanceMode() {
        String val = getSetting("maintenance_mode", "false");
        return "true".equalsIgnoreCase(val);
    }
}
