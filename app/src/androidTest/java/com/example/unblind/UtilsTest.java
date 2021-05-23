package com.example.unblind;
import android.content.Context;
import android.graphics.BitmapFactory;

import com.example.unblind.model.Constants;
import com.example.unblind.model.Utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.runner.AndroidJUnit4;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UtilsTest {
    Context context;
    String input;
    @Before
    public void setUp() {
        context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        input = "labeldroid.pt";
    }
    @Test
    public void isOutputCorrect(){

        String expectedOutput = "/data/user/0/com.example.unblind/files/labeldroid.pt";
        String actualOutput = Utils.assetFilePath(context, input);;
        assertEquals(expectedOutput,actualOutput);
    }
}
