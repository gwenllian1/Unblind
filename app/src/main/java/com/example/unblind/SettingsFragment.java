package com.example.unblind;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

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

        Intent serviceIntent = new Intent(this.getContext(), ExampleService.class);
        ContextCompat.startForegroundService(this.getContext(), serviceIntent); // Auto start service

        SwitchPreferenceCompat activateSwitch = (SwitchPreferenceCompat) findPreference(getString(R.string.activate_service_key));
        activateSwitch.setChecked(true); // Always set switch as activated when opened

        activateSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    ContextCompat.startForegroundService(SettingsFragment.this.requireContext(), serviceIntent);
                } else {
                    getActivity().stopService(serviceIntent);
                }
                return true;
            }
        });

    }




}