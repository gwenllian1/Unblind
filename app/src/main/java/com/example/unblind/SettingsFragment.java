//        Copyright 2021 Project 3

//        This file is part of UnBlind.
//
//        UnBlind is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        UnBlind is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with UnBlind.  If not, see <https://www.gnu.org/licenses/>.

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


    /**
     * Returns a boolean value of whether the Unblind Service is running or not.
     * Used as a helper function for a condition check in serviceCheck.
     *
     * @param  context  the current context
     * @param  accessibilityServiceClass the unblind service class
     * @return      a boolean value (true/false)
     */
    public boolean isAccessServiceEnabled(Context context, Class accessibilityServiceClass)
    {
        // get currently running accessibility service, will be null if none are running
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        // return true if the Unblind service is running, and false if it isn't
        return prefString!= null && prefString.contains(context.getPackageName() + "/" + accessibilityServiceClass.getName());
    }


    /**
     * Sets the Service Status text to On or Off in the UI, depending on whether the service is
     * running at the time.
     *
     * @param  button  the UI button whose text will be edited.
     */
    public void serviceCheck (Preference button) {
        // if statement to check if the Unblind service is currently running
        if (isAccessServiceEnabled(getContext(), UnblindAccessibilityService.class)) {
            button.setTitle("Service Status: ON"); // set service status button text to ON
        } else {
            button.setTitle("Service Status: OFF"); // set service status button text to OFF
        }
    }


    /**
     * Listener function for the preference settings. Will trigger activities based on what
     * button is pressed in the UI.
     *
     * @param  savedInstanceState  the state of the app
     * @param  rootKey the preference key of the PreferenceScreen to use as the root of the preference hierarchy, or null to use the root PreferenceScreen.
     * @return      true on button press
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference button = findPreference(getString(R.string.service_button));
        Preference ttsOptions = findPreference(getString(R.string.ttsOptions_button));
        Preference notificationButton = findPreference(getString(R.string.activate_notification_key));

        // listener for the Service Status button
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Send user to Accessibility Settings when Service Status is clicked
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return true;
            }
        });

        // listener for the Language button
        ttsOptions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Send user to text-to-speech settings when Language is clicked
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
                return true;
            }
        });

        // listener for the Notification button
        notificationButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Reactivate Notification when the Notification button is clicked
                getActivity().stopService(new Intent(getActivity(), ModelService.class));
                getActivity().startService(new Intent(getActivity(),ModelService.class));
                return true;
            }
        });

    }


    /**
     * Utilises the android onResume method to update the service status text when the service
     * is enabled/disabled.
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));
        serviceCheck(button);

    }


    /**
     * Utilises the android onStart method to update the service status text when the service
     * is enabled/disabled.
     *
     */
    @Override
    public void onStart() {
        super.onStart();

        // Service Status Display (ON/OFF)
        Preference button = findPreference(getString(R.string.service_button));
        serviceCheck(button);
    }

}