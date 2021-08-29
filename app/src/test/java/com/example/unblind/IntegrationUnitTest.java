package com.example.unblind;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Pair;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.testng.mustache.Model;
//import androidx.test.runner.AndroidJUnit4;

import com.example.unblind.DatabaseService;
import com.example.unblind.UnblindMediator;

import java.io.IOException;
import java.io.InputStream;

//@RunWith(AndroidJUnit4::class)
public class IntegrationUnitTest {
    String inputPref = "preferenceName1";
    String inputKey = "InputKey1";
    String inputLabel = "InputLabel1";
    private Context ApplicationProvider;
    IBinder service;

    //mediator


//    @Rule
//    public final ServiceTestRule serviceRule = new ServiceTestRule();



    public void TestStringAndStringOne() throws IOException {
//        UnblindMediator mediator = new UnblindMediator();
//        UnblindAccessibilityService testInterface = new UnblindAccessibilityService();
//        ModelService testInterface2 = new ModelService();
//        ColleagueInterface testInterface3 = new UnblindAccessibilityService();
//        ColleagueInterface testInterface4 = new ModelService();
//        mediator.addObserver(testInterface3);
//        mediator.addObserver(testInterface4);
//
//        InputStream inputStream = testInterface.getAssets().open("86.png");
//        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

//        testInterface.update();

        //testing with new class just for testing
        UnblindMediator mediator = new UnblindMediator();
        testClass testinterface1 = new testClass();
//        mediator.addObserver(testinterface1);
        testinterface1.addMediator();



    }

    public void DatabaseTest1(){
        //testing if the database is working
//        DatabaseService TestDB = new DatabaseService();

//        Intent serviceIntent = new Intent(ApplicationProvider.getApplicationContext(), DatabaseService.class);

//        IBinder binder = serviceRule.bindService(serviceIntent);
        DatabaseService.LocalBinder binder = (DatabaseService.LocalBinder) service;
//        DatabaseService TestDB = ((DatabaseService.LocalBinder) service).getService();
        DatabaseService TestDB = binder.getService();

        byte[] inputKeyBytes = inputKey.getBytes();
        TestDB.setSharedData(inputPref, inputKeyBytes, inputLabel);
        String outputResult = TestDB.getSharedData(inputPref, inputKeyBytes);
//        assertThat(outputResult).isEqualTo(inputLabel);
        Assertions.assertEquals(outputResult, inputLabel);

        //check if it works if I put in a wrong key
        String outputResult2 = TestDB.getSharedData(inputPref, "wrongKey1".getBytes());
        Assertions.assertEquals(outputResult2, "None");
    }


}

class testClass implements ColleagueInterface
{
    private Pair<Bitmap, String> currentElement = new Pair(null, "");
    UnblindMediator mediator = new UnblindMediator();

    @Override
    public void update() {
        currentElement = mediator.getElementFromIncoming();
    }

    public Pair<Bitmap, String> getCurrentElement(){
        return currentElement;
    }

    public void addMediator(){
        mediator.addObserver((ColleagueInterface) this);
    }
}
