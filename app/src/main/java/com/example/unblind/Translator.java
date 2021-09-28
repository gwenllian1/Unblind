package com.example.unblind;

import android.content.Context;
import android.util.Log;

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

    /**
     * This is a function that will be called inside the constructor to load and store data into our vocabulary.
     * @param context: the class that contains useful information about the runtime environment.
     */
    private void setup(Context context) {
        String fileName = "vocab_data.txt";
        String content = getStringFromFile(fileName, context);      // get all the content of the vocab file.
        String[] items = content.split(";");        // split the data to build vocab data structure
        for (String item : items) {
            String[] itemLanguage = item.split(",");
            vocabulary.add(itemLanguage);
        }
    }

    /**
     * This is a function that will convert a stream input into a string.
     * @param inputStream : the stream of input as bytes
     * @return : a string for given input stream
     * @throws Exception
     */
    public static String convertStreamToString(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));      // read data from txt file
        StringBuilder stringBuilder = new StringBuilder();      // initialize a string builder
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");    // append each line from the file into the created stream builder.
        }
        reader.close();     // close the reader from the input file
        return stringBuilder.toString();    // convert string builder to string and return.
    }


    /**
     * This function will get the string data from a given file
     * @param filePath : the path to file( filename) for files in assets folder.
     * @param context : the runtime information bundle.
     * @return: string for the data from the file.
     */
    public static String getStringFromFile(String filePath, Context context) {
        InputStream inputStream = null;     // initialize the input data stream
        try {
            inputStream = context.getAssets().open(filePath);   // set it to the passed in filename
        } catch (IOException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            returnString = convertStreamToString(inputStream);  // read the data from stream to return
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            assert inputStream != null;
            inputStream.close();    // close the stream to avoid any memory leaks.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    /**
     * This function will be used inside the TTS engine to find the text translation for given label
     * @param englishLabel: the original label, which will be used to find the translation.
     * @param desiredLanguageCode: the index of that string (0-1-2) in the language item String array.
     * @return the string represents the label of the word ( can be in different languages)
     */
    public String searchMatchingLanguageLabel(String englishLabel, int desiredLanguageCode) {
        for (String[] item : vocabulary) {  // find the label inside the dictionary
            if (item[0].toLowerCase().trim().equals(englishLabel.toLowerCase().trim()) && item.length == 3) {
                return item[desiredLanguageCode];
            }
        }
        return englishLabel;        // return english label if there are no matched string for the other language found.
    }
}
