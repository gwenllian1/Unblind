package com.example.unblind;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
//import androidx.test.runner.AndroidJUnit4;

import com.example.unblind.DatabaseService;

//@RunWith(AndroidJUnit4::class)
public class IntegrationUnitTest {
    String inputPref = "preferenceName1";
    String inputKey = "InputKey1";
    String inputLabel = "InputLabel1";
    private Context ApplicationProvider;
    IBinder service;

//    @Rule
//    public final ServiceTestRule serviceRule = new ServiceTestRule();


    @Test
    public void TestStringAndStringOne(){
        //comparing the bitmaps
    }

    @Test
    public void DatabaseTest1(){
        //testing if the database is working
//        DatabaseService TestDB = new DatabaseService();

//        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DatabaseService.class);

//        IBinder binder = serviceRule.bindService(serviceIntent);
        DatabaseService.LocalBinder binder = (DatabaseService.LocalBinder) service;
//        DatabaseService TestDB = ((DatabaseService.LocalBinder) service).getService();
        DatabaseService TestDB = binder.getService();

        TestDB.setSharedData(inputPref, inputKey, inputLabel);
        String outputResult = TestDB.getSharedData(inputPref, inputKey);
//        assertThat(outputResult).isEqualTo(inputLabel);
        Assertions.assertEquals(outputResult, inputLabel);

        //check if it works if I put in a wrong key
        String outputResult2 = TestDB.getSharedData(inputPref, "wrongKey1");
        Assertions.assertEquals(outputResult2, "None");
    }


}
