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
import com.harriague.curso.util.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Joke>> listDataChild;
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
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Joke>>();
        mapJokes = new LinkedHashMap<String, List<Joke>>();

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

        RequestBuilder.requestGetAllJokes(this, new VolleyCallback(){
            @Override
            public void onSuccess(String result){
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
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
                    editor.putBoolean(MY_ENABLED_HEAVY_JOKE, true);
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
        List<Joke> subCategoriesJokes = new LinkedList<>();
        for (Joke joke: jokes){
            //TODO replace the string by object
            subCategoriesJokes.add(joke);
        }
        listDataHeader.add(Util.BEST_JOKES);
        listDataChild.put(Util.BEST_JOKES, subCategoriesJokes);
    }

    private List<Joke> getTheBestJokes(int top){
        List<Joke> jokes = new LinkedList<>();
        float minAdded = 0;
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
        RequestBuilder.requestGetAllJokes(this, new VolleyCallback(){
            @Override
            public void onSuccess(String result){
                try {
                    sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
                    boolean includeDirtyJokes = sharedpreferences.getBoolean(MY_ENABLED_HEAVY_JOKE, false);
                    JSONArray jArray = new JSONArray(result);
                    for (int i = 0; i < jArray.length(); i++) {
                        String id = jArray.getJSONObject(i).getString("id");
                        String user = jArray.getJSONObject(i).getString("user");
                        String jokeTitle = jArray.getJSONObject(i).getString("title");
                        String jokeText = jArray.getJSONObject(i).getString("jokeText");
                        int likes = jArray.getJSONObject(i).getInt("likes");
                        int dislikes = jArray.getJSONObject(i).getInt("dislikes");
                        String jokeCategory = jArray.getJSONObject(i).getString("category");
                        jokeCategory = jokeCategory != null ? jokeCategory.toUpperCase() : "";
                        boolean isDirtyJoke = jArray.getJSONObject(i).getBoolean("dirtyJoke");
                        String creationDate = jArray.getJSONObject(i).getString("creationDate");
                        Joke joke = new Joke(id, jokeTitle, jokeCategory, jokeText, user, likes, dislikes, isDirtyJoke, creationDate);
                        List<Joke> jokes;

                        if (!mapJokes.containsKey(jokeCategory)) {
                            jokes = new LinkedList<>();
                        } else {
                            jokes = mapJokes.get(jokeCategory);
                        }
                        jokes.add(joke);
                        mapJokes.put(jokeCategory, jokes);
                    }

                    for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                        List<Joke> jokeList = entry.getValue();
                        Collections.sort(jokeList);
                        List<Joke> subCategoriesJokes = new LinkedList<>();
                        for (Joke joke: jokeList){
                            if (!includeDirtyJokes && joke.isDirtyJoke()){
                                continue;
                            }
                            //TODO replace the string by object
                            subCategoriesJokes.add(joke);
                        }
                        listDataHeader.add(entry.getKey());
                        listDataChild.put(entry.getKey(), subCategoriesJokes);
                    }
                } catch (Exception e) {
                    Log.e(Util.TAG,"error trying to read the json file: "+ e.getMessage());
                }
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*TODO - to avoid call the server and read from categories file use the following code and comment the previous*/
       /* InputStream inputStream =  getResources().openRawResource(R.raw.categories);
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
            sharedpreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
            boolean includeDirtyJokes = sharedpreferences.getBoolean(MY_ENABLED_HEAVY_JOKE, false);
            JSONArray jArray = new JSONArray(byteArrayOutputStream.toString());
            for (int i = 0; i < jArray.length(); i++) {
                String id = jArray.getJSONObject(i).getString("id");
                String user = jArray.getJSONObject(i).getString("user");
                String jokeTitle = jArray.getJSONObject(i).getString("title");
                String jokeText = jArray.getJSONObject(i).getString("jokeText");
                int likes = jArray.getJSONObject(i).getInt("likes");
                int dislikes = jArray.getJSONObject(i).getInt("dislikes");
                String jokeCategory = jArray.getJSONObject(i).getString("category");
                jokeCategory = jokeCategory != null ? jokeCategory.toUpperCase() : "";
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

            for(Map.Entry<String, List<Joke>> entry : mapJokes.entrySet()) {
                List<Joke> jokeList = entry.getValue();
                Collections.sort(jokeList);
                List<String> subCategoriesJokes = new ArrayList<String>();
                for (Joke joke: jokeList){
                    if (!includeDirtyJokes && joke.isDirtyJoke()){
                        continue;
                    }
                    subCategoriesJokes.add(joke.getId()+"<->"+joke.getTitle()+"<->"+joke.getLikes()+"<->"+joke.getDislikes()+"<->"+joke.getJokeText()+"<->"+joke.getCategory()+"<->"+joke.isDirtyJoke()+"<->"+joke.getCreationDate()+"<->"+joke.getUser());
                }
                listDataHeader.add(entry.getKey());
                listDataChild.put(entry.getKey(), subCategoriesJokes);
            }
        } catch (Exception e) {
            Log.e(Util.TAG,"error trying to read the json file: "+ e.getMessage());
        }
        */
    }
}
