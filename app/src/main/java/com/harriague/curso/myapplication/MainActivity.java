package com.harriague.curso.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "Jokes";
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPreference" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
	   // get the listview
        expListView = (ExpandableListView) findViewById(R.id.likesList);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter.setSharedPreference(sharedpreferences);

        String itemSelected = sharedpreferences.getString("Item selected", "nothing");
        Toast.makeText(this, itemSelected, Toast.LENGTH_LONG).show();

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        /*// Adding child data
        listDataHeader.add("Top 250");
        listDataHeader.add("Now Showing");
        listDataHeader.add("Coming Soon..");

        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);*/
        try {
            readJson();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.option_heavy_joke:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
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
            e.printStackTrace();
        }
        try {
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
            JSONArray jArray = jObject.getJSONArray("categories");
            String catName;
            for (int i = 0; i < jArray.length(); i++) {
                catName = jArray.getJSONObject(i).getString("name");
                JSONArray subcategories = jArray.getJSONObject(i).getJSONArray("subcategory");
                int id = 0;
                String subcatName;
                int likes = 0;
                int dislikes = 0;
                List<String> subCategories = new ArrayList<String>();
                for (int j = 0; j < subcategories.length(); j++){
                    id = subcategories.getJSONObject(j).getInt("id");
                    subcatName = subcategories.getJSONObject(j).getString("name");
                    likes = subcategories.getJSONObject(j).getInt("likes");
                    dislikes = subcategories.getJSONObject(j).getInt("dislikes");
                    subCategories.add(id+"<->"+subcatName+"<->"+likes+"<->"+dislikes);
                }
                listDataHeader.add(catName);
                listDataChild.put(catName, subCategories);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
