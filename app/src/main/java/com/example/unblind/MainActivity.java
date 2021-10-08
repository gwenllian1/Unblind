package com.example.unblind;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main Activity provides the class to control the main interface screen of Unblind
 * Contains functions to handle the layout of activity_main xml
 */
public class MainActivity extends AppCompatActivity {
    Button buttonModelTest;
    Button buttonRefreshStatus;

    /**
     * Initialises the activity main page.
     * @param savedInstanceState The most recent state supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Initialises the content of the Activity's standard menu
     * @param menu Options menu where items belong
     * @return boolean value, true for the menu to be displayed, false if not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //do not need menu main (3 dots)
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Called whenever an item in the menu is selected
     * @param item selected menu item
     * @return boolean, false if menu processing to proceed and true if it consumes here
     */
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


}