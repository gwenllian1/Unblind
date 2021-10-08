package com.example.unblind;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Display;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.security.Provider;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    public boolean isAccessServiceEnabled(Context context, Class accessibilityServiceClass)
    {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        return prefString!= null && prefString.contains(context.getPackageName() + "/" + accessibilityServiceClass.getName());
    }

    public void serviceCheck (Preference button) {
        if (isAccessServiceEnabled(getContext(), UnblindAccessibilityService.class)) {
            button.setTitle("Service Status: ON");
        } else {
            button.setTitle("Service Status: OFF");
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference button = findPreference(getString(R.string.service_button));
        Preference ttsOptions = findPreference(getString(R.string.ttsOptions_button));
        Preference notificationButton = findPreference(getString(R.string.activate_notification_key));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Code below for when Settings Preference is clicked
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return true;
            }
        });

        ttsOptions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Code below for when Language is clicked
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
                return true;
            }
        });

        notificationButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Code below for when Notification is clicked
                getActivity().stopService(new Intent(getActivity(), ModelService.class));
                getActivity().startService(new Intent(getActivity(),ModelService.class));
                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));

        serviceCheck(button);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));

        serviceCheck(button);
    }

}