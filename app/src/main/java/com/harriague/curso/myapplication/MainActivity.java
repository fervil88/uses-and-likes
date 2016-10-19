package com.harriague.curso.myapplication;

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

import com.harriague.curso.domain.Joke;
import com.harriague.curso.util.RequestBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public final static String URL_MAIN_JSON = "http://192.168.1.4:9090/jokes";
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    SharedPreferences sharedpreferences;
    public static final String MY_PREFERENCES = "MyPreference" ;
    public static final String MY_ENABLED_HEAVY_JOKE = "ENABLED_HEAVY_JOKE";
    public static final String TAG = "JOKEAPP";
    private MainActivity context;
    private Map<String, List<Joke>> mapJokes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

	   // get the listview
        expListView = (ExpandableListView) findViewById(R.id.likesList);
        sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter.setSharedPreference(sharedpreferences);

        includeTheBestJokes(10);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        FloatingActionButton addJokeButton = (FloatingActionButton) findViewById(R.id.add_joke);
        assert addJokeButton != null;
        addJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createJokeIntent = new Intent(context, CreateJokeActivity.class);
                String[] stockArr = new String[listDataHeader.size() - 2];
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

        RequestBuilder.requestGetAllJokes(this);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        mapJokes = new LinkedHashMap<String, List<Joke>>();
        try {
            readJson();
        } catch (FileNotFoundException e) {
            Log.e(Util.TAG,"json file not found: "+ e.getMessage());
        } catch (JSONException e) {
            Log.e(Util.TAG,"error reading json: "+ e.getMessage());
        }
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
        sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        menu.getItem(0).setChecked(sharedpreferences.getBoolean(MY_ENABLED_HEAVY_JOKE, false));
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
                sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                listDataHeader.clear();
                listDataChild.clear();
                if (item.isChecked()){
                    item.setChecked(false);
                    editor.putBoolean(MY_ENABLED_HEAVY_JOKE, false);
                    for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<String> subCategoriesJokes = new ArrayList<String>();
                        for (Joke joke: entry.getValue()){
                            if (joke.isDirtyJoke()){
                                continue;
                            }
                            subCategoriesJokes.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategoriesJokes);
                    }
                }
                else {
                    item.setChecked(true);
                    editor.putBoolean(MY_ENABLED_HEAVY_JOKE, true);
                    for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<String> subCategories = new ArrayList<String>();
                        for (Joke joke: entry.getValue()){
                            subCategories.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategories);
                    }
                }
                editor.commit();
                includeTheBestJokes(10);
                listAdapter.notifyDataSetChanged();
                return true;
            case R.id.option_feedback:
                Toast.makeText(getApplicationContext(),"Danos un feedback vieja!",Toast.LENGTH_LONG).show();
                return true;
            case R.id.option_about:
                Toast.makeText(getApplicationContext(),"Acerca de chistoso",Toast.LENGTH_LONG).show();
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

    private void includeTheBestJokes(int top){
        List<Joke> jokes = getTheBestJokes(top);
        List<String> subCategoriesJokes = new ArrayList<String>();
        for (Joke joke: jokes){
            subCategoriesJokes.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
        }
        listDataHeader.add(Util.BEST_JOKES);
        listDataChild.put(Util.BEST_JOKES, subCategoriesJokes);
    }

    private List<Joke> getTheBestJokes(int top){
        List<Joke> jokes = new ArrayList<Joke>();
        int minAdded = 0;
        sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        boolean includeDirtyJokes = sharedpreferences.getBoolean(MY_ENABLED_HEAVY_JOKE, false);

        for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
            if(Util.NEW_JOKES.equalsIgnoreCase(entry.getKey()) || Util.BEST_JOKES.equalsIgnoreCase(entry.getKey()))
                continue;

            for (Joke joke: entry.getValue()){
                if (!includeDirtyJokes && joke.isDirtyJoke()){
                    continue;
                }
                try {
                    Joke newJoke = (Joke) joke.clone();
                    newJoke.setCategory(Util.BEST_JOKES);
                    if (jokes.size() < top) {
                        jokes.add(newJoke);
                        Collections.sort(jokes);
                        minAdded = (jokes.get(jokes.size() - 1).getLikes() / (jokes.get(jokes.size() - 1).getDislikes() != 0 ? jokes.get(jokes.size() - 1).getDislikes() : 1));
                    } else if (minAdded <= (joke.getLikes() / (joke.getDislikes() != 0 ? joke.getDislikes() : 1))) {
                        jokes.remove(jokes.size() - 1);
                        jokes.add(newJoke);
                        Collections.sort(jokes);
                        minAdded = (jokes.get(jokes.size() - 1).getLikes() / (jokes.get(jokes.size() - 1).getDislikes() != 0 ? jokes.get(jokes.size() - 1).getDislikes() : 1));
                    } else {
                        break;
                    }
                } catch (CloneNotSupportedException e) {
                    Log.e(Util.TAG, "Error clone joke: " + e.getMessage());
                }
            }
        }
        Collections.sort(jokes);
        return jokes;
    }


    private void readJson() throws FileNotFoundException, JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.categories);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(Util.TAG, e.getMessage());
        }
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("categories");
            String catName;
            sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(MY_ENABLED_HEAVY_JOKE, false);
            for (int i = 0; i < jArray.length(); i++) {
                catName = jArray.getJSONObject(i).getString("name");
                JSONArray subCategories = jArray.getJSONObject(i).getJSONArray("subcategory");
                int id = 0;
                String subcatName;
                int likes = 0;
                int dislikes = 0;
                List<String> subCategoriesJokes = new ArrayList<String>();
                List<Joke> jokes = new ArrayList<>();
                for (int j = 0; j < subCategories.length(); j++){
                    boolean isDirtyJoke = subCategories.getJSONObject(j).getBoolean("is_dirty_joke");
                    id = subCategories.getJSONObject(j).getInt("id");
                    subcatName = subCategories.getJSONObject(j).getString("name");
                    likes = subCategories.getJSONObject(j).getInt("likes");
                    dislikes = subCategories.getJSONObject(j).getInt("dislikes");
                    Joke joke = new Joke(""+id, subcatName, catName, null, null, likes, dislikes, isDirtyJoke);
                    jokes.add(joke);
                }
                Collections.sort(jokes);
                for (Joke joke: jokes){
                    if (!includeDirtyJokes && joke.isDirtyJoke()){
                        continue;
                    }
                    subCategoriesJokes.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
                }
                mapJokes.put(catName, jokes);
                listDataHeader.add(catName);
                listDataChild.put(catName, subCategoriesJokes);
            }
        } catch (Exception e) {
            Log.e(Util.TAG,"error trying to read the json file: "+ e.getMessage());
        }
    }
}
