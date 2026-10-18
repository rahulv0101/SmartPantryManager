package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_ID =
            "com.example.smartpantrymanager.EXTRA_RECIPE_ID";

    private DatabaseHelper databaseHelper;
    private TextView recipeNameTextView;
    private TextView ingredientsTextView;
    private TextView instructionsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        databaseHelper = new DatabaseHelper(this);
        recipeNameTextView = findViewById(R.id.recipeNameTextView);
        ingredientsTextView = findViewById(R.id.recipeIngredientsTextView);
        instructionsTextView = findViewById(R.id.recipeInstructionsTextView);
        findViewById(R.id.recipeDetailBackButton).setOnClickListener(view -> finish());

        loadRecipe();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }

    private void loadRecipe() {
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, 0L);
        Recipe recipe = databaseHelper.getRecipeById(recipeId);
        if (recipe == null) {
            recipeNameTextView.setText(R.string.recipe_not_found_message);
            ingredientsTextView.setText(R.string.recipe_not_found_message);
            instructionsTextView.setText(R.string.recipe_not_found_message);
            return;
        }

        recipeNameTextView.setText(recipe.getName());
        instructionsTextView.setText(recipe.getInstructions());
        ingredientsTextView.setText(formatIngredients(
                databaseHelper.getRecipeIngredients(recipe.getId())));
    }

    private String formatIngredients(List<RecipeIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return getString(R.string.recipe_ingredients_unavailable);
        }

        DecimalFormat quantityFormat = new DecimalFormat("0.####",
                DecimalFormatSymbols.getInstance(Locale.US));
        StringBuilder builder = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(getString(R.string.recipe_ingredient_format,
                    ingredient.getIngredientName(),
                    quantityFormat.format(ingredient.getRequiredQuantity()),
                    ingredient.getUnit()));
        }
        return builder.toString();
    }
}
