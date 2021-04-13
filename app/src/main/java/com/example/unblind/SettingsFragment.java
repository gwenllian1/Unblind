package com.example.unblind;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference button = findPreference(getString(R.string.accessibility_button));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Add code for team two. Code below for when Settings Preference is clicked
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return true;
            }
        });
    }



}