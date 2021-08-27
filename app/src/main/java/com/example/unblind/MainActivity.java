// GitHub
package com.example.unblind;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.unblind.model.ModelBlackBoxTesting;

public class MainActivity extends AppCompatActivity {
    private BackgroundViewModel mViewModel;
    Button buttonModelTest;
    Button buttonRefreshStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // get the ViewModel
        mViewModel = new ViewModelProvider(this).get(BackgroundViewModel.class);

        // start the model service when the app is launched
        Intent mServiceIntent = new Intent(this, ModelService.class);
        getApplicationContext().startService(mServiceIntent);
//        buttonModelTest = findViewById(R.id.button2);
//        buttonModelTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ModelBlackBoxTesting test = new ModelBlackBoxTesting(context);
//                Toast.makeText(context,"Test finished, check LogCat for the result",Toast.LENGTH_LONG).show();
//            }
//        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //do not need menu main (3 dots)
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startBackgroundTask(View view) {
        // Ask the ViewModel to access the database
        mViewModel.accessDatabase();
    }






}