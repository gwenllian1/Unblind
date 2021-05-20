package com.example.unblind;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.unblind.model.BitmapProcessor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pytorch.Tensor;

import java.io.IOException;
import java.io.InputStream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BitmapProcessUnitTest {

    private Bitmap bitmap;
    private BitmapProcessor processor;

    @BeforeEach
    public void setUp(){
        processor = new BitmapProcessor();
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.png", "56.png", "64.png", "86.png", "11978.png", "2002.png"})
    public void setUpBitmap(String filename) throws IOException {
        InputStream inputStream = testOpenFile(filename);
        bitmap = BitmapFactory.decodeStream(inputStream);
//        bitmap = mock(Bitmap.class);
//        when(bitmap.getWidth()).thenReturn(bitmapSize);
//        when(bitmap.getHeight()).thenReturn(bitmapSize);
        Assertions.assertNotEquals(bitmap.getHeight(), 224);
        Assertions.assertNotEquals(bitmap.getWidth(), 224);
    }

    @AfterEach
    public void testPreprocess() {
        Tensor tensor = processor.preProcess(bitmap, 224);
        long[] expectedShape = {1, 2, 224, 224};
        Assertions.assertEquals(expectedShape, tensor.shape());
    }

    private InputStream testOpenFile(String filename) throws IOException {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

}
