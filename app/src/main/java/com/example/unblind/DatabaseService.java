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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A service that provides access to the mediator and item catalogue.
 */
public class DatabaseService extends Service {
    private final IBinder binder = new LocalBinder();
    private static final String TAG = "UnBlindDatabaseService";
    private final UnblindMediator unblindMediator = new UnblindMediator();
    private static MessageDigest messageDigest = null;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Client methods go below

    /**
     * Stores the hashed image as a key with the generated label as its value
     * @param prefName name of the SharedPreference file
     * @param imageKey byte array of the icon
     * @param label corresponding label for the icon
     */
    public void setSharedData(String prefName, byte[] imageKey, String label)
    {
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor spEdit = sp.edit();

        String stringHash = hashBytes(imageKey);
        spEdit.putString(stringHash, label);
        Log.v(TAG, "Edwin: Saved to SP");
        spEdit.commit();
    }

    /**
     * Retrieves the label if its corresponding hashed image exists in the SharedPreference
     * @param prefName name of the SharedPreference file
     * @param imageKey byte array of the icon
     * @return corresponding label for the icon, null if the key was not found
     */
    public String getSharedData(String prefName, byte[] imageKey)
    {
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);

        // default string null
        String stringHash = hashBytes(imageKey);
        String label = sp.getString(stringHash, null);
        Log.v(TAG, "Edwin: Retrieved from SP: " + label);
        return label;
    }

    /**
     * Hashes input and returns it as a String
     * @param bytes byte array to be hashed
     * @return hashed input as a String
     */
    private String hashBytes(byte[] bytes) {
        messageDigest.update(bytes);
        return new String(messageDigest.digest());
    }

    /**
     * The singleton method getInstance()
     * @return the mediator instance
     */
    public UnblindMediator getUnblindMediator() {
        return unblindMediator;
    }
}
