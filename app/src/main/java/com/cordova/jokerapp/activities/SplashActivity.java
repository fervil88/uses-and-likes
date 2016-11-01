package com.cordova.jokerapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.cordova.jokerapp.R;
import com.cordova.jokerapp.domain.Joke;
import com.cordova.jokerapp.util.RequestBuilder;
import com.cordova.jokerapp.util.Util;
import com.cordova.jokerapp.util.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;

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
    private final String FILENAME = "local_jokes.json";
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
        readJsonFromServer();

        sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.getBoolean(COPY_LOCAL_FILE, true)){
            copyFile();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(COPY_LOCAL_FILE, false);
            editor.commit();
        }


        new Handler().postDelayed(new Runnable(){
            public void run(){
                if(listDataHeader.size() == 0){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.problem_to_download_json),Toast.LENGTH_LONG).show();
                    readJsonFromFile();
                }
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
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
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
            FileInputStream fos = openFileInput(FILENAME);
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


    private void readJsonFromServer() {
        //TODO - Get the location
        RequestBuilder.requestGetAllJokes(this, "ES", true, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                if(!readJson(result)){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.problem_to_download_json),Toast.LENGTH_LONG).show();
                } else {
                    try {
                        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                        fos.write(result.getBytes());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e(Util.TAG, "error trying to read the local json file: " + e.getMessage());
                    }
                }
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
