package com.example.unblind;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UnblindTextToSpeech {
    private boolean ttsReady = false;
    private static final String TAG = "UnBlind AS";
    private TextToSpeech defaultTextToSpeech;
    private int languageCode = 0;
    // Global Variables which change from user input?
    private int speechRate = 100;
    private int pitch = 100;
    private Locale locale;
    private Translator translator;

    public UnblindTextToSpeech(UnblindAccessibilityService unblind) {
        setUpTextToSpeech(unblind);
    }

    public void setUpTextToSpeech(UnblindAccessibilityService unblind){
        translator = new Translator(unblind);

        defaultTextToSpeech = new TextToSpeech(unblind.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Setting locale, speech rate and voice pitch
                   // Log.d(TAG, locale.toLanguageTag() + "");

                    updateTTSConfig(unblind,defaultTextToSpeech);
                    ttsReady = true;
                } else if (status == TextToSpeech.ERROR) {
                    Log.d(TAG,"Error starting Text-to-speech");
                } else if (status == TextToSpeech.LANG_NOT_SUPPORTED  ) {
                    Log.d(TAG,"Language not supported for Text-to-speech");
                }  else {
                    Log.d(TAG,"Text-to-speech failed, error code: " +status);
                }
            }
        });
    }

    public boolean isTtsReady(){
        return ttsReady;
    }
    public void ttsSpeak( CharSequence text,
                    int queueMode,
                    Bundle params,
                    String utteranceId){

        String translatedText = translator.searchMatchingLanguageLabel((String) text,languageCode);
        defaultTextToSpeech.speak(translatedText, queueMode, params, utteranceId);
    }

    public void updateTTSConfig(Context context, TextToSpeech textToSpeech){
        try {
            speechRate = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE);
            Log.d("testt", "The rate is: " + speechRate );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        try {
            pitch = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH);
            Log.d("testt", "The pitch is: " + pitch);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        locale = defaultTextToSpeech.getDefaultVoice().getLocale();

        defaultTextToSpeech.setLanguage(locale);
        defaultTextToSpeech.setSpeechRate((float) speechRate /100 );
        defaultTextToSpeech.setPitch((float) pitch /100 );
        if (locale == Locale.SIMPLIFIED_CHINESE){
            languageCode = 2;
        } else if (locale.toLanguageTag().equals("es-ES")){
            Log.d(TAG,"Set language to Spanish");
            languageCode = 1;
        } else {
            languageCode = 0;
        }
    }

    public void resetTts(float ttsPitch){
        defaultTextToSpeech.setPitch(1.0f);
        defaultTextToSpeech.setSpeechRate(1.0f);
    }
    // Play

}
