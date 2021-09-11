package com.example.unblind;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Translator {

    ArrayList<String[]> vocabulary = new ArrayList<>();

    public Translator(Context context) {
        setup(context);
    }

    private void setup(Context context) {

        String content = getStringFromFile("vocab_data.txt", context);
        System.out.println(content);
        String[] items = content.split(";");
        for (String item : items) {
            String[] itemLanguage = item.split(",");
            vocabulary.add(itemLanguage);
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath, Context context) {
        InputStream fl = null;
        try {
            fl = context.getAssets().open(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ret = null;
        try {
            ret = convertStreamToString(fl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Make sure you close all streams.
        try {
            assert fl != null;
            fl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String searchMatchingLanguageLabel(String englishLabel, int desiredLanguageCode) {
        for (String[] item : vocabulary) {
            if (item[0].toLowerCase().equals(englishLabel.toLowerCase())) {
                return item[desiredLanguageCode];
            }
        }
        return englishLabel;
    }
}
