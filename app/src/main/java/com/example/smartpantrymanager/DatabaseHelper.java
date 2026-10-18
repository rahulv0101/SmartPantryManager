package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the local SQLite schema and pantry persistence operations.
 *
 * Recipe tables are created now so later milestones can add recipe data and
 * strict matching without changing the relationship between recipes and their
 * required ingredients.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT = "unit";
    private static final String COLUMN_EXPIRY_DATE = "expiry_date";
    private static final String COLUMN_INSTRUCTIONS = "instructions";
    private static final String COLUMN_RECIPE_ID = "recipe_id";
    private static final String COLUMN_INGREDIENT_NAME = "ingredient_name";
    private static final String COLUMN_REQUIRED_QUANTITY = "required_quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY_ITEMS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL CHECK(length(trim(" + COLUMN_NAME + ")) > 0), "
                + COLUMN_QUANTITY + " REAL NOT NULL CHECK(" + COLUMN_QUANTITY + " > 0), "
                + COLUMN_UNIT + " TEXT NOT NULL CHECK(length(trim(" + COLUMN_UNIT + ")) > 0), "
                + COLUMN_EXPIRY_DATE + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL CHECK(length(trim(" + COLUMN_NAME + ")) > 0), "
                + COLUMN_INSTRUCTIONS + " TEXT NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_RECIPE_ID + " INTEGER NOT NULL, "
                + COLUMN_INGREDIENT_NAME + " TEXT NOT NULL CHECK(length(trim(" + COLUMN_INGREDIENT_NAME + ")) > 0), "
                + COLUMN_REQUIRED_QUANTITY + " REAL NOT NULL CHECK(" + COLUMN_REQUIRED_QUANTITY + " > 0), "
                + COLUMN_UNIT + " TEXT NOT NULL CHECK(length(trim(" + COLUMN_UNIT + ")) > 0), "
                + "FOREIGN KEY(" + COLUMN_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES
                + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE INDEX idx_recipe_ingredients_recipe_id ON "
                + TABLE_RECIPE_INGREDIENTS + "(" + COLUMN_RECIPE_ID + ")");
        db.execSQL("CREATE INDEX idx_recipe_ingredients_name ON "
                + TABLE_RECIPE_INGREDIENTS + "(" + COLUMN_INGREDIENT_NAME + ")");
        db.execSQL("CREATE UNIQUE INDEX idx_recipes_name ON "
                + TABLE_RECIPES + "(" + COLUMN_NAME + ")");
        seedRecipesIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_recipes_name ON "
                    + TABLE_RECIPES + "(" + COLUMN_NAME + ")");
            seedRecipesIfNeeded(db);
        }
    }

    public long insertPantryItem(PantryItem item) {
        validatePantryItem(item);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = toContentValues(item);
        return db.insertOrThrow(TABLE_PANTRY_ITEMS, null, values);
    }

    public List<PantryItem> getAllPantryItems() {
        SQLiteDatabase db = getReadableDatabase();
        List<PantryItem> items = new ArrayList<>();
        String orderBy = COLUMN_NAME + " COLLATE NOCASE ASC, " + COLUMN_ID + " ASC";

        try (Cursor cursor = db.query(TABLE_PANTRY_ITEMS, null, null, null,
                null, null, orderBy)) {
            while (cursor.moveToNext()) {
                items.add(readPantryItem(cursor));
            }
        }
        return items;
    }

    public PantryItem getPantryItemById(long id) {
        if (id <= 0) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        try (Cursor cursor = db.query(TABLE_PANTRY_ITEMS, null, selection, selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return readPantryItem(cursor);
            }
        }
        return null;
    }

    public int updatePantryItem(PantryItem item) {
        validatePantryItem(item);
        if (item.getId() <= 0) {
            throw new IllegalArgumentException("Pantry item id must be greater than zero");
        }

        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(item.getId())};
        return db.update(TABLE_PANTRY_ITEMS, toContentValues(item), whereClause, whereArgs);
    }

    public int deletePantryItem(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Pantry item id must be greater than zero");
        }

        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return db.delete(TABLE_PANTRY_ITEMS, whereClause, whereArgs);
    }

    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Recipe> recipes = new ArrayList<>();
        String orderBy = COLUMN_NAME + " COLLATE NOCASE ASC, " + COLUMN_ID + " ASC";

        try (Cursor cursor = db.query(TABLE_RECIPES, null, null, null,
                null, null, orderBy)) {
            while (cursor.moveToNext()) {
                recipes.add(readRecipe(cursor));
            }
        }
        return recipes;
    }

    public Recipe getRecipeById(long id) {
        if (id <= 0) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        try (Cursor cursor = db.query(TABLE_RECIPES, null, selection, selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return readRecipe(cursor);
            }
        }
        return null;
    }

    public List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        if (recipeId <= 0) {
            return new ArrayList<>();
        }

        SQLiteDatabase db = getReadableDatabase();
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String selection = COLUMN_RECIPE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(recipeId)};
        String orderBy = COLUMN_ID + " ASC";
        try (Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, selection, selectionArgs,
                null, null, orderBy)) {
            while (cursor.moveToNext()) {
                ingredients.add(readRecipeIngredient(cursor));
            }
        }
        return ingredients;
    }

    public int countRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        return countRecipes(db);
    }

    private int countRecipes(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private void seedRecipesIfNeeded(SQLiteDatabase db) {
        if (countRecipes(db) > 0) {
            return;
        }

        boolean startedTransaction = !db.inTransaction();
        if (startedTransaction) {
            db.beginTransaction();
        }
        try {
            for (RecipeSeedData.SeedRecipe seedRecipe : RecipeSeedData.getRecipes()) {
                ContentValues recipeValues = new ContentValues();
                recipeValues.put(COLUMN_NAME, seedRecipe.getName());
                recipeValues.put(COLUMN_INSTRUCTIONS, seedRecipe.getInstructions());
                long recipeId = db.insertOrThrow(TABLE_RECIPES, null, recipeValues);

                for (RecipeSeedData.SeedIngredient seedIngredient : seedRecipe.getIngredients()) {
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(COLUMN_RECIPE_ID, recipeId);
                    ingredientValues.put(COLUMN_INGREDIENT_NAME, seedIngredient.getName());
                    ingredientValues.put(COLUMN_REQUIRED_QUANTITY, seedIngredient.getQuantity());
                    ingredientValues.put(COLUMN_UNIT, seedIngredient.getUnit());
                    db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
                }
            }
            if (startedTransaction) {
                db.setTransactionSuccessful();
            }
        } finally {
            if (startedTransaction) {
                db.endTransaction();
            }
        }
    }

    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, item.getName().trim());
        values.put(COLUMN_QUANTITY, item.getQuantity());
        values.put(COLUMN_UNIT, item.getUnit().trim());
        if (item.getExpiryDate() == null || item.getExpiryDate().trim().isEmpty()) {
            values.putNull(COLUMN_EXPIRY_DATE);
        } else {
            values.put(COLUMN_EXPIRY_DATE, item.getExpiryDate().trim());
        }
        return values;
    }

    private PantryItem readPantryItem(Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME);
        int quantityIndex = cursor.getColumnIndexOrThrow(COLUMN_QUANTITY);
        int unitIndex = cursor.getColumnIndexOrThrow(COLUMN_UNIT);
        int expiryIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE);
        String expiryDate = cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex);

        return new PantryItem(
                cursor.getLong(idIndex),
                cursor.getString(nameIndex),
                cursor.getDouble(quantityIndex),
                cursor.getString(unitIndex),
                expiryDate
        );
    }

    private Recipe readRecipe(Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME);
        int instructionsIndex = cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS);
        return new Recipe(cursor.getLong(idIndex), cursor.getString(nameIndex),
                cursor.getString(instructionsIndex));
    }

    private RecipeIngredient readRecipeIngredient(Cursor cursor) {
        int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int recipeIdIndex = cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_INGREDIENT_NAME);
        int quantityIndex = cursor.getColumnIndexOrThrow(COLUMN_REQUIRED_QUANTITY);
        int unitIndex = cursor.getColumnIndexOrThrow(COLUMN_UNIT);
        return new RecipeIngredient(cursor.getLong(idIndex), cursor.getLong(recipeIdIndex),
                cursor.getString(nameIndex), cursor.getDouble(quantityIndex),
                cursor.getString(unitIndex));
    }

    private void validatePantryItem(PantryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Pantry item cannot be null");
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        if (Double.isNaN(item.getQuantity()) || Double.isInfinite(item.getQuantity())
                || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be a finite number greater than zero");
        }
        if (item.getUnit() == null || item.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty");
        }
    }
}
