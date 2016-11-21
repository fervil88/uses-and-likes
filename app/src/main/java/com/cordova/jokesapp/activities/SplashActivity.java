package com.cordova.jokesapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;

import com.cordova.jokesapp.R;
import com.cordova.jokesapp.entities.DataBaseHandler;
import com.cordova.jokesapp.entities.Joke;
import com.cordova.jokesapp.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DURATION = 7000;
    private final String COPY_LOCAL_FILE = "READ_LOCAL_FILE";
    SharedPreferences sharedpreferences;
    private Map<String, List<Joke>> mapJokes;
    List<String> listDataHeader;
    Map<String, List<Joke>> listDataChild;
    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        listDataHeader = new ArrayList<String>();
        listDataChild = new LinkedHashMap<String, List<Joke>>();
        mapJokes = new LinkedHashMap<String, List<Joke>>();
        progress = new ProgressDialog(this, R.style.MyTheme);

        // progress.setTitle(getResources().getString(R.string.loading_title));
        progress.setMessage(getResources().getString(R.string.loading_message));
        progress.setCancelable(false);
        progress.getWindow().setGravity(Gravity.BOTTOM);
        // progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progress.show();

        new PopulateDataBase().execute(R.raw.categories);
    }

    private class PopulateDataBase extends AsyncTask<Integer, Integer, Boolean> {
        protected Boolean doInBackground(Integer... resourceFile) {
            sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
            DataBaseHandler dbh = DataBaseHandler.getInstance(getApplicationContext());
            if (sharedpreferences.getBoolean(COPY_LOCAL_FILE, true)) {
                InputStream inputStream =  getResources().openRawResource(resourceFile[0]);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    int ctr = inputStream.read();
                    while (ctr != -1) {
                        byteArrayOutputStream.write(ctr);
                        ctr = inputStream.read();
                    }
                    JSONArray jArray = new JSONArray(byteArrayOutputStream.toString());
                    inputStream.close();
                    List<Joke> jokes = new ArrayList<>();
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject o = jArray.getJSONObject(i);
                        Joke joke = new Joke(o.getString(Util.PARAM_ID), o.getString(Util.PARAM_TITLE), o.getString(Util.PARAM_CATEGORY), o.getString(Util.PARAM_JOKE_TEXT),
                                o.getString(Util.PARAM_USER), o.getInt(Util.PARAM_LIKES), o.getInt(Util.PARAM_DISLIKES), o.getBoolean(Util.PARAM_DIRTY_JOKE),
                                o.getString(Util.PARAM_CREATION_DATE),o.getString(Util.PARAM_TAG),o.getLong(Util.PARAM_CHUNK));
                        jokes.add(joke);
                    }
                    dbh.addJokes(jokes);
                } catch (FileNotFoundException e) {
                    Log.e(Util.TAG, "error trying to found the local json file: " + e.getMessage());
                } catch (IOException e) {
                    Log.e(Util.TAG, e.getMessage());
                } catch (JSONException e) {
                    Log.e(Util.TAG, e.getMessage());
                }
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(COPY_LOCAL_FILE, false);
                editor.commit();
                publishProgress(30);
            }

            listDataHeader = dbh.getAllCategoriesFromJokes();
            for (String key: listDataHeader) {
                mapJokes.put(key, dbh.getAllJokesByCategory(key));
            }

            List<Joke> newCategory = new LinkedList<>();
            for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                List<Joke> subCategoriesJokes = new LinkedList<>();
                for (Joke joke : entry.getValue()) {
                    if (!includeDirtyJokes && joke.isDirtyJoke()) {
                        continue;
                    }
                    if (new Util().isFromCurrentMonth(joke)) {
                        newCategory.add(joke);
                    }
                    subCategoriesJokes.add(joke);
                }
                listDataChild.put(entry.getKey(), subCategoriesJokes);
            }
            dbh.deleteNewJokes();
            listDataHeader.add(Util.NEW_JOKES);
            listDataChild.put(Util.NEW_JOKES, newCategory);
            dbh.addNewJokes(newCategory);

            dbh.deleteBestJokes();
            List<Joke> listBestJoke = new Util().getTheBestJokes(10, mapJokes, sharedpreferences);
            listDataHeader.add(Util.BEST_JOKES);
            listDataChild.put(Util.BEST_JOKES, listBestJoke);
            dbh.addBestJokes(listBestJoke);

            progress.dismiss();

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("mapJokes", (Serializable) mapJokes);
            intent.putExtra("listDataHeader", (Serializable) listDataHeader);
            intent.putExtra("listDataChild", (Serializable) listDataChild);
            startActivity(intent);
            finish();
            return true;
        }

        protected void onProgressUpdate (Integer... progress) {
            int p = Math.round(100*progress[0]);
            // dialog.setProgress(p);
        }

        protected void onPostExecute(Long result) {
            // dialog.dismiss();
        }
    }
}



