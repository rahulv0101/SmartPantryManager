package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class PantryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        findViewById(R.id.addIngredientButton).setOnClickListener(view ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        findViewById(R.id.suggestedRecipesButton).setOnClickListener(view -> openMainSection(SuggestedRecipesActivity.class));
        findViewById(R.id.settingsButton).setOnClickListener(view -> openMainSection(SettingsActivity.class));
    }

    private void openMainSection(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
