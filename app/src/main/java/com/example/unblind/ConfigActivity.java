package com.example.unblind;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ConfigActivity extends AppCompatActivity {

    static final String AUDIO_FEEDBACK_CONFIG = "audio_feedback_config";
    static final String AUDIO_ACCENT = "audio_accent";
    static final String AUDIO_SPEECH_RATE = "audio_speech_rate";
    static final String AUDIO_PITCH = "audio_pitch";

    Locale location = Locale.UK;
    float speechRate = 1.0f;
    float pitch = 1.0f;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);


        sharedPreferences = getSharedPreferences(AUDIO_FEEDBACK_CONFIG,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AUDIO_ACCENT, location.toLanguageTag());
        editor.putFloat(AUDIO_SPEECH_RATE,speechRate);
        editor.putFloat(AUDIO_PITCH, pitch);
        editor.apply();
    }
}
