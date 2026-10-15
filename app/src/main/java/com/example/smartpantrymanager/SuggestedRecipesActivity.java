package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SuggestedRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        findViewById(R.id.recipeDetailsButton).setOnClickListener(view ->
                startActivity(new Intent(this, RecipeDetailActivity.class)));
        findViewById(R.id.pantryButton).setOnClickListener(view -> openSection(PantryActivity.class));
        findViewById(R.id.settingsButton).setOnClickListener(view -> openSection(SettingsActivity.class));
    }

    private void openSection(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
