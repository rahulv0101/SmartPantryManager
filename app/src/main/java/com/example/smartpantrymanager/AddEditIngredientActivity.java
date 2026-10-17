package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {
    public static final String EXTRA_PANTRY_ITEM_ID =
            "com.example.smartpantrymanager.EXTRA_PANTRY_ITEM_ID";

    private EditText ingredientNameEditText;
    private EditText quantityEditText;
    private Spinner unitSpinner;
    private EditText expiryDateEditText;
    private Button saveButton;
    private DatabaseHelper databaseHelper;
    private long pantryItemId;
    private boolean isEditing;
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

        pantryItemId = getIntent().getLongExtra(EXTRA_PANTRY_ITEM_ID, 0L);
        isEditing = pantryItemId > 0;
        configureForm();

        expiryDateEditText.setOnClickListener(view -> showExpiryDatePicker());
        saveButton.setOnClickListener(view -> saveIngredient());
        findViewById(R.id.cancelIngredientButton).setOnClickListener(view -> finish());

        if (isEditing) {
            loadIngredientForEditing();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }

    private void configureForm() {
        TextView titleTextView = findViewById(R.id.ingredientFormTitleTextView);
        if (isEditing) {
            titleTextView.setText(R.string.edit_ingredient);
            saveButton.setText(R.string.save);
        } else {
            titleTextView.setText(R.string.add_ingredient);
        }
    }

    private void loadIngredientForEditing() {
        PantryItem item = databaseHelper.getPantryItemById(pantryItemId);
        if (item == null) {
            Toast.makeText(this, R.string.ingredient_not_found_message, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ingredientNameEditText.setText(item.getName());
        DecimalFormat quantityFormat = new DecimalFormat("0.####",
                DecimalFormatSymbols.getInstance(Locale.US));
        quantityEditText.setText(quantityFormat.format(item.getQuantity()));
        setUnitSelection(item.getUnit());
        expiryDateEditText.setText(item.getExpiryDate() == null ? "" : item.getExpiryDate());
    }

    private void setUnitSelection(String unit) {
        if (unit == null) {
            return;
        }
        for (int index = 0; index < unitSpinner.getCount(); index++) {
            if (unit.equals(unitSpinner.getItemAtPosition(index).toString())) {
                unitSpinner.setSelection(index);
                return;
            }
        }
        unitSpinner.setSelection(0);
    }

    private void showExpiryDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> expiryDateEditText.setText(
                        String.format(Locale.US, "%04d-%02d-%02d",
                                year, month + 1, dayOfMonth)),
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
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

        String unit = unitSpinner.getSelectedItem() == null
                ? "" : unitSpinner.getSelectedItem().toString();
        if (!IngredientInputValidator.isSupportedUnit(unit)) {
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
        PantryItem item = new PantryItem(pantryItemId, name, quantity,
                unit, expiryDate.isEmpty() ? null : expiryDate);
        try {
            if (isEditing) {
                int updatedRows = databaseHelper.updatePantryItem(item);
                if (updatedRows != 1) {
                    throw new IllegalStateException("SQLite did not update the requested row");
                }
                Toast.makeText(this, R.string.ingredient_updated_message, Toast.LENGTH_SHORT).show();
            } else {
                long insertedId = databaseHelper.insertPantryItem(item);
                if (insertedId <= 0) {
                    throw new IllegalStateException("SQLite did not return a new row id");
                }
                Toast.makeText(this, R.string.ingredient_saved_message, Toast.LENGTH_SHORT).show();
            }
            setResult(RESULT_OK);
            finish();
        } catch (RuntimeException exception) {
            isSaving = false;
            saveButton.setEnabled(true);
            Toast.makeText(this,
                    isEditing ? R.string.ingredient_update_failed_message
                            : R.string.ingredient_save_failed_message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void clearValidationErrors() {
        ingredientNameEditText.setError(null);
        quantityEditText.setError(null);
        expiryDateEditText.setError(null);
    }
}
