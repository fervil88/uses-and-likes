package com.cordova.jokesapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private final int MAX_TIME_SPLASH_DURATION = 3500;
    private final String COPY_LOCAL_FILE = "READ_LOCAL_FILE";
    private SharedPreferences sharedpreferences;
    private Map<String, List<Joke>> mapJokes;
    private List<String> listDataHeader;
    private Map<String, List<Joke>> listDataChild;
    private ProgressDialog progress;
    private long startTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        listDataHeader = new ArrayList<String>();
        listDataChild = new TreeMap<String, List<Joke>>();
        mapJokes = new TreeMap<String, List<Joke>>();
        progress = new ProgressDialog(this, R.style.MyTheme);

        new PopulateDataBase().execute(R.raw.categories);
    }

    private class PopulateDataBase extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress.setProgress(0);
            progress.setMax(100);
            startTime = Calendar.getInstance().getTimeInMillis();
            // progress.setTitle(getResources().getString(R.string.loading_title));
            progress.setMessage(getResources().getString(R.string.loading_message));
            progress.setCancelable(false);
            progress.getWindow().setGravity(Gravity.BOTTOM);
            // progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progress.show();
        }

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
                    return false;
                } catch (IOException e) {
                    Log.e(Util.TAG, e.getMessage());
                    return false;
                } catch (JSONException e) {
                    Log.e(Util.TAG, e.getMessage());
                    return false;
                }
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(COPY_LOCAL_FILE, false);
                editor.commit();
                publishProgress(20);
            }

            listDataHeader = dbh.getAllCategoriesFromJokes();
            for (String key: listDataHeader) {
                List<Joke> listJokes = dbh.getAllJokesByCategory(key);
                Collections.sort(listJokes);
                mapJokes.put(key, listJokes);
            }
            publishProgress(30);

            List<Joke> newCategory = new LinkedList<>();
            List<Joke> newAllCategory = new LinkedList<>();
            for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                List<Joke> listJokes = new LinkedList<>();
                for (Joke joke : entry.getValue()) {
                    boolean isCurrentMonth = false;
                    if (new Util().isFromCurrentMonth(joke)) {
                        newAllCategory.add(joke);
                        isCurrentMonth = true;
                    }
                    if (!includeDirtyJokes && joke.isDirtyJoke()) {
                        continue;
                    }
                    if (isCurrentMonth) {
                        newCategory.add(joke);
                    }
                    listJokes.add(joke);
                }
                listDataChild.put(entry.getKey(), listJokes);
            }
        //    dbh.deleteNewJokes();
            Collections.sort(newCategory);
            listDataHeader.add(Util.NEW_JOKES);
            listDataChild.put(Util.NEW_JOKES, newCategory);
            mapJokes.put(Util.NEW_JOKES, newAllCategory);
            // dbh.addNewJokes(newCategory);
            publishProgress(40);

            //dbh.deleteBestJokes();
            listDataHeader.add(Util.BEST_JOKES);
            listDataChild.put(Util.BEST_JOKES, new Util().getTheBestJokes(10, mapJokes, sharedpreferences));
            mapJokes.put(Util.BEST_JOKES, new Util().getAllBestJokes(10, mapJokes));
            //dbh.addBestJokes(listBestJoke);
            publishProgress(60);
            return true;
        }

        protected void onProgressUpdate (Integer... values) {
            progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                long time = (MAX_TIME_SPLASH_DURATION - (Calendar.getInstance().getTimeInMillis() - startTime));
                progress.setProgress(80);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        progress.setProgress(100);
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("mapJokes", (Serializable) mapJokes);
                        intent.putExtra("listDataHeader", (Serializable) listDataHeader);
                        intent.putExtra("listDataChild", (Serializable) listDataChild);
                        progress.dismiss();
                        startActivity(intent);
                        finish();
                    }
                },  (time < 1 ? 1 : time));
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_loading_app), Toast.LENGTH_LONG).show();
            }

        }

    }
}



