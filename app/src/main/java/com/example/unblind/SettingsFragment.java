package com.example.unblind;

import static android.content.Context.ACCESSIBILITY_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    Context mContext = this.getContext();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference button = findPreference(getString(R.string.service_button));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Code below for when Settings Preference is clicked
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                AccessibilityManager am = (AccessibilityManager) getContext().getSystemService(ACCESSIBILITY_SERVICE);
                boolean isUnblindEnabled = am.isEnabled();
                if (isUnblindEnabled) {
                    button.setTitle("Service Status: ON");
                }
                else {
                    button.setTitle("Service Status: OFF");
                }
                return true;
            }
        });
        Preference advancedSettingButton = findPreference(getString(R.string.advanced_settings));
        advancedSettingButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Code below for when Settings Preference is clicked
                Intent intent = new Intent(getContext(), ConfigActivity.class);
                startActivity(intent);
                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));
        AccessibilityManager am = (AccessibilityManager) getContext().getSystemService(ACCESSIBILITY_SERVICE);
        boolean isUnblindEnabled = am.isEnabled();
        if (isUnblindEnabled) {
            button.setTitle("Service Status: ON");
        } else {
            button.setTitle("Service Status: OFF");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        //setContentView(R.layout.activity_main);
        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));
        AccessibilityManager am = (AccessibilityManager) getContext().getSystemService(ACCESSIBILITY_SERVICE);
        boolean isUnblindEnabled = am.isEnabled();
        if (isUnblindEnabled) {
            button.setTitle("Service Status: ON");
        } else {
            button.setTitle("Service Status: OFF");
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));
        AccessibilityManager am = (AccessibilityManager) getContext().getSystemService(ACCESSIBILITY_SERVICE);
        boolean isUnblindEnabled = am.isEnabled();
        if (isUnblindEnabled) {
            button.setTitle("Service Status: ON");
        } else {
            button.setTitle("Service Status: OFF");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));
        AccessibilityManager am = (AccessibilityManager) getContext().getSystemService(ACCESSIBILITY_SERVICE);
        boolean isUnblindEnabled = am.isEnabled();
        if (isUnblindEnabled) {
            button.setTitle("Service Status: ON");
        } else {
            button.setTitle("Service Status: OFF");
        }

    }

}