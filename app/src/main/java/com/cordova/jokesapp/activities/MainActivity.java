package com.cordova.jokesapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.cordova.jokesapp.R;
import com.cordova.jokesapp.adapters.ExpandableListAdapter;
import com.cordova.jokesapp.entities.Joke;
import com.cordova.jokesapp.util.RequestBuilder;
import com.cordova.jokesapp.util.Util;
import com.cordova.jokesapp.util.VolleyCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
    private Map<String, List<Joke>> mapJokeToDelete;


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
        AdRequest adRequest = new AdRequest.Builder().build();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
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
                List<Joke> newCategory = new LinkedList<>();
                if (item.isChecked()) {
                    item.setChecked(false);
                    editor.putBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
                    for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<Joke> subCategoriesJokes = new LinkedList<>();
                        for (Joke joke : entry.getValue()) {
                            if (joke.isDirtyJoke()) {
                                continue;
                            }
                            if (new Util().isFromCurrentMonth(joke)) {
                                newCategory.add(joke);
                            }
                            subCategoriesJokes.add(joke);
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategoriesJokes);
                    }
                } else {
                    item.setChecked(true);
                    editor.putBoolean(Util.MY_ENABLED_HEAVY_JOKE, true);
                    for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<Joke> subCategories = new LinkedList<>();
                        for (Joke joke : entry.getValue()) {
                            subCategories.add(joke);
                            if (new Util().isFromCurrentMonth(joke)) {
                                newCategory.add(joke);
                            }
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategories);
                    }
                }
                editor.commit();

                listDataHeader.add(Util.NEW_JOKES);
                listDataChild.put(Util.NEW_JOKES, newCategory);
                List<Joke> listBestJoke = new Util().getTheBestJokes(10, mapJokes, sharedpreferences);
                listDataHeader.add(Util.BEST_JOKES);
                listDataChild.put(Util.BEST_JOKES, listBestJoke);
                listAdapter.notifyDataSetChanged();
                return true;
            case R.id.option_feedback:
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.give_us_a_feedback), Toast.LENGTH_LONG).show();
                return true;
            case R.id.option_about:
                Toast.makeText(getApplicationContext(), getString(R.string.about) + getResources().getString(R.string.app_name), Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_refresh:
                long currentTime = Calendar.getInstance().getTimeInMillis();
                sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
                if (currentTime >= sharedpreferences.getLong("enableRefreshTime", currentTime)) {
                    setRefreshActionButtonState(true);
                    readJsonFromServer();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.last_version), Toast.LENGTH_LONG).show();
                }
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
        sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
        Long chunk = sharedpreferences.getLong("currentChunk", 0);
        //TODO - Get the location
        RequestBuilder.requestGetJokesByChunk(this, "ES", (chunk), new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if (!readJson(result)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.problem_to_download_json), Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putLong("enableRefreshTime", Calendar.getInstance().getTimeInMillis() + (7 * 24 * 60 * 60 * 1000));
                    edit.commit();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_to_download_json), Toast.LENGTH_LONG).show();
                    //TODO store the jokes into the database, maybe into the readJsonAndParserData method
                }
                setRefreshActionButtonState(false);
            }

            @Override
            public void onError(String error) {
                setRefreshActionButtonState(false);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.problem_to_download_json), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void copyFile(String newJokes) {
        newJokes = newJokes.replace("[", ",");
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = openFileInput(Util.FILENAME);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            String oldJson = byteArrayOutputStream.toString().replace("]", "");
            newJokes = oldJson + newJokes;
            fos = openFileOutput(Util.FILENAME, Context.MODE_PRIVATE);
            fos.write(newJokes.getBytes());
        } catch (FileNotFoundException e) {
            Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(Util.TAG, e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(Util.TAG, "Error closing file: " + e.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(Util.TAG, "Error closing file: " + e.getMessage());
                }
            }
        }
    }


    private boolean readJson(String json) {
        Gson gson = new GsonBuilder().create();
        Type collectionType = new TypeToken<Collection<Joke>>(){}.getType();
        Collection<Joke> enums = gson.fromJson(json, collectionType);
        List listJoke = new ArrayList<Joke>(enums);
        try {
            return readJsonAndParserData(listJoke);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean readJsonAndParserData(List<Joke> listJoke) throws FileNotFoundException, JSONException {
        boolean result = false;
        try {
            sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
            if (listJoke != null) {
                if (listJoke.size() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.last_version), Toast.LENGTH_LONG).show();
                } else {
                    long maxChuck = 0;
                    result = true;
                    mapJokes.remove(Util.NEW_JOKES);
                    for (Joke joke : listJoke) {
                        if (joke.getChunk() > maxChuck) {
                            maxChuck = joke.getChunk();
                        }
                        List<Joke> jokes;
                        List<Joke> newJokes;
                        if (!mapJokes.containsKey(Util.NEW_JOKES)) {
                            newJokes = new ArrayList<>();
                            mapJokes.put(Util.NEW_JOKES, newJokes);
                        } else {
                            newJokes = mapJokes.get(Util.NEW_JOKES);
                        }
                        if (!mapJokes.containsKey(joke.getCategory())) {
                            jokes = new ArrayList<>();
                            mapJokes.put(joke.getCategory(), jokes);
                        } else {
                            jokes = mapJokes.get(joke.getCategory());
                        }
                        jokes.add(joke);
                        Joke newJoke = (Joke) joke.clone();
                        newJoke.setCategory(Util.NEW_JOKES);
                        newJokes.add(newJoke);

                        if (!listDataHeader.contains(joke.getCategory())) {
                            listDataHeader.add(joke.getCategory());
                        }
                        if (!(!includeDirtyJokes && joke.isDirtyJoke())) {
                            listDataChild.put(joke.getCategory(), jokes);
                            listDataChild.put(Util.NEW_JOKES, newJokes);
                        }
                    }
                    Collections.sort(listDataChild.get(Util.NEW_JOKES));
                    Collections.sort(mapJokes.get(Util.NEW_JOKES));
                    listAdapter.notifyDataSetChanged();
                    sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putLong("currentChunk", maxChuck);
                    editor.commit();
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
                .setPositiveButton(R.string.yes_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.no_exit, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Util.SHOW_JOKES) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mapJokeToDelete = (Map<String, List<Joke>>) data.getSerializableExtra("jokesToDelete");
                List<Joke> listJokesToDelete = new ArrayList<Joke>();
                if (mapJokeToDelete.size() > 0) {
                    for (Map.Entry<String, List<Joke>> entry : mapJokeToDelete.entrySet()) {
                        List<Joke> jokes;
                        for (Joke joke : entry.getValue()) {
                            listJokesToDelete.add(joke);
                            jokes = mapJokes.get(Util.NEW_JOKES);
                            if (jokes != null) jokes.remove(joke);
                            jokes = mapJokes.get(Util.BEST_JOKES);
                            if (jokes != null) jokes.remove(joke);

                            switch (entry.getKey()) {
                                case Util.NEW_JOKES:
                                case Util.BEST_JOKES:
                                    String categoryToDelte = "";
                                    for (Map.Entry<String, List<Joke>> key : mapJokes.entrySet()) {
                                        if (key.getValue() != null) {
                                            if (key.getValue().remove(joke)) {
                                                categoryToDelte = key.getKey();
                                                break;
                                            }
                                        }
                                    }
                                    if (listDataChild.get(categoryToDelte) != null) {
                                        listDataChild.get(categoryToDelte).remove(joke);
                                    }
                                    break;
                                default:
                                    jokes = mapJokes.get(joke.getCategory());
                                    if (jokes != null) jokes.remove(joke);

                                    jokes = listDataChild.get(joke.getCategory());
                                    if (jokes != null) jokes.remove(joke);
                                    break;
                            }
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                    removeJokeInJson(listJokesToDelete);
                }
            }
        }
    }

    public void removeJokeInJson(List<Joke> listJokesToDelete) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = openFileInput(Util.FILENAME);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ctr = fis.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = fis.read();
            }
            String json = byteArrayOutputStream.toString();
            try {
                if (json != null) {
                    boolean isAJsonRemoved = false;
                    JSONArray jArray = new JSONArray(json);
                    for (Joke joke : listJokesToDelete) {
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject obj = jArray.getJSONObject(i);
                            if (joke.getId().equals(obj.getString("id"))) {
                                obj.remove(Util.PARAM_ID);
                                obj.remove(Util.PARAM_USER);
                                obj.remove(Util.PARAM_TITLE);
                                obj.remove(Util.PARAM_JOKE_TEXT);
                                obj.remove(Util.PARAM_LIKES);
                                obj.remove(Util.PARAM_DISLIKES);
                                obj.remove(Util.PARAM_CATEGORY);
                                obj.remove(Util.PARAM_DIRTY_JOKE);
                                obj.remove(Util.PARAM_CREATION_DATE);
                                obj.remove(Util.PARAM_TAG);
                                obj.remove(Util.PARAM_CHUNK);
                                isAJsonRemoved = true;
                            }
                        }
                    }
                    if (isAJsonRemoved) {
                        fos = openFileOutput(Util.FILENAME, Context.MODE_PRIVATE);
                        fos.write(jArray.toString().replaceAll("(\\{\\},)", "").getBytes());
                    }
                }
            } catch (Exception e) {
                Log.e(Util.TAG, "error trying to read the json file: " + e.getMessage());
            }

        } catch (FileNotFoundException e) {
            Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(Util.TAG, e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(Util.TAG, "Error trying to close the file: " + e.getMessage());
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(Util.TAG, "Error trying to close the file: " + e.getMessage());
                }
            }
        }
    }
}
