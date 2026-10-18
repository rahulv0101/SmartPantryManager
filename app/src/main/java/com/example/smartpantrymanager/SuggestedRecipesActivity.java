package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestedRecipesActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private RecipeAdapter recipeAdapter;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        databaseHelper = new DatabaseHelper(this);
        emptyStateTextView = findViewById(R.id.emptyRecipesTextView);
        recipeAdapter = new RecipeAdapter(this::openRecipeDetails);
        RecyclerView recipeRecyclerView = findViewById(R.id.recipeRecyclerView);
        recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeRecyclerView.setAdapter(recipeAdapter);

        findViewById(R.id.pantryButton).setOnClickListener(view -> openSection(PantryActivity.class));
        findViewById(R.id.settingsButton).setOnClickListener(view -> openSection(SettingsActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMatchedRecipes();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }

    private void loadMatchedRecipes() {
        List<Recipe> recipes = databaseHelper.getAllRecipes();
        Map<Long, List<RecipeIngredient>> requirementsByRecipe = new HashMap<>();
        for (Recipe recipe : recipes) {
            requirementsByRecipe.put(recipe.getId(),
                    databaseHelper.getRecipeIngredients(recipe.getId()));
        }

        List<Recipe> matches = RecipeMatcher.findMatchingRecipes(
                recipes, requirementsByRecipe, databaseHelper.getAllPantryItems());
        recipeAdapter.setRecipes(matches);
        emptyStateTextView.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openRecipeDetails(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    private void openSection(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
