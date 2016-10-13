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

    public final static String TAG = "Jokes";
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    SharedPreferences sharedpreferences;
    public static final String MY_PREFERENCES = "MyPreference" ;
    public static final String MY_ENABLED_HEAVY_JOKE = "ENABLED_HEAVY_JOKE";
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

        String itemSelected = sharedpreferences.getString("Item selected", "nothing");
        Toast.makeText(this, itemSelected, Toast.LENGTH_LONG).show();

        // setting list adapter
        expListView.setAdapter(listAdapter);
        FloatingActionButton addJokeButton = (FloatingActionButton) findViewById(R.id.add_joke);
        assert addJokeButton != null;
        addJokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createJokeIntent = new Intent(context, CreateJokeActivity.class);
                String[] stockArr = new String[listDataHeader.size()];
                stockArr = listDataHeader.toArray(stockArr);
                createJokeIntent.putExtra(Util.CATEGORIES,stockArr);
                startActivity(createJokeIntent);
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        mapJokes = new LinkedHashMap<String, List<Joke>>();
        readJsonFile();
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
                        String category = entry.getKey();
                        List<Joke> jokes = entry.getValue();
                        List<String> subCategories = new ArrayList<String>();
                        for (Joke joke: jokes){
                            if (joke.isDirtyJoke()){
                                continue;
                            }
                            subCategories.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
                        }
                        listDataHeader.add(category);
                        listDataChild.put(category, subCategories);
                    }
                }
                else {
                    item.setChecked(true);
                    editor.putBoolean(MY_ENABLED_HEAVY_JOKE, true);

                    for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        String category = entry.getKey();
                        List<Joke> jokes = entry.getValue();
                        List<String> subCategories = new ArrayList<String>();
                        for (Joke joke: jokes){
                            subCategories.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
                        }
                        listDataHeader.add(category);
                        listDataChild.put(category, subCategories);
                    }
                }
                editor.commit();
            //    readJsonFile();
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

    private void readJsonFile(){
        try {
            readJson();
        } catch (FileNotFoundException e) {
            Log.e(TAG,"json file not found: "+ e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG,"error reading json: "+ e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }



    public void readJson() throws FileNotFoundException, JSONException {
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
            Log.e(TAG, e.getMessage());
        }
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("categories");
            String catName;
            sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(MY_ENABLED_HEAVY_JOKE, false);
            for (int i = 0; i < jArray.length(); i++) {
                catName = jArray.getJSONObject(i).getString("name");
                JSONArray subcategories = jArray.getJSONObject(i).getJSONArray("subcategory");
                int id = 0;
                String subcatName;
                int likes = 0;
                int dislikes = 0;
                List<String> subCategories = new ArrayList<String>();
                List<Joke> jokes = new ArrayList<>();
                for (int j = 0; j < subcategories.length(); j++){
                    boolean isDirtyJoke = subcategories.getJSONObject(j).getBoolean("is_dirty_joke");
                    id = subcategories.getJSONObject(j).getInt("id");
                    subcatName = subcategories.getJSONObject(j).getString("name");
                    likes = subcategories.getJSONObject(j).getInt("likes");
                    dislikes = subcategories.getJSONObject(j).getInt("dislikes");
                    Joke joke = new Joke(""+id, subcatName, catName, null, null, likes, dislikes, isDirtyJoke);
                    jokes.add(joke);
                }
                Collections.sort(jokes);
                for (Joke joke: jokes){
                    if (!includeDirtyJokes && joke.isDirtyJoke()){
                        continue;
                    }
                    subCategories.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes());
                }
                mapJokes.put(catName, jokes);
                listDataHeader.add(catName);
                listDataChild.put(catName, subCategories);
            }
        } catch (Exception e) {
            Log.e(TAG,"error trying to read the json file: "+ e.getMessage());
        }
    }
}
