package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

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
        pantryAdapter = new PantryAdapter(new PantryAdapter.OnItemActionListener() {
            @Override
            public void onEdit(PantryItem item) {
                openEditIngredient(item);
            }

            @Override
            public void onDelete(PantryItem item) {
                confirmDelete(item);
            }
        });
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

    private void openEditIngredient(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_PANTRY_ITEM_ID, item.getId());
        startActivity(intent);
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .show();
    }

    private void deleteItem(PantryItem item) {
        try {
            int deletedRows = databaseHelper.deletePantryItem(item.getId());
            if (deletedRows == 1) {
                loadPantryItems();
                android.widget.Toast.makeText(this, R.string.ingredient_deleted_message,
                        android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(this, R.string.ingredient_delete_failed_message,
                        android.widget.Toast.LENGTH_LONG).show();
                loadPantryItems();
            }
        } catch (RuntimeException exception) {
            android.widget.Toast.makeText(this, R.string.ingredient_delete_failed_message,
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }
}
