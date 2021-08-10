package com.example.unblind;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SwitchPreferenceCompat activateSwitch;
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

        Intent serviceIntent = new Intent(this.getContext(), ModelService.class);
        ContextCompat.startForegroundService(this.getContext(), serviceIntent); // Auto start service

        activateSwitch = (SwitchPreferenceCompat) findPreference(getString(R.string.activate_service_key));
        activateSwitch.setChecked(false); // Always set switch as unactivated when opened

        activateSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((boolean) newValue) {
                    ContextCompat.startForegroundService(SettingsFragment.this.requireContext(), serviceIntent);
                    popupDialog();

                } else {
                    getActivity().stopService(serviceIntent);
                }
                return true;
            }
        });

    }


    public void popupDialog(){
        AlertDialog.Builder popup = new AlertDialog.Builder(getContext());
        final View popupView = getLayoutInflater().inflate(R.layout.popup_window, null);
        popup.setView(popupView);

        Button goToSettings = (Button) popupView.findViewById(R.id.go_to_settings);
        Button cancel = (Button) popupView.findViewById(R.id.cancel_button);
        AlertDialog newPopup = popup.create();
        newPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        newPopup.show();


        goToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                newPopup.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPopup.dismiss();
                activateSwitch.setChecked(false);
            }
        });
    }

}