package com.example.unblind;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    public UnblindTextToSpeech(UnblindAccessibilityService unblind) {
        setUpTextToSpeech(unblind);
    }

    public void setUpTextToSpeech(UnblindAccessibilityService unblind){
        SharedPreferences sharedPreferences = unblind.getSharedPreferences(ConfigActivity.AUDIO_FEEDBACK_CONFIG,0);;
        Locale locale = Locale.forLanguageTag(sharedPreferences.getString(ConfigActivity.AUDIO_ACCENT,"en-US")) ;
        Float speechRate = sharedPreferences.getFloat(ConfigActivity.AUDIO_SPEECH_RATE,1.0f);
        Float pitch = sharedPreferences.getFloat(ConfigActivity.AUDIO_PITCH,1.0f);
        languageCode = sharedPreferences.getInt(ConfigActivity.AUDIO_LANGUAGE,0);

        defaultTextToSpeech = new TextToSpeech(unblind.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Setting locale, speech rate and voice pitch
                    defaultTextToSpeech.setLanguage(locale);
                    defaultTextToSpeech.setSpeechRate(speechRate);
                    defaultTextToSpeech.setPitch(pitch);

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
        defaultTextToSpeech.speak(text, queueMode, params, utteranceId);
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
