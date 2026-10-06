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
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,24}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[6-9]\\d{9}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9\\s.'-]{2,100}$"
    );

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) return false;
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static final String PLACEHOLDER_EMAIL_DOMAIN = "@noemail.tripzy.com";

    public static boolean isPlaceholderEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true;
        String lower = email.trim().toLowerCase();
        return lower.endsWith(PLACEHOLDER_EMAIL_DOMAIN) || lower.endsWith("@tripzy.user") || "not_provided".equalsIgnoreCase(lower);
    }

    public static String generatePlaceholderEmail(String phone) {
        String clean = cleanPhone(phone);
        if (clean.isEmpty()) {
            clean = "user_" + System.currentTimeMillis();
        }
        return clean + PLACEHOLDER_EMAIL_DOMAIN;
    }

    public static boolean isProvidedEmail(String email) {
        return isNotEmpty(email) && !isPlaceholderEmail(email);
    }

    public static boolean isProvidedPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        String clean = cleanPhone(phone);
        return !clean.isEmpty() && !"NOT_PROVIDED".equalsIgnoreCase(clean);
    }

    /**
     * Strips whitespace, hyphens, and leading +91 or 91 country code to normalize to 10 digits.
     */
    public static String cleanPhone(String phone) {
        if (phone == null) return "";
        String clean = phone.trim().replaceAll("[\\s-()\\.]", "");
        if (clean.startsWith("+91")) {
            clean = clean.substring(3);
        } else if (clean.startsWith("91") && clean.length() == 12) {
            clean = clean.substring(2);
        }
        return clean;
    }

    /**
     * Rejects known dummy, repeated, or sequence mobile numbers.
     */
    public static boolean isDummyPhone(String clean) {
        if (clean == null || clean.length() != 10) return true;
        // All 10 digits identical (0000000000, 1111111111, ..., 8888888888, 9999999999)
        if (clean.chars().distinct().count() <= 1) return true;
        // Obvious sequential dummy numbers
        if ("1234567890".equals(clean) || "0123456789".equals(clean)) return true;
        return false;
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        String clean = cleanPhone(phone);
        if (!PHONE_PATTERN.matcher(clean).matches()) return false;
        if (isDummyPhone(clean)) return false;
        return true;
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "";
        int atIndex = email.indexOf('@');
        String user = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (user.length() <= 1) {
            return user + "***" + domain;
        }
        int starCount = Math.max(3, user.length() - 1);
        StringBuilder sb = new StringBuilder();
        sb.append(user.charAt(0));
        for (int i = 0; i < starCount; i++) sb.append('*');
        sb.append(domain);
        return sb.toString();
    }

    public static String maskPhone(String phone) {
        String clean = cleanPhone(phone);
        if (clean.length() < 4) return "+91 **********";
        return "+91 ******" + clean.substring(clean.length() - 4);
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
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
