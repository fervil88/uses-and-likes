package com.cordova.jokerapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.domain.Joke;
import com.cordova.jokerapp.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DURATION = 7000;
    private final String COPY_LOCAL_FILE = "COPY_LOCAL_FILE";
    SharedPreferences sharedpreferences;
    private Map<String, List<Joke>> mapJokes;
    List<String> listDataHeader;
    Map<String, List<Joke>> listDataChild;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        listDataHeader = new ArrayList<String>();
        listDataChild = new LinkedHashMap<String, List<Joke>>();
        mapJokes = new LinkedHashMap<String, List<Joke>>();

        final ProgressDialog progress = new ProgressDialog(this, R.style.MyTheme);
        // progress.setTitle(getResources().getString(R.string.loading_title));
        progress.setMessage(getResources().getString(R.string.loading_message));
        progress.setCancelable(false);
        progress.getWindow().setGravity(Gravity.BOTTOM);
        // progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progress.show();
       // readJsonFromServer();

        sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.getBoolean(COPY_LOCAL_FILE, true)){
            copyFile();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(COPY_LOCAL_FILE, false);
            editor.commit();
        }

        readJsonFromFile();
        new Handler().postDelayed(new Runnable(){
            public void run(){
                new Util().includeTheBestJokes(10, mapJokes, listDataHeader, listDataChild, sharedpreferences);
                progress.dismiss();

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("mapJokes", (Serializable) mapJokes);
                intent.putExtra("listDataHeader", (Serializable) listDataHeader);
                intent.putExtra("listDataChild", (Serializable) listDataChild);
                startActivity(intent);
                finish();
            };
        }, SPLASH_DURATION);
    }

    private void copyFile(){
        InputStream inputStream =  getResources().openRawResource(R.raw.categories);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            FileOutputStream fos = openFileOutput(Util.FILENAME, Context.MODE_PRIVATE);
            fos.write(byteArrayOutputStream.toString().getBytes());
            fos.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(Util.TAG, e.getMessage());
        }
    }

    private void readJsonFromFile() {
        try {
            FileInputStream fos = openFileInput(Util.FILENAME);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ctr = fos.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = fos.read();
            }
            fos.close();
            readJson(byteArrayOutputStream.toString());
        } catch (FileNotFoundException e) {
            Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
        }catch (IOException e) {
            Log.e(Util.TAG, e.getMessage());
        }
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
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject o = jArray.getJSONObject(i);
                    String jokeCategory = jArray.getJSONObject(i).getString(Util.PARAM_CATEGORY);
                  //  jokeCategory = jokeCategory != null ? jokeCategory.toUpperCase() : "";
                    Joke joke = new Joke(o.getString(Util.PARAM_ID), o.getString(Util.PARAM_TITLE), jokeCategory, o.getString(Util.PARAM_JOKE_TEXT),
                            o.getString(Util.PARAM_USER), o.getInt(Util.PARAM_LIKES), o.getInt(Util.PARAM_DISLIKES), o.getBoolean(Util.PARAM_DIRTY_JOKE),
                            o.getString(Util.PARAM_CREATION_DATE),o.getString(Util.PARAM_TAG),o.getLong(Util.PARAM_CHUNK));
                    List<Joke> jokes;

                    if (!mapJokes.containsKey(jokeCategory)) {
                        jokes = new ArrayList<>();
                    } else {
                        jokes = mapJokes.get(jokeCategory);
                    }
                    jokes.add(joke);
                    mapJokes.put(jokeCategory, jokes);
                }

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
}
