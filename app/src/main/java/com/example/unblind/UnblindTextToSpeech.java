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
    private String[] supportedLanguages = {"en", "es","zh"};
    private Translator translator;

    public UnblindTextToSpeech(UnblindAccessibilityService unblind) {
        setUpTextToSpeech(unblind);
    }

    /**
     * This is the initialization function for TTS, which will be called once we start the engine
     * @param unblind: the actual context for useful information about runtime environment.
     */
    public void setUpTextToSpeech(UnblindAccessibilityService unblind){
        translator = new Translator(unblind);

        defaultTextToSpeech = new TextToSpeech(unblind.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    // set up the TTS config and set the value for ttsReady to be true.
                    updateTTSConfig(unblind);
                    ttsReady = true;
                // Error handling for TTS initialization.
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

    /**
     * This function will change all the setting of current text-to-speech to match with the
     * system configuration for TTS.
     * @param context: the package that contains all useful information about the current environment.
     */
    public void updateTTSConfig(Context context){
        int speechRate = 100;       // initialize the speech rate to be 100
        int pitch = 100;            // initialize the pitch to be 100

        // try to fetch the speechrate and pitch setting from Android system.
        try {
            speechRate = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE);
            Log.d(TAG, "The rate is: " + speechRate );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        try {
            pitch = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH);
            Log.d(TAG, "The pitch is: " + pitch);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        // get the current locale of default TTS.
        Locale locale = defaultTextToSpeech.getDefaultVoice().getLocale();

        // modifying the current TTS engine with fetched information
        defaultTextToSpeech.setLanguage(locale);
        defaultTextToSpeech.setSpeechRate((float) speechRate /100 );
        defaultTextToSpeech.setPitch((float) pitch /100 );
        Log.d(TAG,locale.toLanguageTag());
        // Set the dictionary language.
        languageCode = 0;
        for(int i = 0; i < supportedLanguages.length;i++ ){
            if(locale.toLanguageTag().startsWith(supportedLanguages[i])){
                languageCode = i;
                break;
            }
        }
    }

    public void resetTts(float ttsPitch){
        defaultTextToSpeech.setPitch(1.0f);
        defaultTextToSpeech.setSpeechRate(1.0f);
    }


}