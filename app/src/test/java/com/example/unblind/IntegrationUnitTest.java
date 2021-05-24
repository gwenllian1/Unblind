package com.example.unblind;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.example.unblind.DatabaseService;


public class IntegrationUnitTest {
    String inputPref = "preferenceName1";
    String inputKey = "InputKey1";
    String inputLabel = "InputLabel1";


    @Test
    public void TestStringAndStringOne(){
        //comparing the bitmaps
    }

    @Test
    public void DatabaseTest1(){
        //testing if the database is working
        DatabaseService TestDB = new DatabaseService();

        TestDB.setSharedData(inputPref, inputKey, inputLabel);
        String outputResult = TestDB.getSharedData(inputPref, inputKey);
//        assertThat(outputResult).isEqualTo(inputLabel);
        Assertions.assertEquals(outputResult, inputLabel);

        //check if it works if I put in a wrong key
        String outputResult2 = TestDB.getSharedData(inputPref, "wrongKey1");
        Assertions.assertEquals(outputResult2, "None");
    }


}
