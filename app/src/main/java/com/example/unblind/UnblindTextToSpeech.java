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

    private static final String TAG = "UnBlind AS";
    private TextToSpeech defaultTextToSpeech;
    private int languageCode = 0;
    // Global Variables which change from user input?
    private Float speechRate;
    private Float pitch;
    private Locale locale;
    private Translator translator;

    public UnblindTextToSpeech(UnblindAccessibilityService unblind) {
        setUpTextToSpeech(unblind);
    }

    public void setUpTextToSpeech(UnblindAccessibilityService unblind){
        translator = new Translator(unblind);
        int fetchedRate = 100;
        int fetchedPitch = 100;
        try {
            fetchedRate = Settings.Secure.getInt(unblind.getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE);
            Log.d("testt", "The rate is: " + fetchedRate );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fetchedPitch = Settings.Secure.getInt(unblind.getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH);
            Log.d("testt", "The pitch is: " + fetchedPitch);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }


//        SharedPreferences sharedPreferences = unblind.getSharedPreferences(ConfigActivity.AUDIO_FEEDBACK_CONFIG,0);;
//        Locale locale = Locale.forLanguageTag(sharedPreferences.getString(ConfigActivity.AUDIO_ACCENT,"en-US")) ;
//        Float speechRate = sharedPreferences.getFloat(ConfigActivity.AUDIO_SPEECH_RATE,1.0f);
//        Float pitch = sharedPreferences.getFloat(ConfigActivity.AUDIO_PITCH,1.0f);
//        languageCode = sharedPreferences.getInt(ConfigActivity.AUDIO_LANGUAGE,0);

        int finalFetchedRate = fetchedRate;
        int finalFetchedPitch = fetchedPitch;
        defaultTextToSpeech = new TextToSpeech(unblind.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Setting locale, speech rate and voice pitch
                    Locale locale = defaultTextToSpeech.getDefaultVoice().getLocale();
                    Log.d(TAG, locale.toLanguageTag() + "");
                    if (locale == Locale.SIMPLIFIED_CHINESE){
                        languageCode = 2;
                    } else if (locale.toLanguageTag().equals("es-ES")){
                        Log.d(TAG,"Set language to Spanish");
                        languageCode = 1;
                    }
                    defaultTextToSpeech.setLanguage(locale);
                    defaultTextToSpeech.setSpeechRate((float) finalFetchedRate /100 );
                    defaultTextToSpeech.setPitch((float) finalFetchedPitch /100 );

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

    public void ttsSpeak( CharSequence text,
                    int queueMode,
                    Bundle params,
                    String utteranceId){

        String translatedText = translator.searchMatchingLanguageLabel((String) text,languageCode);
        defaultTextToSpeech.speak(translatedText, queueMode, params, utteranceId);
    }

    public void changeTTSSpeechrate(float ttsSpeechRate){
        defaultTextToSpeech.setSpeechRate(ttsSpeechRate);
    }
    public void changeTTSPitch(float ttsPitch){
        defaultTextToSpeech.setPitch(ttsPitch);
    }
    public void matchTalkback(float ttsPitch, float ttsSpeechRate){ // get from talkback somehow
        defaultTextToSpeech.setPitch(1.0f);
        defaultTextToSpeech.setSpeechRate(1.0f);
    }
    public void resetTts(float ttsPitch){
        defaultTextToSpeech.setPitch(1.0f);
        defaultTextToSpeech.setSpeechRate(1.0f);
    }
    // Play

}
