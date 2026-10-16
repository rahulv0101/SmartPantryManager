package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

import java.util.List;

public class PantryActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        databaseHelper = new DatabaseHelper(this);
        pantryAdapter = new PantryAdapter();
        emptyStateTextView = findViewById(R.id.emptyPantryTextView);
        RecyclerView pantryRecyclerView = findViewById(R.id.pantryRecyclerView);
        pantryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pantryRecyclerView.setAdapter(pantryAdapter);

        findViewById(R.id.addIngredientButton).setOnClickListener(view ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        findViewById(R.id.suggestedRecipesButton).setOnClickListener(view -> openMainSection(SuggestedRecipesActivity.class));
        findViewById(R.id.settingsButton).setOnClickListener(view -> openMainSection(SettingsActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }

    private void loadPantryItems() {
        List<PantryItem> items = databaseHelper.getAllPantryItems();
        pantryAdapter.setItems(items);
        emptyStateTextView.setVisibility(items.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }

    private void openMainSection(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
