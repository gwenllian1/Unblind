package com.example.unblind.model;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * This class provide basic read method used for loading
 * images and model
 *
 * @author  Team 3
 * @version 1.0
 * @since   05/15/2021
 */
public class Utils {


    /**
     * This method is used for loading the data from a specific file path
     * The loaded file must be inside the assets folder as a convention.
     * @param context Current context of the activity which is using this
     * @param assetName  String for the filename.
     * @return string for the absolute asset file path.
     */
    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);

        try (InputStream is = context.getAssets().open(assetName)) {    // try catch block for validating the file
            try (OutputStream os = new FileOutputStream(file)) {    // inner try catch block
                byte[] buffer = new byte[4 * 1024];     // make buffer for loading file
                int read;       // initialize an int as pointer
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();     // return the filepath
        } catch (IOException e) {
            Log.e("Team 3 Model Utils", "Error process asset " + assetName + " to file path"); // log the error
        }
        return null;
    }

}