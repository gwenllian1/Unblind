package com.example.unblind;

import android.graphics.Bitmap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pytorch.Tensor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BitmapProcessUnitTest {

    private Bitmap bitmap;
    private BitmapProcessor processor;

    @BeforeEach
    public void setUp(){
        processor = new BitmapProcessor();
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 56, 64, 86, 11978, 2002})
    public void setUpBitmap(int bitmapSize){
        bitmap = mock(Bitmap.class);
        when(bitmap.getWidth()).thenReturn(bitmapSize);
        when(bitmap.getHeight()).thenReturn(bitmapSize);
        Assertions.assertNotEquals(bitmap.getHeight(), 224);
        Assertions.assertNotEquals(bitmap.getWidth(), 224);
    }

    @AfterEach
    public void testPreprocess() {
        Tensor tensor = processor.preprocess(bitmap, 224);
        long[] expectedShape = {1, 2, 224, 224};
        Assertions.assertEquals(expectedShape, tensor.shape());
    }

}
