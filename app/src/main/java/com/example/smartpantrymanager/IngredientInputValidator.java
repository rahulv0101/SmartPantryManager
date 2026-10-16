package com.example.smartpantrymanager;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Validates and normalizes values entered on the ingredient form.
 * This class has no Android dependencies so its edge cases can be unit tested.
 */
public final class IngredientInputValidator {
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private IngredientInputValidator() {
        // Utility class.
    }

    public static String normalizeName(String value) {
        return value == null ? "" : value.trim();
    }

    public static Double parsePositiveQuantity(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (!normalized.matches("\\d+(\\.\\d+)?")) {
            return null;
        }

        try {
            BigDecimal decimal = new BigDecimal(normalized);
            if (decimal.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }

            double quantity = decimal.doubleValue();
            return Double.isFinite(quantity) ? quantity : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static boolean isValidOptionalExpiryDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String normalized = value.trim();
        if (!normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        formatter.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return formatter.parse(normalized, position) != null
                && position.getIndex() == normalized.length();
    }
}
