package com.example.unblind;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DatabaseTest extends AppCompatActivity {

    private DatabaseService dbS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbS = new DatabaseService();

        setContentView(R.layout.activity_main);
//        Button downButton = findViewById(R.id.dataTestButton1);
//        Button upButton = findViewById(R.id.DataTestButton2);
//
//        downButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                System.out.println("Edwin down");
//                loadtodb();
//            }
//        });
//
//        upButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v)
//            {
//                System.out.println("Edwin up");
//                loadtodb();
//            }
//        });
    }

    public void savetodb()
    {
        dbS.setSharedData("testing", "image1", "label1");
    }

    public void loadtodb()
    {
        dbS.getSharedData("testing", "image1");
    }
}
