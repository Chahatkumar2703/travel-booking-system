package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validation utilities for user input, formats, and business constraints.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[6-9]\\d{9}$"
    );

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        String cleanPhone = phone.trim().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches() || (cleanPhone.length() >= 10 && cleanPhone.matches("\\d+"));
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidPositiveInteger(String numberStr) {
        if (!isNotEmpty(numberStr)) return false;
        try {
            int val = Integer.parseInt(numberStr.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPositiveDecimal(String decimalStr) {
        if (!isNotEmpty(decimalStr)) return false;
        try {
            double val = Double.parseDouble(decimalStr.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if date string is in yyyy-MM-dd format and not in the past.
     */
    public static boolean isValidTravelDate(String dateStr) {
        if (!isNotEmpty(dateStr)) return false;
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return !date.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDate parseDate(String dateStr) {
        if (!isNotEmpty(dateStr)) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
}
