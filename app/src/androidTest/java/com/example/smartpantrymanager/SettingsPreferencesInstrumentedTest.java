package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Verifies that the Settings screen's preference storage survives recreation. */
public class SettingsPreferencesInstrumentedTest {
    private SharedPreferences preferences;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        preferences = context.getSharedPreferences(
                SettingsActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }

    @After
    public void tearDown() {
        preferences.edit().clear().commit();
    }

    @Test
    public void preferenceDefaultsToDisabled() {
        assertFalse(preferences.getBoolean(
                SettingsActivity.EXPIRY_ALERTS_ENABLED_KEY, false));
    }

    @Test
    public void changedPreferenceIsReadAfterReopeningPreferences() {
        preferences.edit()
                .putBoolean(SettingsActivity.EXPIRY_ALERTS_ENABLED_KEY, true)
                .commit();

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences reopenedPreferences = context.getSharedPreferences(
                SettingsActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        assertTrue(reopenedPreferences.getBoolean(
                SettingsActivity.EXPIRY_ALERTS_ENABLED_KEY, false));
    }
}
