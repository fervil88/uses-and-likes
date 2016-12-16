package com.cordova.jokesapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.Toast;
import com.cordova.jokesapp.R;
import com.cordova.jokesapp.adapters.ExpandableListAdapter;
import com.cordova.jokesapp.entities.DataBaseHandler;
import com.cordova.jokesapp.entities.Feeling;
import com.cordova.jokesapp.entities.Joke;
import com.cordova.jokesapp.util.RequestBuilder;
import com.cordova.jokesapp.util.Util;
import com.cordova.jokesapp.util.VolleyCallback;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
            MenuItem item = menu.findItem(R.id.switchItem);
            final Activity thisActivity = this;
            final SwitchCompat mSwitch = (SwitchCompat) item.getActionView().findViewById(R.id.switchButton);
            mSwitch.setChecked(sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false));
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = sharedpreferences.edit();
                    // listDataHeader.clear();
                    List<Joke> newCategory = new LinkedList<>();
                    if (isChecked) {
                        // The toggle is disabled
                        new AlertDialog.Builder(thisActivity)
                                .setMessage(R.string.warning_dirty_joke)
                                .setPositiveButton(R.string.yes_dirty_joke, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        listDataChild.clear();
                                        editor.putBoolean(Util.MY_ENABLED_HEAVY_JOKE, true);
                                        editor.commit();
                                        for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                                            Collections.sort(entry.getValue());
                                            listDataChild.put(entry.getKey(), entry.getValue());
                                        }
                                        listAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton(R.string.no_dirty_joke, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSwitch.setChecked(false);
                                    }
                                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mSwitch.setChecked(false);
                            }
                        }).show();
                    } else {
                        // The switch is enabled
                        listDataChild.clear();
                        editor.putBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
                        editor.commit();
                        for (Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                            if(Util.NEW_JOKES.equalsIgnoreCase(entry.getKey()) || Util.BEST_JOKES.equalsIgnoreCase(entry.getKey()))
                                continue;
                            List<Joke> listJokes = new LinkedList<>();
                            for (Joke joke : entry.getValue()) {
                                if (joke.isDirtyJoke()) {
                                    continue;
                                }
                                if (new Util().isFromCurrentMonth(joke)) {
                                    newCategory.add(joke);
                                }
                                listJokes.add(joke);
                            }
                            listDataChild.put(entry.getKey(), listJokes);
                        }
                        Collections.sort(newCategory);
                        listDataChild.put(Util.NEW_JOKES, newCategory);
                        listDataChild.put(Util.BEST_JOKES, new Util().getTheBestJokes(10, mapJokes, sharedpreferences));
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
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
                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<Collection<Joke>>(){}.getType();
                Collection<Joke> enums = gson.fromJson(result, collectionType);
                final List listJoke = new ArrayList<Joke>(enums);
                //TODO - Try to implement using thread
                //  new Thread(new Runnable() {
               //     public void run() {
                        if (!updateNewJokesFromServer(listJoke)) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.problem_to_download_json), Toast.LENGTH_LONG).show();
                        } else {
                            SharedPreferences.Editor edit = sharedpreferences.edit();
                            edit.putLong("enableRefreshTime", Calendar.getInstance().getTimeInMillis() + (7 * 24 * 60 * 60 * 1000));
                            edit.commit();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_to_download_json), Toast.LENGTH_LONG).show();
                        }
                  //  }
               // }).start();
                setRefreshActionButtonState(false);
            }

            @Override
            public void onError(String error) {
                setRefreshActionButtonState(false);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.problem_to_download_json), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean updateNewJokesFromServer(List<Joke> listJoke) {
        boolean result = false;
        try {
            sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(Util.MY_ENABLED_HEAVY_JOKE, false);
            if (listJoke != null) {
                if (listJoke.size() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.last_version), Toast.LENGTH_LONG).show();
                } else {
                    // TODO - Verify the duplicate jokes from the server
                    DataBaseHandler.getInstance(getApplicationContext()).addJokes(listJoke);
                    long maxChuck = 0;
                    result = true;
                    List<Joke> newCategory = listDataChild.get(Util.NEW_JOKES);
                    List<Joke> newAllCategory = mapJokes.get(Util.NEW_JOKES);
                    for (Joke joke : listJoke) {
                        if (joke.getChunk() > maxChuck) {
                            maxChuck = joke.getChunk();
                        }

                        List<Joke> allJokes = !mapJokes.containsKey(joke.getCategory()) ? new LinkedList<Joke>() : mapJokes.get(joke.getCategory());
                        allJokes.add(joke);
                        Collections.sort(allJokes);
                        mapJokes.put(joke.getCategory(), allJokes);

                        boolean isCurrentMonth = false;
                        if (new Util().isFromCurrentMonth(joke)) {
                            newAllCategory.add(joke);
                            isCurrentMonth = true;
                        }
                        if (!includeDirtyJokes && joke.isDirtyJoke()) {
                            continue;
                        } else {
                            List<Joke> jokes = !listDataChild.containsKey(joke.getCategory()) ? new LinkedList<Joke>() : listDataChild.get(joke.getCategory());
                            jokes.add(joke);
                            Collections.sort(jokes);
                            listDataChild.put(joke.getCategory(), jokes);
                            if (!listDataHeader.contains(joke.getCategory())) {
                                listDataHeader.add(joke.getCategory());
                            }
                        }
                        if (isCurrentMonth) {
                            newCategory.add(joke);
                        }
                    }
                    Collections.sort(newCategory);
                    Collections.sort(newAllCategory);

                    listDataChild.put(Util.NEW_JOKES, newCategory);
                    mapJokes.put(Util.NEW_JOKES, newAllCategory);

                    listDataChild.put(Util.BEST_JOKES, new Util().getTheBestJokes(10, mapJokes, sharedpreferences));
                    mapJokes.put(Util.BEST_JOKES, new Util().getAllBestJokes(10, mapJokes));

                    listAdapter.notifyDataSetChanged();

                    sharedpreferences = getSharedPreferences(Util.MY_PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putLong("currentChunk", maxChuck);
                    editor.commit();
                }
            }
        } catch (Exception e) {
            Log.e(Util.TAG, "Error trying to read the json response: " + e.getMessage());
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
               // new UpdateListFromInfoJoke().execute(data);
                updateListFromInfoJoke(data);
            }
        }
    }

    private void updateListFromInfoJoke(Intent data){
        Map<String, List<Joke>> mapJokeToDelete = (Map<String, List<Joke>>) data.getSerializableExtra("jokesToDelete");
        List<Feeling> listFeeling = (List<Feeling>) data.getSerializableExtra("listFeeling");
      //  DataBaseHandler dbh = DataBaseHandler.getInstance(getApplicationContext());
        if (mapJokeToDelete.size() > 0) {
            boolean wasABestJokeRemoved = false;
            for (Map.Entry<String, List<Joke>> entry : mapJokeToDelete.entrySet()) {
                List<Joke> jokes;
                for (Joke joke : entry.getValue()) {
                    jokes = mapJokes.get(joke.getCategory());
                    if (jokes != null) jokes.remove(joke);
                    jokes = listDataChild.get(joke.getCategory());
                    if (jokes != null) jokes.remove(joke);

                    jokes = mapJokes.get(Util.NEW_JOKES);
                    if (jokes != null) if(jokes.remove(joke)) wasABestJokeRemoved = true;
                    jokes = listDataChild.get(Util.NEW_JOKES);
                    if (jokes != null) if(jokes.remove(joke)) wasABestJokeRemoved = true;

                    jokes = mapJokes.get(Util.BEST_JOKES);
                    if (jokes != null) jokes.remove(joke);
                    jokes = listDataChild.get(Util.BEST_JOKES);
                    if (jokes != null) jokes.remove(joke);
                }
            }
            if (wasABestJokeRemoved){
                listDataChild.put(Util.BEST_JOKES, new Util().getTheBestJokes(10, mapJokes, sharedpreferences));
                mapJokes.put(Util.BEST_JOKES, new Util().getAllBestJokes(10, mapJokes));
             //   dbh.deleteBestJokes();
             //   dbh.addBestJokes(mapJokes.get(Util.BEST_JOKES));
            }
            listAdapter.notifyDataSetChanged();
        }

        if (listFeeling.size() > 0) {
            for (Feeling f : listFeeling) {
                for (Joke joke : mapJokes.get(f.getCategory())) {
                    if (joke.getId().equals(f.getId())) {
                        joke.setLikes(joke.getLikes() + f.getLikes());
                        joke.setDislikes(joke.getDislikes() + f.getDislikes());
                        int feelingIndex = mapJokes.get(Util.NEW_JOKES).indexOf(joke);
                        if(feelingIndex > -1){
                            mapJokes.get(Util.NEW_JOKES).get(feelingIndex).setDislikes(joke.getDislikes());
                            mapJokes.get(Util.NEW_JOKES).get(feelingIndex).setLikes(joke.getLikes());
                        }

                        feelingIndex = listDataChild.get(joke.getCategory()).indexOf(joke);
                        if(feelingIndex > -1){
                            listDataChild.get(joke.getCategory()).get(feelingIndex).setDislikes(joke.getDislikes());
                            listDataChild.get(joke.getCategory()).get(feelingIndex).setLikes(joke.getLikes());
                        }

                        feelingIndex = listDataChild.get(Util.NEW_JOKES).indexOf(joke);
                        if(feelingIndex > -1){
                            listDataChild.get(Util.NEW_JOKES).get(feelingIndex).setDislikes(joke.getDislikes());
                            listDataChild.get(Util.NEW_JOKES).get(feelingIndex).setLikes(joke.getLikes());
                        }
                        break;
                    }
                }
                Collections.sort(mapJokes.get(f.getCategory()));
                Collections.sort(listDataChild.get(f.getCategory()));
            }

            Collections.sort(mapJokes.get(Util.NEW_JOKES));
            Collections.sort(listDataChild.get(Util.NEW_JOKES));

            listDataChild.put(Util.BEST_JOKES, new Util().getTheBestJokes(10, mapJokes, sharedpreferences));
            mapJokes.put(Util.BEST_JOKES, new Util().getAllBestJokes(10, mapJokes));
         //   dbh.deleteBestJokes();
          //  dbh.addBestJokes(mapJokes.get(Util.BEST_JOKES));
            listAdapter.notifyDataSetChanged();
        }
    }


    private class UpdateListFromInfoJoke extends AsyncTask<Intent, Integer, Void> {
        @Override
        protected Void doInBackground(Intent... data) {
            updateListFromInfoJoke(data[0]);
            return null;
        }
    }
}
