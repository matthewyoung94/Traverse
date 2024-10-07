package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String DARK_MODE_KEY = "light_dark_toggle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Theme: " + getTheme().toString());
        getSupportActionBar().hide();
        setTheme(R.style.Theme_MyApplication);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static boolean isDarkModeEnabled(SharedPreferences prefs) {
        return prefs.getBoolean(DARK_MODE_KEY, false);
    }

    public static void setDarkModeEnabled(SharedPreferences prefs, boolean enabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DARK_MODE_KEY, enabled);
        editor.apply();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.light_dark_preferenes, rootKey);

            SwitchPreferenceCompat darkModeToggle = findPreference(DARK_MODE_KEY);
            darkModeToggle.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isDarkModeEnabled = (Boolean) newValue;
                if (isDarkModeEnabled) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    getActivity().setTheme(R.style.Theme_MyApplication_Dark);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    getActivity().setTheme(R.style.Theme_MyApplication);
                }
                requireActivity().recreate();
                return true;
            });
        }
    }
}
