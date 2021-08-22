package com.example.unblind;
import com.example.unblind.model.Constants;

import org.junit.Before;
import org.junit.Test;
import org.testng.annotations.AfterTest;

import static org.junit.Assert.assertEquals;

public class ConstantTest {

    @Test
    public void sizeIsCorrect(){
        int expectedSize = 1475;
        int actualSize = Constants.IMAGENET_CLASSES.length;
        assertEquals(expectedSize,actualSize);
    }
}
