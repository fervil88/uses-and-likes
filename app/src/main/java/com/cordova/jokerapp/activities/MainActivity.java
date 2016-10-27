package com.cordova.jokerapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.adapters.ExpandableListAdapter;
import com.cordova.jokerapp.domain.Joke;
import com.cordova.jokerapp.util.Util;
import com.cordova.jokerapp.util.VolleyCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    Map<String, List<Joke>> listDataChild;
    SharedPreferences sharedpreferences;
    public static final String TAG = "JOKEAPP";
    private MainActivity context;
    private Map<String, List<Joke>> mapJokes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        listDataHeader = (List<String>) getIntent().getSerializableExtra("listDataHeader");
        listDataChild = (Map<String, List<Joke>>) getIntent().getSerializableExtra("listDataChild");
        mapJokes = (Map<String, List<Joke>>) getIntent().getSerializableExtra("mapJokes");

	   // get the listview
        expListView = (ExpandableListView) findViewById(R.id.likesList);
        sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .build();
        mAdView.loadAd(adRequest);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter.setSharedPreference(sharedpreferences);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        FloatingActionButton addJokeButton = (FloatingActionButton) findViewById(R.id.add_joke);
        assert addJokeButton != null;
        addJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createJokeIntent = new Intent(context, CreateJokeActivity.class);
                String[] stockArr = new String[listDataHeader.size() - 1];
                int index = 0;
                for (String header: listDataHeader){
                    if(Util.BEST_JOKES.equalsIgnoreCase(header) || Util.NEW_JOKES.equalsIgnoreCase(header)){
                        continue;
                    }
                    stockArr[index] = header;
                    index++;
                }
                createJokeIntent.putExtra(Util.CATEGORIES,stockArr);
                startActivity(createJokeIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
        menu.getItem(0).setChecked(sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.option_dirty_joke:
                sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                listDataHeader.clear();
                listDataChild.clear();
                if (item.isChecked()){
                    item.setChecked(false);
                    editor.putBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
                    for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<Joke> subCategoriesJokes = new LinkedList<>();
                        for (Joke joke: entry.getValue()){
                            if (joke.isDirtyJoke()){
                                continue;
                            }
                            //TODO replace the string by object
                            subCategoriesJokes.add(joke);
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategoriesJokes);
                    }
                }
                else {
                    item.setChecked(true);
                    editor.putBoolean(Util.MY_ENABLED_HEAVY_JOKE, true);
                    for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<Joke> subCategories = new LinkedList<>();
                        for (Joke joke: entry.getValue()){
                            //TODO replace the string by object
                            subCategories.add(joke);
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategories);
                    }
                }
                editor.commit();
                new Util().includeTheBestJokes(10, mapJokes, listDataHeader, listDataChild, sharedpreferences);
                listAdapter.notifyDataSetChanged();
                return true;
            case R.id.option_feedback:
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.give_us_a_feedback),Toast.LENGTH_LONG).show();
                return true;
            case R.id.option_about:
                Toast.makeText(getApplicationContext(),getString(R.string.about) + getResources().getString(R.string.app_name),Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Util.TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(Util.TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(Util.TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(Util.TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Util.TAG, "onDestroy");
    }
}
