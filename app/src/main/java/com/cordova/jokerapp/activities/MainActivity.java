package com.cordova.jokerapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.adapters.ExpandableListAdapter;
import com.cordova.jokerapp.domain.Joke;
import com.cordova.jokerapp.util.RequestBuilder;
import com.cordova.jokerapp.util.Util;
import com.cordova.jokerapp.util.VolleyCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private Menu optionsMenu;


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
                .build();
        mAdView.loadAd(adRequest);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter.setSharedPreference(sharedpreferences);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
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
            case R.id.action_refresh:
                setRefreshActionButtonState(true);
                readJsonFromServer();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu.findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }


    private void readJsonFromServer() {
        //TODO - Get the location
        RequestBuilder.requestGetAllJokes(this, "ES", true, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if(!readJson(result)){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.problem_to_download_json),Toast.LENGTH_LONG).show();
                } else {
                    listAdapter.notifyDataSetChanged();
                    try {
                        FileOutputStream fos = openFileOutput(Util.FILENAME, Context.MODE_PRIVATE);
                        fos.write(result.getBytes());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e(Util.TAG, "error trying to read the local json file: " + e.getMessage());
                    }
                }
                setRefreshActionButtonState(false);
            }

            @Override
            public void onError(String error) {
                setRefreshActionButtonState(false);
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.problem_to_download_json),Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean readJson(String json){
        boolean result = false;
        try {
            result = readJsonAndParserData(json);
        } catch (FileNotFoundException e) {
            Log.e(Util.TAG,"json file not found: "+ e.getMessage());
        } catch (JSONException e) {
            Log.e(Util.TAG,"error reading json: "+ e.getMessage());
        }
        return result;
    }

    private boolean readJsonAndParserData(String json) throws FileNotFoundException, JSONException {
        boolean result = false;
        try {
            sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
            if (json != null){
                result = true;
                JSONArray jArray = new JSONArray(json);
                mapJokes.clear();
                for (int i = 0; i < jArray.length(); i++) {
                    String id = jArray.getJSONObject(i).getString("id");
                    String user = jArray.getJSONObject(i).getString("user");
                    String jokeTitle = jArray.getJSONObject(i).getString("title");
                    String jokeText = jArray.getJSONObject(i).getString("jokeText");
                    int likes = jArray.getJSONObject(i).getInt("likes");
                    int dislikes = jArray.getJSONObject(i).getInt("dislikes");
                    String jokeCategory = jArray.getJSONObject(i).getString("category");
                    //  jokeCategory = jokeCategory != null ? jokeCategory.toUpperCase() : "";
                    boolean isDirtyJoke = jArray.getJSONObject(i).getBoolean("dirtyJoke");
                    String creationDate = jArray.getJSONObject(i).getString("creationDate");
                    Joke joke = new Joke(id, jokeTitle, jokeCategory, jokeText, user, likes, dislikes, isDirtyJoke, creationDate);
                    List<Joke> jokes;

                    if (!mapJokes.containsKey(jokeCategory)) {
                        jokes = new ArrayList<>();
                    } else {
                        jokes = mapJokes.get(jokeCategory);
                    }
                    jokes.add(joke);
                    mapJokes.put(jokeCategory, jokes);
                }

                listDataHeader.clear();
                listDataChild.clear();

                for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                    List<Joke> jokeList = entry.getValue();
                    Collections.sort(jokeList);
                    List<Joke> subCategoriesJokes = new LinkedList<>();
                    for (Joke joke : jokeList) {
                        if (!includeDirtyJokes && joke.isDirtyJoke()) {
                            continue;
                        }
                        subCategoriesJokes.add(joke);
                    }
                    listDataHeader.add(entry.getKey());
                    listDataChild.put(entry.getKey(), subCategoriesJokes);
                }
            }
        } catch (Exception e) {
            Log.e(Util.TAG, "error trying to read the json file: " + e.getMessage());
            result = false;
        }
        return result;
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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.close_message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }
}
