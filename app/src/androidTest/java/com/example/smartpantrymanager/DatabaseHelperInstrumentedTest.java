package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Database tests that run on an Android device or emulator because they use
 * the platform SQLite implementation.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseHelperInstrumentedTest {
    private Context context;
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        databaseHelper = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        context.deleteDatabase(DatabaseHelper.DATABASE_NAME);
    }

    @Test
    public void databaseCreatesExpectedTablesAndForeignKeys() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        assertTrue(tableExists(db, DatabaseHelper.TABLE_PANTRY_ITEMS));
        assertTrue(tableExists(db, DatabaseHelper.TABLE_RECIPES));
        assertTrue(tableExists(db, DatabaseHelper.TABLE_RECIPE_INGREDIENTS));

        try (Cursor cursor = db.rawQuery("PRAGMA foreign_keys", null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(1, cursor.getInt(0));
        }

        try (Cursor cursor = db.rawQuery("PRAGMA foreign_key_list(recipe_ingredients)", null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals("recipes", cursor.getString(cursor.getColumnIndexOrThrow("table")));
        }
    }

    @Test
    public void pantryCrudPersistsAndReturnsExpectedValues() {
        PantryItem original = new PantryItem("Milk", 500.0, "ml", "2026-10-20");
        long id = databaseHelper.insertPantryItem(original);

        PantryItem inserted = databaseHelper.getPantryItemById(id);
        assertNotNull(inserted);
        assertEquals("Milk", inserted.getName());
        assertEquals(500.0, inserted.getQuantity(), 0.001);
        assertEquals("ml", inserted.getUnit());
        assertEquals("2026-10-20", inserted.getExpiryDate());

        inserted.setQuantity(750.0);
        inserted.setExpiryDate(null);
        assertEquals(1, databaseHelper.updatePantryItem(inserted));

        PantryItem updated = databaseHelper.getPantryItemById(id);
        assertNotNull(updated);
        assertEquals(750.0, updated.getQuantity(), 0.001);
        assertNull(updated.getExpiryDate());

        List<PantryItem> allItems = databaseHelper.getAllPantryItems();
        assertEquals(1, allItems.size());
        assertEquals(id, allItems.get(0).getId());

        assertEquals(1, databaseHelper.deletePantryItem(id));
        assertNull(databaseHelper.getPantryItemById(id));
        assertEquals(0, databaseHelper.deletePantryItem(id));
    }

    @Test
    public void invalidPantryValuesAreRejected() {
        assertInvalid(new PantryItem("Flour", 0.0, "g", null));
        assertInvalid(new PantryItem("Flour", -2.0, "g", null));
        assertInvalid(new PantryItem("   ", 100.0, "g", null));
        assertInvalid(new PantryItem("Flour", 100.0, "", null));
    }

    @Test
    public void recipeIngredientForeignKeyRejectsUnknownRecipe() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("recipe_id", 999999L);
        values.put("ingredient_name", "Milk");
        values.put("required_quantity", 100.0);
        values.put("unit", "ml");

        try {
            db.insertOrThrow(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null, values);
            fail("An unknown recipe id should violate the foreign key");
        } catch (SQLiteConstraintException expected) {
            assertTrue(expected.getMessage().contains("FOREIGN KEY"));
        }
    }

    private void assertInvalid(PantryItem item) {
        try {
            databaseHelper.insertPantryItem(item);
            fail("Invalid pantry item should be rejected");
        } catch (IllegalArgumentException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        String[] args = {tableName};
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?", args)) {
            return cursor.moveToFirst();
        }
    }
}
