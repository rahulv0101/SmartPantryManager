package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_NAME = "smart_pantry_preferences";
    public static final String EXPIRY_ALERTS_ENABLED_KEY = "expiry_alerts_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchCompat expiryAlertsSwitch = findViewById(R.id.expiryAlertsSwitch);
        expiryAlertsSwitch.setChecked(getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                .getBoolean(EXPIRY_ALERTS_ENABLED_KEY, false));
        expiryAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(EXPIRY_ALERTS_ENABLED_KEY, isChecked)
                        .apply());

        findViewById(R.id.pantryButton).setOnClickListener(view -> openSection(PantryActivity.class));
        findViewById(R.id.suggestedRecipesButton).setOnClickListener(view -> openSection(SuggestedRecipesActivity.class));
    }

    private void openSection(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
