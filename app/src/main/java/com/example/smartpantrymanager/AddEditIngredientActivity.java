package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditIngredientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        findViewById(R.id.saveIngredientButton).setOnClickListener(view ->
                Toast.makeText(this, R.string.development_save_placeholder, Toast.LENGTH_LONG).show());

        findViewById(R.id.cancelIngredientButton).setOnClickListener(view -> finish());
    }
}
