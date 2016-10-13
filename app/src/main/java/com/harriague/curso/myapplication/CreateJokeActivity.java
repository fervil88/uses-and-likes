package com.harriague.curso.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.harriague.curso.domain.Joke;

public class CreateJokeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_joke);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle b = getIntent().getExtras();
        String[] categories = b.getStringArray(Util.CATEGORIES);

        final Spinner spinner = (Spinner) findViewById(R.id.category_joke_input);
        assert spinner != null;
        spinner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(spinner.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        }) ;
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            TextView title = (TextView) findViewById(R.id.title_joke_input);
            Spinner category = (Spinner) findViewById(R.id.category_joke_input);
            TextView jokeText = (TextView) findViewById(R.id.joke_text_input);
            TextView user = (TextView) findViewById(R.id.user_joke_input);
            CheckBox checkBox = (CheckBox) findViewById(R.id.check_dirty_input);

            Joke currentJoke = new Joke(title.getText().toString(),category.getSelectedItem().toString(),
                    jokeText.getText().toString(), user.getText().toString(), checkBox.isChecked());

            Log.i(MainActivity.TAG, currentJoke.toString());

            //TODO Send joke to the server
            //TODO Update joke on existing list

            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
