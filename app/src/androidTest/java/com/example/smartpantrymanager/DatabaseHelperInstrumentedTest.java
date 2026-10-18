package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
    public void updatingAnItemPreservesItsIdAndDoesNotDuplicateIt() {
        long id = databaseHelper.insertPantryItem(new PantryItem("Eggs", 4.0, "pieces", null));
        PantryItem item = databaseHelper.getPantryItemById(id);
        assertNotNull(item);

        item.setName("Large Eggs");
        item.setQuantity(6.0);
        assertEquals(1, databaseHelper.updatePantryItem(item));

        List<PantryItem> items = databaseHelper.getAllPantryItems();
        assertEquals(1, items.size());
        assertEquals(id, items.get(0).getId());
        assertEquals("Large Eggs", items.get(0).getName());
        assertEquals(6.0, items.get(0).getQuantity(), 0.001);
    }

    @Test
    public void deletingAnItemDoesNotDeleteAnotherItem() {
        long deletedId = databaseHelper.insertPantryItem(
                new PantryItem("Milk", 500.0, "ml", null));
        long retainedId = databaseHelper.insertPantryItem(
                new PantryItem("Flour", 1.5, "kg", null));

        assertEquals(1, databaseHelper.deletePantryItem(deletedId));
        assertNull(databaseHelper.getPantryItemById(deletedId));
        assertNotNull(databaseHelper.getPantryItemById(retainedId));
        assertEquals(1, databaseHelper.getAllPantryItems().size());
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

    @Test
    public void recipeSeedContainsCompleteCatalogueWithSupportedUnits() {
        List<Recipe> recipes = databaseHelper.getAllRecipes();
        assertTrue(recipes.size() >= 20);

        Set<String> recipeNames = new HashSet<>();
        for (Recipe recipe : recipes) {
            assertTrue(recipeNames.add(recipe.getName()));
            assertFalse(recipe.getInstructions().trim().isEmpty());

            List<RecipeIngredient> ingredients = databaseHelper.getRecipeIngredients(recipe.getId());
            assertFalse(ingredients.isEmpty());
            for (RecipeIngredient ingredient : ingredients) {
                assertEquals(recipe.getId(), ingredient.getRecipeId());
                assertTrue(ingredient.getRequiredQuantity() > 0);
                assertTrue(IngredientInputValidator.isSupportedUnit(ingredient.getUnit()));
            }
        }
    }

    @Test
    public void recipeCanBeRetrievedByIdWithItsIngredients() {
        Recipe recipe = databaseHelper.getAllRecipes().get(0);
        Recipe retrieved = databaseHelper.getRecipeById(recipe.getId());

        assertNotNull(retrieved);
        assertEquals(recipe.getName(), retrieved.getName());
        assertFalse(databaseHelper.getRecipeIngredients(retrieved.getId()).isEmpty());
        assertNull(databaseHelper.getRecipeById(-1));
    }

    @Test
    public void reopeningDatabaseDoesNotDuplicateRecipeSeed() {
        int initialCount = databaseHelper.countRecipes();
        databaseHelper.close();
        databaseHelper = new DatabaseHelper(context);

        assertEquals(initialCount, databaseHelper.countRecipes());
    }

    @Test
    public void versionOnePantryDataSurvivesRecipeMigration() {
        databaseHelper.close();
        SQLiteOpenHelper versionOneHelper = new SQLiteOpenHelper(
                context, DatabaseHelper.DATABASE_NAME, null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE pantry_items ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "name TEXT NOT NULL, quantity REAL NOT NULL, "
                        + "unit TEXT NOT NULL, expiry_date TEXT)");
                db.execSQL("CREATE TABLE recipes ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "name TEXT NOT NULL, instructions TEXT NOT NULL)");
                db.execSQL("CREATE TABLE recipe_ingredients ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "recipe_id INTEGER NOT NULL, ingredient_name TEXT NOT NULL, "
                        + "required_quantity REAL NOT NULL, unit TEXT NOT NULL, "
                        + "FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // This helper represents the already-installed version 1 schema.
            }
        };

        ContentValues values = new ContentValues();
        values.put("name", "Existing Flour");
        values.put("quantity", 1.0);
        values.put("unit", "kg");
        SQLiteDatabase legacyDatabase = versionOneHelper.getWritableDatabase();
        long pantryId = legacyDatabase.insertOrThrow(DatabaseHelper.TABLE_PANTRY_ITEMS, null, values);
        versionOneHelper.close();

        databaseHelper = new DatabaseHelper(context);
        PantryItem preserved = databaseHelper.getPantryItemById(pantryId);
        assertNotNull(preserved);
        assertEquals("Existing Flour", preserved.getName());
        assertTrue(databaseHelper.countRecipes() >= 20);
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
