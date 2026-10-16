package com.example.smartpantrymanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IngredientInputValidatorTest {

    @Test
    public void trimsNamesAndRejectsBlankNames() {
        assertEquals("Eggs", IngredientInputValidator.normalizeName("  Eggs  "));
        assertEquals("", IngredientInputValidator.normalizeName("   "));
        assertEquals("", IngredientInputValidator.normalizeName(null));
    }

    @Test
    public void acceptsPositiveIntegerAndDecimalQuantities() {
        assertEquals(4.0, IngredientInputValidator.parsePositiveQuantity("4"), 0.001);
        assertEquals(1.5, IngredientInputValidator.parsePositiveQuantity("1.5"), 0.001);
        assertEquals(0.25, IngredientInputValidator.parsePositiveQuantity(" 0.25 "), 0.001);
    }

    @Test
    public void rejectsInvalidQuantities() {
        assertNull(IngredientInputValidator.parsePositiveQuantity("0"));
        assertNull(IngredientInputValidator.parsePositiveQuantity("-1"));
        assertNull(IngredientInputValidator.parsePositiveQuantity("NaN"));
        assertNull(IngredientInputValidator.parsePositiveQuantity("Infinity"));
        assertNull(IngredientInputValidator.parsePositiveQuantity("1,5"));
        assertNull(IngredientInputValidator.parsePositiveQuantity("not-a-number"));
    }

    @Test
    public void validatesOptionalIsoExpiryDates() {
        assertTrue(IngredientInputValidator.isValidOptionalExpiryDate(""));
        assertTrue(IngredientInputValidator.isValidOptionalExpiryDate("2026-02-28"));
        assertFalse(IngredientInputValidator.isValidOptionalExpiryDate("2026-02-30"));
        assertFalse(IngredientInputValidator.isValidOptionalExpiryDate("28/02/2026"));
    }
}
