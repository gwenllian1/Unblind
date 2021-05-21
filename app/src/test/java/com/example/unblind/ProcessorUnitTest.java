package com.example.unblind;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.example.unblind.model.BitmapProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { BitmapFactory.class,Bitmap.class, TensorImageUtils.class,})
public class ProcessorUnitTest {
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private BitmapProcessor processor;
    public String[] imageFileNames = { "3.png","56.png","64.png","86.png","2002.png"};
    int numberOfItems = 0;
    @Before
    public void setUp() throws Exception {

        mockStatic(BitmapFactory.class);
        mockStatic(Bitmap.class);
        mockStatic(TensorImageUtils.class);
        //mockStatic(Log.class);
        processor = new BitmapProcessor();
        for(String imageName : imageFileNames){
            InputStream inputStream = testOpenFile(imageName);
            bitmaps.add(BitmapFactory.decodeStream(inputStream));
        }

        numberOfItems = imageFileNames.length;
        int actualNumberOfItems = bitmaps.size();

        Assertions.assertEquals(numberOfItems,actualNumberOfItems);

    }

    @Test
    public void variableTesting() throws Exception{
        float[] expectedMean = {0.485f, 0.456f, 0.406f};        // the mean for normalization
        float[] expectedStd = {0.229f, 0.224f, 0.225f};         // the std for normalization
      //  Assertions.assertArrayEquals(expectedMean,processor.mean);
      //  Assertions.assertArrayEquals(expectedStd,processor.std);
    }


    @Test
    public void testPreProcess()  {
        for(Bitmap testCase: bitmaps){
            Tensor tensor = processor.preProcess(testCase,224); // why always null
            long[] actualShape = tensor.shape();
            long[] expectedShape = {1, 3, 224, 224};
            Assertions.assertArrayEquals(expectedShape, tensor.shape());
        }
    }

    private InputStream testOpenFile(String filename) throws IOException {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
}
