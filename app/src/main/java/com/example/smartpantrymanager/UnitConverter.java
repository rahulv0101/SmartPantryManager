package com.example.smartpantrymanager;

import java.math.BigDecimal;
import java.util.Locale;

/** Converts only measurement units with the same physical dimension. */
public final class UnitConverter {
    private UnitConverter() {
        // Utility class.
    }

    public static Double convert(double quantity, String fromUnit, String toUnit) {
        BigDecimal converted = convertAsBigDecimal(quantity, fromUnit, toUnit);
        return converted == null ? null : converted.doubleValue();
    }

    static BigDecimal convertAsBigDecimal(double quantity, String fromUnit, String toUnit) {
        if (!Double.isFinite(quantity) || quantity < 0 || fromUnit == null || toUnit == null) {
            return null;
        }

        String from = fromUnit.trim().toLowerCase(Locale.US);
        String to = toUnit.trim().toLowerCase(Locale.US);
        if (from.equals(to)) {
            return BigDecimal.valueOf(quantity);
        }
        if (from.equals("kg") && to.equals("g")) {
            return BigDecimal.valueOf(quantity).multiply(BigDecimal.valueOf(1000));
        }
        if (from.equals("g") && to.equals("kg")) {
            return BigDecimal.valueOf(quantity).divide(BigDecimal.valueOf(1000));
        }
        if (from.equals("l") && to.equals("ml")) {
            return BigDecimal.valueOf(quantity).multiply(BigDecimal.valueOf(1000));
        }
        if (from.equals("ml") && to.equals("l")) {
            return BigDecimal.valueOf(quantity).divide(BigDecimal.valueOf(1000));
        }
        return null;
    }
}
