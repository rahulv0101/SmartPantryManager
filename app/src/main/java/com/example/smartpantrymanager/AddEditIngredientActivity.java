package com.example.smartpantrymanager;

import android.os.Bundle;
import android.app.DatePickerDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditIngredientActivity extends AppCompatActivity {
    private EditText ingredientNameEditText;
    private EditText quantityEditText;
    private Spinner unitSpinner;
    private EditText expiryDateEditText;
    private Button saveButton;
    private DatabaseHelper databaseHelper;
    private boolean isSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        databaseHelper = new DatabaseHelper(this);
        ingredientNameEditText = findViewById(R.id.ingredientNameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        unitSpinner = findViewById(R.id.unitSpinner);
        expiryDateEditText = findViewById(R.id.expiryDateEditText);
        saveButton = findViewById(R.id.saveIngredientButton);

        expiryDateEditText.setOnClickListener(view -> showExpiryDatePicker());
        saveButton.setOnClickListener(view -> saveIngredient());

        findViewById(R.id.cancelIngredientButton).setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }

    private void showExpiryDatePicker() {
        java.util.Calendar today = java.util.Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> expiryDateEditText.setText(
                        String.format(java.util.Locale.US, "%04d-%02d-%02d",
                                year, month + 1, dayOfMonth)),
                today.get(java.util.Calendar.YEAR),
                today.get(java.util.Calendar.MONTH),
                today.get(java.util.Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveIngredient() {
        if (isSaving) {
            return;
        }

        clearValidationErrors();
        String name = IngredientInputValidator.normalizeName(
                ingredientNameEditText.getText().toString());
        if (name.isEmpty()) {
            ingredientNameEditText.setError(getString(R.string.name_required_error));
            ingredientNameEditText.requestFocus();
            return;
        }

        String quantityText = quantityEditText.getText().toString().trim();
        if (quantityText.isEmpty()) {
            quantityEditText.setError(getString(R.string.quantity_required_error));
            quantityEditText.requestFocus();
            return;
        }
        Double quantity = IngredientInputValidator.parsePositiveQuantity(quantityText);
        if (quantity == null) {
            quantityEditText.setError(getString(R.string.quantity_invalid_error));
            quantityEditText.requestFocus();
            return;
        }

        if (unitSpinner.getSelectedItemPosition() == 0) {
            unitSpinner.performClick();
            Toast.makeText(this, R.string.unit_required_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String expiryDate = expiryDateEditText.getText().toString().trim();
        if (!IngredientInputValidator.isValidOptionalExpiryDate(expiryDate)) {
            expiryDateEditText.setError(getString(R.string.expiry_date_invalid_error));
            expiryDateEditText.requestFocus();
            return;
        }

        isSaving = true;
        saveButton.setEnabled(false);
        PantryItem item = new PantryItem(name, quantity,
                unitSpinner.getSelectedItem().toString(), expiryDate.isEmpty() ? null : expiryDate);
        try {
            long insertedId = databaseHelper.insertPantryItem(item);
            if (insertedId <= 0) {
                throw new IllegalStateException("SQLite did not return a new row id");
            }
            Toast.makeText(this, R.string.ingredient_saved_message, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (RuntimeException exception) {
            isSaving = false;
            saveButton.setEnabled(true);
            Toast.makeText(this, R.string.ingredient_save_failed_message, Toast.LENGTH_LONG).show();
        }
    }

    private void clearValidationErrors() {
        ingredientNameEditText.setError(null);
        quantityEditText.setError(null);
        expiryDateEditText.setError(null);
    }
}
